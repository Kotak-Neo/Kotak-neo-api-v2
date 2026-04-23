import { Configuration } from "../configuration";
import { RestClient, Response } from "../rest";
import { BASE_URL, PROD_PATHS, UAT_PATHS } from "../urls";

export class LoginApi {
  constructor(private rest: RestClient, private config: Configuration) {}

  async totpLogin(mobileNumber: string, ucc: string, totp: string): Promise<Response> {
    if (!mobileNumber || !ucc || !totp) {
      return { error: [{ message: "mobile_number, ucc or totp missing" }] };
    }
    const host = this.config.host.trim().toLowerCase();
    const path = host === "prod" ? PROD_PATHS.totp_login : UAT_PATHS.totp_login;
    const url = `${BASE_URL.replace(/\/$/, "")}/${path}`;
    const res = await this.rest.request({
      method: "POST",
      url,
      headers: {
        Authorization: this.config.consumerKey || "",
        "neo-fin-key": this.config.getNeoFinKey(),
        "Content-Type": "application/json",
      },
      body: { mobileNumber, ucc, totp },
    });
    if (res.status >= 200 && res.status <= 299) {
      const data = res.data?.data;
      if (data) {
        this.config.viewToken = data.token;
        this.config.sid = data.sid;
      }
    }
    return res.data;
  }

  async totpValidate(mpin: string): Promise<Response> {
    if (!mpin) return { error: [{ message: "Mpin is missing" }] };
    const host = this.config.host.trim().toLowerCase();
    const path = host === "prod" ? PROD_PATHS.totp_validate : UAT_PATHS.totp_validate;
    const url = `${BASE_URL.replace(/\/$/, "")}/${path}`;
    const res = await this.rest.request({
      method: "POST",
      url,
      headers: {
        Authorization: this.config.consumerKey || "",
        sid: this.config.sid || "",
        Auth: this.config.viewToken || "",
        "neo-fin-key": this.config.getNeoFinKey(),
        "Content-Type": "application/json",
      },
      body: { mpin },
    });
    if (res.status >= 200 && res.status <= 299) {
      const data = res.data?.data;
      if (data) {
        this.config.editToken = data.token;
        this.config.editSid = data.sid;
        this.config.editRid = data.rid;
        this.config.serverId = data.hsServerId;
        this.config.dataCenter = data.dataCenter;
        this.config.baseUrl = data.baseUrl;
      }
    }
    return res.data;
  }
}
