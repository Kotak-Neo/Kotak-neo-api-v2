using System.Text.Json;
using Kotak.Neo.Rest;

namespace Kotak.Neo.Api;

public class LoginApi
{
    private readonly RestClient _rest;
    private readonly Configuration _config;

    public LoginApi(RestClient rest, Configuration config)
    {
        _rest = rest;
        _config = config;
    }

    public async Task<JsonElement> TotpLoginAsync(string mobileNumber, string ucc, string totp, CancellationToken ct = default)
    {
        if (string.IsNullOrEmpty(mobileNumber) || string.IsNullOrEmpty(ucc) || string.IsNullOrEmpty(totp))
        {
            return JsonDocument.Parse("""{"error":[{"message":"mobile_number, ucc or totp missing"}]}""").RootElement.Clone();
        }

        var host = _config.Host.Trim().ToLowerInvariant();
        var path = host == "prod" ? Urls.ProdPaths["totp_login"] : Urls.UatPaths["totp_login"];
        var url = $"{Urls.BaseUrl.TrimEnd('/')}/{path}";

        var (status, data, _) = await _rest.RequestAsync(
            "POST", url,
            headers: new Dictionary<string, string>
            {
                ["Authorization"] = _config.ConsumerKey ?? "",
                ["neo-fin-key"] = _config.GetNeoFinKey(),
                ["Content-Type"] = "application/json",
            },
            body: new { mobileNumber, ucc, totp },
            ct: ct);

        if (status is >= 200 and <= 299 && data.TryGetProperty("data", out var inner))
        {
            if (inner.TryGetProperty("token", out var tok)) _config.ViewToken = tok.GetString();
            if (inner.TryGetProperty("sid", out var sid)) _config.Sid = sid.GetString();
        }
        return data;
    }

    public async Task<JsonElement> TotpValidateAsync(string mpin, CancellationToken ct = default)
    {
        if (string.IsNullOrEmpty(mpin))
            return JsonDocument.Parse("""{"error":[{"message":"Mpin is missing"}]}""").RootElement.Clone();

        var host = _config.Host.Trim().ToLowerInvariant();
        var path = host == "prod" ? Urls.ProdPaths["totp_validate"] : Urls.UatPaths["totp_validate"];
        var url = $"{Urls.BaseUrl.TrimEnd('/')}/{path}";

        var (status, data, _) = await _rest.RequestAsync(
            "POST", url,
            headers: new Dictionary<string, string>
            {
                ["Authorization"] = _config.ConsumerKey ?? "",
                ["sid"] = _config.Sid ?? "",
                ["Auth"] = _config.ViewToken ?? "",
                ["neo-fin-key"] = _config.GetNeoFinKey(),
                ["Content-Type"] = "application/json",
            },
            body: new { mpin },
            ct: ct);

        if (status is >= 200 and <= 299 && data.TryGetProperty("data", out var inner))
        {
            if (inner.TryGetProperty("token", out var tok)) _config.EditToken = tok.GetString();
            if (inner.TryGetProperty("sid", out var sid)) _config.EditSid = sid.GetString();
            if (inner.TryGetProperty("rid", out var rid)) _config.EditRid = rid.GetString();
            if (inner.TryGetProperty("hsServerId", out var srv)) _config.ServerId = srv.GetString();
            if (inner.TryGetProperty("dataCenter", out var dc)) _config.DataCenter = dc.GetString();
            if (inner.TryGetProperty("baseUrl", out var bu)) _config.BaseUrl = bu.GetString();
        }
        return data;
    }
}
