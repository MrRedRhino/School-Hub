package org.pipeman.books.search;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.pipeman.Database;
import org.pipeman.books.BookIndex;
import org.pipeman.books.BookIndex.Book;
import org.pipeman.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PipeComplete {
    private static Map<Integer, Double> scoreCache = null;
    private static long scoreRefresh = 0;
    private static final JaroWinkler JW = new JaroWinkler(0.1);

    private static float similarity(String s1, String s2) {
        return Math.abs(s1.length() - s2.length()) > 3 ? 0 : (float) JW.similarity(s1, s2);
    }

    private static float getSimilarity(String query, Book book) {
        int length = query.length();
        float similarity = 0;
        for (String word : book.searchTerms()) {
            similarity = Math.max(similarity(Utils.substring(word, length), query), similarity);
        }
        return similarity;
    }

    public static List<Completion> getCompletions(String query) {
        String q = query.toLowerCase();
        List<Completion> out = new ArrayList<>();
        for (Book book : BookIndex.INSTANCE.books().values()) {
            out.add(new Completion(book, getSimilarity(q, book)));
        }
        return out;
    }

    private static Map<Integer, Double> getScores() {
        if (System.currentTimeMillis() - 10 * 60 * 1000 > scoreRefresh) {
            scoreRefresh = System.currentTimeMillis();

            scoreCache = Database.getJdbi().withHandle(h -> h.createQuery("""
                            WITH RecentBookStats AS (SELECT book,
                                                            sum(count) AS sum_count
                                                     FROM books_stats
                                                     WHERE date > current_date - INTERVAL '14 days'
                                                     GROUP BY book),
                                 MaxSum AS (SELECT max(sum_count) AS max_sum
                                            FROM RecentBookStats)
                            SELECT rbs.book,
                                   rbs.sum_count / CAST(ms.max_sum AS float) AS score
                            FROM RecentBookStats rbs,
                                 MaxSum ms
                            """)
                    .mapToMap()
                    .collectToMap(map -> (int) map.get("book"), map -> (double) map.get("score")));
        }
        return scoreCache;
    }

    public static List<Book> getCompletionsSorted(String query) {
        List<Book> out = new ArrayList<>();
        if (query.isBlank()) return List.of();

        Map<Integer, Double> scores = getScores();
        Comparator<Completion> comparator = Comparator.comparing(c -> c.similarity * scores.getOrDefault(c.book.id(), 0d));

        List<Completion> completions = getCompletions(query);
        completions.removeIf(c -> c.similarity <= 0.6);
        completions.sort(comparator.reversed());

        for (Completion entry : completions) {
            out.add(entry.book);
            if (out.size() >= 3) break;
        }
        return out;
    }

    public record Completion(Book book, float similarity) {
    }
}
