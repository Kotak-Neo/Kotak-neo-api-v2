import { Configuration } from "../configuration";
import { RestClient, Response } from "../rest";
import { EXCHANGE_SEGMENT, ORDER_TYPE, PRODUCT } from "../settings";
import { MarginRequiredRequest } from "../types";
import { validateMargin } from "../validation";

export class MarginApi {
  constructor(private rest: RestClient, private config: Configuration) {}

  async marginRequired(r: MarginRequiredRequest): Promise<Response> {
    validateMargin(r);
    const res = await this.rest.request({
      method: "POST",
      url: this.config.getUrl("margin"),
      queryParams: { sId: this.config.serverId || "" },
      headers: {
        Sid: this.config.editSid || "",
        Auth: this.config.editToken || "",
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: {
        exSeg: EXCHANGE_SEGMENT[r.exchange_segment],
        prc: r.price,
        prcTp: ORDER_TYPE[r.order_type],
        prod: PRODUCT[r.product],
        qty: r.quantity,
        tok: r.instrument_token,
        trnsTp: r.transaction_type,
        trgPrc: r.trigger_price,
        brkName: r.broker_name ?? "KOTAK",
        brnchId: r.branch_id ?? "ONLINE",
        slAbsOrTks: r.stop_loss_type,
        slVal: r.stop_loss_value,
        sqrOffAbsOrTks: r.square_off_type,
        sqrOffVal: r.square_off_value,
        trailSL: r.trailing_stop_loss,
        tSLTks: r.trailing_sl_value,
      },
    });
    return { data: res.data };
  }
}
