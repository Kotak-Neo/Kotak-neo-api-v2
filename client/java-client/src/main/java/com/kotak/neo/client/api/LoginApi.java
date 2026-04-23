package com.kotak.neo.client.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kotak.neo.client.Configuration;
import com.kotak.neo.client.Urls;
import com.kotak.neo.client.rest.RestClient;

import java.util.HashMap;
import java.util.Map;

public class LoginApi {
    private final RestClient rest;
    private final Configuration config;

    public LoginApi(RestClient rest, Configuration config) {
        this.rest = rest;
        this.config = config;
    }

    public JsonElement totpLogin(String mobileNumber, String ucc, String totp) {
        if (mobileNumber == null || ucc == null || totp == null
                || mobileNumber.isEmpty() || ucc.isEmpty() || totp.isEmpty()) {
            JsonObject error = new JsonObject();
            JsonObject msg = new JsonObject();
            msg.addProperty("message", "mobile_number, ucc or totp missing");
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            arr.add(msg);
            error.add("error", arr);
            return error;
        }

        String host = config.host.trim().toLowerCase();
        String path = host.equals("prod") ? Urls.PROD_PATHS.get("totp_login") : Urls.UAT_PATHS.get("totp_login");
        String url = Urls.BASE_URL.replaceAll("/$", "") + "/" + path;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", config.consumerKey == null ? "" : config.consumerKey);
        headers.put("neo-fin-key", config.getNeoFinKey());
        headers.put("Content-Type", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("mobileNumber", mobileNumber);
        body.put("ucc", ucc);
        body.put("totp", totp);

        RestClient.Response r = rest.request("POST", url, null, headers, body);
        if (r.status >= 200 && r.status <= 299 && r.data.isJsonObject()) {
            JsonObject obj = r.data.getAsJsonObject();
            if (obj.has("data") && obj.get("data").isJsonObject()) {
                JsonObject data = obj.getAsJsonObject("data");
                if (data.has("token")) config.viewToken = data.get("token").getAsString();
                if (data.has("sid")) config.sid = data.get("sid").getAsString();
            }
        }
        return r.data;
    }

    public JsonElement totpValidate(String mpin) {
        if (mpin == null || mpin.isEmpty()) {
            JsonObject error = new JsonObject();
            JsonObject msg = new JsonObject();
            msg.addProperty("message", "Mpin is missing");
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            arr.add(msg);
            error.add("error", arr);
            return error;
        }

        String host = config.host.trim().toLowerCase();
        String path = host.equals("prod") ? Urls.PROD_PATHS.get("totp_validate") : Urls.UAT_PATHS.get("totp_validate");
        String url = Urls.BASE_URL.replaceAll("/$", "") + "/" + path;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", config.consumerKey == null ? "" : config.consumerKey);
        headers.put("sid", config.sid == null ? "" : config.sid);
        headers.put("Auth", config.viewToken == null ? "" : config.viewToken);
        headers.put("neo-fin-key", config.getNeoFinKey());
        headers.put("Content-Type", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("mpin", mpin);

        RestClient.Response r = rest.request("POST", url, null, headers, body);
        if (r.status >= 200 && r.status <= 299 && r.data.isJsonObject()) {
            JsonObject obj = r.data.getAsJsonObject();
            if (obj.has("data") && obj.get("data").isJsonObject()) {
                JsonObject data = obj.getAsJsonObject("data");
                if (data.has("token")) config.editToken = data.get("token").getAsString();
                if (data.has("sid")) config.editSid = data.get("sid").getAsString();
                if (data.has("rid")) config.editRid = data.get("rid").getAsString();
                if (data.has("hsServerId")) config.serverId = data.get("hsServerId").getAsString();
                if (data.has("dataCenter")) config.dataCenter = data.get("dataCenter").getAsString();
                if (data.has("baseUrl")) config.baseUrl = data.get("baseUrl").getAsString();
            }
        }
        return r.data;
    }
}
