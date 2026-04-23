namespace Kotak.Neo;

public static class Settings
{
    public static readonly Dictionary<string, string> ExchangeSegment = new()
    {
        ["nse_cm"] = "nse_cm", ["NSE"] = "nse_cm", ["nse"] = "nse_cm",
        ["BSE"] = "bse_cm", ["bse"] = "bse_cm", ["bse_cm"] = "bse_cm",
        ["NFO"] = "nse_fo", ["nse_fo"] = "nse_fo", ["nfo"] = "nse_fo",
        ["BFO"] = "bse_fo", ["bse_fo"] = "bse_fo", ["bfo"] = "bse_fo",
        ["CDS"] = "cde_fo", ["cde_fo"] = "cde_fo", ["cds"] = "cde_fo",
        ["BCD"] = "bcs-fo", ["bcs-fo"] = "bcs-fo", ["bcd"] = "bcs-fo",
        ["MCX"] = "mcx_fo", ["mcx"] = "mcx_fo", ["mcx_fo"] = "mcx_fo",
    };

    public static readonly Dictionary<string, string> Product = new()
    {
        ["Normal"] = "NRML", ["NRML"] = "NRML",
        ["CNC"] = "CNC", ["cnc"] = "CNC", ["Cash and Carry"] = "CNC",
        ["MIS"] = "MIS", ["mis"] = "MIS",
        ["INTRADAY"] = "INTRADAY", ["intraday"] = "INTRADAY",
        ["Cover Order"] = "CO", ["co"] = "CO", ["CO"] = "CO",
        ["BO"] = "BO", ["Bracket Order"] = "BO", ["bo"] = "BO",
        ["mtf"] = "MTF", ["MTF"] = "MTF",
    };

    public static readonly Dictionary<string, string> OrderType = new()
    {
        ["Limit"] = "L", ["L"] = "L", ["l"] = "L",
        ["MKT"] = "MKT", ["mkt"] = "MKT", ["Market"] = "MKT",
        ["sl"] = "SL", ["SL"] = "SL", ["Stop loss limit"] = "SL",
        ["Stop loss market"] = "SL-M", ["SL-M"] = "SL-M", ["sl-m"] = "SL-M",
        ["Spread"] = "SP", ["SP"] = "SP", ["sp"] = "SP",
        ["2L"] = "2L", ["2l"] = "2L", ["Two Leg"] = "2L",
        ["3L"] = "3L", ["3l"] = "3L", ["Three leg"] = "3L",
    };

    public static readonly HashSet<string> SegmentLimits = new() { "CASH", "CUR", "FO", "ALL" };
    public static readonly HashSet<string> ExchangeLimits = new() { "NSE", "BSE", "ALL" };
    public static readonly HashSet<string> ProductLimits = new() { "CNC", "MIS", "NRML", "ALL" };

    public static readonly Dictionary<string, string> ReqTypeValues = new()
    {
        ["CONNECTION"] = "cn",
        ["SCRIP_SUBS"] = "mws",
        ["SCRIP_UNSUBS"] = "mwu",
        ["INDEX_SUBS"] = "ifs",
        ["INDEX_UNSUBS"] = "ifu",
        ["DEPTH_SUBS"] = "dps",
        ["DEPTH_UNSUBS"] = "dpu",
        ["SNAP_MW"] = "mwsp",
        ["SNAP_DP"] = "dpsp",
        ["SNAP_IF"] = "ifsp",
    };

    public const string OrderSource = "NEOTRADEAPI";
    public const int QuotesChannel = 1;
}
