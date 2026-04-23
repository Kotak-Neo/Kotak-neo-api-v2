using Kotak.Neo.Exceptions;

namespace Kotak.Neo.Validation;

public static class Validators
{
    private static readonly HashSet<string> ValidityValues = new() { "DAY", "IOC" };
    private static readonly HashSet<string> PlaceOrderTt = new() { "B", "S", "Buy", "Sell" };
    private static readonly HashSet<string> MarginTt = new() { "B", "S", "Buy", "Sell", "sell", "buy" };

    private static void NonEmpty(string? value, string name)
    {
        if (string.IsNullOrWhiteSpace(value))
            throw new ApiValueError($"{name} is mandatory");
    }

    public static void ValidatePlaceOrder(string exchangeSegment, string product, string price,
        string orderType, string quantity, string validity, string tradingSymbol, string transactionType)
    {
        NonEmpty(exchangeSegment, "exchange_segment");
        NonEmpty(product, "product");
        NonEmpty(price, "price");
        NonEmpty(orderType, "order_type");
        NonEmpty(quantity, "quantity");
        NonEmpty(validity, "validity");
        NonEmpty(tradingSymbol, "trading_symbol");
        NonEmpty(transactionType, "transaction_type");
        if (!Settings.ExchangeSegment.ContainsKey(exchangeSegment))
            throw new ApiValueError($"invalid exchange_segment: {exchangeSegment}");
        if (!Settings.Product.ContainsKey(product))
            throw new ApiValueError($"invalid product: {product}");
        if (!Settings.OrderType.ContainsKey(orderType))
            throw new ApiValueError($"invalid order_type: {orderType}");
        if (!ValidityValues.Contains(validity))
            throw new ApiValueError("Invalid validity. Allowed values are DAY, IOC.");
        if (!PlaceOrderTt.Contains(transactionType))
            throw new ApiValueError("Invalid transaction type. Allowed values are B or Buy, S or Sell.");
    }

    public static void ValidateCancelOrder(string orderId) => NonEmpty(orderId, "order_id");
    public static void ValidateOrderHistory(string orderId) => NonEmpty(orderId, "order_id");

    public static void ValidateMargin(string exchangeSegment, string price, string orderType,
        string product, string quantity, string instrumentToken, string transactionType)
    {
        NonEmpty(exchangeSegment, "exchange_segment");
        NonEmpty(price, "price");
        NonEmpty(orderType, "order_type");
        NonEmpty(product, "product");
        NonEmpty(quantity, "quantity");
        NonEmpty(instrumentToken, "instrument_token");
        NonEmpty(transactionType, "transaction_type");
        if (!Settings.ExchangeSegment.ContainsKey(exchangeSegment))
            throw new ApiValueError($"invalid exchange_segment: {exchangeSegment}");
        if (!Settings.Product.ContainsKey(product))
            throw new ApiValueError($"invalid product: {product}");
        if (!Settings.OrderType.ContainsKey(orderType))
            throw new ApiValueError($"invalid order_type: {orderType}");
        if (!MarginTt.Contains(transactionType))
            throw new ApiValueError("Invalid transaction type. Allowed values are B or Buy, S or Sell.");
    }

    public static void ValidateLimits(string segment, string exchange, string product)
    {
        if (!Settings.SegmentLimits.Contains(segment))
            throw new ApiValueError($"invalid segment: {segment}");
        if (!Settings.ExchangeLimits.Contains(exchange))
            throw new ApiValueError($"invalid exchange: {exchange}");
        if (!Settings.ProductLimits.Contains(product))
            throw new ApiValueError($"invalid product: {product}");
    }
}
