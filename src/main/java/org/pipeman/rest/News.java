package org.pipeman.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.javalin.http.Context;
import org.jdbi.v3.json.Json;
import org.pipeman.Database;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

public class News {
    public static void getNews(Context ctx) {
        long lastRead = ctx.queryParamAsClass("last-read", Long.class).getOrDefault(0L);
//        long lastRead = 0;

        List<NewsData> news = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT cast(extract(EPOCH FROM date_published) AS bigint), content
                        FROM news
                        WHERE date_published > to_timestamp(:last_read)
                        ORDER BY date_published DESC
                        """)
                .bind("last_read", lastRead)
                .mapTo(NewsData.class)
                .list());

        ctx.json(news);
    }

    public record NewsData(@JsonSerialize(using = ToStringSerializer.class) long datePublished, @Json Map<String, ?> content) {
        @ConstructorProperties({"date_part", "content"})
        public NewsData {}
    }
}
