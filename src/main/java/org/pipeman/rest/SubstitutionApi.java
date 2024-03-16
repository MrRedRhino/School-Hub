package org.pipeman.rest;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.NotFoundResponse;
import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.ilaw.ILAW;
import org.pipeman.ilaw.LoginException;
import org.pipeman.substitution_plan.*;

import java.util.List;
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

    public static void addAccount(Context ctx) {
        long accountId = LoginApi.getUser(ctx).id();

        String username = ctx.queryParamAsClass("username", String.class).get();
        String password = ctx.queryParamAsClass("password", String.class).get();
        int todayPlanId = ctx.queryParamAsClass("todayPlanId", Integer.class).get();
        int tomorrowPlanId = ctx.queryParamAsClass("tomorrowPlanId", Integer.class).get();

        try {
            ILAW ilaw = ILAW.login(Config.get().ilUrl, username, password);
            ilaw.downloadOnedriveFile(String.valueOf(todayPlanId));
            ilaw.downloadOnedriveFile(String.valueOf(tomorrowPlanId));
        } catch (LoginException e) {
            throw new BadRequestResponse("Invalid credentials");
        } catch (RuntimeException e) {
            throw new BadRequestResponse("Invalid plan ids");
        }

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        INSERT INTO substitution_accounts (username, password, today_plan_id, tomorrow_plan_id, class, added_by)
                        VALUES (:username, pgp_sym_encrypt(:password, :encryption_password), :today_plan_id, :tomorrow_plan_id, :class,
                                :added_by)
                        ON CONFLICT ON CONSTRAINT substitution_accounts_pk DO UPDATE SET username         = :username,
                                                                                         password         = pgp_sym_encrypt(:password, :encryption_password),
                                                                                         today_plan_id    = :today_plan_id,
                                                                                         tomorrow_plan_id = :tomorrow_plan_id,
                                                                                         class            = :class,
                                                                                         added_by         = :added_by
                        """)
                .bind("username", username)
                .bind("password", password)
                .bind("today_plan_id", todayPlanId)
                .bind("tomorrow_plan_id", tomorrowPlanId)
                .bind("class", ctx.pathParam("class"))
                .bind("added_by", accountId)
                .bind("encryption_password", Config.get().encryptionPassword)
                .execute());
    }

    public static void removeAccount(Context ctx) {
        long accountId = LoginApi.getUser(ctx).id();
        String substitutionClass = ctx.pathParam("class");

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        DELETE
                        FROM substitution_accounts
                        WHERE added_by = :added_by
                          AND class = :class
                        """)
                .bind("class", substitutionClass)
                .bind("added_by", accountId)
                .execute());

        CACHE.removeAccount(substitutionClass);
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

    public static void getPlans(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        List<Map<String, Object>> plans = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT class, added_by = :user AS removable
                        FROM substitution_accounts
                        """)
                .bind("user", userId)
                .mapToMap()
                .list());

        ctx.json(plans);
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
