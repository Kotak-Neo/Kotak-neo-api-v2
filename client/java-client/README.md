# Kotak Neo — Java client

Java 17+ port of the Python `neo_api_client`.

## Install

```bash
cd client/java-client
mvn -q compile
```

## Quickstart

```java
import com.kotak.neo.client.NeoAPI;
import com.kotak.neo.client.api.OrderApi;

NeoAPI client = new NeoAPI("prod", null, null, "YOUR_CONSUMER_KEY");

client.totpLogin("+919999999999", "ABC12", "123456");
client.totpValidate("1234");

OrderApi.PlaceOrderRequest req = new OrderApi.PlaceOrderRequest();
req.exchangeSegment = "nse_cm";
req.product = "MIS";
req.price = "100.50";
req.orderType = "L";
req.quantity = "1";
req.validity = "DAY";
req.tradingSymbol = "RELIANCE-EQ";
req.transactionType = "B";
System.out.println(client.placeOrder(req));
```

## Streaming

```java
client.onMessage = m -> System.out.println(m);
client.subscribe(
    java.util.List.of(new com.kotak.neo.client.websocket.Instrument("11536", "nse_cm")),
    false, false);
```

## Method map (Python → Java)

| Python | Java |
|---|---|
| `totp_login` / `totp_validate` / `logout` | `totpLogin` / `totpValidate` / `logout` |
| `place_order` / `modify_order` | `placeOrder` / `modifyOrder` |
| `cancel_order` etc. | `cancelOrder` / `cancelCoverOrder` / `cancelBracketOrder` |
| `order_report` / `order_history` / `trade_report` | `orderReport` / `orderHistory` / `tradeReport` |
| `positions` / `holdings` / `limits` | `positions` / `holdings` / `limits` |
| `margin_required` | `marginRequired` |
| `scrip_master` / `search_scrip` | `scripMaster` / `searchScrip` |
| `quotes` | `quotes` |
| `subscribe` / `un_subscribe` | `subscribe` / `unSubscribe` |
| `subscribe_to_orderfeed` | `subscribeToOrderFeed` |

## Run the demo

```bash
NEO_ENV=prod \
NEO_CONSUMER=BASE64_CONSUMER \
NEO_MOBILE=+919999999999 NEO_UCC=ABC12 NEO_TOTP=123456 NEO_MPIN=1234 \
mvn -q exec:java -Dexec.mainClass=com.kotak.neo.client.Demo
```
