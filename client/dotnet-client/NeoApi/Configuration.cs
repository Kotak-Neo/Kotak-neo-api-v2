using System.Text.Json;
using Kotak.Neo.Exceptions;

namespace Kotak.Neo;

public class Configuration
{
    public string Host { get; set; }
    public string? BearerToken { get; set; }
    public string? ViewToken { get; set; }
    public string? Sid { get; set; }
    public string? UserId { get; set; }
    public string? EditToken { get; set; }
    public string? EditSid { get; set; }
    public string? EditRid { get; set; }
    public string? ServerId { get; set; }
    public string? NeoFinKey { get; set; }
    public string? DataCenter { get; set; }
    public string? BaseUrl { get; set; }
    public string? ConsumerKey { get; set; }

    public Configuration(string environment = "uat")
    {
        Host = environment;
    }

    public bool IsLoggedIn => !string.IsNullOrEmpty(EditToken) && !string.IsNullOrEmpty(EditSid);

    public string GetDomain(bool sessionInit = false)
    {
        var host = Host.Trim().ToLowerInvariant();
        if (host != "prod" && host != "uat")
            throw new ApiValueError("environment must be 'prod' or 'uat'");
        if (sessionInit) return Urls.BaseUrl;
        if (host == "prod") return string.IsNullOrEmpty(BaseUrl) ? Urls.ProdBaseUrl : BaseUrl!;
        return Urls.UatBaseUrl;
    }

    public string GetUrl(string apiInfo)
    {
        var host = Host.Trim().ToLowerInvariant();
        var domain = GetDomain(false).TrimEnd('/');
        var path = host == "prod"
            ? (Urls.ProdPaths.TryGetValue(apiInfo, out var p) ? p : null)
            : (Urls.UatPaths.TryGetValue(apiInfo, out var p2) ? p2 : null);
        if (path == null) throw new ApiValueError($"unknown endpoint: {apiInfo}");
        return $"{domain}/{path}";
    }

    public string GetNeoFinKey()
    {
        if (!string.IsNullOrEmpty(NeoFinKey)) return NeoFinKey!;
        return Host.Trim().Equals("prod", StringComparison.OrdinalIgnoreCase)
            ? "neotradeapi"
            : "bQJNkL5z8m4aGcRgjDvXhHfSx7VpZnE";
    }

    public string ExtractUserId(string viewToken)
    {
        if (string.IsNullOrEmpty(viewToken))
            throw new ApiValueError("view_token is empty — call TotpLogin first");
        var parts = viewToken.Split('.');
        if (parts.Length < 2) throw new ApiValueError("invalid JWT");
        var payload = parts[1].Replace('-', '+').Replace('_', '/');
        payload = payload.PadRight(payload.Length + (4 - payload.Length % 4) % 4, '=');
        var json = System.Text.Encoding.UTF8.GetString(Convert.FromBase64String(payload));
        using var doc = JsonDocument.Parse(json);
        if (!doc.RootElement.TryGetProperty("sub", out var sub))
            throw new ApiKeyError("sub claim missing from token");
        UserId = sub.GetString();
        return UserId!;
    }
}
