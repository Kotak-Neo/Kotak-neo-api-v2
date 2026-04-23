import { Configuration } from "../configuration";
import { RestClient, Response } from "../rest";
import { QuoteInstrument } from "../types";

export class QuotesApi {
  constructor(private rest: RestClient, private config: Configuration) {}

  async quotes(instruments: QuoteInstrument[], quoteType?: string): Promise<Response> {
    if (!instruments || instruments.length === 0) {
      return { error: [{ message: "Validation Errors! instrument_tokens are missing" }] };
    }
    const qt = quoteType || "all";
    const parts = instruments
      .map((i) => `${i.exchange_segment}|${i.instrument_token}`)
      .join(",");
    const raw = this.config.getUrl("quotes_neo_symbol");
    const url = raw
      .replace("{neo_symbols}", encodeURIComponent(parts))
      .replace("{quote_type}", qt);
    const res = await this.rest.request({
      method: "GET",
      url,
      headers: {
        Authorization: this.config.consumerKey || "",
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
    return res.data;
  }
}
