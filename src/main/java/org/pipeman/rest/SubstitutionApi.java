package org.pipeman.rest;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.NotFoundResponse;
import org.pipeman.substitution_plan.*;

import org.pipeman.ilaw.LoginException;
import java.util.Map;
import java.util.Set;

public class SubstitutionApi {
    private static final PlanCache CACHE = new PlanCache();

    public static void getPlanToday(Context ctx) {
        sendPlan(ctx, Day.TODAY);
    }

    public static void getPlanTomorrow(Context ctx) {
        sendPlan(ctx, Day.TOMORROW);
    }

    private static void sendForYou(Context ctx, Day day) throws LoginException {
        Set<String> filter = User.fromRequest(ctx)
                .map(user -> user.data().courseFilter())
                .orElse(Set.of());

        Plan cachedPlan = CACHE.getCachedPlan(new PlanIdentifier(ctx.pathParam("class"), day));
        if (cachedPlan == null) throw new NotFoundResponse();

        PlanData planData = cachedPlan.data();
        ctx.json(Map.of(
                "message", planData.message(),
                "date", planData.date(),
                "for-you", ForYou.generate(planData.filterSubstitutions(filter))
        ));
    }

    private static void sendPlan(Context ctx, Day day) {
        String formatString = ctx.queryParamAsClass("format", String.class).get();

        Format format = Format.fromString(formatString);
        if (format == null) {
            throw new BadRequestResponse("Query parameter 'format' must be one of 'HTML', 'PDF', 'PNG', 'JSON', 'FOR_YOU'");
        }
        try {
            if (format == Format.FOR_YOU) {
                sendForYou(ctx, day);
                return;
            }
            Plan data = CACHE.getPlan(new PlanIdentifier(ctx.pathParam("class"), day));
            if (data == null) throw new NotFoundResponse();

            switch (format) {
                case HTML -> ctx.html(data.html());
                case PDF -> ctx.result(data.pdf()).header(Header.CONTENT_TYPE, "application/pdf");
                case PNG -> ctx.result(data.image()).header(Header.CONTENT_TYPE, "image/png");
                case JSON -> ctx.json(data.data());
            }
        } catch (LoginException e) {
            throw new BadRequestResponse("Invalid itslearning credentials");
        }
    }

    private enum Format {
        HTML,
        PDF,
        PNG,
        JSON,
        FOR_YOU;

        public static Format fromString(String s) {
            return switch (s.toLowerCase()) {
                case "html" -> HTML;
                case "pdf" -> PDF;
                case "png" -> PNG;
                case "json" -> JSON;
                case "for-you" -> FOR_YOU;
                default -> null;
            };
        }
    }
}
