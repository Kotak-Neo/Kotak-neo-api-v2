using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Kotak.Neo.Exceptions;

namespace Kotak.Neo.Rest;

public class RestClient
{
    private readonly HttpClient _http;
    public Configuration Config { get; }

    public RestClient(Configuration config, HttpClient? http = null)
    {
        Config = config;
        _http = http ?? new HttpClient { Timeout = TimeSpan.FromSeconds(30) };
        _http.DefaultRequestHeaders.UserAgent.ParseAdd("NeoTradeApi-dotnet/1.0.0");
    }

    public async Task<(int Status, JsonElement Data, string Text)> RequestAsync(
        string method,
        string url,
        IDictionary<string, string>? queryParams = null,
        IDictionary<string, string>? headers = null,
        object? body = null,
        CancellationToken ct = default)
    {
        headers ??= new Dictionary<string, string>();
        if (!headers.ContainsKey("Content-Type"))
            headers["Content-Type"] = "application/json";

        if (queryParams is { Count: > 0 })
        {
            var pairs = string.Join("&", queryParams.Select(kv =>
                $"{Uri.EscapeDataString(kv.Key)}={Uri.EscapeDataString(kv.Value)}"));
            url += (url.Contains('?') ? "&" : "?") + pairs;
        }

        var request = new HttpRequestMessage(new HttpMethod(method.ToUpperInvariant()), url);
        var contentType = headers["Content-Type"];

        if (method is "POST" or "PUT" or "PATCH" or "DELETE")
        {
            if (contentType.Contains("json", StringComparison.OrdinalIgnoreCase))
            {
                var json = body is null ? "" : JsonSerializer.Serialize(body);
                request.Content = new StringContent(json, Encoding.UTF8, "application/json");
            }
            else if (contentType.Contains("x-www-form-urlencoded", StringComparison.OrdinalIgnoreCase))
            {
                var pairs = new List<KeyValuePair<string, string>>();
                if (body is not null)
                {
                    var json = JsonSerializer.Serialize(body);
                    pairs.Add(new KeyValuePair<string, string>("jData", json));
                }
                request.Content = new FormUrlEncodedContent(pairs);
            }
            else
            {
                throw new ApiException(0, "Invalid Content-Type", contentType);
            }
        }

        foreach (var (k, v) in headers)
        {
            if (k.Equals("Content-Type", StringComparison.OrdinalIgnoreCase)) continue;
            request.Headers.TryAddWithoutValidation(k, v);
        }

        var resp = await _http.SendAsync(request, ct).ConfigureAwait(false);
        var text = await resp.Content.ReadAsStringAsync(ct).ConfigureAwait(false);
        JsonElement data;
        try
        {
            data = string.IsNullOrEmpty(text)
                ? JsonDocument.Parse("{}").RootElement.Clone()
                : JsonDocument.Parse(text).RootElement.Clone();
        }
        catch
        {
            using var doc = JsonDocument.Parse(JsonSerializer.Serialize(new { raw = text }));
            data = doc.RootElement.Clone();
        }
        return ((int)resp.StatusCode, data, text);
    }
}
