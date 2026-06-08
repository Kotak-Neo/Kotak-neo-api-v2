package com.kotak.neo.client.api;

import com.google.gson.JsonElement;
import com.kotak.neo.client.Configuration;
import com.kotak.neo.client.rest.RestClient;
import com.kotak.neo.client.validation.Validators;

import java.util.HashMap;
import java.util.Map;

public class PortfolioApi {
    private final RestClient rest;
    private final Configuration config;

    public PortfolioApi(RestClient rest, Configuration config) {
        this.rest = rest;
        this.config = config;
    }

    private Map<String, String> auth(String ct) {
        Map<String, String> h = new HashMap<>();
        h.put("Sid", config.editSid == null ? "" : config.editSid);
        h.put("Auth", config.editToken == null ? "" : config.editToken);
        h.put("Content-Type", ct);
        return h;
    }

    private Map<String, String> serverId() {
        Map<String, String> q = new HashMap<>();
        q.put("sId", config.serverId == null ? "" : config.serverId);
        return q;
    }

    public JsonElement positions() {
        Map<String, String> h = auth("application/x-www-form-urlencoded");
        h.put("accept", "application/json");
        return rest.request("GET", config.getUrl("positions"), serverId(), h, null).data;
    }

    public JsonElement holdings() {
        Map<String, String> h = auth("application/x-www-form-urlencoded");
        h.put("accept", "*/*");
        return rest.request("GET", config.getUrl("holdings"), serverId(), h, null).data;
    }

    public JsonElement limits(String segment, String exchange, String product) {
        Validators.validateLimits(segment, exchange, product);
        Map<String, Object> body = new HashMap<>();
        body.put("seg", segment);
        body.put("exch", exchange);
        body.put("prod", product);
        return rest.request("POST", config.getUrl("limits"), serverId(),
                auth("application/x-www-form-urlencoded"), body).data;
    }
}
