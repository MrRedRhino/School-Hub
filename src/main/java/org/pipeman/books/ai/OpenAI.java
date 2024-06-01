package org.pipeman.books.ai;

import io.javalin.http.ContentType;
import io.javalin.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.pipeman.utils.Utils;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class OpenAI {
    private static final URI API_URL = URI.create("https://api.openai.com/v1/chat/completions");
    private static final URI SPEECH_URL = URI.create("https://api.openai.com/v1/audio/speech");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final JSONPointer RESPONSE_POINTER = new JSONPointer("/choices/0/message/content");

    public static String getCompletion(String input, String token) {
        input = input.replaceAll("(?<= )[A-z\\\\](?= )", "")
                .replaceAll("[@©ı]", "");

        JSONObject body = new JSONObject()
                .put("model", "gpt-3.5-turbo")
                .put("temperature", 0.7)
                .put("messages", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("content", input.trim())
                ));

        HttpRequest request = HttpRequest.newBuilder(API_URL)
                .header(Header.AUTHORIZATION, "Bearer " + token)
                .header(Header.CONTENT_TYPE, ContentType.JSON)
                .POST(BodyPublishers.ofString(body.toString()))
                .build();

        try {
            AI.USAGE_LIMITER.reserve(1500);
            HttpResponse<String> response = Utils.tryThis(() -> CLIENT.send(request, BodyHandlers.ofString()));
            JSONObject responseBody = new JSONObject(response.body());

            int usedTokens = responseBody.getJSONObject("usage").getInt("total_tokens");
            AI.USAGE_LIMITER.use(usedTokens);

            String content = (String) responseBody.query(RESPONSE_POINTER);
            return content.trim();
        } finally {
            AI.USAGE_LIMITER.deReserve(1500);
        }
    }

    public static InputStream doTTS(String input, String token) {
        JSONObject body = new JSONObject()
                .put("model", "tts-1")
                .put("voice", "alloy")
                .put("response_format", "aac")
                .put("input", input);

        HttpRequest request = HttpRequest.newBuilder(SPEECH_URL)
                .header(Header.AUTHORIZATION, "Bearer " + token)
                .header(Header.CONTENT_TYPE, ContentType.JSON)
                .POST(BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<InputStream> response = Utils.tryThis(() -> CLIENT.send(request, BodyHandlers.ofInputStream()));
        return response.body();
    }
}
