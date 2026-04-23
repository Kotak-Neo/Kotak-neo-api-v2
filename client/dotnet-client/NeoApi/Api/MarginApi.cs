using System.Text.Json;
using Kotak.Neo.Models;
using Kotak.Neo.Rest;
using Kotak.Neo.Validation;

namespace Kotak.Neo.Api;

public class MarginApi
{
    private readonly RestClient _rest;
    private readonly Configuration _config;

    public MarginApi(RestClient rest, Configuration config)
    {
        _rest = rest;
        _config = config;
    }

    public async Task<JsonElement> MarginRequiredAsync(MarginRequiredRequest r, CancellationToken ct = default)
    {
        Validators.ValidateMargin(r.ExchangeSegment, r.Price, r.OrderType, r.Product,
            r.Quantity, r.InstrumentToken, r.TransactionType);

        var (_, data, _) = await _rest.RequestAsync("POST",
            _config.GetUrl("margin"),
            queryParams: new Dictionary<string, string> { ["sId"] = _config.ServerId ?? "" },
            headers: new Dictionary<string, string>
            {
                ["Sid"] = _config.EditSid ?? "",
                ["Auth"] = _config.EditToken ?? "",
                ["Content-Type"] = "application/x-www-form-urlencoded",
            },
            body: new Dictionary<string, object?>
            {
                ["exSeg"] = Settings.ExchangeSegment[r.ExchangeSegment],
                ["prc"] = r.Price,
                ["prcTp"] = Settings.OrderType[r.OrderType],
                ["prod"] = Settings.Product[r.Product],
                ["qty"] = r.Quantity,
                ["tok"] = r.InstrumentToken,
                ["trnsTp"] = r.TransactionType,
                ["trgPrc"] = r.TriggerPrice,
                ["brkName"] = r.BrokerName ?? "KOTAK",
                ["brnchId"] = r.BranchId ?? "ONLINE",
                ["slAbsOrTks"] = r.StopLossType,
                ["slVal"] = r.StopLossValue,
                ["sqrOffAbsOrTks"] = r.SquareOffType,
                ["sqrOffVal"] = r.SquareOffValue,
                ["trailSL"] = r.TrailingStopLoss,
                ["tSLTks"] = r.TrailingSLValue,
            }, ct: ct);
        return JsonDocument.Parse(JsonSerializer.Serialize(new { data })).RootElement.Clone();
    }
}
