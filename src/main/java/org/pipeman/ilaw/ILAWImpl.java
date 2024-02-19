package org.pipeman.ilaw;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pipeman.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class ILAWImpl implements ILAW {
    private static final Logger LOGGER = LoggerFactory.getLogger(ILAWImpl.class);
    private final HttpClient HTTP_CLIENT = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    private final String url;
    private final String username;
    private final String password;
    private String antiForgeryToken = null;

    ILAWImpl(String url, String username, String password) throws URISyntaxException, LoginException {
        this.url = url;
        this.username = username;
        this.password = password;
        login();
        startKeepaliveTimer();
    }

    private synchronized void login() throws LoginException {
        antiForgeryToken = null;
        long start = System.nanoTime();

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString(getLoginBody(username, password)))
                .build();

        HttpResponse<Void> response2 = sendRequest(postRequest, BodyHandlers.discarding());
        if (response2.statusCode() == 200) {
            throw new LoginException();
        }

        LOGGER.info("Itslearning login successful! Took " + (System.nanoTime() - start) / 1_000_000 + "ms");
    }

    private void startKeepaliveTimer() {
        long interval = Duration.ofMinutes(20).toMillis();

        new Timer("ILAW-Keepalive", true).schedule(new TimerTask() {
            @Override
            public void run() {
                HttpRequest messageRequest = HttpRequest.newBuilder(URI.create(url + "/restapi/keepalive/lastactivity/v1"))
                        .PUT(BodyPublishers.ofString("a"))
                        .header("Content-Type", "application/json")
                        .header("__ITSL_ANTIFORGERY_TOKEN__", getAntiForgeryToken())
                        .build();

                sendRequest(messageRequest, BodyHandlers.discarding());
            }
        }, interval, interval);
    }

    private String getLoginBody(String username, String password) {
        HttpRequest indexRequest = HttpRequest.newBuilder(URI.create(url)).build();
        HttpResponse<String> response = sendRequest(indexRequest, BodyHandlers.ofString());

        Document soup = Jsoup.parse(response.body());
        Element form = soup.getElementById("aspnetForm");
        Objects.requireNonNull(form, "Form not found.");

        Map<String, String> formData = new HashMap<>();
        for (Element element : form.getElementsByTag("input")) {
            if (!element.hasAttr("name")) continue;

            String name = element.attr("name");
            String value = switch (name) {
                case "ctl00$ContentPlaceHolder1$Username" -> username;
                case "ctl00$ContentPlaceHolder1$Password" -> password;
                default -> element.attr("value");
            };

            formData.put(name, value);
        }

        formData.put("__EVENTTARGET", "__Page");
        formData.put("__EVENTARGUMENT", "NativeLoginButtonClicked");

        return ILAWUtils.writeFormBody(formData);
    }

    private String getAntiForgeryToken() {
        if (antiForgeryToken == null) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url + "/main.aspx?TextURL=CourseCards")).build();
            String response = sendRequest(request, BodyHandlers.ofString()).body();

            String keyword = "\"antiForgeryHeaderValue\":\"";
            int index = response.indexOf(keyword) + keyword.length();
            antiForgeryToken = response.substring(index, response.indexOf('"', index));
        }
        return antiForgeryToken;
    }

    public void sendMessage(long recipientId, String content) {
        JSONObject bodyObject = createMessageBody(recipientId, content);

        HttpRequest messageRequest = HttpRequest.newBuilder(URI.create(url + "/restapi/personal/instantmessages/v2"))
                .POST(BodyPublishers.ofString(bodyObject.toString()))
                .header("Content-Type", "application/json")
                .header("__ITSL_ANTIFORGERY_TOKEN__", getAntiForgeryToken())
                .build();

        sendRequest(messageRequest, BodyHandlers.discarding());
    }

    @Override
    public List<Recipient> getMessageRecipients(String search) {
        String apiUrl = url + "/restapi/personal/instantmessages/recipients/search/v1?searchText=";

        String encodedSearch = URLEncoder.encode(search, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl + encodedSearch)).build();

        HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString());

        JSONArray array = new JSONArray(response.body());
        List<Recipient> out = new ArrayList<>(array.length());
        for (Object o : array) {
            JSONObject recipient = (JSONObject) o;
            out.add(new Recipient(recipient.getLong("Id"), recipient.getString("Label")));
        }
        return out;
    }

    private static JSONObject createMessageBody(long recipientId, String content) {
        JSONObject bodyObject = new JSONObject("""
                {
                  "ToPersonIds": [
                  ],
                  "CourseIds": [],
                  "ProjectIds": [],
                  "Text": "",
                  "SendAsIndividualMessages": false,
                  "OriginInstantMessageId": null,
                  "OriginMessageThreadId": null,
                  "ReferencedInstantMessageType": "None"
                }
                """);
        bodyObject.put("Text", content);
        bodyObject.getJSONArray("ToPersonIds").put(recipientId);
        return bodyObject;
    }

    @Override
    public <T> HttpResponse<T> sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        try {
            HttpResponse<T> response = HTTP_CLIENT.send(request, handler);
            LOGGER.info(request.uri() + ": " + response.statusCode());

            if (isUnauthorized(response)) {
                login();
                response = HTTP_CLIENT.send(request, handler);
                LOGGER.info(request.uri() + ": " + response.statusCode());
            }

            return response;
        } catch (IOException | InterruptedException | LoginException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> boolean isUnauthorized(HttpResponse<T> response) {
        if (response.statusCode() == 401 || response.statusCode() == 403) return true;
        if (response.statusCode() == 302) {
            String locationHeader = response.headers().firstValue("Location").orElse("");
            return locationHeader.equals("/log_out.aspx?SessionExpired=0");
        }
        return false;
    }

    @Override
    public byte[] downloadOnedriveFile(String fileId) {
        try {
            String fileUrl = url + "/LearningToolElement/ViewLearningToolElement.aspx?LearningToolElementId=" + fileId;

            HttpResponse<String> response = sendRequest(ILAWUtils.createRequest(fileUrl), BodyHandlers.ofString());
            String url = Jsoup.parse(response.body()).getElementById("ctl00_ContentPlaceHolder_ExtensionIframe")
                    .attr("src");

            Document doc = Jsoup.parse(ILAWUtils.getLast(ILAWUtils.followRedirects(url, this)).body());
            String proxyUrl = doc.getElementById("ctl00_ctl00_MainFormContent_PreviewIframe_OneDrivePreviewFrame")
                    .attr("src");

            String oneDriveBody = ILAWUtils.getLast(ILAWUtils.followRedirects(proxyUrl, this)).body();

            int start = oneDriveBody.indexOf("\"downloadUrl\":\"") + 15;
            String downloadUrl = oneDriveBody.substring(start, oneDriveBody.indexOf('"', start));
            return sendRequest(ILAWUtils.createRequest(downloadUrl), BodyHandlers.ofByteArray()).body();
        } catch (Exception e) {
            LOGGER.error("Failed to download file", e);
            throw new RuntimeException(e);
        }
    }
}
