package org.pipeman.rest;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.TooManyRequestsResponse;
import org.pipeman.Database;
import org.pipeman.books.BookIndex;
import org.pipeman.books.ai.AI;
import org.pipeman.books.ai.UsageLimit;
import org.pipeman.books.search.SearchParser;
import org.pipeman.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BookApi {
    private static final SearchParser COMPLETER = new SearchParser();

    public static void getBook(Context ctx) {
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        try {
            ctx.json(BookIndex.INSTANCE.books().get(book).serialize());
            return;
        } catch (Exception ignored) {
        }
        throw new NotFoundResponse();
    }

    public static void listBooks(Context ctx) {
        List<Map<String, ?>> books = new ArrayList<>();
        for (BookIndex.Book book : BookIndex.INSTANCE.books().values()) {
            books.add(book.serialize());
        }
        ctx.json(books);
    }

    public static void getPage(Context ctx) {
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        int page = ctx.pathParamAsClass("page", Integer.class).get();

        ctx.html(BookIndex.INSTANCE.getHtml(book, page));
    }

    public static void getSummary(Context ctx) {
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        int page = ctx.pathParamAsClass("page", Integer.class).get();

        try {
            ctx.result(AI.getSummary(book, page));
        } catch (UsageLimit.LimitExceededException ignored) {
            throw ulr();
        }
    }

    private static TooManyRequestsResponse ulr() {
        return new TooManyRequestsResponse("Sorry, the daily usage limit has been exceeded");
    }

    public static void completions(Context ctx) {
        String query = ctx.queryParam("query");
        if (query == null) {
            ctx.status(400).json(Map.of("missing-query-params", new String[]{"query"}));
            return;
        }

        List<Map<String, ?>> completions = new ArrayList<>();
        for (SearchParser.CompletionResult completion : COMPLETER.getCompletions(Utils.substring(query, 35))) {
            completions.add(completion.serialize());
        }
        ctx.json(completions);
    }

    public static void putAnnotation(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        int page = ctx.pathParamAsClass("page", Integer.class).get();

        String content = ctx.body();

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        INSERT INTO annotations (user_id, book, page, content)
                        VALUES (:user_id, :book, :page, CAST(:content AS jsonb))
                        ON CONFLICT ON CONSTRAINT annotations_pk DO UPDATE SET content = CAST(:content AS jsonb)
                        """)
                .bind("book", book)
                .bind("page", page)
                .bind("user_id", userId)
                .bind("content", content)
                .execute());
    }

    public static void getAnnotation(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        int page = ctx.pathParamAsClass("page", Integer.class).get();

        Optional<String> annotations = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT content
                        FROM annotations
                        WHERE user_id = :user_id
                          AND book = :book
                          AND page = :page
                        """)
                .bind("book", book)
                .bind("page", page)
                .bind("user_id", userId)
                .mapTo(String.class)
                .findFirst());

        ctx.contentType(ContentType.APPLICATION_JSON).result(annotations.orElse("[]"));
    }
}
