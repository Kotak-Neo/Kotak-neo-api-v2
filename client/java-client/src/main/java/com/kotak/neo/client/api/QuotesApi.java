package com.kotak.neo.client.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kotak.neo.client.Configuration;
import com.kotak.neo.client.rest.RestClient;
import com.kotak.neo.client.websocket.Instrument;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuotesApi {
    private final RestClient rest;
    private final Configuration config;

    public QuotesApi(RestClient rest, Configuration config) {
        this.rest = rest;
        this.config = config;
    }

    public JsonElement quotes(List<Instrument> instruments, String quoteType) {
        if (instruments == null || instruments.isEmpty()) {
            JsonObject err = new JsonObject();
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            JsonObject inner = new JsonObject();
            inner.addProperty("message", "Validation Errors! instrument_tokens are missing");
            arr.add(inner);
            err.add("error", arr);
            return err;
        }
        String qt = quoteType == null || quoteType.isEmpty() ? "all" : quoteType;
        String joined = instruments.stream()
                .map(i -> i.exchangeSegment + "|" + i.instrumentToken)
                .collect(Collectors.joining(","));
        String raw = config.getUrl("quotes_neo_symbol");
        String url = raw.replace("{neo_symbols}", URLEncoder.encode(joined, StandardCharsets.UTF_8))
                .replace("{quote_type}", qt);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", config.consumerKey == null ? "" : config.consumerKey);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return rest.request("GET", url, null, headers, null).data;
    }
}
