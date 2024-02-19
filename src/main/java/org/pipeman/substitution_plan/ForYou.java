package org.pipeman.substitution_plan;

import org.pipeman.substitution_plan.PlanData.Row;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ForYou {
    // 0: Applying hours, 1: subject, 2: teacher, 3: room, 4: other, 5: substitution
    private static final List<Mapper> mappers = List.of(
            new Mapper(
                    row -> row.substitution().equals("entfällt"),
                    new MessageFormat("{0} entfällt {1} bei {2}")
            ),
            new Mapper(
                    row -> row.other().equals("Raumtausch"),
                    new MessageFormat("{0} Raumtausch zu Raum {3} in {1}/{2}")
            ),
            new Mapper(
                    row -> !row.other().isBlank() && row.teacher().isBlank(),
                    new MessageFormat("{0}: {4} bei {5}/{1} in {3}")
            ),
            new Mapper(
                    row -> true,
                    new MessageFormat("{0}: {1} {2} {3} {4}")
            )
    );

    public static List<String> generate(List<Row> rows) {
        Map<Row, List<String>> groupedByHour = groupByHour(rows);
        List<String> messages = new ArrayList<>();

        groupedByHour.forEach((row, lessons) -> {
            for (Mapper mapper : mappers) {
                if (mapper.appliesForRow(row)) {
                    String line = mapper.format(row, formatLessons(lessons));
                    messages.add(line.replaceAll(" +", " "));
                    return;
                }
            }
        });

        return messages;
    }

    private static String formatLessons(List<String> lessons) {
        if (lessons.size() == 1) {
            return lessons.get(0) + ". Stunde";
        }

        return String.join("., ", lessons.subList(0, lessons.size() - 1))
                        + ". und " + lessons.get(lessons.size() - 1) + ". Stunde";
    }

    private static Map<Row, List<String>> groupByHour(List<Row> rows) {
        Map<Row, List<String>> grouped = new HashMap<>();

        for (Row row : rows) {
            grouped.computeIfAbsent(row, k -> new ArrayList<>()).add(row.lesson());
        }

        return grouped;
    }

    private record Mapper(Predicate<Row> applyCheck, MessageFormat formatter) {
        public boolean appliesForRow(Row row) {
            return applyCheck.test(row);
        }

        public String format(Row row, String applyingHours) {
            String other = '"' + row.other() + '"';
            return formatter.format(new Object[]{applyingHours, row.clazz(), row.teacher(), row.room(), other, row.substitution()});
        }
    }
}
