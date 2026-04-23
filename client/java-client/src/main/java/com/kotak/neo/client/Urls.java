package com.kotak.neo.client;

import java.util.Map;

public final class Urls {
    public static final String WEBSOCKET_URL = "wss://mlhsm.kotaksecurities.com";
    public static final String ORDER_FEED_URL = "wss://mis.kotaksecurities.com/realtime";
    public static final String ORDER_FEED_URL_ADC = "wss://cis.kotaksecurities.com/realtime";
    public static final String ORDER_FEED_URL_E21 = "wss://e21.kotaksecurities.com/realtime";
    public static final String ORDER_FEED_URL_E22 = "wss://e22.kotaksecurities.com/realtime";
    public static final String ORDER_FEED_URL_E41 = "wss://e41.kotaksecurities.com/realtime";
    public static final String ORDER_FEED_URL_E43 = "wss://e43.kotaksecurities.com/realtime";

    public static final String BASE_URL = "https://mis.kotaksecurities.com";

    public static final String UAT_BASE_URL = "https://nsbxapi-gw.kotaksecurities.com/";
    public static final String PROD_BASE_URL = "https://mnapi.kotaksecurities.com/";

    public static final Map<String, String> UAT_PATHS = Map.ofEntries(
            Map.entry("totp_login", "api/1.0/login/v6/totp/login"),
            Map.entry("totp_validate", "api/1.0/login/v6/totp/validate"),
            Map.entry("place_order", "orderapi/1.0/quick/order/rule/ms/place"),
            Map.entry("cancel_order", "orderapi/1.0/quick/order/cancel"),
            Map.entry("modify_order", "orderapi/1.0/quick/order/vr/modify"),
            Map.entry("order_history", "orderapi/1.0/quick/order/history"),
            Map.entry("order_book", "orderapi/1.0/quick/user/orders"),
            Map.entry("trade_report", "orderapi/1.0/quick/user/trades"),
            Map.entry("positions", "orderapi/1.0/quick/user/positions"),
            Map.entry("holdings", "portfolio/1.0/portfolio/v1/holdings"),
            Map.entry("margin", "orderapi/1.0/quick/user/check-margin"),
            Map.entry("scrip_master", "scrip/1.0/masterscrip/file-paths"),
            Map.entry("limits", "orderapi/1.0/quick/user/limits"),
            Map.entry("logout", "api/1.0/logout"),
            Map.entry("quotes_neo_symbol", "script-details/1.0/quotes/neosymbol/{neo_symbols}/{quote_type}")
    );

    public static final Map<String, String> PROD_PATHS = Map.ofEntries(
            Map.entry("totp_login", "login/1.0/tradeApiLogin"),
            Map.entry("totp_validate", "login/1.0/tradeApiValidate"),
            Map.entry("place_order", "quick/order/rule/ms/place"),
            Map.entry("cancel_order", "quick/order/cancel"),
            Map.entry("cancel_cover_order", "quick/order/co/exit"),
            Map.entry("cancel_bracket_order", "quick/order/bo/exit"),
            Map.entry("modify_order", "quick/order/vr/modify"),
            Map.entry("order_history", "quick/order/history"),
            Map.entry("order_book", "quick/user/orders"),
            Map.entry("trade_report", "quick/user/trades"),
            Map.entry("positions", "quick/user/positions"),
            Map.entry("holdings", "portfolio/v1/holdings"),
            Map.entry("margin", "quick/user/check-margin"),
            Map.entry("scrip_master", "script-details/1.0/masterscrip/file-paths"),
            Map.entry("limits", "quick/user/limits"),
            Map.entry("logout", "apim/login/2.0/logout"),
            Map.entry("quotes_neo_symbol", "/script-details/1.0/quotes/neosymbol/{neo_symbols}/{quote_type}")
    );

    private Urls() {}
}
