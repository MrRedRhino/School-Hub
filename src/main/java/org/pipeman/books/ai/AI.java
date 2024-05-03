package org.pipeman.books.ai;

import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.books.TextExtractor;
import org.pipeman.books.ai.impl.GroqSummarizer;

import java.util.Optional;

public class AI {
    public static final UsageLimit USAGE_LIMITER = new UsageLimit();
    private static final Summarizer SUMMARIZER = new GroqSummarizer(Config.get().aiKey);

    public static String getSummary(int bookId, int page) {
        String key = bookId + " " + page;

        synchronized (key) {
            return getSummaryFromDatabase(bookId, page).orElseGet(() -> summarize(bookId, page));
        }
    }

    private static String summarize(int bookId, int page) {
        String summary = SUMMARIZER.summarize(TextExtractor.getText(bookId, page));
        storeSummary(bookId, page, summary);
        return summary;
    }


    private static void storeSummary(int bookId, int page, String summary) {
        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        INSERT INTO books_summaries (book, page, summary)
                        VALUES (:book, :page, :summary)
                        ON CONFLICT (book, page) DO UPDATE SET summary = :summary
                        """)
                .bind("book", bookId)
                .bind("page", page)
                .bind("summary", summary)
                .execute());
    }

    private static Optional<String> getSummaryFromDatabase(int bookId, int page) {
        return Database.getJdbi().withHandle(h -> h.createQuery("""   
                        SELECT summary
                        FROM books_summaries
                        WHERE book = :book
                          AND page = :page
                        """)
                .bind("book", bookId)
                .bind("page", page)
                .mapTo(String.class)
                .findFirst());
    }
}
