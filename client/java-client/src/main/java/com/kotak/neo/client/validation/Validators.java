package com.kotak.neo.client.validation;

import com.kotak.neo.client.Settings;
import com.kotak.neo.client.exceptions.ApiValueError;

public final class Validators {
    private Validators() {}

    private static void nonEmpty(String val, String name) {
        if (val == null || val.trim().isEmpty())
            throw new ApiValueError(name + " is mandatory");
    }

    public static void validatePlaceOrder(String exchangeSegment, String product, String price,
                                          String orderType, String quantity, String validity,
                                          String tradingSymbol, String transactionType) {
        nonEmpty(exchangeSegment, "exchange_segment");
        nonEmpty(product, "product");
        nonEmpty(price, "price");
        nonEmpty(orderType, "order_type");
        nonEmpty(quantity, "quantity");
        nonEmpty(validity, "validity");
        nonEmpty(tradingSymbol, "trading_symbol");
        nonEmpty(transactionType, "transaction_type");
        if (!Settings.EXCHANGE_SEGMENT.containsKey(exchangeSegment))
            throw new ApiValueError("invalid exchange_segment: " + exchangeSegment);
        if (!Settings.PRODUCT.containsKey(product))
            throw new ApiValueError("invalid product: " + product);
        if (!Settings.ORDER_TYPE.containsKey(orderType))
            throw new ApiValueError("invalid order_type: " + orderType);
        String tt = transactionType.toUpperCase();
        if (!tt.equals("B") && !tt.equals("S") && !tt.equals("BUY") && !tt.equals("SELL"))
            throw new ApiValueError("transaction_type must be B/S");
    }

    public static void validateCancelOrder(String orderId) { nonEmpty(orderId, "order_id"); }
    public static void validateOrderHistory(String orderId) { nonEmpty(orderId, "order_id"); }

    public static void validateMargin(String exchangeSegment, String price, String orderType,
                                      String product, String quantity, String instrumentToken,
                                      String transactionType) {
        nonEmpty(exchangeSegment, "exchange_segment");
        nonEmpty(price, "price");
        nonEmpty(orderType, "order_type");
        nonEmpty(product, "product");
        nonEmpty(quantity, "quantity");
        nonEmpty(instrumentToken, "instrument_token");
        nonEmpty(transactionType, "transaction_type");
        if (!Settings.EXCHANGE_SEGMENT.containsKey(exchangeSegment))
            throw new ApiValueError("invalid exchange_segment: " + exchangeSegment);
    }

    public static void validateLimits(String segment, String exchange, String product) {
        if (!Settings.SEGMENT_LIMITS.contains(segment))
            throw new ApiValueError("invalid segment: " + segment);
        if (!Settings.EXCHANGE_LIMITS.contains(exchange))
            throw new ApiValueError("invalid exchange: " + exchange);
        if (!Settings.PRODUCT_LIMITS.contains(product))
            throw new ApiValueError("invalid product: " + product);
    }
}
