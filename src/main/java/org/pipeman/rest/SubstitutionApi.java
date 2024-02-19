package org.pipeman.rest;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Header;
import org.pipeman.substitution_plan.*;

import java.util.Map;
import java.util.Set;

public class SubstitutionApi {
    private static final PlanCache DOWNLOADER = new PlanCache();

    public static void getPlanToday(Context ctx) {
        sendPlan(ctx, Day.TODAY);
    }

    public static void getPlanTomorrow(Context ctx) {
        sendPlan(ctx, Day.TOMORROW);
    }

    public static void getForYouToday(Context ctx) {
        sendForYou(ctx, Day.TODAY);
    }

    public static void getForYouTomorrow(Context ctx) {
        sendForYou(ctx, Day.TOMORROW);
    }

    private static void sendForYou(Context ctx, Day day) {
        Set<String> filter = User.fromRequest(ctx)
                .map(user -> user.data().courseFilter())
                .orElse(Set.of());

        PlanData planData = DOWNLOADER.getCachedPlan(new PlanIdentifier("", day)).data();
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
            throw new BadRequestResponse("Query parameter 'format' must be one of 'HTML', 'PDF', 'PNG', 'JSON'");
        }

        Plan data = DOWNLOADER.getPlan(new PlanIdentifier("", day));
        switch (format) {
            case HTML -> ctx.html(data.html());
            case PDF -> ctx.result(data.pdf()).header(Header.CONTENT_TYPE, "application/pdf");
            case PNG -> ctx.result(data.image()).header(Header.CONTENT_TYPE, "image/png");
            case JSON -> ctx.json(data.data());
        }
    }

    private enum Format {
        HTML,
        PDF,
        PNG,
        JSON;

        public static Format fromString(String s) {
            return switch (s.toLowerCase()) {
                case "html" -> HTML;
                case "pdf" -> PDF;
                case "png" -> PNG;
                case "json" -> JSON;
                default -> null;
            };
        }
    }
}
