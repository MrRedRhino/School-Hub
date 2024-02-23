package org.pipeman.rest.reservation;

import io.javalin.http.sse.SseClient;
import org.pipeman.Config;
import org.pipeman.Database;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Live {
    private static final Queue<SseClient> sseClients = new ConcurrentLinkedDeque<>();

    public static void addClient(SseClient client) {
        sseClients.add(client);
        client.keepAlive();
        client.onClose(() -> sseClients.remove(client));

        client.sendEvent("set_reservations", Map.of(
                "reservations", getReservations(),
                "max-seats", Config.get().maxSeatsPerPerson
        ));
    }

    public static void broadcastRemoveReservation(String seatId) {
        for (SseClient client : sseClients) {
            client.sendEvent("remove_reservation", Map.of("seat", seatId));
        }
    }

    public static void broadcastAddReservation(long userId, String seatId) {
        String name = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT name
                        FROM users
                        WHERE id = :user_id
                        """)
                .bind("user_id", userId)
                .mapTo(String.class)
                .first());

        for (SseClient client : sseClients) {
            client.sendEvent("add_reservation", new Reservation(seatId, name));
        }
    }

    private static List<Reservation> getReservations() {
        return Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT seat, u.name AS username
                        FROM reservations
                                 JOIN public.users u on u.id = reservations.user_id
                        """)
                .mapTo(Reservation.class)
                .list());
    }

    public record Reservation(String seat, String name) {
        @ConstructorProperties({"seat", "username"})
        public Reservation {
        }
    }
}
