package com.kotak.neo.client;

import java.util.Map;
import java.util.Set;

public final class Settings {
    public static final Map<String, String> EXCHANGE_SEGMENT = Map.ofEntries(
            Map.entry("nse_cm", "nse_cm"), Map.entry("NSE", "nse_cm"), Map.entry("nse", "nse_cm"),
            Map.entry("BSE", "bse_cm"), Map.entry("bse", "bse_cm"), Map.entry("bse_cm", "bse_cm"),
            Map.entry("NFO", "nse_fo"), Map.entry("nse_fo", "nse_fo"), Map.entry("nfo", "nse_fo"),
            Map.entry("BFO", "bse_fo"), Map.entry("bse_fo", "bse_fo"), Map.entry("bfo", "bse_fo"),
            Map.entry("CDS", "cde_fo"), Map.entry("cde_fo", "cde_fo"), Map.entry("cds", "cde_fo"),
            Map.entry("BCD", "bcs-fo"), Map.entry("bcs-fo", "bcs-fo"), Map.entry("bcd", "bcs-fo"),
            Map.entry("MCX", "mcx_fo"), Map.entry("mcx", "mcx_fo"), Map.entry("mcx_fo", "mcx_fo")
    );

    public static final Map<String, String> PRODUCT = Map.ofEntries(
            Map.entry("Normal", "NRML"), Map.entry("NRML", "NRML"),
            Map.entry("CNC", "CNC"), Map.entry("cnc", "CNC"), Map.entry("Cash and Carry", "CNC"),
            Map.entry("MIS", "MIS"), Map.entry("mis", "MIS"),
            Map.entry("INTRADAY", "INTRADAY"), Map.entry("intraday", "INTRADAY"),
            Map.entry("Cover Order", "CO"), Map.entry("co", "CO"), Map.entry("CO", "CO"),
            Map.entry("BO", "BO"), Map.entry("Bracket Order", "BO"), Map.entry("bo", "BO"),
            Map.entry("mtf", "MTF"), Map.entry("MTF", "MTF")
    );

    public static final Map<String, String> ORDER_TYPE = Map.ofEntries(
            Map.entry("Limit", "L"), Map.entry("L", "L"), Map.entry("l", "L"),
            Map.entry("MKT", "MKT"), Map.entry("mkt", "MKT"), Map.entry("Market", "MKT"),
            Map.entry("sl", "SL"), Map.entry("SL", "SL"), Map.entry("Stop loss limit", "SL"),
            Map.entry("Stop loss market", "SL-M"), Map.entry("SL-M", "SL-M"), Map.entry("sl-m", "SL-M"),
            Map.entry("Spread", "SP"), Map.entry("SP", "SP"), Map.entry("sp", "SP"),
            Map.entry("2L", "2L"), Map.entry("2l", "2L"), Map.entry("Two Leg", "2L"),
            Map.entry("3L", "3L"), Map.entry("3l", "3L"), Map.entry("Three leg", "3L")
    );

    public static final Set<String> SEGMENT_LIMITS = Set.of("CASH", "CUR", "FO", "ALL");
    public static final Set<String> EXCHANGE_LIMITS = Set.of("NSE", "BSE", "ALL");
    public static final Set<String> PRODUCT_LIMITS = Set.of("CNC", "MIS", "NRML", "ALL");

    public static final Map<String, String> REQ_TYPE_VALUES = Map.ofEntries(
            Map.entry("CONNECTION", "cn"),
            Map.entry("SCRIP_SUBS", "mws"),
            Map.entry("SCRIP_UNSUBS", "mwu"),
            Map.entry("INDEX_SUBS", "ifs"),
            Map.entry("INDEX_UNSUBS", "ifu"),
            Map.entry("DEPTH_SUBS", "dps"),
            Map.entry("DEPTH_UNSUBS", "dpu"),
            Map.entry("SNAP_MW", "mwsp"),
            Map.entry("SNAP_DP", "dpsp"),
            Map.entry("SNAP_IF", "ifsp")
    );

    public static final String ORDER_SOURCE = "NEOTRADEAPI";
    public static final int QUOTES_CHANNEL = 1;

    private Settings() {}
}
