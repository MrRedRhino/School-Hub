package org.pipeman.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.javalin.http.Context;
import org.pipeman.Database;
import org.pipeman.GoogleStorage;

import java.beans.ConstructorProperties;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ResourcesApi {
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d\\d?\\.\\d\\d?(\\.\\d{2,4})?");

    public static void getResources(Context ctx) {
        String query = Optional.ofNullable(ctx.queryParam("query")).orElse("");

        List<Resource> resources = Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT *
                        FROM class_resources
                        ORDER BY date_added DESC
                        """)
                .mapTo(Resource.class)
                .list());

        if (query.isEmpty()) {
            ctx.json(resources);
            return;
        }

        List<Integer> fullTextExcludedWords = new ArrayList<>();
        String classFilter = null;
        LocalDate dateFilter = null;

        String[] splitQuery = query.split(" +");
        for (int i = 0; i < splitQuery.length; i++) {
            String s = splitQuery[i];
            if (DATE_PATTERN.matcher(s).matches()) {
                try {
                    dateFilter = parseDate(s);
                    fullTextExcludedWords.add(i);
                } catch (DateTimeException | NumberFormatException ignored) {
                }
                break;
            }
        }

        for (int i = 0; i < splitQuery.length; i++) {
            String s = splitQuery[i];
            for (Resource resource : resources) {
                for (String aClass : resource.classes()) {
                    if (aClass.startsWith(s)) {
                        fullTextExcludedWords.add(i);
                        classFilter = s;
                        break;
                    }
                }
            }
        }

        List<Resource> filteredResources = new ArrayList<>();
        List<String> fullTextQuery = new ArrayList<>(List.of(splitQuery));
        for (int i : fullTextExcludedWords) {
            fullTextQuery.remove(i);
        }

        for (Resource resource : resources) {
            if (dateFilter != null && !resource.dateAdded().isEqual(dateFilter)) continue;
            if (classFilter != null && !hasClass(classFilter, resource)) continue;

            for (String s : fullTextQuery) {
                if (resource.title().contains(s) || resource.description().contains(s)) {
                    filteredResources.add(resource);
                    break;
                }
            }
        }
        ctx.json(filteredResources);
    }

    private static LocalDate parseDate(String date) {
        String[] split = date.split("\\.");
        int day = Integer.parseInt(split[0]);
        int month = Integer.parseInt(split[1]);
        int year = split.length == 3 ? Integer.parseInt(split[2]) : LocalDate.now().getYear();

        return LocalDate.of(year, month, day);
    }

    private static boolean hasClass(String className, Resource resource) {
        for (String aClass : resource.classes()) {
            if (aClass.startsWith(className)) {
                return true;
            }
        }
        return false;
    }

    public static void putResource(Context ctx) {
        
    }

    public static void deleteResource(Context ctx) {
        long resourceId = ctx.pathParamAsClass("id", Long.class).get();

        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        DELETE
                        FROM class_resources
                        WHERE id = :id
                        """)
                .bind("id", resourceId)
                .execute());
    }

    public static void getUploadUrl(Context ctx) {
        ctx.json(GoogleStorage.createNewUpload());
    }

    public static void addAttachment(Context ctx) {
        long resourceId = ctx.pathParamAsClass("id", Long.class).get();
        long attachmentId = ctx.queryParamAsClass("attachment-id", Long.class).get();

        Database.getJdbi().withHandle(h -> h.createUpdate("""
                        UPDATE class_resources
                        SET attachments = array_append(attachments, :attachment_id)
                        WHERE id = :resource_id
                        """)
                .bind("attachment_id", attachmentId)
                .bind("resource_id", resourceId)
                .execute());
    }

    public static void removeAttachment(Context ctx) {
        long resourceId = ctx.pathParamAsClass("id", Long.class).get();
        long attachmentId = ctx.pathParamAsClass("attachment-id", Long.class).get();

        int changes = Database.getJdbi().withHandle(h -> h.createUpdate("""
                        UPDATE class_resources
                        SET attachments = array_remove(attachments, :attachment_id)
                        WHERE id = :resource_id
                        """)
                .bind("attachment_id", attachmentId)
                .bind("resource_id", resourceId)
                .execute());

        if (changes > 0) {
            GoogleStorage.removeObject(attachmentId);
        }
    }

    public record Resource(
            String title,
            String description,
            String[] attachments,
            String[] classes,
            LocalDate dateAdded,
            @JsonSerialize(using = ToStringSerializer.class) long id
    ) {
        @ConstructorProperties({"title", "description", "attachments", "classes", "date_added", "id"})
        public Resource(String title, String description, long[] attachments, String[] classes, LocalDate dateAdded, long id) {
            this(title, description, getAttachmentUrls(attachments), classes, dateAdded, id);
        }

        private static String[] getAttachmentUrls(long[] attachments) {
            String[] attachmentUrls = new String[attachments.length];
            for (int i = 0; i < attachments.length; i++) {
                attachmentUrls[i] = GoogleStorage.getObjectUrl(attachments[i]);
            }
            return attachmentUrls;
        }
    }
}
