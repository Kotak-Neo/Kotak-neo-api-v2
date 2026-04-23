import { Configuration } from "../configuration";
import { RestClient, Response } from "../rest";
import { validateLimits } from "../validation";

export class PortfolioApi {
  constructor(private rest: RestClient, private config: Configuration) {}

  private authHeaders(contentType: string): Record<string, string> {
    return {
      Sid: this.config.editSid || "",
      Auth: this.config.editToken || "",
      "Content-Type": contentType,
    };
  }
  private serverId(): Record<string, string> {
    return { sId: this.config.serverId || "" };
  }

  async positions(): Promise<Response> {
    const res = await this.rest.request({
      method: "GET",
      url: this.config.getUrl("positions"),
      queryParams: this.serverId(),
      headers: { ...this.authHeaders("application/x-www-form-urlencoded"), accept: "application/json" },
    });
    return res.data;
  }

  async holdings(): Promise<Response> {
    const res = await this.rest.request({
      method: "GET",
      url: this.config.getUrl("holdings"),
      queryParams: this.serverId(),
      headers: { ...this.authHeaders("application/x-www-form-urlencoded"), accept: "*/*" },
    });
    return res.data;
  }

  async limits(segment = "ALL", exchange = "ALL", product = "ALL"): Promise<Response> {
    validateLimits(segment, exchange, product);
    const res = await this.rest.request({
      method: "POST",
      url: this.config.getUrl("limits"),
      queryParams: this.serverId(),
      headers: this.authHeaders("application/x-www-form-urlencoded"),
      body: { seg: segment, exch: exchange, prod: product },
    });
    return res.data;
  }
}
