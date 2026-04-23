# Kotak Neo — TypeScript client

Node/TypeScript port of the Python `neo_api_client`.

## Install

```bash
cd client/typescript-client
npm install
npm run typecheck
```

## Quickstart

```typescript
import { NeoAPI } from "@kotak-neo/client";

const client = new NeoAPI({
  environment: "prod",
  consumerKey: "YOUR_CONSUMER_KEY",
});

await client.totpLogin("+919999999999", "ABC12", "123456");
await client.totpValidate("1234");

const resp = await client.placeOrder({
  exchange_segment: "nse_cm",
  product: "MIS",
  price: "100.50",
  order_type: "L",
  quantity: "1",
  validity: "DAY",
  trading_symbol: "RELIANCE-EQ",
  transaction_type: "B",
});
console.log(resp);
```

## Streaming

```typescript
client.onMessage = (m) => console.log(m);
await client.subscribe([
  { instrument_token: "11536", exchange_segment: "nse_cm" },
]);
```

## Method map (Python → TypeScript)

| Python | TypeScript |
|---|---|
| `totp_login` / `totp_validate` / `logout` | `totpLogin` / `totpValidate` / `logout` |
| `place_order` / `modify_order` | `placeOrder` / `modifyOrder` |
| `cancel_order` / `cancel_cover_order` / `cancel_bracket_order` | `cancelOrder` / `cancelCoverOrder` / `cancelBracketOrder` |
| `order_report` / `order_history` / `trade_report` | `orderReport` / `orderHistory` / `tradeReport` |
| `positions` / `holdings` / `limits` | `positions` / `holdings` / `limits` |
| `margin_required` | `marginRequired` |
| `scrip_master` / `search_scrip` | `scripMaster` / `searchScrip` |
| `quotes` | `quotes` |
| `subscribe` / `un_subscribe` | `subscribe` / `unSubscribe` |
| `subscribe_to_orderfeed` | `subscribeToOrderFeed` |

## Node-only WebSocket

Uses [`ws`](https://www.npmjs.com/package/ws) which is Node-only. For browser use, swap with the native `WebSocket` API by editing `src/websocket/NeoWebSocket.ts`.
