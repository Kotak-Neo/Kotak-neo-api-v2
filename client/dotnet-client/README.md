# Kotak Neo — .NET client

.NET 8 port of the Python `neo_api_client`.

## Install

```bash
cd client/dotnet-client
dotnet build
```

## Quickstart

```csharp
using Kotak.Neo;
using Kotak.Neo.Models;

using var client = new NeoApi(environment: "prod", consumerKey: "YOUR_CONSUMER_KEY");

await client.TotpLoginAsync("+919999999999", "ABC12", "123456");
await client.TotpValidateAsync("1234");

var resp = await client.PlaceOrderAsync(new PlaceOrderRequest
{
    ExchangeSegment = "nse_cm",
    Product = "MIS",
    Price = "100.50",
    OrderType = "L",
    Quantity = "1",
    Validity = "DAY",
    TradingSymbol = "RELIANCE-EQ",
    TransactionType = "B",
});
Console.WriteLine(resp);
```

## Streaming

```csharp
client.OnMessage += m => Console.WriteLine(m);
await client.SubscribeAsync(new[]
{
    new Instrument { InstrumentToken = "11536", ExchangeSegment = "nse_cm" },
});
```

## Method map (Python → .NET)

| Python | .NET |
|---|---|
| `totp_login` / `totp_validate` / `logout` | `TotpLoginAsync` / `TotpValidateAsync` / `Logout` |
| `place_order` / `modify_order` | `PlaceOrderAsync` / `ModifyOrderAsync` |
| `cancel_order` etc. | `CancelOrderAsync` / `CancelCoverOrderAsync` / `CancelBracketOrderAsync` |
| `order_report` / `order_history` / `trade_report` | `OrderReportAsync` / `OrderHistoryAsync` / `TradeReportAsync` |
| `positions` / `holdings` / `limits` | `PositionsAsync` / `HoldingsAsync` / `LimitsAsync` |
| `margin_required` | `MarginRequiredAsync` |
| `scrip_master` / `search_scrip` | `ScripMasterAsync` / `SearchScripAsync` |
| `quotes` | `QuotesAsync` |
| `subscribe` / `un_subscribe` | `SubscribeAsync` / `UnSubscribeAsync` |
| `subscribe_to_orderfeed` | `SubscribeToOrderFeedAsync` |

## Run the example

```bash
NEO_ENV=prod \
NEO_CONSUMER=BASE64_CONSUMER \
NEO_MOBILE=+919999999999 NEO_UCC=ABC12 NEO_TOTP=123456 NEO_MPIN=1234 \
dotnet run --project NeoApi.Examples
```
