package com.kotak.neo.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kotak.neo.client.api.LoginApi;
import com.kotak.neo.client.api.MarginApi;
import com.kotak.neo.client.api.OrderApi;
import com.kotak.neo.client.api.PortfolioApi;
import com.kotak.neo.client.api.QuotesApi;
import com.kotak.neo.client.api.ScripApi;
import com.kotak.neo.client.rest.RestClient;
import com.kotak.neo.client.websocket.Instrument;
import com.kotak.neo.client.websocket.NeoWebSocket;

import java.util.List;
import java.util.function.Consumer;

/**
 * NeoAPI — top-level SDK entry point. Mirrors the Python NeoAPI class.
 */
public class NeoAPI {
    public final Configuration configuration;
    private final RestClient rest;
    private final LoginApi loginApi;
    private final OrderApi orderApi;
    private final PortfolioApi portfolioApi;
    private final MarginApi marginApi;
    private final ScripApi scripApi;
    private final QuotesApi quotesApi;
    private NeoWebSocket neoWebSocket;

    public Consumer<String> onOpen;
    public Consumer<Object> onMessage;
    public Consumer<Throwable> onError;
    public Consumer<String> onClose;

    public NeoAPI() {
        this("uat", null, null, null);
    }

    public NeoAPI(String environment, String accessToken, String neoFinKey, String consumerKey) {
        this.configuration = new Configuration(environment);
        this.configuration.bearerToken = accessToken;
        this.configuration.neoFinKey = neoFinKey;
        this.configuration.consumerKey = consumerKey;
        this.rest = new RestClient(this.configuration);
        this.loginApi = new LoginApi(rest, configuration);
        this.orderApi = new OrderApi(rest, configuration);
        this.portfolioApi = new PortfolioApi(rest, configuration);
        this.marginApi = new MarginApi(rest, configuration);
        this.scripApi = new ScripApi(rest, configuration);
        this.quotesApi = new QuotesApi(rest, configuration);
    }

    private JsonObject notLoggedInError() {
        JsonObject err = new JsonObject();
        err.addProperty("Error Message", "Complete the 2fa process before accessing this application");
        return err;
    }

    private JsonElement gated(java.util.function.Supplier<JsonElement> op) {
        if (!configuration.isLoggedIn()) return notLoggedInError();
        try { return op.get(); }
        catch (Exception ex) {
            JsonObject err = new JsonObject();
            err.addProperty("Error", ex.getMessage() == null ? ex.toString() : ex.getMessage());
            return err;
        }
    }

    // ---------- Auth ----------
    public JsonElement totpLogin(String mobileNumber, String ucc, String totp) {
        return loginApi.totpLogin(mobileNumber, ucc, totp);
    }
    public JsonElement totpValidate(String mpin) { return loginApi.totpValidate(mpin); }

    public JsonElement logout() {
        if (!configuration.isLoggedIn()) return notLoggedInError();
        configuration.bearerToken = null;
        configuration.editSid = null;
        configuration.editToken = null;
        JsonObject ok = new JsonObject();
        ok.addProperty("State", "OK");
        ok.addProperty("message", "You have been successfully logged out");
        return ok;
    }

    // ---------- Orders ----------
    public JsonElement placeOrder(OrderApi.PlaceOrderRequest req) { return gated(() -> orderApi.placeOrder(req)); }
    public JsonElement modifyOrder(OrderApi.ModifyOrderRequest req) { return gated(() -> orderApi.modifyOrder(req)); }
    public JsonElement cancelOrder(String orderId, String amo, boolean verify) {
        return gated(() -> orderApi.cancelOrder(orderId, amo == null ? "NO" : amo, verify));
    }
    public JsonElement cancelCoverOrder(String orderId, String amo, boolean verify) {
        return gated(() -> orderApi.cancelCoverOrder(orderId, amo == null ? "NO" : amo, verify));
    }
    public JsonElement cancelBracketOrder(String orderId, String amo, boolean verify) {
        return gated(() -> orderApi.cancelBracketOrder(orderId, amo == null ? "NO" : amo, verify));
    }

    // ---------- Reports ----------
    public JsonElement orderReport() { return gated(orderApi::orderReport); }
    public JsonElement orderHistory(String orderId) { return gated(() -> orderApi.orderHistory(orderId)); }
    public JsonElement tradeReport(String orderId) { return gated(() -> orderApi.tradeReport(orderId)); }

    // ---------- Portfolio ----------
    public JsonElement positions() { return gated(portfolioApi::positions); }
    public JsonElement holdings() { return gated(portfolioApi::holdings); }
    public JsonElement limits(String segment, String exchange, String product) {
        String s = segment == null ? "ALL" : segment;
        String e = exchange == null ? "ALL" : exchange;
        String p = product == null ? "ALL" : product;
        return gated(() -> portfolioApi.limits(s, e, p));
    }

    // ---------- Pricing ----------
    public JsonElement marginRequired(MarginApi.MarginRequiredRequest req) {
        return gated(() -> marginApi.marginRequired(req));
    }
    public JsonElement quotes(List<Instrument> instruments, String quoteType) {
        return quotesApi.quotes(instruments, quoteType);
    }

    // ---------- Scrip ----------
    public JsonElement scripMaster(String exchangeSegment) { return gated(() -> scripApi.scripMaster(exchangeSegment)); }
    public JsonElement searchScrip(String exchangeSegment, String symbol, String expiry,
                                   String optionType, String strikePrice) {
        return gated(() -> scripApi.searchScrip(exchangeSegment, symbol, expiry, optionType, strikePrice));
    }

    // ---------- Streaming ----------
    public void subscribe(List<Instrument> instruments, boolean isIndex, boolean isDepth) {
        if (!configuration.isLoggedIn()) { if (onError != null) onError.accept(new IllegalStateException("not logged in")); return; }
        ensureSocket();
        try { neoWebSocket.getLiveFeed(instruments, isIndex, isDepth); }
        catch (Exception ex) { if (onError != null) onError.accept(ex); }
    }

    public void unSubscribe(List<Instrument> instruments, boolean isIndex, boolean isDepth) {
        if (!configuration.isLoggedIn()) { if (onError != null) onError.accept(new IllegalStateException("not logged in")); return; }
        ensureSocket();
        neoWebSocket.unSubscribeList(instruments, isIndex, isDepth);
    }

    public void subscribeToOrderFeed() {
        if (!configuration.isLoggedIn()) { if (onError != null) onError.accept(new IllegalStateException("not logged in")); return; }
        ensureSocket();
        try { neoWebSocket.getOrderFeed(); }
        catch (Exception ex) { if (onError != null) onError.accept(ex); }
    }

    private void ensureSocket() {
        if (neoWebSocket != null) return;
        neoWebSocket = new NeoWebSocket(
                configuration.editSid, configuration.editToken,
                configuration.serverId, configuration.dataCenter);
        neoWebSocket.onOpen = m -> { if (onOpen != null) onOpen.accept(m); };
        neoWebSocket.onMessage = m -> { if (onMessage != null) onMessage.accept(m); };
        neoWebSocket.onError = e -> { if (onError != null) onError.accept(e); };
        neoWebSocket.onClose = m -> { if (onClose != null) onClose.accept(m); };
    }
}
