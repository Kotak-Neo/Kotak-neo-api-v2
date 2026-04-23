import { Configuration } from "../configuration";
import { RestClient, Response } from "../rest";
import { EXCHANGE_SEGMENT, ORDER_SOURCE, ORDER_TYPE, PRODUCT } from "../settings";
import { PlaceOrderRequest, ModifyOrderRequest } from "../types";
import {
  validatePlaceOrder,
  validateCancelOrder,
  validateOrderHistory,
} from "../validation";

export class OrderApi {
  constructor(private rest: RestClient, private config: Configuration) {}

  private authHeaders(contentType: string): Record<string, string> {
    return {
      Sid: this.config.editSid || "",
      Auth: this.config.editToken || "",
      "Content-Type": contentType,
    };
  }

  private queryWithServerId(): Record<string, string> {
    return { sId: this.config.serverId || "" };
  }

  async placeOrder(r: PlaceOrderRequest): Promise<Response> {
    validatePlaceOrder(r);
    const body = {
      am: r.amo ?? "NO",
      dq: r.disclosed_quantity ?? "0",
      es: EXCHANGE_SEGMENT[r.exchange_segment],
      mp: r.market_protection ?? "0",
      pc: PRODUCT[r.product],
      pf: r.pf ?? "N",
      pr: r.price,
      pt: ORDER_TYPE[r.order_type],
      qt: r.quantity,
      rt: r.validity,
      tp: r.trigger_price ?? "0",
      ts: r.trading_symbol,
      tt: r.transaction_type,
      ig: r.tag,
      tk: r.scrip_token,
      sot: r.square_off_type,
      slt: r.stop_loss_type,
      slv: r.stop_loss_value,
      sov: r.square_off_value,
      lat: r.last_traded_price,
      tlt: r.trailing_stop_loss,
      tsv: r.trailing_sl_value,
      os: ORDER_SOURCE,
    };
    const res = await this.rest.request({
      method: "POST",
      url: this.config.getUrl("place_order"),
      queryParams: this.queryWithServerId(),
      headers: this.authHeaders("application/x-www-form-urlencoded"),
      body,
    });
    return res.data;
  }

  async cancelOrder(orderId: string, amo = "NO", verify = false, bookFetch?: () => Promise<Response>): Promise<Response> {
    return this.cancelEndpoint("cancel_order", orderId, amo, verify, bookFetch);
  }

  async cancelCoverOrder(orderId: string, amo = "NO", verify = false, bookFetch?: () => Promise<Response>): Promise<Response> {
    return this.cancelEndpoint("cancel_cover_order", orderId, amo, verify, bookFetch);
  }

  async cancelBracketOrder(orderId: string, amo = "NO", verify = false, bookFetch?: () => Promise<Response>): Promise<Response> {
    return this.cancelEndpoint("cancel_bracket_order", orderId, amo, verify, bookFetch);
  }

  private async cancelEndpoint(
    endpoint: string,
    orderId: string,
    amo: string,
    verify: boolean,
    bookFetch?: () => Promise<Response>,
  ): Promise<Response> {
    validateCancelOrder(orderId);
    if (verify && bookFetch) {
      const book = await bookFetch();
      if (Array.isArray(book?.data)) {
        for (const row of book.data as any[]) {
          if (row.nOrdNo === orderId) {
            if (["rejected", "cancelled", "complete", "traded"].includes(row.ordSt)) {
              const st = row.ordSt === "complete" ? "Traded" : row.ordSt;
              return { Error: `The Given Order Status is ${st}`, Reason: row.rejRsn };
            }
          }
        }
      }
    }
    const res = await this.rest.request({
      method: "POST",
      url: this.config.getUrl(endpoint),
      queryParams: this.queryWithServerId(),
      headers: this.authHeaders("application/x-www-form-urlencoded"),
      body: { on: orderId, am: amo },
    });
    return res.data;
  }

  async modifyOrder(r: ModifyOrderRequest, bookFetch: () => Promise<Response>): Promise<Response> {
    if (!r.order_id) throw new Error("order_id is mandatory");
    const body: Record<string, any> = {
      tk: r.instrument_token,
      mp: r.market_protection ?? "0",
      pc: r.product,
      dd: r.dd ?? "NA",
      dq: r.disclosed_quantity ?? "0",
      vd: r.validity,
      ts: r.trading_symbol,
      tt: r.transaction_type,
      pr: r.price,
      pt: r.order_type,
      fq: r.filled_quantity ?? "0",
      tp: r.trigger_price ?? "0",
      qt: r.quantity,
      no: r.order_id,
      es: r.exchange_segment,
      am: r.amo ?? "NO",
      os: ORDER_SOURCE,
    };

    const hasAll = !!(r.instrument_token && r.exchange_segment && r.trading_symbol && r.product);
    if (hasAll) {
      body.es = EXCHANGE_SEGMENT[r.exchange_segment!];
      body.pc = PRODUCT[r.product!];
      body.pt = ORDER_TYPE[r.order_type];
    } else {
      // hydrate from order book
      const book = await bookFetch();
      if (!Array.isArray(book?.data)) return { Message: "There is no Data in the Order Book" };
      const row = (book.data as any[]).find((it) => it.nOrdNo === r.order_id);
      if (!row) return { Message: `The Given Order Number ${r.order_id} is not matching with any of the orders` };
      if (["rejected", "cancelled", "complete", "traded"].includes(row.ordSt)) {
        const st = row.ordSt === "complete" ? "Traded" : row.ordSt;
        return {
          Error: `The Given Order Status is ${st}, So we can't proceed further`,
          Reason: row.rejRsn,
        };
      }
      body.ts = r.trading_symbol ?? row.trdSym;
      body.tk = r.instrument_token ?? row.tok;
      body.pc = r.product ?? row.prod;
      body.tt = r.transaction_type ?? row.trnsTp;
      body.es = r.exchange_segment ?? row.exSeg;
      if ((r.trigger_price ?? "0") === "0") body.tp = row.trgPrc;
    }

    const res = await this.rest.request({
      method: "POST",
      url: this.config.getUrl("modify_order"),
      queryParams: this.queryWithServerId(),
      headers: this.authHeaders("application/x-www-form-urlencoded"),
      body,
    });
    return res.data;
  }

  async orderReport(): Promise<Response> {
    const res = await this.rest.request({
      method: "GET",
      url: this.config.getUrl("order_book"),
      queryParams: this.queryWithServerId(),
      headers: { ...this.authHeaders("application/x-www-form-urlencoded"), accept: "application/json" },
    });
    return res.data;
  }

  async orderHistory(orderId: string): Promise<Response> {
    validateOrderHistory(orderId);
    const res = await this.rest.request({
      method: "POST",
      url: this.config.getUrl("order_history"),
      queryParams: this.queryWithServerId(),
      headers: this.authHeaders("application/x-www-form-urlencoded"),
      body: { nOrdNo: orderId },
    });
    return res.data;
  }

  async tradeReport(orderId?: string): Promise<Response> {
    const res = await this.rest.request({
      method: "GET",
      url: this.config.getUrl("trade_report"),
      queryParams: this.queryWithServerId(),
      headers: { ...this.authHeaders("application/x-www-form-urlencoded"), accept: "application/json" },
    });
    const data = res.data;
    if (!orderId) return data;
    if (!Array.isArray(data?.data))
      return { Error: "There is no trades available with the given order id" };
    const match = (data.data as any[]).find((it) => it.nOrdNo === orderId);
    if (!match) return { Error: "There is no trades available with the given order id" };
    return { stat: data.stat, stCode: data.stCode, data: match };
  }
}
