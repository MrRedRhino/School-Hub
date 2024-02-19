package org.pipeman.rest;

import io.javalin.http.Context;
import org.jdbi.v3.json.Json;
import org.pipeman.Database;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class User {
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

    public static Optional<User> fromRequest(Context ctx) {
        String cookie = "MY28TW02JxoYUrqDai7uAT2BZbMXtY954XLr8GJ98u";
//        String cookie = ctx.cookie("auth");
//        if (cookie == null) {
//            return Optional.empty();
//        }

        return Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT user_id
                        FROM sessions
                        WHERE token = :token
                        """)
                .bind("token", cookie)
                .map((rs, ctx1) -> new User(rs.getLong(1), null))
                .findFirst());
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
