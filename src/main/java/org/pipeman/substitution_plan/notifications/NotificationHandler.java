package org.pipeman.substitution_plan.notifications;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushAsyncService;
import org.asynchttpclient.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.substitution_plan.Day;
import org.pipeman.substitution_plan.Plan;
import org.pipeman.substitution_plan.PlanData.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NotificationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHandler.class);
    private static final PushAsyncService service;

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            service = new PushAsyncService(
                    Config.get().vapidPublicKey,
                    Config.get().vapidPrivateKey,
                    Config.get().vapidSubject
            );
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendNotification(Subscriber subscriber, String message) {
        try {
            LOGGER.info("Sending notification to {}; filter: {}", subscriber.subscription().endpoint, subscriber.filter());
            service.send(new Notification(subscriber.subscription(), message))
                    .thenAccept(s -> deleteSubscriptionIfInvalid(s, subscriber.subscription().endpoint));
        } catch (GeneralSecurityException | IOException | JoseException e) {
            LOGGER.warn("Failed to send notification", e);
        }
    }

    private static void deleteSubscriptionIfInvalid(Response response, String endpoint) {
        int status = response.getStatusCode();
        if (status < 200 || status >= 300) {
            deleteSubscriber(endpoint);
        }
    }

    public static void handlePlanUpdate(Day day, Plan plan) {
        forEachSubscriber(subscriber -> {
            List<Row> filtered = plan.data().filterSubstitutions(subscriber.filter());
            if (filtered.isEmpty()) return;

            String message = createMessage(day, filtered.size());
            sendNotification(subscriber, message);
        });
    }

    private static String createMessage(Day day, int entries) {
        if (entries == 1) {
            return "Der %s Vertretungsplan enthält einen Eintrag, der Dich betrifft."
                    .formatted(day.localization());
        } else {
            return "Der %s Vertretungsplan enthält %s Einträge, die Dich betreffen."
                    .formatted(day.localization(), entries);
        }
    }

    private static void forEachSubscriber(Consumer<Subscriber> action) {
        Database.getJdbi().useHandle(h -> h.createQuery("""
                        SELECT endpoint, key, auth, course_filter
                        FROM subscriptions
                                 JOIN users u ON subscriptions.user_id = u.id
                        """)
                .mapTo(Subscriber.class)
                .forEach(action));
    }

    public static boolean deleteSubscriber(String endpoint) {
        return Database.getJdbi().withHandle(h -> h.createUpdate("""
                        DELETE
                        FROM subscriptions
                        WHERE endpoint = :endpoint AND user_id = :user_id
                        """)
                .bind("endpoint", endpoint)
                .execute()) == 1;
    }

    public static boolean putSubscriber(String endpoint, String key, String auth, long userId) {
        return Database.getJdbi().withHandle(h -> h.createUpdate("""
                        INSERT INTO subscriptions (endpoint, key, auth, user_id)
                        VALUES (:endpoint, :key, :auth, :user_id)
                        ON CONFLICT DO NOTHING
                        """)
                .bind("endpoint", endpoint)
                .bind("key", key)
                .bind("auth", auth)
                .bind("user_id", userId)
                .execute()) == 1;
    }
}
