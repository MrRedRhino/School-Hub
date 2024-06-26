package org.pipeman.rest.reservation;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.rest.LoginApi;
import org.pipeman.rest.User;
import org.pipeman.utils.Utils;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ReservationApi {
    private static final JSONArray SEATS = new JSONArray(Utils.readResourceString("seats.json"));
    private static final String emailBody = Utils.readResourceString("email-template.html");
    private static final String emailListEntry = Utils.readResourceString("seat_template.html");

    public static void reserveSeat(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        String seatId = ctx.pathParam("seat");
        for (Object seat : SEATS) {
            JSONObject seatObject = (JSONObject) seat;
            if (getId(seatObject).equals(seatId) && !seatObject.isNull("blockedFor")) {
                throw new BadRequestResponse("Seat is blocked");
            }
        }

        boolean success = Database.getJdbi().withHandle(h -> h.createUpdate("""
                        INSERT INTO reservations (seat, user_id)
                        SELECT :seat, :user_id
                        WHERE (SELECT count(*) FROM reservations WHERE user_id = :user_id) < :max_seats
                        """)
                .bind("user_id", userId)
                .bind("seat", seatId)
                .bind("max_seats", Config.get().maxSeatsPerPerson)
                .execute()) > 0;

        if (!success) {
            ctx.status(400).result("Seat taken or maximum number of seats reached");
        } else {
            Live.broadcastAddReservation(userId, seatId);
        }
    }

    public static void removeReservation(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        String seatId = ctx.pathParam("seat");

        boolean success = Database.getJdbi().withHandle(h -> h.createUpdate("""
                        DELETE
                        FROM reservations
                        WHERE user_id = :user_id
                          AND seat = :seat
                        """)
                .bind("user_id", userId)
                .bind("seat", seatId)
                .execute()) > 0;

        if (!success) {
            ctx.status(400).result("This seat is not reserved by you or anyone");
        } else {
            Live.broadcastRemoveReservation(seatId);
        }
    }

    public static void sendReservationsAsEmail(Context ctx) throws IOException, MessagingException {
        User user = LoginApi.getUser(ctx);
        String recipientEmail = ctx.queryParamAsClass("email-address", String.class).get();

        List<String> reservations = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT seat
                        FROM reservations
                        WHERE user_id = :user
                        """)
                .bind("user", user.id())
                .mapTo(String.class)
                .list());

        SES.sendReservationConfirmation(recipientEmail, "Deine Abi-Feier Reservierungen",
                getEmailContent(user.data().surname(), reservations),
                createPlan(reservations)
        );
    }

    private static String getEmailContent(String username, List<String> reservations) {
        StringBuilder list = new StringBuilder();
        for (String reservation : reservations) {
            String[] parts = reservation.split("-");
            list.append(emailListEntry.formatted(parts[0], parts[1]));
        }

        return emailBody.formatted(username, list.toString());
    }

    private static byte[] createPlan(List<String> reservations) throws IOException {
        int width = 3000;
        int height = 1400;
        int seatSize = 50;
        int xOffset = 1500;
        int yOffset = 600;
        Color reserved = new Color(255, 32, 70);
        Color available = new Color(179, 179, 179);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, width, height);

        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.PLAIN, 100));
        graphics.drawString("Bühne", 1350, 300);

        for (Object seat : SEATS) {
            JSONObject seatJson = (JSONObject) seat;

            int x = xOffset + seatJson.getInt("x");
            int y = yOffset + seatJson.getInt("y");

            Area area = new Area(new Rectangle2D.Double(x, y + seatSize / 2.0, seatSize, seatSize / 2.0));
            Area topHalf = new Area(new Ellipse2D.Double(x, y, seatSize, seatSize));
            area.add(topHalf);

            double centerX = x + seatSize / 2.0;
            double centerY = y + seatSize / 2.0;

            graphics.setColor(reservations.contains(getId(seatJson)) ? reserved : available);

            graphics.setTransform(AffineTransform.getRotateInstance(Math.toRadians(seatJson.getInt("angle")), centerX, centerY));
            graphics.fill(area);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        return stream.toByteArray();
    }

    private static String getId(JSONObject seat) {
        JSONObject location = seat.getJSONObject("location");
        return location.getString("row") + "-" + location.get("seat");
    }

    public static void sse(SseClient client) {
        Live.addClient(client);
    }

    public static void getSeats(Context ctx) {
        ctx.json(SEATS.toList());
    }
}
