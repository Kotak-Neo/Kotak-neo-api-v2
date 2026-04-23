package com.kotak.neo.client.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kotak.neo.client.Configuration;
import com.kotak.neo.client.exceptions.ApiException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class RestClient {
    private static final Gson GSON = new Gson();
    private final HttpClient http;
    public final Configuration config;

    public RestClient(Configuration config) {
        this.config = config;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    }

    public static class Response {
        public int status;
        public String text;
        public JsonElement data;
    }

    public Response request(String method, String url, Map<String, String> queryParams,
                            Map<String, String> headers, Object body) {
        method = method.toUpperCase();
        if (headers == null) headers = new java.util.HashMap<>();
        headers.putIfAbsent("Content-Type", "application/json");
        headers.putIfAbsent("User-Agent", "NeoTradeApi-java/1.0.0");

        if (queryParams != null && !queryParams.isEmpty()) {
            String qs = queryParams.entrySet().stream()
                    .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                    .collect(Collectors.joining("&"));
            url += (url.contains("?") ? "&" : "?") + qs;
        }

        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url));
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();

        String ct = headers.get("Content-Type");
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")) {
            if (ct.toLowerCase().contains("json")) {
                String payload = body == null ? "" : GSON.toJson(body);
                bodyPublisher = HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8);
            } else if (ct.toLowerCase().contains("x-www-form-urlencoded")) {
                String payload = "";
                if (body != null) {
                    payload = "jData=" + enc(GSON.toJson(body));
                }
                bodyPublisher = HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8);
            } else {
                throw new ApiException(0, "Invalid Content-Type", ct);
            }
        }

        switch (method) {
            case "GET": b.GET(); break;
            case "DELETE": b.method("DELETE", bodyPublisher); break;
            default: b.method(method, bodyPublisher);
        }

        for (Map.Entry<String, String> h : headers.entrySet()) {
            // Restricted headers cannot be set directly; let the JDK add them.
            try { b.header(h.getKey(), h.getValue()); } catch (IllegalArgumentException ignored) { }
        }

        try {
            HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Response r = new Response();
            r.status = resp.statusCode();
            r.text = resp.body() == null ? "" : resp.body();
            try {
                r.data = r.text.isEmpty() ? new JsonObject() : JsonParser.parseString(r.text);
            } catch (Exception parseFail) {
                JsonObject obj = new JsonObject();
                obj.addProperty("raw", r.text);
                r.data = obj;
            }
            return r;
        } catch (IOException | InterruptedException e) {
            throw new ApiException(0, "http error", e.getMessage());
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
