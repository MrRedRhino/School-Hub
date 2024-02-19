package org.pipeman.rest;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.pipeman.Database;

import java.beans.ConstructorProperties;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupervisionApi {
    public static void getSupervisions(Context ctx) {
        ctx.json(getSupervisions());
    }

    public static void deleteSupervision(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        LocalDate date = ctx.pathParamAsClass("date", LocalDate.class).get();
        int breakNumber = ctx.queryParamAsClass("break", Integer.class).get();

        boolean deleted = deleteSupervision(breakNumber, date, userId);
        if (!deleted) {
            throw new BadRequestResponse("No entry found");
        }
    }

    public static void putSupervision(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        LocalDate date = ctx.pathParamAsClass("date", LocalDate.class).get();
        if (date.getDayOfWeek().getValue() >= 6) throw new BadRequestResponse("This date is on a weekend");

        int breakNumber = ctx.queryParamAsClass("break", Integer.class).get();
        if (breakNumber < 1 || breakNumber > 2) throw new BadRequestResponse("Invalid break");

        boolean put = putSupervision(breakNumber, date, userId);
        if (!put) {
            throw new BadRequestResponse("Could not add supervision");
        }
    }

    private static Map<LocalDate, Map<Integer, List<String>>> getSupervisions() {
        Map<LocalDate, Map<Integer, List<String>>> supervisions = new HashMap<>();

        List<Supervision> result = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT date, array_agg(u.name) AS names, break_num
                        FROM supervisions
                                 JOIN users u on u.id = supervisions.user_id
                        GROUP BY date, break_num;
                        """)
                .mapTo(Supervision.class)
                .list());

        for (Supervision supervision : result) {
            Map<Integer, List<String>> supervisors = supervisions.get(supervision.date());
            if (supervisors == null) {
                Map<Integer, List<String>> newMap = new HashMap<>();
                newMap.put(supervision.breakNum(), supervision.names());
                supervisions.put(supervision.date(), newMap);
            } else {
                supervisors.computeIfAbsent(supervision.breakNum(), i -> new ArrayList<>())
                        .addAll(supervision.names());
            }
        }
        return supervisions;
    }

    private static boolean deleteSupervision(int breakNumber, LocalDate date, long userId) {
        int updateCount = Database.getJdbi().withHandle(h -> h.createUpdate("""
                        DELETE
                        FROM supervisions
                        WHERE break_num = :break_num
                          AND date = :date
                          AND user_id = :user_id
                        """)
                .bind("break_num", breakNumber)
                .bind("date", date)
                .bind("user_id", userId)
                .execute()
        );

        return updateCount > 0;
    }

    private static boolean putSupervision(int breakNumber, LocalDate date, long userId) {
        int updateCount = Database.getJdbi().withHandle(h -> h.createUpdate("""
                        INSERT INTO supervisions (break_num, date, user_id)
                        SELECT :break_num, :date, :user_id
                        WHERE NOT exists(SELECT 1 FROM supervisions WHERE date = :date AND break_num = :break_num AND user_id = :user_id)
                          AND (SELECT count(*)
                               FROM supervisions
                               WHERE break_num = :break_num
                                 AND date = :date) < 2;
                        """)
                .bind("date", date)
                .bind("break_num", breakNumber)
                .bind("user_id", userId)
                .execute());

        return updateCount > 0;
    }

    public record Supervision(LocalDate date, List<String> names, int breakNum) {
        @ConstructorProperties({"date", "names", "break_num"})
        public Supervision {
        }
    }
}
