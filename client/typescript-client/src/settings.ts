export const EXCHANGE_SEGMENT: Record<string, string> = {
  nse_cm: "nse_cm", NSE: "nse_cm", nse: "nse_cm",
  BSE: "bse_cm", bse: "bse_cm", bse_cm: "bse_cm",
  NFO: "nse_fo", nse_fo: "nse_fo", nfo: "nse_fo",
  BFO: "bse_fo", bse_fo: "bse_fo", bfo: "bse_fo",
  CDS: "cde_fo", cde_fo: "cde_fo", cds: "cde_fo",
  BCD: "bcs-fo", "bcs-fo": "bcs-fo", bcd: "bcs-fo",
  MCX: "mcx_fo", mcx: "mcx_fo", mcx_fo: "mcx_fo",
};

export const PRODUCT: Record<string, string> = {
  Normal: "NRML", NRML: "NRML",
  CNC: "CNC", cnc: "CNC", "Cash and Carry": "CNC",
  MIS: "MIS", mis: "MIS",
  INTRADAY: "INTRADAY", intraday: "INTRADAY",
  "Cover Order": "CO", co: "CO", CO: "CO",
  BO: "BO", "Bracket Order": "BO", bo: "BO",
  mtf: "MTF", MTF: "MTF",
};

export const ORDER_TYPE: Record<string, string> = {
  Limit: "L", L: "L", l: "L",
  MKT: "MKT", mkt: "MKT", Market: "MKT",
  sl: "SL", SL: "SL", "Stop loss limit": "SL",
  "Stop loss market": "SL-M", "SL-M": "SL-M", "sl-m": "SL-M",
  Spread: "SP", SP: "SP", sp: "SP",
  "2L": "2L", "2l": "2L", "Two Leg": "2L",
  "3L": "3L", "3l": "3L", "Three leg": "3L",
};

export const SEGMENT_LIMITS = ["CASH", "CUR", "FO", "ALL"];
export const EXCHANGE_LIMITS = ["NSE", "BSE", "ALL"];
export const PRODUCT_LIMITS = ["CNC", "MIS", "NRML", "ALL"];

export const REQ_TYPE_VALUES: Record<string, string> = {
  CONNECTION: "cn",
  SCRIP_SUBS: "mws",
  SCRIP_UNSUBS: "mwu",
  INDEX_SUBS: "ifs",
  INDEX_UNSUBS: "ifu",
  DEPTH_SUBS: "dps",
  DEPTH_UNSUBS: "dpu",
  CHANNEL_RESUME: "cr",
  CHANNEL_PAUSE: "cp",
  SNAP_MW: "mwsp",
  SNAP_DP: "dpsp",
  SNAP_IF: "ifsp",
  OPC_SUBS: "opc",
  THROTTLING_INTERVAL: "ti",
  STR: "str",
  FORCE_CONNECTION: "fcn",
};

export const STOCK_KEY_MAPPING: Record<string, string> = {
  ltt: "last_traded_time", v: "volume", ltp: "last_traded_price",
  ltq: "last_traded_quantity", tbq: "total_buy_quantity",
  tsq: "total_sell_quantity", bp: "buy_price", sp: "sell_price",
  bq: "buy_quantity", sq: "sell_quantity", ap: "average_price",
  oi: "open_interest", lo: "low", h: "high",
  lcl: "lower_circuit_limit", ucl: "upper_circuit_limit",
  yh: "52week_high", yl: "52week_low", op: "open", c: "close",
  mul: "multiplier", prec: "precision", cng: "change",
  nc: "net_change_percentage", to: "total_traded_value",
  tk: "instrument_token", e: "exchange_segment", ts: "trading_symbol",
  request_type: "request_type",
};

export const INDEX_KEY_MAPPING: Record<string, string> = {
  iv: "last_traded_price", ic: "prev_day_close",
  tvalue: "timestamp", highPrice: "high_price",
  lowPrice: "low_price", openingPrice: "open",
  mul: "multiplier", prec: "precision",
  cng: "change", nc: "net_change_percentage",
  tk: "instrument_token", e: "exchange_segment",
};

export const ORDER_SOURCE = "NEOTRADEAPI";
export const QUOTES_CHANNEL = 1;
