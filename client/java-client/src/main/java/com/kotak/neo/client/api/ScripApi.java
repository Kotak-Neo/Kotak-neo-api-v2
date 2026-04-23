package com.kotak.neo.client.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kotak.neo.client.Configuration;
import com.kotak.neo.client.Settings;
import com.kotak.neo.client.rest.RestClient;

import java.util.HashMap;
import java.util.Map;

public class ScripApi {
    private final RestClient rest;
    private final Configuration config;

    public ScripApi(RestClient rest, Configuration config) {
        this.rest = rest;
        this.config = config;
    }

    public JsonElement scripMaster(String exchangeSegment) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", config.consumerKey == null ? "" : config.consumerKey);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        RestClient.Response r = rest.request("GET", config.getUrl("scrip_master"), null, headers, null);
        if (r.status != 200) return r.data;
        if (!r.data.isJsonObject() || !r.data.getAsJsonObject().has("data")) return r.data;
        JsonElement inner = r.data.getAsJsonObject().get("data");
        if (exchangeSegment == null || exchangeSegment.isEmpty()) return inner;

        String seg = Settings.EXCHANGE_SEGMENT.get(exchangeSegment);
        if (seg == null) {
            JsonObject err = new JsonObject();
            err.addProperty("Error", "Exchange segment not found");
            return err;
        }
        if (inner.isJsonObject() && inner.getAsJsonObject().has("filesPaths")
                && inner.getAsJsonObject().get("filesPaths").isJsonArray()) {
            for (JsonElement el : inner.getAsJsonObject().getAsJsonArray("filesPaths")) {
                String p = el.getAsString();
                if (p.toLowerCase().contains(seg.toLowerCase())) {
                    JsonObject out = new JsonObject();
                    out.addProperty("path", p);
                    return out;
                }
            }
        }
        JsonObject err = new JsonObject();
        err.addProperty("Error", "Exchange segment not found");
        return err;
    }

    public JsonElement searchScrip(String exchangeSegment, String symbol, String expiry,
                                   String optionType, String strikePrice) {
        if (exchangeSegment == null || exchangeSegment.isEmpty()) {
            JsonObject outer = new JsonObject();
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            JsonObject inner = new JsonObject();
            inner.addProperty("code", "10300");
            inner.addProperty("message", "Validation Errors! Exchange Segment is Mandate to proceed further");
            arr.add(inner);
            outer.add("error", arr);
            return outer;
        }
        JsonElement master = scripMaster(exchangeSegment);
        if (!master.isJsonObject() || !master.getAsJsonObject().has("path")) {
            JsonObject err = new JsonObject();
            err.addProperty("Error", "Exchange Segment is not available");
            return err;
        }
        JsonObject out = new JsonObject();
        out.addProperty("exchange_segment", exchangeSegment);
        out.addProperty("symbol", symbol == null ? "" : symbol.toLowerCase());
        if (expiry != null) out.addProperty("expiry", expiry);
        if (optionType != null) out.addProperty("option_type", optionType);
        if (strikePrice != null) out.addProperty("strike_price", strikePrice);
        out.addProperty("csv_url", master.getAsJsonObject().get("path").getAsString());
        out.addProperty("hint", "Download csv_url and filter client-side by symbol/expiry/strike");
        return out;
    }
}
