package com.kotak.neo.client.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kotak.neo.client.Configuration;
import com.kotak.neo.client.Settings;
import com.kotak.neo.client.rest.RestClient;
import com.kotak.neo.client.validation.Validators;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrderApi {
    private final RestClient rest;
    private final Configuration config;

    public OrderApi(RestClient rest, Configuration config) {
        this.rest = rest;
        this.config = config;
    }

    private Map<String, String> authHeaders(String contentType) {
        Map<String, String> h = new HashMap<>();
        h.put("Sid", config.editSid == null ? "" : config.editSid);
        h.put("Auth", config.editToken == null ? "" : config.editToken);
        h.put("Content-Type", contentType);
        return h;
    }

    private Map<String, String> serverId() {
        Map<String, String> q = new HashMap<>();
        q.put("sId", config.serverId == null ? "" : config.serverId);
        return q;
    }

    public JsonElement placeOrder(PlaceOrderRequest r) {
        Validators.validatePlaceOrder(r.exchangeSegment, r.product, r.price, r.orderType,
                r.quantity, r.validity, r.tradingSymbol, r.transactionType);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("am", or(r.amo, "NO"));
        body.put("dq", or(r.disclosedQuantity, "0"));
        body.put("es", Settings.EXCHANGE_SEGMENT.get(r.exchangeSegment));
        body.put("mp", or(r.marketProtection, "0"));
        body.put("pc", Settings.PRODUCT.get(r.product));
        body.put("pf", or(r.pf, "N"));
        body.put("pr", r.price);
        body.put("pt", Settings.ORDER_TYPE.get(r.orderType));
        body.put("qt", r.quantity);
        body.put("rt", r.validity);
        body.put("tp", or(r.triggerPrice, "0"));
        body.put("ts", r.tradingSymbol);
        body.put("tt", r.transactionType);
        body.put("ig", r.tag);
        body.put("tk", r.scripToken);
        body.put("sot", r.squareOffType);
        body.put("slt", r.stopLossType);
        body.put("slv", r.stopLossValue);
        body.put("sov", r.squareOffValue);
        body.put("lat", r.lastTradedPrice);
        body.put("tlt", r.trailingStopLoss);
        body.put("tsv", r.trailingSLValue);
        body.put("os", Settings.ORDER_SOURCE);

        return rest.request("POST", config.getUrl("place_order"), serverId(),
                authHeaders("application/x-www-form-urlencoded"), body).data;
    }

    public JsonElement cancelOrder(String orderId, String amo, boolean verify) {
        return cancelEndpoint("cancel_order", orderId, amo, verify);
    }

    public JsonElement cancelCoverOrder(String orderId, String amo, boolean verify) {
        return cancelEndpoint("cancel_cover_order", orderId, amo, verify);
    }

    public JsonElement cancelBracketOrder(String orderId, String amo, boolean verify) {
        return cancelEndpoint("cancel_bracket_order", orderId, amo, verify);
    }

    private JsonElement cancelEndpoint(String endpoint, String orderId, String amo, boolean verify) {
        Validators.validateCancelOrder(orderId);
        if (verify) {
            JsonElement book = orderReport();
            if (book.isJsonObject() && book.getAsJsonObject().has("data")
                    && book.getAsJsonObject().get("data").isJsonArray()) {
                for (JsonElement el : book.getAsJsonObject().getAsJsonArray("data")) {
                    JsonObject row = el.getAsJsonObject();
                    if (row.has("nOrdNo") && row.get("nOrdNo").getAsString().equals(orderId)
                            && row.has("ordSt")) {
                        String st = row.get("ordSt").getAsString();
                        if (st.equals("rejected") || st.equals("cancelled")
                                || st.equals("complete") || st.equals("traded")) {
                            if (st.equals("complete")) st = "Traded";
                            JsonObject err = new JsonObject();
                            err.addProperty("Error", "The Given Order Status is " + st);
                            err.addProperty("Reason", row.has("rejRsn") ? row.get("rejRsn").getAsString() : "");
                            return err;
                        }
                    }
                }
            }
        }
        Map<String, Object> body = new HashMap<>();
        body.put("on", orderId);
        body.put("am", amo);
        return rest.request("POST", config.getUrl(endpoint), serverId(),
                authHeaders("application/x-www-form-urlencoded"), body).data;
    }

    public JsonElement modifyOrder(ModifyOrderRequest r) {
        if (r.orderId == null || r.orderId.isEmpty())
            throw new com.kotak.neo.client.exceptions.ApiValueError("order_id is mandatory");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tk", r.instrumentToken);
        body.put("mp", or(r.marketProtection, "0"));
        body.put("pc", r.product);
        body.put("dd", or(r.dd, "NA"));
        body.put("dq", or(r.disclosedQuantity, "0"));
        body.put("vd", r.validity);
        body.put("ts", r.tradingSymbol);
        body.put("tt", r.transactionType);
        body.put("pr", r.price);
        body.put("pt", r.orderType);
        body.put("fq", or(r.filledQuantity, "0"));
        body.put("tp", or(r.triggerPrice, "0"));
        body.put("qt", r.quantity);
        body.put("no", r.orderId);
        body.put("es", r.exchangeSegment);
        body.put("am", or(r.amo, "NO"));
        body.put("os", Settings.ORDER_SOURCE);

        boolean hasAll = r.instrumentToken != null && r.exchangeSegment != null
                && r.tradingSymbol != null && r.product != null;

        if (hasAll) {
            body.put("es", Settings.EXCHANGE_SEGMENT.get(r.exchangeSegment));
            body.put("pc", Settings.PRODUCT.get(r.product));
            body.put("pt", Settings.ORDER_TYPE.get(r.orderType));
        } else {
            JsonElement book = orderReport();
            if (!book.isJsonObject() || !book.getAsJsonObject().has("data")
                    || !book.getAsJsonObject().get("data").isJsonArray()) {
                JsonObject m = new JsonObject();
                m.addProperty("Message", "There is no Data in the Order Book");
                return m;
            }
            JsonArray arr = book.getAsJsonObject().getAsJsonArray("data");
            JsonObject match = null;
            for (JsonElement el : arr) {
                JsonObject row = el.getAsJsonObject();
                if (row.has("nOrdNo") && row.get("nOrdNo").getAsString().equals(r.orderId)) {
                    match = row; break;
                }
            }
            if (match == null) {
                JsonObject m = new JsonObject();
                m.addProperty("Message", "The Given Order Number " + r.orderId + " is not matching with any of the orders");
                return m;
            }
            if (match.has("ordSt")) {
                String st = match.get("ordSt").getAsString();
                if (st.equals("rejected") || st.equals("cancelled")
                        || st.equals("complete") || st.equals("traded")) {
                    if (st.equals("complete")) st = "Traded";
                    JsonObject m = new JsonObject();
                    m.addProperty("Error", "The Given Order Status is " + st + ", So we can't proceed further");
                    m.addProperty("Reason", match.has("rejRsn") ? match.get("rejRsn").getAsString() : "");
                    return m;
                }
            }
            if (r.tradingSymbol == null && match.has("trdSym")) body.put("ts", match.get("trdSym").getAsString());
            if (r.instrumentToken == null && match.has("tok")) body.put("tk", match.get("tok").getAsString());
            if (r.product == null && match.has("prod")) body.put("pc", match.get("prod").getAsString());
            if (r.transactionType == null && match.has("trnsTp")) body.put("tt", match.get("trnsTp").getAsString());
            if (r.exchangeSegment == null && match.has("exSeg")) body.put("es", match.get("exSeg").getAsString());
            if ("0".equals(or(r.triggerPrice, "0")) && match.has("trgPrc"))
                body.put("tp", match.get("trgPrc").getAsString());
        }

        return rest.request("POST", config.getUrl("modify_order"), serverId(),
                authHeaders("application/x-www-form-urlencoded"), body).data;
    }

    public JsonElement orderReport() {
        Map<String, String> headers = authHeaders("application/x-www-form-urlencoded");
        headers.put("accept", "application/json");
        return rest.request("GET", config.getUrl("order_book"), serverId(), headers, null).data;
    }

    public JsonElement orderHistory(String orderId) {
        Validators.validateOrderHistory(orderId);
        Map<String, Object> body = new HashMap<>();
        body.put("nOrdNo", orderId);
        return rest.request("POST", config.getUrl("order_history"), serverId(),
                authHeaders("application/x-www-form-urlencoded"), body).data;
    }

    public JsonElement tradeReport(String orderId) {
        Map<String, String> headers = authHeaders("application/x-www-form-urlencoded");
        headers.put("accept", "application/json");
        JsonElement data = rest.request("GET", config.getUrl("trade_report"), serverId(), headers, null).data;
        if (orderId == null || orderId.isEmpty()) return data;
        if (!data.isJsonObject() || !data.getAsJsonObject().has("data")
                || !data.getAsJsonObject().get("data").isJsonArray()) {
            JsonObject err = new JsonObject();
            err.addProperty("Error", "There is no trades available with the given order id");
            return err;
        }
        for (JsonElement el : data.getAsJsonObject().getAsJsonArray("data")) {
            JsonObject row = el.getAsJsonObject();
            if (row.has("nOrdNo") && row.get("nOrdNo").getAsString().equals(orderId)) {
                JsonObject out = new JsonObject();
                if (data.getAsJsonObject().has("stat")) out.add("stat", data.getAsJsonObject().get("stat"));
                if (data.getAsJsonObject().has("stCode")) out.add("stCode", data.getAsJsonObject().get("stCode"));
                out.add("data", row);
                return out;
            }
        }
        JsonObject err = new JsonObject();
        err.addProperty("Error", "There is no trades available with the given order id");
        return err;
    }

    private static String or(String v, String d) { return v == null || v.isEmpty() ? d : v; }

    public static class PlaceOrderRequest {
        public String exchangeSegment, product, price, orderType, quantity, validity,
                tradingSymbol, transactionType;
        public String amo, disclosedQuantity, marketProtection, pf, triggerPrice,
                tag, scripToken, squareOffType, stopLossType, stopLossValue,
                squareOffValue, lastTradedPrice, trailingStopLoss, trailingSLValue;
    }

    public static class ModifyOrderRequest {
        public String orderId, price, orderType, quantity, validity;
        public String instrumentToken, exchangeSegment, product, tradingSymbol, transactionType,
                triggerPrice, dd, marketProtection, disclosedQuantity, filledQuantity, amo;
    }
}
