namespace Kotak.Neo.Models;

public class PlaceOrderRequest
{
    public string ExchangeSegment { get; set; } = "";
    public string Product { get; set; } = "";
    public string Price { get; set; } = "";
    public string OrderType { get; set; } = "";
    public string Quantity { get; set; } = "";
    public string Validity { get; set; } = "";
    public string TradingSymbol { get; set; } = "";
    public string TransactionType { get; set; } = "";
    public string? AMO { get; set; }
    public string? DisclosedQuantity { get; set; }
    public string? MarketProtection { get; set; }
    public string? PF { get; set; }
    public string? TriggerPrice { get; set; }
    public string? Tag { get; set; }
    public string? ScripToken { get; set; }
    public string? SquareOffType { get; set; }
    public string? StopLossType { get; set; }
    public string? StopLossValue { get; set; }
    public string? SquareOffValue { get; set; }
    public string? LastTradedPrice { get; set; }
    public string? TrailingStopLoss { get; set; }
    public string? TrailingSLValue { get; set; }
}

public class ModifyOrderRequest
{
    public string OrderId { get; set; } = "";
    public string Price { get; set; } = "";
    public string OrderType { get; set; } = "";
    public string Quantity { get; set; } = "";
    public string Validity { get; set; } = "";
    public string? InstrumentToken { get; set; }
    public string? ExchangeSegment { get; set; }
    public string? Product { get; set; }
    public string? TradingSymbol { get; set; }
    public string? TransactionType { get; set; }
    public string? TriggerPrice { get; set; }
    public string? DD { get; set; }
    public string? MarketProtection { get; set; }
    public string? DisclosedQuantity { get; set; }
    public string? FilledQuantity { get; set; }
    public string? AMO { get; set; }
}

public class MarginRequiredRequest
{
    public string ExchangeSegment { get; set; } = "";
    public string Price { get; set; } = "";
    public string OrderType { get; set; } = "";
    public string Product { get; set; } = "";
    public string Quantity { get; set; } = "";
    public string InstrumentToken { get; set; } = "";
    public string TransactionType { get; set; } = "";
    public string? TriggerPrice { get; set; }
    public string? BrokerName { get; set; }
    public string? BranchId { get; set; }
    public string? StopLossType { get; set; }
    public string? StopLossValue { get; set; }
    public string? SquareOffType { get; set; }
    public string? SquareOffValue { get; set; }
    public string? TrailingStopLoss { get; set; }
    public string? TrailingSLValue { get; set; }
}

public class Instrument
{
    public string InstrumentToken { get; set; } = "";
    public string ExchangeSegment { get; set; } = "";
}

public class QuoteInstrument : Instrument { }
