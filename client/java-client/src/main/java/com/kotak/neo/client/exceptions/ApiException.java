package com.kotak.neo.client.exceptions;

public class ApiException extends OpenApiException {
    public final int status;
    public final String reason;
    public final String body;

    public ApiException(int status, String reason, String body) {
        super(String.format("(%d) %s%s", status, reason, body == null ? "" : ": " + body));
        this.status = status;
        this.reason = reason;
        this.body = body;
    }
}
