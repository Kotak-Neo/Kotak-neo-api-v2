import { Configuration } from "./configuration";
import { ApiException } from "./exceptions";

export type Response = Record<string, any>;

export interface RestRequestOptions {
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  url: string;
  queryParams?: Record<string, string>;
  headers?: Record<string, string>;
  body?: unknown;
}

export class RestClient {
  constructor(public config: Configuration) {}

  async request(opts: RestRequestOptions): Promise<{ status: number; data: any; text: string }> {
    const headers: Record<string, string> = { ...(opts.headers || {}) };
    if (!headers["Content-Type"]) headers["Content-Type"] = "application/json";
    headers["User-Agent"] = headers["User-Agent"] || "NeoTradeApi-ts/1.0.0";

    let url = opts.url;
    if (opts.queryParams && Object.keys(opts.queryParams).length > 0) {
      const params = new URLSearchParams(opts.queryParams).toString();
      url += (url.includes("?") ? "&" : "?") + params;
    }

    let body: string | undefined;
    if (["POST", "PUT", "PATCH", "DELETE"].includes(opts.method)) {
      const ct = headers["Content-Type"].toLowerCase();
      if (ct.includes("json")) {
        body = opts.body !== undefined ? JSON.stringify(opts.body) : undefined;
      } else if (ct.includes("x-www-form-urlencoded")) {
        const form = new URLSearchParams();
        if (opts.body !== undefined) form.set("jData", JSON.stringify(opts.body));
        body = form.toString();
      } else {
        throw new ApiException(0, "Invalid Content-Type", ct);
      }
    }

    const res = await fetch(url, { method: opts.method, headers, body });
    const text = await res.text();
    let data: any;
    try {
      data = text ? JSON.parse(text) : {};
    } catch {
      data = { raw: text };
    }
    return { status: res.status, data, text };
  }
}
