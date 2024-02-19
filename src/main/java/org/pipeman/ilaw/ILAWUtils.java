package org.pipeman.ilaw;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ILAWUtils {
    public static List<HttpResponse<String>> followRedirects(String url, ILAW ilaw) throws IOException, InterruptedException {
        String[] urlAndDomain = splitDomainAndUrl(url);
        return followRedirects(urlAndDomain[0], urlAndDomain[1], ilaw);
    }

    public static List<HttpResponse<String>> followRedirects(String domain, String path, ILAW ilaw) throws IOException, InterruptedException {
        List<HttpResponse<String>> out = new ArrayList<>();

        HttpResponse<String> lastResponse = ilaw.sendRequest(createRequest(domain + path), HttpResponse.BodyHandlers.ofString());
        out.add(lastResponse);

        while (lastResponse.statusCode() == 301 || lastResponse.statusCode() == 302) {
            lastResponse = ilaw.sendRequest(createRequest(createUrl(domain, lastResponse.headers().firstValue("location").orElse(""))), HttpResponse.BodyHandlers.ofString());
            out.add(lastResponse);
        }

        return out;
    }

    public static String[] splitDomainAndUrl(String url) {
        String[] out = new String[2];
        if (url.startsWith("/")) {
            out[0] = "";
            out[1] = url;
        } else {
            int domain = url.indexOf("://") + 3;
            int path = url.substring(domain).indexOf("/") + domain;

            out[0] = url.substring(0, path);
            out[1] = url.substring(path);
        }
        return out;
    }

    public static <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static String createUrl(String domain, String path) {
        return path.startsWith("https://") || path.startsWith("http://") ? path : domain + path;
    }

    public static HttpRequest createRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
    }

    public static <R, K, V> List<R> map(Map<K, V> map, BiFunction<K, V, R> mapper) {
        List<R> out = new ArrayList<>(map.size());
        map.forEach((k, v) -> out.add(mapper.apply(k ,v)));
        return out;
    }

    public static String writeFormBody(Map<String, String> entries) {
        List<String> list = map(entries, (k, v) -> {
            String encodedK = URLEncoder.encode(k, Charset.defaultCharset());
            String encodedV = URLEncoder.encode(v, Charset.defaultCharset());
            return encodedK + "=" + encodedV;
        });
        return String.join("&", list);
    }
}
