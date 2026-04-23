package com.kotak.neo.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kotak.neo.client.exceptions.ApiKeyError;
import com.kotak.neo.client.exceptions.ApiValueError;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Configuration {
    public String host;
    public String bearerToken;
    public String viewToken;
    public String sid;
    public String userId;
    public String editToken;
    public String editSid;
    public String editRid;
    public String serverId;
    public String neoFinKey;
    public String dataCenter;
    public String baseUrl;
    public String consumerKey;

    public Configuration(String environment) {
        this.host = environment == null ? "uat" : environment;
    }

    public boolean isLoggedIn() {
        return editToken != null && !editToken.isEmpty() && editSid != null && !editSid.isEmpty();
    }

    public String getDomain(boolean sessionInit) {
        String h = host.trim().toLowerCase();
        if (!h.equals("prod") && !h.equals("uat")) {
            throw new ApiValueError("environment must be 'prod' or 'uat'");
        }
        if (sessionInit) return Urls.BASE_URL;
        if (h.equals("prod")) return (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl : Urls.PROD_BASE_URL;
        return Urls.UAT_BASE_URL;
    }

    public String getUrl(String apiInfo) {
        String h = host.trim().toLowerCase();
        String domain = getDomain(false);
        if (domain.endsWith("/")) domain = domain.substring(0, domain.length() - 1);
        String path = h.equals("prod") ? Urls.PROD_PATHS.get(apiInfo) : Urls.UAT_PATHS.get(apiInfo);
        if (path == null) throw new ApiValueError("unknown endpoint: " + apiInfo);
        return domain + "/" + path;
    }

    public String getNeoFinKey() {
        if (neoFinKey != null && !neoFinKey.isEmpty()) return neoFinKey;
        return host.trim().equalsIgnoreCase("prod")
                ? "neotradeapi"
                : "bQJNkL5z8m4aGcRgjDvXhHfSx7VpZnE";
    }

    public String extractUserId(String viewToken) {
        if (viewToken == null || viewToken.isEmpty()) {
            throw new ApiValueError("view_token is empty — call totpLogin first");
        }
        String[] parts = viewToken.split("\\.");
        if (parts.length < 2) throw new ApiValueError("invalid JWT");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        JsonObject claims = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
        if (!claims.has("sub")) throw new ApiKeyError("sub claim missing from token");
        this.userId = claims.get("sub").getAsString();
        return this.userId;
    }
}
