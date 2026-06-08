using System.Text.Json;
using Kotak.Neo.Rest;

namespace Kotak.Neo.Api;

public class ScripApi
{
    private readonly RestClient _rest;
    private readonly Configuration _config;

    public ScripApi(RestClient rest, Configuration config)
    {
        _rest = rest;
        _config = config;
    }

    public async Task<JsonElement> ScripMasterAsync(string? exchangeSegment = null, CancellationToken ct = default)
    {
        var (status, data, _) = await _rest.RequestAsync("GET",
            _config.GetUrl("scrip_master"),
            headers: new Dictionary<string, string>
            {
                ["Authorization"] = _config.ConsumerKey ?? "",
                ["Content-Type"] = "application/x-www-form-urlencoded",
            }, ct: ct);
        if (status != 200) return data;

        if (!data.TryGetProperty("data", out var inner)) return data;
        if (string.IsNullOrEmpty(exchangeSegment)) return inner;

        if (!Settings.ExchangeSegment.TryGetValue(exchangeSegment, out var seg))
            return JsonDocument.Parse("""{"Error":"Exchange segment not found"}""").RootElement.Clone();

        if (inner.TryGetProperty("filesPaths", out var paths) && paths.ValueKind == JsonValueKind.Array)
        {
            foreach (var p in paths.EnumerateArray())
            {
                var s = p.GetString();
                if (s != null && s.Contains(seg, StringComparison.OrdinalIgnoreCase))
                    return JsonDocument.Parse(JsonSerializer.Serialize(new { path = s })).RootElement.Clone();
            }
        }
        return JsonDocument.Parse("""{"Error":"Exchange segment not found"}""").RootElement.Clone();
    }

    public async Task<JsonElement> SearchScripAsync(string exchangeSegment, string symbol = "",
        string? expiry = null, string? optionType = null, string? strikePrice = null, CancellationToken ct = default)
    {
        if (string.IsNullOrEmpty(exchangeSegment))
            return JsonDocument.Parse("""{"error":[{"code":"10300","message":"Validation Errors! Exchange Segment is Mandate to proceed further"}]}""").RootElement.Clone();

        var master = await ScripMasterAsync(exchangeSegment, ct).ConfigureAwait(false);
        if (!master.TryGetProperty("path", out var path))
            return JsonDocument.Parse("""{"Error":"Exchange Segment is not available"}""").RootElement.Clone();

        return JsonDocument.Parse(JsonSerializer.Serialize(new
        {
            exchange_segment = exchangeSegment,
            symbol = symbol.ToLowerInvariant(),
            expiry,
            option_type = optionType,
            strike_price = strikePrice,
            csv_url = path.GetString(),
            hint = "Download csv_url and filter client-side by symbol/expiry/strike",
        })).RootElement.Clone();
    }
}
