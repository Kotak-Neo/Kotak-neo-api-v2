package com.kotak.neo.client.validation;

import com.kotak.neo.client.Settings;
import com.kotak.neo.client.exceptions.ApiValueError;

import java.util.Set;

public final class Validators {
    private static final Set<String> VALIDITY_VALUES = Set.of("DAY", "IOC");
    private static final Set<String> PLACE_ORDER_TT = Set.of("B", "S", "Buy", "Sell");
    private static final Set<String> MARGIN_TT = Set.of("B", "S", "Buy", "Sell", "sell", "buy");

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
        if (!VALIDITY_VALUES.contains(validity))
            throw new ApiValueError("Invalid validity. Allowed values are DAY, IOC.");
        if (!PLACE_ORDER_TT.contains(transactionType))
            throw new ApiValueError("Invalid transaction type. Allowed values are B or Buy, S or Sell.");
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
        if (!Settings.PRODUCT.containsKey(product))
            throw new ApiValueError("invalid product: " + product);
        if (!Settings.ORDER_TYPE.containsKey(orderType))
            throw new ApiValueError("invalid order_type: " + orderType);
        if (!MARGIN_TT.contains(transactionType))
            throw new ApiValueError("Invalid transaction type. Allowed values are B or Buy, S or Sell.");
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
