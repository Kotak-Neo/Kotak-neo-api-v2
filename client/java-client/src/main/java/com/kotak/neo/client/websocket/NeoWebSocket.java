package com.kotak.neo.client.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.kotak.neo.client.Settings;
import com.kotak.neo.client.Urls;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * NeoWebSocket manages market-data and order-feed WebSocket connections.
 * Mirrors the Python NeoWebSocket class.
 */
public class NeoWebSocket {
    private static final Gson GSON = new Gson();

    private final String sid;
    private final String token;
    private final String serverId;
    private final String dataCenter;

    private WebSocketClient marketClient;
    private WebSocketClient orderClient;
    private final ScheduledExecutorService heartbeatPool = Executors.newScheduledThreadPool(2);

    public Consumer<String> onOpen;
    public Consumer<Object> onMessage;
    public Consumer<Throwable> onError;
    public Consumer<String> onClose;

    public NeoWebSocket(String sid, String token, String serverId, String dataCenter) {
        this.sid = sid;
        this.token = token;
        this.serverId = serverId;
        this.dataCenter = dataCenter;
    }

    public void getLiveFeed(List<Instrument> instruments, boolean isIndex, boolean isDepth) throws Exception {
        String subType = Settings.REQ_TYPE_VALUES.get("SCRIP_SUBS");
        if (isIndex) subType = Settings.REQ_TYPE_VALUES.get("INDEX_SUBS");
        if (isDepth) subType = Settings.REQ_TYPE_VALUES.get("DEPTH_SUBS");

        if (marketClient == null || !marketClient.isOpen()) openMarket();

        String scrips = instruments.stream()
                .map(i -> i.exchangeSegment + "|" + i.instrumentToken)
                .collect(Collectors.joining("&"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", subType);
        payload.put("scrips", scrips);
        payload.put("channelnum", 2);
        marketClient.send(GSON.toJson(payload));
    }

    public void unSubscribeList(List<Instrument> instruments, boolean isIndex, boolean isDepth) {
        if (marketClient == null || !marketClient.isOpen())
            throw new IllegalStateException("Socket Connection has been closed");
        String unsub = Settings.REQ_TYPE_VALUES.get("SCRIP_UNSUBS");
        if (isIndex) unsub = Settings.REQ_TYPE_VALUES.get("INDEX_UNSUBS");
        if (isDepth) unsub = Settings.REQ_TYPE_VALUES.get("DEPTH_UNSUBS");
        String scrips = instruments.stream()
                .map(i -> i.exchangeSegment + "|" + i.instrumentToken)
                .collect(Collectors.joining("&"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", unsub);
        payload.put("scrips", scrips);
        payload.put("channelnum", 2);
        marketClient.send(GSON.toJson(payload));
    }

    public void getOrderFeed() throws Exception {
        if (orderClient != null && orderClient.isOpen()) return;
        openOrder();
    }

    public void close() {
        if (marketClient != null) marketClient.close();
        if (orderClient != null) orderClient.close();
        heartbeatPool.shutdownNow();
    }

    private void openMarket() throws Exception {
        marketClient = new WebSocketClient(new URI(Urls.WEBSOCKET_URL)) {
            @Override public void onOpen(ServerHandshake hs) {
                Map<String, Object> handshake = new HashMap<>();
                handshake.put("type", "cn");
                handshake.put("Authorization", token);
                handshake.put("Sid", sid);
                send(GSON.toJson(handshake));
                if (onOpen != null) onOpen.accept("market socket opened");
                heartbeatPool.scheduleAtFixedRate(() -> {
                    if (isOpen()) send("{\"type\":\"hb\"}");
                }, 29, 29, TimeUnit.SECONDS);
            }
            @Override public void onMessage(String msg) { handleMarketText(msg); }
            @Override public void onMessage(ByteBuffer bytes) {
                byte[] buf = new byte[bytes.remaining()];
                bytes.get(buf);
                try {
                    List<HsWebSocketCodec.Frame> frames = HsWebSocketCodec.decode(buf);
                    if (onMessage != null) {
                        Map<String, Object> out = new HashMap<>();
                        out.put("type", "stock_feed");
                        out.put("data", frames);
                        onMessage.accept(out);
                    }
                } catch (Throwable t) {
                    if (onError != null) onError.accept(t);
                }
            }
            @Override public void onClose(int code, String reason, boolean remote) {
                if (onClose != null) onClose.accept("market socket closed");
            }
            @Override public void onError(Exception ex) {
                if (onError != null) onError.accept(ex);
            }
        };
        marketClient.connectBlocking();
    }

    private void openOrder() throws Exception {
        String url = Urls.ORDER_FEED_URL;
        if (dataCenter != null) {
            switch (dataCenter.toLowerCase()) {
                case "adc": url = Urls.ORDER_FEED_URL_ADC; break;
                case "e21": url = Urls.ORDER_FEED_URL_E21; break;
                case "e22": url = Urls.ORDER_FEED_URL_E22; break;
                case "e41": url = Urls.ORDER_FEED_URL_E41; break;
                case "e43": url = Urls.ORDER_FEED_URL_E43; break;
                default: break;
            }
        }
        orderClient = new WebSocketClient(new URI(url)) {
            @Override public void onOpen(ServerHandshake hs) {
                Map<String, Object> handshake = new HashMap<>();
                handshake.put("type", "CONNECTION");
                handshake.put("Authorization", token);
                handshake.put("Sid", sid);
                handshake.put("source", "WEB");
                send(GSON.toJson(handshake));
                if (onOpen != null) onOpen.accept("order feed opened");
                heartbeatPool.scheduleAtFixedRate(() -> {
                    if (isOpen()) send("{\"type\":\"HB\"}");
                }, 30, 30, TimeUnit.SECONDS);
            }
            @Override public void onMessage(String msg) {
                try {
                    if (onMessage != null) {
                        Map<String, Object> out = new HashMap<>();
                        out.put("type", "order_feed");
                        out.put("data", JsonParser.parseString(msg));
                        onMessage.accept(out);
                    }
                } catch (Throwable t) {
                    if (onMessage != null) onMessage.accept(msg);
                }
            }
            @Override public void onClose(int code, String reason, boolean remote) {
                if (onClose != null) onClose.accept("order feed closed");
            }
            @Override public void onError(Exception ex) {
                if (onError != null) onError.accept(ex);
            }
        };
        orderClient.connectBlocking();
    }

    private void handleMarketText(String msg) {
        try {
            if (onMessage != null) {
                Map<String, Object> out = new HashMap<>();
                out.put("type", "stock_feed");
                out.put("data", JsonParser.parseString(msg));
                onMessage.accept(out);
            }
        } catch (Throwable t) {
            if (onMessage != null) onMessage.accept(msg);
        }
    }
}
