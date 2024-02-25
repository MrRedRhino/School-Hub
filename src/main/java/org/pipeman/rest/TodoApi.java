package org.pipeman.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.javalin.http.Context;
import org.jetbrains.annotations.Nullable;
import org.pipeman.Database;
import org.pipeman.Main;
import org.pipeman.books.BookIndex;
import org.pipeman.books.search.PipeComplete;
import org.pipeman.utils.LocalDateSerializer;
import org.pipeman.utils.Utils;

import java.beans.ConstructorProperties;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoApi {
    private static final Pattern REFERENCED_BOOK_PATTERN = Pattern.compile("(?<subject>\\w+):[\\d\\D]+[SP]\\. (?<page>\\d{1,3})[\\d\\D]*");

    public static void getTodos(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        List<Todo> todos = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT id, task, date_due, date_done
                        FROM todos
                        WHERE user_id = :user_id
                          AND date_done IS NULL
                        ORDER BY CASE WHEN date_due IS NULL THEN 0.5 ELSE date_due - current_date END;
                        """)
                .bind("user_id", userId)
                .mapTo(Todo.class)
                .list());

        List<Todo> completedTodos = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT id, task, date_due, date_done
                        FROM todos
                        WHERE user_id = :user_id
                          AND date_done IS NOT NULL
                          AND date_done > current_date - INTERVAL '5 days'
                        ORDER BY date_done;
                        """)
                .bind("user_id", userId)
                .mapTo(Todo.class)
                .list());

        ctx.json(Map.of(
                "todos", todos,
                "completedTodos", completedTodos
        ));
    }

    public static void putTodo(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        String task = ctx.queryParamAsClass("task", String.class).get();
        Date dateDue = parseDate(ctx.queryParam("date-due"));

        long id = Main.ID_GENERATOR.next();

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        INSERT INTO todos (id, user_id, task, date_due)
                        VALUES (:id, :user_id, :task, :date_due)
                        """)
                .bind("id", id)
                .bind("user_id", userId)
                .bind("task", task)
                .bind("date_due", dateDue)
                .execute());

        ctx.json(Map.of("id", String.valueOf(id)));
    }

    public static void completeTodo(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        long todoId = ctx.pathParamAsClass("id", Long.class).get();

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        UPDATE todos
                        SET date_done = current_date
                        WHERE id = :id
                          AND user_id = :user_id
                          AND date_done IS NULL
                        """)
                .bind("id", todoId)
                .bind("user_id", userId)
                .execute());
    }

    public static void patchTodo(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        long todoId = ctx.pathParamAsClass("id", Long.class).get();
        String task = ctx.queryParamAsClass("task", String.class).get();
        Date dateDue = parseDate(ctx.queryParam("date-due"));

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        UPDATE todos
                        SET task     = :task,
                            date_due = :date_due
                        WHERE id = :id
                          AND user_id = :user_id
                        """)
                .bind("id", todoId)
                .bind("user_id", userId)
                .bind("task", task)
                .bind("date_due", dateDue)
                .execute());
    }

    private static @Nullable Date parseDate(String s) {
        if (s == null) return null;
        try {
            return new Date(new SimpleDateFormat("yyyy-MM-dd").parse(s).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static void putCompletedTodo(Context ctx) {
        long userId = LoginApi.getUser(ctx).id();

        long todoId = ctx.pathParamAsClass("id", Long.class).get();

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        UPDATE todos
                        SET date_done = null
                        WHERE id = :id
                          AND user_id = :user_id
                        """)
                .bind("id", todoId)
                .bind("user_id", userId)
                .execute());
    }

    public record Todo(
            @JsonSerialize(using = ToStringSerializer.class) long id,
            String task,
            @Nullable @JsonSerialize(using = LocalDateSerializer.class) LocalDate dateDue,
            @Nullable @JsonSerialize(using = LocalDateSerializer.class) LocalDate dateDone,
            @Nullable Map<String, ?> referencedBook
    ) {
        @ConstructorProperties({"id", "task", "date_due", "date_done"})
        public Todo(long id, String task, @Nullable LocalDate dateDue, @Nullable LocalDate dateDone) {
            this(id, task, dateDue, dateDone, dateDone == null ? null : parseReferencedBook(task));
        }

        private static @Nullable Map<String, ?> parseReferencedBook(String task) {
            Matcher matcher = REFERENCED_BOOK_PATTERN.matcher(task);
            if (!matcher.matches()) return null;

            List<BookIndex.Book> book = PipeComplete.getCompletionsSorted(matcher.group("subject"));
            if (book.isEmpty()) return null;

            int page = Utils.parseInt(matcher.group("page")).orElse(-1);
            if (page <= 0) return null;

            return Map.of("book-id", book.get(0).id(), "page", page);
        }
    }
}
