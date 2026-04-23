import { ApiValueError, ApiKeyError } from "./exceptions";
import { PROD_BASE_URL, UAT_BASE_URL, BASE_URL, PROD_PATHS, UAT_PATHS } from "./urls";

export class Configuration {
  host: string;
  bearerToken?: string;
  viewToken?: string;
  sid?: string;
  userId?: string;
  editToken?: string;
  editSid?: string;
  editRid?: string;
  serverId?: string;
  neoFinKey?: string;
  dataCenter?: string;
  baseUrl?: string;
  totpSessionId?: string;
  consumerKey?: string;

  constructor(host: string = "uat") {
    this.host = host;
  }

  isLoggedIn(): boolean {
    return !!(this.editToken && this.editSid);
  }

  extractUserId(viewToken: string): string {
    if (!viewToken) throw new ApiValueError("view_token is empty — call totp_login first");
    const parts = viewToken.split(".");
    if (parts.length < 2) throw new ApiValueError("invalid JWT");
    const payload = Buffer.from(parts[1], "base64url").toString("utf8");
    const claims = JSON.parse(payload);
    if (!claims.sub) throw new ApiKeyError("sub claim missing from token");
    this.userId = claims.sub;
    return claims.sub;
  }

  getDomain(sessionInit: boolean = false): string {
    const host = this.host.trim().toLowerCase();
    if (host !== "prod" && host !== "uat") {
      throw new ApiValueError("environment must be 'prod' or 'uat'");
    }
    if (sessionInit) return BASE_URL;
    if (host === "prod") return this.baseUrl || PROD_BASE_URL;
    return UAT_BASE_URL;
  }

  getUrl(apiInfo: string): string {
    const host = this.host.trim().toLowerCase();
    const domain = this.getDomain(false).replace(/\/$/, "");
    const path = host === "prod" ? PROD_PATHS[apiInfo] : UAT_PATHS[apiInfo];
    if (!path) throw new ApiValueError(`unknown endpoint: ${apiInfo}`);
    return `${domain}/${path}`;
  }

  getNeoFinKey(): string {
    if (this.neoFinKey) return this.neoFinKey;
    return this.host.trim().toLowerCase() === "prod"
      ? "neotradeapi"
      : "bQJNkL5z8m4aGcRgjDvXhHfSx7VpZnE";
  }
}
