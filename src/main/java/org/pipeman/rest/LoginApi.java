package org.pipeman.rest;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.UnauthorizedResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.Main;
import org.pipeman.ilaw.ILAW;
import org.pipeman.ilaw.Recipient;
import org.pipeman.utils.Utils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LoginApi {
    private static final ILAW ilaw = ILAW.login(Config.get().ilUrl, Config.get().ilUser, Config.get().ilPassword);
    private static final Random RANDOM = new Random();
    private static final Cache<Integer, Recipient> loginCodes = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public static void sendCode(Context ctx) {
        String username = ctx.queryParam("name");
        if (username == null || username.isBlank() || username.length() > 42) {
            throw new BadRequestResponse("Illegal name");
        }

        List<Recipient> recipients = ilaw.getMessageRecipients(username.replace(".", " "));

        if (recipients.size() != 1) {
            throw new BadRequestResponse("Illegal name");
        }
        Recipient recipient = recipients.get(0);

        int code = generateCode();
        loginCodes.put(code, recipient);
        ilaw.sendMessage(recipient.id(), "Dein Code für pipeman.org ist: %s. Falls Du nicht gerade versucht hast, Dich anzumelden, kannst Du diese Nachricht ignorieren. \nDies ist eine automatisch versendete Nachricht.".formatted(code));
    }

    public static void login(Context ctx) {
        int code = Utils.parseInt(ctx.queryParam("code")).orElse(0);

        Recipient recipient = code == Config.get().adminPassword
                ? new Recipient(0, Config.get().adminUsername)
                : loginCodes.getIfPresent(code);

        if (recipient == null) throw new UnauthorizedResponse("Invalid code");

        loginCodes.invalidate(code);

        Optional<Long> user = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT id
                        FROM users
                        WHERE name = :name
                        """)
                .bind("name", recipient.name())
                .mapTo(Long.class)
                .findFirst());

        long id = user.orElseGet(Main.ID_GENERATOR::next);
        if (user.isEmpty()) {
            Database.getJdbi().withHandle(h -> h.createUpdate("""
                            INSERT INTO users (id, name, course_filter, settings)
                            VALUES (:id, :name, '{}', '{}')
                            """)
                    .bind("id", id)
                    .bind("name", recipient.name())
                    .execute());
        }

        String token = User.createSession(id);
        ctx.json(Map.of("token", token));
        ctx.header(Header.SET_COOKIE, "auth=%s; Max-Age=34560000; Path=/".formatted(token));
    }

    private static int generateCode() {
        while (true) {
            int code = RANDOM.nextInt(10_000, 100_000);
            if (loginCodes.getIfPresent(code) == null) {
                return code;
            }
        }
    }

    public static void logout(Context ctx) {
        String cookie = ctx.cookie("auth");
        if (cookie == null || cookie.length() != 42) {
            return;
        }

        Database.getJedis().del("sessions:" + cookie);
    }

    public static User getUser(Context ctx) {
        return User.fromRequest(ctx).orElseThrow(UnauthorizedResponse::new);
    }

    public static void getAccount(Context ctx) {
        User user = getUser(ctx);
        User.Data data = user.data();
        ctx.json(Map.of("id", user.id(),
                        "name", data.name(),
                        "course-filter", String.join(", ", data.courseFilter()),
                        "settings", data.settings())
                // TODO return string id
        );
    }

    public static void patchSettings(Context ctx) {
        long userId = getUser(ctx).id();

        try {
            JSONObject body = new JSONObject(ctx.body());

            Map<String, Class<?>> settings = Map.of(
                    "substitution-html-view", Boolean.class,
                    "theme", String.class,
                    "news-last-read", String.class,
                    "class", String.class
            );
            if (!settings.keySet().containsAll(body.keySet())) {
                throw new BadRequestResponse("Unknown setting");
            }

            settings.forEach((key, type) -> {
                if (body.has(key) && body.get(key).getClass() != type) {
                    throw new BadRequestResponse("Invalid setting value");
                }
            });

            Database.getJdbi().useHandle(h -> h.createUpdate("""
                            UPDATE users
                            SET settings = cast(:settings AS jsonb)
                            WHERE id = :user_id
                            """)
                    .bind("user_id", userId)
                    .bind("settings", body.toString())
                    .execute());
        } catch (JSONException ignored) {
            throw new BadRequestResponse("Malformed body");
        }
    }

    public static void patchCourseFilter(Context ctx) {
        long userId = getUser(ctx).id();

        String[] splitBody = ctx.body().split(",");
        List<String> filter = new ArrayList<>(List.of(splitBody));
        filter.replaceAll(String::trim);
        filter.removeIf(String::isEmpty);

        Database.getJdbi().withHandle(h -> h.createUpdate("""
                        UPDATE users
                        SET course_filter = :filter
                        WHERE id = :user_id
                        """)
                .bind("user_id", userId)
                .bindArray("filter", String.class, filter)
                .execute());
    }
}
