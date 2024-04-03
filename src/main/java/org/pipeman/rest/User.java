package org.pipeman.rest;

import io.javalin.http.Context;
import org.jdbi.v3.json.Json;
import org.pipeman.Database;
import org.pipeman.utils.Utils;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class User {
    private static final long sessionTTL = Duration.ofDays(30).toSeconds();
    private final long id;
    private Data data;

    private User(long id, Data data) {
        this.id = id;
        this.data = data;
    }

    public Data data() {
        if (data == null) {
            data = getData();
        }
        return data;
    }

    public long id() {
        return id;
    }

    public static String createSession(long userId) {
        String token = Utils.generateRandomString(42);
        Database.getJedis().setex("sessions:" + token, sessionTTL, String.valueOf(userId));
        return token;
    }

    public static Optional<User> fromRequest(Context ctx) {
        String cookie = ctx.cookie("auth");
        if (cookie == null) {
            return Optional.empty();
        }

        Database.getJedis().expire("sessions:" + cookie, sessionTTL);
        String userId = Database.getJedis().get("sessions:" + cookie);
        if (userId == null) return Optional.empty();

        return Optional.of(new User(Long.parseLong(userId), null));
    }

    private Data getData() {
        return Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT name, course_filter, settings
                        FROM users
                        WHERE id = :id
                        """)
                .bind("id", id)
                .mapTo(Data.class)
                .first());
    }

    public record Data(String name, Set<String> courseFilter, @Json Map<String, ?> settings) {
    }
}
