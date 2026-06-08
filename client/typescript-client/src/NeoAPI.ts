import { Configuration } from "./configuration";
import { RestClient, Response } from "./rest";
import { LoginApi } from "./api/loginApi";
import { OrderApi } from "./api/orderApi";
import { PortfolioApi } from "./api/portfolioApi";
import { MarginApi } from "./api/marginApi";
import { ScripApi } from "./api/scripApi";
import { QuotesApi } from "./api/quotesApi";
import { NeoWebSocket } from "./websocket/NeoWebSocket";
import {
  PlaceOrderRequest,
  ModifyOrderRequest,
  MarginRequiredRequest,
  Instrument,
  QuoteInstrument,
  MessageCallback,
  ErrorCallback,
  VoidCallback,
} from "./types";

/**
 * NeoAPI — top-level SDK entry point. Mirrors the Python NeoAPI class.
 */
export class NeoAPI {
  readonly configuration: Configuration;
  private rest: RestClient;
  private loginApi: LoginApi;
  private orderApi: OrderApi;
  private portfolioApi: PortfolioApi;
  private marginApi: MarginApi;
  private scripApi: ScripApi;
  private quotesApi: QuotesApi;
  private neoWebSocket?: NeoWebSocket;

  onOpen?: VoidCallback;
  onMessage?: MessageCallback;
  onError?: ErrorCallback;
  onClose?: VoidCallback;

  constructor(opts: {
    environment?: string;
    accessToken?: string;
    neoFinKey?: string;
    consumerKey?: string;
  } = {}) {
    this.configuration = new Configuration(opts.environment || "uat");
    this.configuration.bearerToken = opts.accessToken;
    this.configuration.neoFinKey = opts.neoFinKey;
    this.configuration.consumerKey = opts.consumerKey;

    this.rest = new RestClient(this.configuration);
    this.loginApi = new LoginApi(this.rest, this.configuration);
    this.orderApi = new OrderApi(this.rest, this.configuration);
    this.portfolioApi = new PortfolioApi(this.rest, this.configuration);
    this.marginApi = new MarginApi(this.rest, this.configuration);
    this.scripApi = new ScripApi(this.rest, this.configuration);
    this.quotesApi = new QuotesApi(this.rest, this.configuration);
  }

  private requireLogin(): Response | null {
    if (!this.configuration.isLoggedIn()) {
      return { "Error Message": "Complete the 2fa process before accessing this application" };
    }
    return null;
  }

  // ---------- Auth ----------
  totpLogin(mobileNumber: string, ucc: string, totp: string): Promise<Response> {
    return this.loginApi.totpLogin(mobileNumber, ucc, totp);
  }
  totpValidate(mpin: string): Promise<Response> {
    return this.loginApi.totpValidate(mpin);
  }
  logout(): Response {
    const e = this.requireLogin();
    if (e) return e;
    this.configuration.bearerToken = undefined;
    this.configuration.editSid = undefined;
    this.configuration.editToken = undefined;
    return { State: "OK", message: "You have been successfully logged out" };
  }

  // ---------- Orders ----------
  async placeOrder(req: PlaceOrderRequest): Promise<Response> {
    const e = this.requireLogin();
    if (e) return e;
    try {
      return await this.orderApi.placeOrder(req);
    } catch (err) {
      return { Error: (err as Error).message };
    }
  }
  async modifyOrder(req: ModifyOrderRequest): Promise<Response> {
    const e = this.requireLogin();
    if (e) return e;
    try {
      return await this.orderApi.modifyOrder(req, () => this.orderApi.orderReport());
    } catch (err) {
      return { Error: (err as Error).message };
    }
  }
  async cancelOrder(orderId: string, amo = "NO", verify = false): Promise<Response> {
    const e = this.requireLogin();
    if (e) return e;
    try {
      return await this.orderApi.cancelOrder(orderId, amo, verify, () => this.orderApi.orderReport());
    } catch (err) {
      return { Error: (err as Error).message };
    }
  }
  async cancelCoverOrder(orderId: string, amo = "NO", verify = false): Promise<Response> {
    const e = this.requireLogin();
    if (e) return e;
    try {
      return await this.orderApi.cancelCoverOrder(orderId, amo, verify, () => this.orderApi.orderReport());
    } catch (err) {
      return { Error: (err as Error).message };
    }
  }
  async cancelBracketOrder(orderId: string, amo = "NO", verify = false): Promise<Response> {
    const e = this.requireLogin();
    if (e) return e;
    try {
      return await this.orderApi.cancelBracketOrder(orderId, amo, verify, () => this.orderApi.orderReport());
    } catch (err) {
      return { Error: (err as Error).message };
    }
  }

  // ---------- Reports ----------
  async orderReport(): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.orderApi.orderReport(); } catch (err) { return { Error: (err as Error).message }; }
  }
  async orderHistory(orderId: string): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.orderApi.orderHistory(orderId); } catch (err) { return { Error: (err as Error).message }; }
  }
  async tradeReport(orderId?: string): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.orderApi.tradeReport(orderId); } catch (err) { return { Error: (err as Error).message }; }
  }

  // ---------- Portfolio ----------
  async positions(): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.portfolioApi.positions(); } catch (err) { return { Error: (err as Error).message }; }
  }
  async holdings(): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.portfolioApi.holdings(); } catch (err) { return { Error: (err as Error).message }; }
  }
  async limits(segment = "ALL", exchange = "ALL", product = "ALL"): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.portfolioApi.limits(segment, exchange, product); } catch (err) { return { Error: (err as Error).message }; }
  }

  // ---------- Pricing ----------
  async marginRequired(req: MarginRequiredRequest): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.marginApi.marginRequired(req); } catch (err) { return { Error: (err as Error).message }; }
  }
  quotes(instruments: QuoteInstrument[], quoteType?: string): Promise<Response> {
    return this.quotesApi.quotes(instruments, quoteType);
  }

  // ---------- Scrip ----------
  async scripMaster(exchangeSegment?: string): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.scripApi.scripMaster(exchangeSegment); } catch (err) { return { Error: (err as Error).message }; }
  }
  async searchScrip(
    exchangeSegment: string,
    symbol = "",
    expiry?: string,
    optionType?: string,
    strikePrice?: string,
  ): Promise<Response> {
    const e = this.requireLogin(); if (e) return e;
    try { return await this.scripApi.searchScrip(exchangeSegment, symbol, expiry, optionType, strikePrice); } catch (err) { return { Error: (err as Error).message }; }
  }

  // ---------- Streaming ----------
  async subscribe(instruments: Instrument[], isIndex = false, isDepth = false): Promise<void> {
    const e = this.requireLogin(); if (e) { console.log(e); return; }
    this.ensureSocket();
    await this.neoWebSocket!.getLiveFeed(instruments, isIndex, isDepth);
  }
  async unSubscribe(instruments: Instrument[], isIndex = false, isDepth = false): Promise<void> {
    const e = this.requireLogin(); if (e) { console.log(e); return; }
    this.ensureSocket();
    await this.neoWebSocket!.unSubscribeList(instruments, isIndex, isDepth);
  }
  async subscribeToOrderFeed(): Promise<void> {
    const e = this.requireLogin(); if (e) { console.log(e); return; }
    this.ensureSocket();
    await this.neoWebSocket!.getOrderFeed();
  }

  private ensureSocket(): void {
    if (this.neoWebSocket) return;
    this.neoWebSocket = new NeoWebSocket(
      this.configuration.editSid || "",
      this.configuration.editToken || "",
      this.configuration.serverId || "",
      this.configuration.dataCenter,
    );
    this.neoWebSocket.onOpen = (m) => this.onOpen?.(m);
    this.neoWebSocket.onMessage = (m) => this.onMessage?.(m);
    this.neoWebSocket.onError = (err) => this.onError?.(err);
    this.neoWebSocket.onClose = (m) => this.onClose?.(m);
  }
}
