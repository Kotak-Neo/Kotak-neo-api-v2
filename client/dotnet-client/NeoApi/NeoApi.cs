using System.Text.Json;
using Kotak.Neo.Api;
using Kotak.Neo.Models;
using Kotak.Neo.Rest;
using Kotak.Neo.WebSocket;

namespace Kotak.Neo;

/// <summary>
/// NeoApi — top-level SDK entry point. Mirrors the Python NeoAPI class.
/// </summary>
public class NeoApi : IDisposable
{
    public Configuration Configuration { get; }
    private readonly RestClient _rest;
    private readonly LoginApi _login;
    private readonly OrderApi _orders;
    private readonly PortfolioApi _portfolio;
    private readonly MarginApi _margin;
    private readonly ScripApi _scrip;
    private readonly QuotesApi _quotes;
    private NeoWebSocket? _ws;

    public event Action<string>? OnOpen;
    public event Action<object>? OnMessage;
    public event Action<Exception>? OnError;
    public event Action<string>? OnClose;

    public NeoApi(string environment = "uat", string? accessToken = null, string? neoFinKey = null, string? consumerKey = null)
    {
        Configuration = new Configuration(environment)
        {
            BearerToken = accessToken,
            NeoFinKey = neoFinKey,
            ConsumerKey = consumerKey,
        };
        _rest = new RestClient(Configuration);
        _login = new LoginApi(_rest, Configuration);
        _orders = new OrderApi(_rest, Configuration);
        _portfolio = new PortfolioApi(_rest, Configuration);
        _margin = new MarginApi(_rest, Configuration);
        _scrip = new ScripApi(_rest, Configuration);
        _quotes = new QuotesApi(_rest, Configuration);
    }

    private JsonElement? RequireLogin()
    {
        if (!Configuration.IsLoggedIn)
            return JsonDocument.Parse("""{"Error Message":"Complete the 2fa process before accessing this application"}""").RootElement.Clone();
        return null;
    }

    // ---------- Auth ----------
    public Task<JsonElement> TotpLoginAsync(string mobileNumber, string ucc, string totp, CancellationToken ct = default)
        => _login.TotpLoginAsync(mobileNumber, ucc, totp, ct);

    public Task<JsonElement> TotpValidateAsync(string mpin, CancellationToken ct = default)
        => _login.TotpValidateAsync(mpin, ct);

    public JsonElement Logout()
    {
        var gate = RequireLogin(); if (gate is not null) return gate.Value;
        Configuration.BearerToken = null;
        Configuration.EditSid = null;
        Configuration.EditToken = null;
        return JsonDocument.Parse("""{"State":"OK","message":"You have been successfully logged out"}""").RootElement.Clone();
    }

    // ---------- Orders ----------
    public Task<JsonElement> PlaceOrderAsync(PlaceOrderRequest req, CancellationToken ct = default)
        => Gated(() => _orders.PlaceOrderAsync(req, ct));

    public Task<JsonElement> ModifyOrderAsync(ModifyOrderRequest req, CancellationToken ct = default)
        => Gated(() => _orders.ModifyOrderAsync(req, ct));

    public Task<JsonElement> CancelOrderAsync(string orderId, string amo = "NO", bool verify = false, CancellationToken ct = default)
        => Gated(() => _orders.CancelOrderAsync(orderId, amo, verify, ct));

    public Task<JsonElement> CancelCoverOrderAsync(string orderId, string amo = "NO", bool verify = false, CancellationToken ct = default)
        => Gated(() => _orders.CancelCoverOrderAsync(orderId, amo, verify, ct));

    public Task<JsonElement> CancelBracketOrderAsync(string orderId, string amo = "NO", bool verify = false, CancellationToken ct = default)
        => Gated(() => _orders.CancelBracketOrderAsync(orderId, amo, verify, ct));

    // ---------- Reports ----------
    public Task<JsonElement> OrderReportAsync(CancellationToken ct = default) => Gated(() => _orders.OrderReportAsync(ct));
    public Task<JsonElement> OrderHistoryAsync(string orderId, CancellationToken ct = default) => Gated(() => _orders.OrderHistoryAsync(orderId, ct));
    public Task<JsonElement> TradeReportAsync(string? orderId = null, CancellationToken ct = default) => Gated(() => _orders.TradeReportAsync(orderId, ct));

    // ---------- Portfolio ----------
    public Task<JsonElement> PositionsAsync(CancellationToken ct = default) => Gated(() => _portfolio.PositionsAsync(ct));
    public Task<JsonElement> HoldingsAsync(CancellationToken ct = default) => Gated(() => _portfolio.HoldingsAsync(ct));
    public Task<JsonElement> LimitsAsync(string segment = "ALL", string exchange = "ALL", string product = "ALL", CancellationToken ct = default)
        => Gated(() => _portfolio.LimitsAsync(segment, exchange, product, ct));

    // ---------- Pricing ----------
    public Task<JsonElement> MarginRequiredAsync(MarginRequiredRequest req, CancellationToken ct = default)
        => Gated(() => _margin.MarginRequiredAsync(req, ct));
    public Task<JsonElement> QuotesAsync(IEnumerable<QuoteInstrument> instruments, string? quoteType = null, CancellationToken ct = default)
        => _quotes.QuotesAsync(instruments, quoteType, ct);

    // ---------- Scrip ----------
    public Task<JsonElement> ScripMasterAsync(string? exchangeSegment = null, CancellationToken ct = default)
        => Gated(() => _scrip.ScripMasterAsync(exchangeSegment, ct));
    public Task<JsonElement> SearchScripAsync(string exchangeSegment, string symbol = "", string? expiry = null,
        string? optionType = null, string? strikePrice = null, CancellationToken ct = default)
        => Gated(() => _scrip.SearchScripAsync(exchangeSegment, symbol, expiry, optionType, strikePrice, ct));

    // ---------- Streaming ----------
    public async Task SubscribeAsync(IEnumerable<Instrument> instruments, bool isIndex = false, bool isDepth = false, CancellationToken ct = default)
    {
        if (!Configuration.IsLoggedIn) { OnError?.Invoke(new InvalidOperationException("not logged in")); return; }
        EnsureSocket();
        await _ws!.GetLiveFeedAsync(instruments, isIndex, isDepth, ct).ConfigureAwait(false);
    }
    public async Task UnSubscribeAsync(IEnumerable<Instrument> instruments, bool isIndex = false, bool isDepth = false, CancellationToken ct = default)
    {
        if (!Configuration.IsLoggedIn) { OnError?.Invoke(new InvalidOperationException("not logged in")); return; }
        EnsureSocket();
        await _ws!.UnSubscribeListAsync(instruments, isIndex, isDepth, ct).ConfigureAwait(false);
    }
    public async Task SubscribeToOrderFeedAsync(CancellationToken ct = default)
    {
        if (!Configuration.IsLoggedIn) { OnError?.Invoke(new InvalidOperationException("not logged in")); return; }
        EnsureSocket();
        await _ws!.GetOrderFeedAsync(ct).ConfigureAwait(false);
    }

    private void EnsureSocket()
    {
        if (_ws is not null) return;
        _ws = new NeoWebSocket(
            Configuration.EditSid ?? "",
            Configuration.EditToken ?? "",
            Configuration.ServerId ?? "",
            Configuration.DataCenter);
        _ws.OnOpen += m => OnOpen?.Invoke(m);
        _ws.OnMessage += m => OnMessage?.Invoke(m);
        _ws.OnError += e => OnError?.Invoke(e);
        _ws.OnClose += m => OnClose?.Invoke(m);
    }

    private async Task<JsonElement> Gated(Func<Task<JsonElement>> op)
    {
        var gate = RequireLogin();
        if (gate is not null) return gate.Value;
        try { return await op().ConfigureAwait(false); }
        catch (Exception ex)
        {
            return JsonDocument.Parse(JsonSerializer.Serialize(new { Error = ex.Message })).RootElement.Clone();
        }
    }

    public void Dispose() => _ws?.Dispose();
}
