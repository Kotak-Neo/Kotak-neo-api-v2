import WebSocket from "ws";
import {
  WEBSOCKET_URL,
  ORDER_FEED_URL,
  ORDER_FEED_URL_ADC,
  ORDER_FEED_URL_E21,
  ORDER_FEED_URL_E22,
  ORDER_FEED_URL_E41,
  ORDER_FEED_URL_E43,
} from "../urls";
import { REQ_TYPE_VALUES } from "../settings";
import { Instrument, MessageCallback, ErrorCallback, VoidCallback } from "../types";
import { decodeBinaryFrame } from "./hsWebSocketCodec";

/**
 * NeoWebSocket manages the market-data and order-feed WebSocket connections.
 * Mirrors the Python NeoWebSocket class.
 */
export class NeoWebSocket {
  private marketSocket?: WebSocket;
  private orderSocket?: WebSocket;
  private marketOpen = false;
  private orderOpen = false;
  private marketHeartbeatTimer?: NodeJS.Timeout;
  private orderHeartbeatTimer?: NodeJS.Timeout;

  onOpen?: VoidCallback;
  onMessage?: MessageCallback;
  onError?: ErrorCallback;
  onClose?: VoidCallback;

  constructor(
    private sid: string,
    private token: string,
    private serverId: string,
    private dataCenter: string | undefined,
  ) {}

  async getLiveFeed(instruments: Instrument[], isIndex = false, isDepth = false): Promise<void> {
    let subType = REQ_TYPE_VALUES.SCRIP_SUBS;
    if (isIndex) subType = REQ_TYPE_VALUES.INDEX_SUBS;
    if (isDepth) subType = REQ_TYPE_VALUES.DEPTH_SUBS;

    if (!this.marketOpen) await this.openMarket();
    const scrips = instruments
      .map((i) => `${i.exchange_segment}|${i.instrument_token}`)
      .join("&");
    this.sendMarket({ type: subType, scrips, channelnum: 2 });
  }

  async unSubscribeList(instruments: Instrument[], isIndex = false, isDepth = false): Promise<void> {
    let unsub = REQ_TYPE_VALUES.SCRIP_UNSUBS;
    if (isIndex) unsub = REQ_TYPE_VALUES.INDEX_UNSUBS;
    if (isDepth) unsub = REQ_TYPE_VALUES.DEPTH_UNSUBS;
    const scrips = instruments
      .map((i) => `${i.exchange_segment}|${i.instrument_token}`)
      .join("&");
    this.sendMarket({ type: unsub, scrips, channelnum: 2 });
  }

  async getOrderFeed(): Promise<void> {
    if (this.orderOpen) return;
    await this.openOrder();
  }

  close(): void {
    this.marketOpen = false;
    this.orderOpen = false;
    if (this.marketHeartbeatTimer) clearInterval(this.marketHeartbeatTimer);
    if (this.orderHeartbeatTimer) clearInterval(this.orderHeartbeatTimer);
    this.marketSocket?.close();
    this.orderSocket?.close();
  }

  private openMarket(): Promise<void> {
    return new Promise((resolve, reject) => {
      const ws = new WebSocket(WEBSOCKET_URL);
      this.marketSocket = ws;
      ws.on("open", () => {
        this.marketOpen = true;
        ws.send(JSON.stringify({ type: "cn", Authorization: this.token, Sid: this.sid }));
        this.marketHeartbeatTimer = setInterval(() => {
          if (this.marketOpen) ws.send(JSON.stringify({ type: "hb" }));
        }, 29_000);
        this.onOpen?.("market socket opened");
        resolve();
      });
      ws.on("message", (data) => this.handleMarketMessage(data as Buffer));
      ws.on("error", (err) => {
        this.marketOpen = false;
        this.onError?.(err);
        reject(err);
      });
      ws.on("close", () => {
        this.marketOpen = false;
        if (this.marketHeartbeatTimer) clearInterval(this.marketHeartbeatTimer);
        this.onClose?.("market socket closed");
      });
    });
  }

  private openOrder(): Promise<void> {
    return new Promise((resolve, reject) => {
      let url = ORDER_FEED_URL;
      switch ((this.dataCenter || "").toLowerCase()) {
        case "adc": url = ORDER_FEED_URL_ADC; break;
        case "e21": url = ORDER_FEED_URL_E21; break;
        case "e22": url = ORDER_FEED_URL_E22; break;
        case "e41": url = ORDER_FEED_URL_E41; break;
        case "e43": url = ORDER_FEED_URL_E43; break;
      }
      const ws = new WebSocket(url);
      this.orderSocket = ws;
      ws.on("open", () => {
        this.orderOpen = true;
        ws.send(JSON.stringify({
          type: "CONNECTION", Authorization: this.token, Sid: this.sid, source: "WEB",
        }));
        this.orderHeartbeatTimer = setInterval(() => {
          if (this.orderOpen) ws.send(JSON.stringify({ type: "HB" }));
        }, 30_000);
        this.onOpen?.("order feed opened");
        resolve();
      });
      ws.on("message", (data) => this.handleOrderMessage(data as Buffer));
      ws.on("error", (err) => {
        this.orderOpen = false;
        this.onError?.(err);
        reject(err);
      });
      ws.on("close", () => {
        this.orderOpen = false;
        if (this.orderHeartbeatTimer) clearInterval(this.orderHeartbeatTimer);
        this.onClose?.("order feed closed");
      });
    });
  }

  private sendMarket(payload: unknown): void {
    if (!this.marketSocket || this.marketSocket.readyState !== WebSocket.OPEN) return;
    this.marketSocket.send(JSON.stringify(payload));
  }

  private handleMarketMessage(data: Buffer | string): void {
    if (typeof data === "string") {
      try {
        const parsed = JSON.parse(data);
        this.onMessage?.({ type: "stock_feed", data: parsed });
      } catch {
        this.onMessage?.({ type: "stock_feed", data });
      }
      return;
    }
    // Binary frame — attempt text first, fall back to binary codec.
    const txt = data.toString("utf8");
    if (txt.startsWith("[") || txt.startsWith("{")) {
      try {
        this.onMessage?.({ type: "stock_feed", data: JSON.parse(txt) });
        return;
      } catch {
        /* fall through */
      }
    }
    try {
      const frames = decodeBinaryFrame(data);
      this.onMessage?.({ type: "stock_feed", data: frames });
    } catch (err) {
      this.onError?.(err);
    }
  }

  private handleOrderMessage(data: Buffer | string): void {
    const txt = typeof data === "string" ? data : data.toString("utf8");
    try {
      this.onMessage?.({ type: "order_feed", data: JSON.parse(txt) });
    } catch {
      this.onMessage?.({ type: "order_feed", data: txt });
    }
  }
}
