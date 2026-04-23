# Kotak Neo API — Multi-language Clients

This folder ships four language ports of the reference Python SDK (`neo_api_client/` at the repo root). Each port offers the same public surface — a single `NeoAPI` / `NeoApi` class that speaks to Kotak Neo's REST and WebSocket endpoints.

| Client | Path | Language | Build tool |
|---|---|---|---|
| Go | [`go-client/`](./go-client) | Go 1.21+ | `go build` |
| Java | [`java-client/`](./java-client) | Java 17+ | Maven |
| TypeScript | [`typescript-client/`](./typescript-client) | Node 18+ / TypeScript 5 | npm |
| .NET | [`dotnet-client/`](./dotnet-client) | .NET 8 | `dotnet` CLI |

## Feature parity matrix

All four clients expose the same methods that the Python SDK does. Method names are adjusted to the idiom of each language (`snake_case` in Python, `camelCase` in TypeScript/Java, `PascalCase` in Go/.NET).



## Shared semantics

- **Environment**: every client takes `"prod"` or `"uat"` and routes to the matching base URL.
- **Auth**: 2-step TOTP (login → validate) stores `editToken`, `sid`, `rid`, `serverId` in a per-instance Configuration.
- **Headers**: `Sid`, `Auth`, `neo-fin-key`, and `Authorization` (consumer key) mirror the Python wire contract.
- **Request bodies**: order/modify/cancel endpoints send `application/x-www-form-urlencoded` with a `jData` field, matching Python.
- **Responses**: kept loose (`map[string]any` / `JsonObject` / `any` / `JsonElement`) to match Python's dict contract — callers parse fields they care about.

## Reference

- Python source: `../neo_api_client/`
- Endpoint docs: `../docs/`

Each subfolder has its own README with a quickstart. The Python SDK remains the source-of-truth; if the wire contract changes in `neo_api_client/`, port the change to all four clients.
