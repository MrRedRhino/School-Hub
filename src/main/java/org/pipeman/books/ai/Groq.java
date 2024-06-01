package org.pipeman.books.ai;

import io.javalin.http.ContentType;
import io.javalin.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.pipeman.utils.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Groq {
    private static final URI API_URL = URI.create("https://api.groq.com/openai/v1/chat/completions");
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final JSONPointer RESPONSE_POINTER = new JSONPointer("/choices/0/message/content");

    public static String generateResponse(String systemMessage, String input, String token) {
        JSONObject body = new JSONObject()
                .put("model", "llama3-8b-8192")
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "system")
                                .put("content", systemMessage)
                        )
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", input.trim()
                                )
                        )
                );

        HttpRequest request = HttpRequest.newBuilder(API_URL)
                .header(Header.AUTHORIZATION, "Bearer " + token)
                .header(Header.CONTENT_TYPE, ContentType.JSON)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = Utils.tryThis(() -> HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()));
        JSONObject responseBody = new JSONObject(response.body());

        String content = (String) responseBody.query(RESPONSE_POINTER);
        return content.trim();
    }
}
