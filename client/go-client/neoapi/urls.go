package neoapi

// WebSocket endpoints
const (
	WebSocketURL      = "wss://mlhsm.kotaksecurities.com"
	OrderFeedURL      = "wss://mis.kotaksecurities.com/realtime"
	OrderFeedURLADC   = "wss://cis.kotaksecurities.com/realtime"
	OrderFeedURLE21   = "wss://e21.kotaksecurities.com/realtime"
	OrderFeedURLE22   = "wss://e22.kotaksecurities.com/realtime"
	OrderFeedURLE41   = "wss://e41.kotaksecurities.com/realtime"
	OrderFeedURLE43   = "wss://e43.kotaksecurities.com/realtime"
	BaseURL           = "https://mis.kotaksecurities.com"
)

// REST base URLs
const (
	UATBaseURL            = "https://nsbxapi-gw.kotaksecurities.com/"
	ProdBaseURL           = "https://mnapi.kotaksecurities.com/"
	ProdBaseURLADC        = "https://cnapi.kotaksecurities.com/"
	ProdBaseURLNAPI       = "https://napi.kotaksecurities.com/"
	ProdBaseURLGWNAPI     = "https://gw-napi.kotaksecurities.com/"
	SessionUATBaseURL     = "https://nsbxapi.kotaksecurities.com/"
	SessionProdBaseURL    = "https://mnapi.kotaksecurities.com/"
	SessionProdBaseURLADC = "https://cnapi.kotaksecurities.com/"
)

// UATPaths — endpoint paths for UAT environment
var UATPaths = map[string]string{
	"totp_login":        "api/1.0/login/v6/totp/login",
	"totp_validate":     "api/1.0/login/v6/totp/validate",
	"place_order":       "orderapi/1.0/quick/order/rule/ms/place",
	"cancel_order":      "orderapi/1.0/quick/order/cancel",
	"modify_order":      "orderapi/1.0/quick/order/vr/modify",
	"order_history":     "orderapi/1.0/quick/order/history",
	"order_book":        "orderapi/1.0/quick/user/orders",
	"trade_report":      "orderapi/1.0/quick/user/trades",
	"positions":         "orderapi/1.0/quick/user/positions",
	"holdings":          "portfolio/1.0/portfolio/v1/holdings",
	"margin":            "orderapi/1.0/quick/user/check-margin",
	"scrip_master":      "scrip/1.0/masterscrip/file-paths",
	"limits":            "orderapi/1.0/quick/user/limits",
	"logout":            "api/1.0/logout",
	"quotes_neo_symbol": "script-details/1.0/quotes/neosymbol/{neo_symbols}/{quote_type}",
}

// ProdPaths — endpoint paths for PROD environment
var ProdPaths = map[string]string{
	"totp_login":           "login/1.0/tradeApiLogin",
	"totp_validate":        "login/1.0/tradeApiValidate",
	"place_order":          "quick/order/rule/ms/place",
	"cancel_order":         "quick/order/cancel",
	"cancel_cover_order":   "quick/order/co/exit",
	"cancel_bracket_order": "quick/order/bo/exit",
	"modify_order":         "quick/order/vr/modify",
	"order_history":        "quick/order/history",
	"order_book":           "quick/user/orders",
	"trade_report":         "quick/user/trades",
	"positions":            "quick/user/positions",
	"holdings":             "portfolio/v1/holdings",
	"margin":               "quick/user/check-margin",
	"scrip_master":         "script-details/1.0/masterscrip/file-paths",
	"limits":               "quick/user/limits",
	"logout":               "apim/login/2.0/logout",
	"quotes_neo_symbol":    "/script-details/1.0/quotes/neosymbol/{neo_symbols}/{quote_type}",
}
