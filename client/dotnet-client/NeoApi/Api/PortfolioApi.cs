using System.Text.Json;
using Kotak.Neo.Rest;
using Kotak.Neo.Validation;

namespace Kotak.Neo.Api;

public class PortfolioApi
{
    private readonly RestClient _rest;
    private readonly Configuration _config;

    public PortfolioApi(RestClient rest, Configuration config)
    {
        _rest = rest;
        _config = config;
    }

    private Dictionary<string, string> Auth(string contentType) => new()
    {
        ["Sid"] = _config.EditSid ?? "",
        ["Auth"] = _config.EditToken ?? "",
        ["Content-Type"] = contentType,
    };

    private Dictionary<string, string> ServerId() =>
        new() { ["sId"] = _config.ServerId ?? "" };

    public async Task<JsonElement> PositionsAsync(CancellationToken ct = default)
    {
        var headers = Auth("application/x-www-form-urlencoded");
        headers["accept"] = "application/json";
        var (_, data, _) = await _rest.RequestAsync("GET",
            _config.GetUrl("positions"),
            queryParams: ServerId(), headers: headers, ct: ct);
        return data;
    }

    public async Task<JsonElement> HoldingsAsync(CancellationToken ct = default)
    {
        var headers = Auth("application/x-www-form-urlencoded");
        headers["accept"] = "*/*";
        var (_, data, _) = await _rest.RequestAsync("GET",
            _config.GetUrl("holdings"),
            queryParams: ServerId(), headers: headers, ct: ct);
        return data;
    }

    public async Task<JsonElement> LimitsAsync(string segment = "ALL", string exchange = "ALL", string product = "ALL", CancellationToken ct = default)
    {
        Validators.ValidateLimits(segment, exchange, product);
        var (_, data, _) = await _rest.RequestAsync("POST",
            _config.GetUrl("limits"),
            queryParams: ServerId(),
            headers: Auth("application/x-www-form-urlencoded"),
            body: new Dictionary<string, object?>
            {
                ["seg"] = segment, ["exch"] = exchange, ["prod"] = product,
            },
            ct: ct);
        return data;
    }
}
