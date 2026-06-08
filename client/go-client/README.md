# Kotak Neo — Go client

Idiomatic Go port of the Python `neo_api_client`.

## Install

```bash
cd client/go-client
go mod tidy
```

## Quickstart

```go
package main

import (
    "fmt"
    "log"

    "github.com/kotak-neo/neoapi/neoapi"
)

func main() {
    c, err := neoapi.NewClient("prod", "")
    if err != nil {
        log.Fatal(err)
    }
    c.WithConsumerKey("YOUR_CONSUMER_KEY")

    if _, err := c.TotpLogin("+919999999999", "ABC12", "123456"); err != nil {
        log.Fatal(err)
    }
    if _, err := c.TotpValidate("1234"); err != nil {
        log.Fatal(err)
    }

    resp, err := c.PlaceOrder(neoapi.PlaceOrderRequest{
        ExchangeSegment: "nse_cm",
        Product:         "MIS",
        Price:           "100.50",
        OrderType:       "L",
        Quantity:        "1",
        Validity:        "DAY",
        TradingSymbol:   "RELIANCE-EQ",
        TransactionType: "B",
    })
    fmt.Println(resp, err)
}
```

## Streaming

```go
c.OnMessage = func(m any) { fmt.Println(m) }
c.Subscribe([]neoapi.Instrument{
    {InstrumentToken: "11536", ExchangeSegment: "nse_cm"},
}, false, false)
```

## Method map

| Go method | Python equivalent |
|---|---|
| `TotpLogin` | `totp_login` |
| `TotpValidate` | `totp_validate` |
| `Logout` | `logout` |
| `PlaceOrder` | `place_order` |
| `ModifyOrder` | `modify_order` |
| `CancelOrder` / `CancelCoverOrder` / `CancelBracketOrder` | `cancel_*` |
| `OrderReport` | `order_report` |
| `OrderHistory` | `order_history` |
| `TradeReport` | `trade_report` |
| `Positions` | `positions` |
| `Holdings` | `holdings` |
| `Limits` | `limits` |
| `MarginRequired` | `margin_required` |
| `ScripMaster` | `scrip_master` |
| `SearchScrip` | `search_scrip` |
| `Quotes` | `quotes` |
| `Subscribe` / `Unsubscribe` | `subscribe` / `un_subscribe` |
| `SubscribeToOrderFeed` | `subscribe_to_orderfeed` |

## Run the example

```bash
NEO_ENV=prod \
NEO_CONSUMER=BASE64_CONSUMER \
NEO_MOBILE=+919999999999 NEO_UCC=ABC12 NEO_TOTP=123456 NEO_MPIN=1234 \
go run ./examples
```
