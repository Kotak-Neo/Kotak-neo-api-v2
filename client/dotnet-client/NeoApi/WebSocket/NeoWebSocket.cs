using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using Kotak.Neo.Models;

namespace Kotak.Neo.WebSocket;

/// <summary>
/// NeoWebSocket manages the market-data and order-feed WebSocket connections.
/// Mirrors the Python NeoWebSocket class.
/// </summary>
public class NeoWebSocket : IDisposable
{
    private readonly string _sid;
    private readonly string _token;
    private readonly string _serverId;
    private readonly string? _dataCenter;

    private ClientWebSocket? _marketSocket;
    private ClientWebSocket? _orderSocket;
    private CancellationTokenSource? _marketCts;
    private CancellationTokenSource? _orderCts;

    public event Action<string>? OnOpen;
    public event Action<object>? OnMessage;
    public event Action<Exception>? OnError;
    public event Action<string>? OnClose;

    public NeoWebSocket(string sid, string token, string serverId, string? dataCenter)
    {
        _sid = sid;
        _token = token;
        _serverId = serverId;
        _dataCenter = dataCenter;
    }

    public async Task GetLiveFeedAsync(IEnumerable<Instrument> instruments, bool isIndex = false, bool isDepth = false, CancellationToken ct = default)
    {
        var list = instruments.ToList();
        var subType = Settings.ReqTypeValues["SCRIP_SUBS"];
        if (isIndex) subType = Settings.ReqTypeValues["INDEX_SUBS"];
        if (isDepth) subType = Settings.ReqTypeValues["DEPTH_SUBS"];

        if (_marketSocket == null || _marketSocket.State != WebSocketState.Open)
            await OpenMarketAsync(ct).ConfigureAwait(false);

        var scrips = string.Join("&", list.Select(i => $"{i.ExchangeSegment}|{i.InstrumentToken}"));
        await SendAsync(_marketSocket!, new { type = subType, scrips, channelnum = 2 }, ct).ConfigureAwait(false);
    }

    public async Task UnSubscribeListAsync(IEnumerable<Instrument> instruments, bool isIndex = false, bool isDepth = false, CancellationToken ct = default)
    {
        var list = instruments.ToList();
        var unsub = Settings.ReqTypeValues["SCRIP_UNSUBS"];
        if (isIndex) unsub = Settings.ReqTypeValues["INDEX_UNSUBS"];
        if (isDepth) unsub = Settings.ReqTypeValues["DEPTH_UNSUBS"];

        if (_marketSocket is null || _marketSocket.State != WebSocketState.Open)
            throw new InvalidOperationException("Socket Connection has been closed");

        var scrips = string.Join("&", list.Select(i => $"{i.ExchangeSegment}|{i.InstrumentToken}"));
        await SendAsync(_marketSocket, new { type = unsub, scrips, channelnum = 2 }, ct).ConfigureAwait(false);
    }

    public Task GetOrderFeedAsync(CancellationToken ct = default) => OpenOrderAsync(ct);

    private async Task OpenMarketAsync(CancellationToken ct)
    {
        _marketSocket = new ClientWebSocket();
        _marketCts = CancellationTokenSource.CreateLinkedTokenSource(ct);
        await _marketSocket.ConnectAsync(new Uri(Urls.WebSocketUrl), _marketCts.Token).ConfigureAwait(false);
        await SendAsync(_marketSocket, new { type = "cn", Authorization = _token, Sid = _sid }, _marketCts.Token).ConfigureAwait(false);
        OnOpen?.Invoke("market socket opened");
        _ = Task.Run(() => ReceiveLoopAsync(_marketSocket, "stock_feed", _marketCts.Token));
        _ = Task.Run(() => HeartbeatAsync(_marketSocket, "hb", 29, _marketCts.Token));
    }

    private async Task OpenOrderAsync(CancellationToken ct)
    {
        var url = Urls.OrderFeedUrl;
        switch ((_dataCenter ?? "").ToLowerInvariant())
        {
            case "adc": url = Urls.OrderFeedUrlAdc; break;
            case "e21": url = Urls.OrderFeedUrlE21; break;
            case "e22": url = Urls.OrderFeedUrlE22; break;
            case "e41": url = Urls.OrderFeedUrlE41; break;
            case "e43": url = Urls.OrderFeedUrlE43; break;
        }
        _orderSocket = new ClientWebSocket();
        _orderCts = CancellationTokenSource.CreateLinkedTokenSource(ct);
        await _orderSocket.ConnectAsync(new Uri(url), _orderCts.Token).ConfigureAwait(false);
        await SendAsync(_orderSocket, new { type = "CONNECTION", Authorization = _token, Sid = _sid, source = "WEB" }, _orderCts.Token).ConfigureAwait(false);
        OnOpen?.Invoke("order feed opened");
        _ = Task.Run(() => ReceiveLoopAsync(_orderSocket, "order_feed", _orderCts.Token));
        _ = Task.Run(() => HeartbeatAsync(_orderSocket, "HB", 30, _orderCts.Token));
    }

    private static async Task SendAsync(ClientWebSocket ws, object payload, CancellationToken ct)
    {
        var bytes = Encoding.UTF8.GetBytes(JsonSerializer.Serialize(payload));
        await ws.SendAsync(bytes, WebSocketMessageType.Text, true, ct).ConfigureAwait(false);
    }

    private async Task ReceiveLoopAsync(ClientWebSocket ws, string label, CancellationToken ct)
    {
        var buffer = new byte[8192];
        try
        {
            while (ws.State == WebSocketState.Open && !ct.IsCancellationRequested)
            {
                var ms = new MemoryStream();
                WebSocketReceiveResult? res;
                do
                {
                    res = await ws.ReceiveAsync(buffer, ct).ConfigureAwait(false);
                    if (res.MessageType == WebSocketMessageType.Close)
                    {
                        OnClose?.Invoke($"{label} closed");
                        return;
                    }
                    ms.Write(buffer, 0, res.Count);
                } while (!res.EndOfMessage);
                var payload = ms.ToArray();
                if (res.MessageType == WebSocketMessageType.Text)
                {
                    var text = Encoding.UTF8.GetString(payload);
                    try
                    {
                        using var doc = JsonDocument.Parse(text);
                        OnMessage?.Invoke(new { type = label, data = doc.RootElement.Clone() });
                    }
                    catch
                    {
                        OnMessage?.Invoke(new { type = label, data = text });
                    }
                }
                else
                {
                    var frames = HsWebSocketCodec.Decode(payload);
                    OnMessage?.Invoke(new { type = label, data = frames });
                }
            }
        }
        catch (Exception ex) { OnError?.Invoke(ex); }
    }

    private async Task HeartbeatAsync(ClientWebSocket ws, string type, int seconds, CancellationToken ct)
    {
        try
        {
            while (ws.State == WebSocketState.Open && !ct.IsCancellationRequested)
            {
                await Task.Delay(TimeSpan.FromSeconds(seconds), ct).ConfigureAwait(false);
                await SendAsync(ws, new { type }, ct).ConfigureAwait(false);
            }
        }
        catch { /* exit quietly on cancel */ }
    }

    public void Dispose()
    {
        _marketCts?.Cancel();
        _orderCts?.Cancel();
        _marketSocket?.Dispose();
        _orderSocket?.Dispose();
    }
}
