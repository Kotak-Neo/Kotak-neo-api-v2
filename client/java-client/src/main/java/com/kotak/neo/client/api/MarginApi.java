package com.kotak.neo.client.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kotak.neo.client.Configuration;
import com.kotak.neo.client.Settings;
import com.kotak.neo.client.rest.RestClient;
import com.kotak.neo.client.validation.Validators;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MarginApi {
    private final RestClient rest;
    private final Configuration config;

    public MarginApi(RestClient rest, Configuration config) {
        this.rest = rest;
        this.config = config;
    }

    public JsonElement marginRequired(MarginRequiredRequest r) {
        Validators.validateMargin(r.exchangeSegment, r.price, r.orderType, r.product,
                r.quantity, r.instrumentToken, r.transactionType);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("exSeg", Settings.EXCHANGE_SEGMENT.get(r.exchangeSegment));
        body.put("prc", r.price);
        body.put("prcTp", Settings.ORDER_TYPE.get(r.orderType));
        body.put("prod", Settings.PRODUCT.get(r.product));
        body.put("qty", r.quantity);
        body.put("tok", r.instrumentToken);
        body.put("trnsTp", r.transactionType);
        body.put("trgPrc", r.triggerPrice);
        body.put("brkName", r.brokerName == null ? "KOTAK" : r.brokerName);
        body.put("brnchId", r.branchId == null ? "ONLINE" : r.branchId);
        body.put("slAbsOrTks", r.stopLossType);
        body.put("slVal", r.stopLossValue);
        body.put("sqrOffAbsOrTks", r.squareOffType);
        body.put("sqrOffVal", r.squareOffValue);
        body.put("trailSL", r.trailingStopLoss);
        body.put("tSLTks", r.trailingSLValue);

        Map<String, String> headers = new HashMap<>();
        headers.put("Sid", config.editSid == null ? "" : config.editSid);
        headers.put("Auth", config.editToken == null ? "" : config.editToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        Map<String, String> query = new HashMap<>();
        query.put("sId", config.serverId == null ? "" : config.serverId);

        JsonElement resp = rest.request("POST", config.getUrl("margin"), query, headers, body).data;
        JsonObject wrap = new JsonObject();
        wrap.add("data", resp);
        return wrap;
    }

    public static class MarginRequiredRequest {
        public String exchangeSegment, price, orderType, product, quantity, instrumentToken, transactionType;
        public String triggerPrice, brokerName, branchId, stopLossType, stopLossValue,
                squareOffType, squareOffValue, trailingStopLoss, trailingSLValue;
    }
}
