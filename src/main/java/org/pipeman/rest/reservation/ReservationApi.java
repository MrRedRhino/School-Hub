package org.pipeman.rest.reservation;

import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.rest.LoginApi;

public class ReservationApi {
    public static void reserveSeat(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        String seatId = ctx.pathParam("seat");

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

    public static void sse(SseClient client) {
        Live.addClient(client);
    }
}
