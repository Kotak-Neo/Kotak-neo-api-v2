using System.Text.Json;
using Kotak.Neo.Models;
using Kotak.Neo.Rest;
using Kotak.Neo.Validation;

namespace Kotak.Neo.Api;

public class OrderApi
{
    private readonly RestClient _rest;
    private readonly Configuration _config;

    public OrderApi(RestClient rest, Configuration config)
    {
        _rest = rest;
        _config = config;
    }

    private Dictionary<string, string> AuthHeaders(string contentType) => new()
    {
        ["Sid"] = _config.EditSid ?? "",
        ["Auth"] = _config.EditToken ?? "",
        ["Content-Type"] = contentType,
    };

    private Dictionary<string, string> ServerIdQuery() =>
        new() { ["sId"] = _config.ServerId ?? "" };

    public async Task<JsonElement> PlaceOrderAsync(PlaceOrderRequest r, CancellationToken ct = default)
    {
        Validators.ValidatePlaceOrder(r.ExchangeSegment, r.Product, r.Price, r.OrderType,
            r.Quantity, r.Validity, r.TradingSymbol, r.TransactionType);

        var body = new Dictionary<string, object?>
        {
            ["am"] = r.AMO ?? "NO",
            ["dq"] = r.DisclosedQuantity ?? "0",
            ["es"] = Settings.ExchangeSegment[r.ExchangeSegment],
            ["mp"] = r.MarketProtection ?? "0",
            ["pc"] = Settings.Product[r.Product],
            ["pf"] = r.PF ?? "N",
            ["pr"] = r.Price,
            ["pt"] = Settings.OrderType[r.OrderType],
            ["qt"] = r.Quantity,
            ["rt"] = r.Validity,
            ["tp"] = r.TriggerPrice ?? "0",
            ["ts"] = r.TradingSymbol,
            ["tt"] = r.TransactionType,
            ["ig"] = r.Tag,
            ["tk"] = r.ScripToken,
            ["sot"] = r.SquareOffType,
            ["slt"] = r.StopLossType,
            ["slv"] = r.StopLossValue,
            ["sov"] = r.SquareOffValue,
            ["lat"] = r.LastTradedPrice,
            ["tlt"] = r.TrailingStopLoss,
            ["tsv"] = r.TrailingSLValue,
            ["os"] = Settings.OrderSource,
        };

        var (_, data, _) = await _rest.RequestAsync("POST",
            _config.GetUrl("place_order"),
            queryParams: ServerIdQuery(),
            headers: AuthHeaders("application/x-www-form-urlencoded"),
            body: body, ct: ct);
        return data;
    }

    public Task<JsonElement> CancelOrderAsync(string orderId, string amo = "NO", bool verify = false, CancellationToken ct = default)
        => CancelEndpointAsync("cancel_order", orderId, amo, verify, ct);

    public Task<JsonElement> CancelCoverOrderAsync(string orderId, string amo = "NO", bool verify = false, CancellationToken ct = default)
        => CancelEndpointAsync("cancel_cover_order", orderId, amo, verify, ct);

    public Task<JsonElement> CancelBracketOrderAsync(string orderId, string amo = "NO", bool verify = false, CancellationToken ct = default)
        => CancelEndpointAsync("cancel_bracket_order", orderId, amo, verify, ct);

    private async Task<JsonElement> CancelEndpointAsync(string endpoint, string orderId, string amo, bool verify, CancellationToken ct)
    {
        Validators.ValidateCancelOrder(orderId);
        if (verify)
        {
            var book = await OrderReportAsync(ct).ConfigureAwait(false);
            if (book.TryGetProperty("data", out var arr) && arr.ValueKind == JsonValueKind.Array)
            {
                foreach (var row in arr.EnumerateArray())
                {
                    if (row.TryGetProperty("nOrdNo", out var nOrd) && nOrd.GetString() == orderId)
                    {
                        if (row.TryGetProperty("ordSt", out var st))
                        {
                            var s = st.GetString();
                            if (s is "rejected" or "cancelled" or "complete" or "traded")
                            {
                                if (s == "complete") s = "Traded";
                                return JsonDocument.Parse(JsonSerializer.Serialize(new
                                {
                                    Error = $"The Given Order Status is {s}",
                                    Reason = row.TryGetProperty("rejRsn", out var rr) ? rr.GetString() : null,
                                })).RootElement.Clone();
                            }
                        }
                    }
                }
            }
        }

        var body = new Dictionary<string, object?> { ["on"] = orderId, ["am"] = amo };
        var (_, data, _) = await _rest.RequestAsync("POST",
            _config.GetUrl(endpoint),
            queryParams: ServerIdQuery(),
            headers: AuthHeaders("application/x-www-form-urlencoded"),
            body: body, ct: ct);
        return data;
    }

    public async Task<JsonElement> ModifyOrderAsync(ModifyOrderRequest r, CancellationToken ct = default)
    {
        if (string.IsNullOrEmpty(r.OrderId))
            throw new Exceptions.ApiValueError("order_id is mandatory");

        var body = new Dictionary<string, object?>
        {
            ["tk"] = r.InstrumentToken,
            ["mp"] = r.MarketProtection ?? "0",
            ["pc"] = r.Product,
            ["dd"] = r.DD ?? "NA",
            ["dq"] = r.DisclosedQuantity ?? "0",
            ["vd"] = r.Validity,
            ["ts"] = r.TradingSymbol,
            ["tt"] = r.TransactionType,
            ["pr"] = r.Price,
            ["pt"] = r.OrderType,
            ["fq"] = r.FilledQuantity ?? "0",
            ["tp"] = r.TriggerPrice ?? "0",
            ["qt"] = r.Quantity,
            ["no"] = r.OrderId,
            ["es"] = r.ExchangeSegment,
            ["am"] = r.AMO ?? "NO",
            ["os"] = Settings.OrderSource,
        };

        var hasAll = !string.IsNullOrEmpty(r.InstrumentToken)
            && !string.IsNullOrEmpty(r.ExchangeSegment)
            && !string.IsNullOrEmpty(r.TradingSymbol)
            && !string.IsNullOrEmpty(r.Product);

        if (hasAll)
        {
            body["es"] = Settings.ExchangeSegment[r.ExchangeSegment!];
            body["pc"] = Settings.Product[r.Product!];
            body["pt"] = Settings.OrderType[r.OrderType];
        }
        else
        {
            var book = await OrderReportAsync(ct).ConfigureAwait(false);
            if (!book.TryGetProperty("data", out var arr) || arr.ValueKind != JsonValueKind.Array)
                return JsonDocument.Parse("""{"Message":"There is no Data in the Order Book"}""").RootElement.Clone();

            var match = arr.EnumerateArray().FirstOrDefault(row =>
                row.TryGetProperty("nOrdNo", out var n) && n.GetString() == r.OrderId);
            if (match.ValueKind != JsonValueKind.Object)
                return JsonDocument.Parse(JsonSerializer.Serialize(new
                {
                    Message = $"The Given Order Number {r.OrderId} is not matching with any of the orders"
                })).RootElement.Clone();

            if (match.TryGetProperty("ordSt", out var st))
            {
                var s = st.GetString();
                if (s is "rejected" or "cancelled" or "complete" or "traded")
                {
                    if (s == "complete") s = "Traded";
                    return JsonDocument.Parse(JsonSerializer.Serialize(new
                    {
                        Error = $"The Given Order Status is {s}, So we can't proceed further",
                        Reason = match.TryGetProperty("rejRsn", out var rr) ? rr.GetString() : null,
                    })).RootElement.Clone();
                }
            }

            body["ts"] = string.IsNullOrEmpty(r.TradingSymbol) && match.TryGetProperty("trdSym", out var ts) ? ts.GetString() : r.TradingSymbol;
            body["tk"] = string.IsNullOrEmpty(r.InstrumentToken) && match.TryGetProperty("tok", out var tk) ? tk.GetString() : r.InstrumentToken;
            body["pc"] = string.IsNullOrEmpty(r.Product) && match.TryGetProperty("prod", out var pr) ? pr.GetString() : r.Product;
            body["tt"] = string.IsNullOrEmpty(r.TransactionType) && match.TryGetProperty("trnsTp", out var tt) ? tt.GetString() : r.TransactionType;
            body["es"] = string.IsNullOrEmpty(r.ExchangeSegment) && match.TryGetProperty("exSeg", out var es) ? es.GetString() : r.ExchangeSegment;
            if ((r.TriggerPrice ?? "0") == "0" && match.TryGetProperty("trgPrc", out var tp))
                body["tp"] = tp.GetString();
        }

        var (_, data, _) = await _rest.RequestAsync("POST",
            _config.GetUrl("modify_order"),
            queryParams: ServerIdQuery(),
            headers: AuthHeaders("application/x-www-form-urlencoded"),
            body: body, ct: ct);
        return data;
    }

    public async Task<JsonElement> OrderReportAsync(CancellationToken ct = default)
    {
        var headers = AuthHeaders("application/x-www-form-urlencoded");
        headers["accept"] = "application/json";
        var (_, data, _) = await _rest.RequestAsync("GET",
            _config.GetUrl("order_book"),
            queryParams: ServerIdQuery(),
            headers: headers, ct: ct);
        return data;
    }

    public async Task<JsonElement> OrderHistoryAsync(string orderId, CancellationToken ct = default)
    {
        Validators.ValidateOrderHistory(orderId);
        var (_, data, _) = await _rest.RequestAsync("POST",
            _config.GetUrl("order_history"),
            queryParams: ServerIdQuery(),
            headers: AuthHeaders("application/x-www-form-urlencoded"),
            body: new Dictionary<string, object?> { ["nOrdNo"] = orderId }, ct: ct);
        return data;
    }

    public async Task<JsonElement> TradeReportAsync(string? orderId = null, CancellationToken ct = default)
    {
        var headers = AuthHeaders("application/x-www-form-urlencoded");
        headers["accept"] = "application/json";
        var (_, data, _) = await _rest.RequestAsync("GET",
            _config.GetUrl("trade_report"),
            queryParams: ServerIdQuery(),
            headers: headers, ct: ct);
        if (string.IsNullOrEmpty(orderId)) return data;
        if (!data.TryGetProperty("data", out var arr) || arr.ValueKind != JsonValueKind.Array)
            return JsonDocument.Parse("""{"Error":"There is no trades available with the given order id"}""").RootElement.Clone();
        foreach (var row in arr.EnumerateArray())
        {
            if (row.TryGetProperty("nOrdNo", out var n) && n.GetString() == orderId)
            {
                return JsonDocument.Parse(JsonSerializer.Serialize(new
                {
                    stat = data.TryGetProperty("stat", out var s) ? s.GetString() : null,
                    stCode = data.TryGetProperty("stCode", out var c) ? c.GetInt32() : 0,
                    data = JsonSerializer.Deserialize<Dictionary<string, JsonElement>>(row.GetRawText()),
                })).RootElement.Clone();
            }
        }
        return JsonDocument.Parse("""{"Error":"There is no trades available with the given order id"}""").RootElement.Clone();
    }
}
