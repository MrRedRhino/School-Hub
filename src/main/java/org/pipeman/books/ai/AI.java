package org.pipeman.books.ai;

import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.books.TextExtractor;
import org.pipeman.books.ai.impl.GroqTextProcessor;
import org.pipeman.books.ai.impl.OpenAITextProcessor;

import java.io.InputStream;
import java.util.Optional;

public class AI {
    public static final UsageLimit USAGE_LIMITER = new UsageLimit();
    private static final TextProcessor TEXT_PROCESSOR = new GroqTextProcessor(Config.get().aiKey);
    private static final TextProcessor SPEECH_GENERATOR = new OpenAITextProcessor(Config.get().speechKey);

    public static String getSummary(int bookId, int page) {
        String key = bookId + " " + page;

        synchronized (key) {
            return getSummaryFromDatabase(bookId, page).orElseGet(() -> summarize(bookId, page));
        }
    }

    private static String summarize(int bookId, int page) {
        String text = TextExtractor.getText(bookId, page)
                .replaceAll("(?<= )[A-z\\\\](?= )", "")
                .replaceAll("[@©ı]", "");
        String summary = TEXT_PROCESSOR.summarize(text);
        storeSummary(bookId, page, summary);
        return summary;
    }

    public static InputStream doTTS(int bookId, int page) {
        String text = TextExtractor.getText(bookId, page)
                .replaceAll("(?<= )[A-z\\\\](?= )", "")
                .replaceAll("[@©ı]", "");

        String preparedText = TEXT_PROCESSOR.prepareForTTS(text);
        return SPEECH_GENERATOR.doTTS(preparedText);
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
