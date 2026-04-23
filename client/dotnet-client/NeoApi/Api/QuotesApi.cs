using System.Text.Json;
using Kotak.Neo.Models;
using Kotak.Neo.Rest;

namespace Kotak.Neo.Api;

public class QuotesApi
{
    private readonly RestClient _rest;
    private readonly Configuration _config;

    public QuotesApi(RestClient rest, Configuration config)
    {
        _rest = rest;
        _config = config;
    }

    public async Task<JsonElement> QuotesAsync(IEnumerable<QuoteInstrument> instruments, string? quoteType = null, CancellationToken ct = default)
    {
        var list = instruments?.ToList() ?? new List<QuoteInstrument>();
        if (list.Count == 0)
            return JsonDocument.Parse("""{"error":[{"message":"Validation Errors! instrument_tokens are missing"}]}""").RootElement.Clone();

        var qt = string.IsNullOrEmpty(quoteType) ? "all" : quoteType!;
        var joined = string.Join(",", list.Select(i => $"{i.ExchangeSegment}|{i.InstrumentToken}"));
        var raw = _config.GetUrl("quotes_neo_symbol");
        var url = raw
            .Replace("{neo_symbols}", Uri.EscapeDataString(joined))
            .Replace("{quote_type}", qt);

        var (_, data, _) = await _rest.RequestAsync("GET", url,
            headers: new Dictionary<string, string>
            {
                ["Authorization"] = _config.ConsumerKey ?? "",
                ["Content-Type"] = "application/x-www-form-urlencoded",
            }, ct: ct);
        return data;
    }
}
