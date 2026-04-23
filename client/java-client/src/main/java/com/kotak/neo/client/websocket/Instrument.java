package com.kotak.neo.client.websocket;

public class Instrument {
    public String instrumentToken;
    public String exchangeSegment;

    public Instrument() {}

    public Instrument(String instrumentToken, String exchangeSegment) {
        this.instrumentToken = instrumentToken;
        this.exchangeSegment = exchangeSegment;
    }
}
