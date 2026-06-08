import { Configuration } from "../configuration";
import { RestClient, Response } from "../rest";
import { EXCHANGE_SEGMENT } from "../settings";

export class ScripApi {
  constructor(private rest: RestClient, private config: Configuration) {}

  async scripMaster(exchangeSegment?: string): Promise<Response> {
    const res = await this.rest.request({
      method: "GET",
      url: this.config.getUrl("scrip_master"),
      headers: {
        Authorization: this.config.consumerKey || "",
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
    if (res.status !== 200) return res.data;
    const data = res.data?.data;
    if (!data) return res.data;
    if (!exchangeSegment) return data;
    const seg = EXCHANGE_SEGMENT[exchangeSegment];
    if (!seg) return { Error: "Exchange segment not found" };
    const match = (data.filesPaths as string[] | undefined)?.find((p) =>
      p.toLowerCase().includes(seg.toLowerCase()),
    );
    if (!match) return { Error: "Exchange segment not found" };
    return { path: match };
  }

  async searchScrip(
    exchangeSegment: string,
    symbol = "",
    expiry?: string,
    optionType?: string,
    strikePrice?: string,
  ): Promise<Response> {
    if (!exchangeSegment) {
      return {
        error: [
          {
            code: "10300",
            message: "Validation Errors! Exchange Segment is Mandate to proceed further",
          },
        ],
      };
    }
    const master = await this.scripMaster(exchangeSegment);
    if (!master.path) return { Error: "Exchange Segment is not available" };
    return {
      exchange_segment: exchangeSegment,
      symbol: symbol.toLowerCase(),
      expiry,
      option_type: optionType,
      strike_price: strikePrice,
      csv_url: master.path,
      hint: "Download csv_url and filter client-side by symbol/expiry/strike",
    };
  }
}
