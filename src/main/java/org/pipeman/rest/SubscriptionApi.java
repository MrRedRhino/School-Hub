package org.pipeman.rest;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.json.JSONObject;
import org.pipeman.substitution_plan.notifications.NotificationHandler;
import org.pipeman.utils.ThrowingSupplier;

public class SubscriptionApi {
    public static void addSubscription(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        JSONObject body = get("Invalid body", () -> new JSONObject(ctx.body()));
        String endpoint = get("Missing endpoint", () -> body.getString("endpoint").trim());

        String key = get("Missing key", () -> body.getString("key"));
        String auth = get("Missing auth", () -> body.getString("auth"));

        boolean success = NotificationHandler.putSubscriber(endpoint, key, auth, userId);
        if (!success) {
            throw new BadRequestResponse("Subscription already exists");
        }
    }

    public static void removeSubscription(Context ctx) {
        LoginApi.getUser(ctx);

        String endpoint = ctx.queryParam("endpoint");
        if (endpoint == null) throw new BadRequestResponse("Query parameter 'endpoint' missing");

        boolean isDeleted = NotificationHandler.deleteSubscriber(endpoint.strip());
        if (!isDeleted) {
            throw new BadRequestResponse("Subscription not found");
        }
    }

    private static <T> T get(String message, ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ignored) {
            throw new BadRequestResponse(message);
        }
    }
}
