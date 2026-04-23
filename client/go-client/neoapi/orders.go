package neoapi

// PlaceOrderRequest mirrors the Python place_order parameter list.
// Optional fields can be left empty.
type PlaceOrderRequest struct {
	ExchangeSegment   string
	Product           string
	Price             string
	OrderType         string
	Quantity          string
	Validity          string
	TradingSymbol     string
	TransactionType   string
	AMO               string // default "NO"
	DisclosedQuantity string // default "0"
	MarketProtection  string // default "0"
	PF                string // default "N"
	TriggerPrice      string // default "0"
	Tag               string
	ScripToken        string
	SquareOffType     string
	StopLossType      string
	StopLossValue     string
	SquareOffValue    string
	LastTradedPrice   string
	TrailingStopLoss  string
	TrailingSLValue   string
}

func (r *PlaceOrderRequest) applyDefaults() {
	if r.AMO == "" {
		r.AMO = "NO"
	}
	if r.DisclosedQuantity == "" {
		r.DisclosedQuantity = "0"
	}
	if r.MarketProtection == "" {
		r.MarketProtection = "0"
	}
	if r.PF == "" {
		r.PF = "N"
	}
	if r.TriggerPrice == "" {
		r.TriggerPrice = "0"
	}
}

// PlaceOrder places a regular/cover/bracket order.
func (c *Client) PlaceOrder(req PlaceOrderRequest) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	if err := ValidatePlaceOrder(req.ExchangeSegment, req.Product, req.Price, req.OrderType,
		req.Quantity, req.Validity, req.TradingSymbol, req.TransactionType); err != nil {
		return Response{"Error": err.Error()}, nil
	}
	req.applyDefaults()

	body := map[string]any{
		"am":  req.AMO,
		"dq":  req.DisclosedQuantity,
		"es":  ExchangeSegment[req.ExchangeSegment],
		"mp":  req.MarketProtection,
		"pc":  Product[req.Product],
		"pf":  req.PF,
		"pr":  req.Price,
		"pt":  OrderType[req.OrderType],
		"qt":  req.Quantity,
		"rt":  req.Validity,
		"tp":  req.TriggerPrice,
		"ts":  req.TradingSymbol,
		"tt":  req.TransactionType,
		"ig":  req.Tag,
		"tk":  req.ScripToken,
		"sot": req.SquareOffType,
		"slt": req.StopLossType,
		"slv": req.StopLossValue,
		"sov": req.SquareOffValue,
		"lat": req.LastTradedPrice,
		"tlt": req.TrailingStopLoss,
		"tsv": req.TrailingSLValue,
		"os":  OrderSource,
	}

	url, err := c.Config.GetURL("place_order")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("POST", url, c.queryWithServerID(), c.authHeaders("application/x-www-form-urlencoded"), body)
	if err != nil {
		return Response{"error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}

// CancelOrder cancels a regular order. If verify is true, the order book is checked first.
func (c *Client) CancelOrder(orderID, amo string, verify bool) (Response, error) {
	return c.cancelEndpoint("cancel_order", orderID, amo, verify)
}

// CancelCoverOrder cancels a cover order.
func (c *Client) CancelCoverOrder(orderID, amo string, verify bool) (Response, error) {
	return c.cancelEndpoint("cancel_cover_order", orderID, amo, verify)
}

// CancelBracketOrder cancels a bracket order.
func (c *Client) CancelBracketOrder(orderID, amo string, verify bool) (Response, error) {
	return c.cancelEndpoint("cancel_bracket_order", orderID, amo, verify)
}

func (c *Client) cancelEndpoint(endpoint, orderID, amo string, verify bool) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	if err := ValidateCancelOrder(orderID); err != nil {
		return Response{"Error": err.Error()}, nil
	}
	if verify {
		book, _ := c.OrderReport()
		if items, ok := book["data"].([]any); ok {
			for _, item := range items {
				row, ok := item.(map[string]any)
				if !ok {
					continue
				}
				if row["nOrdNo"] == orderID {
					if st, ok := row["ordSt"].(string); ok {
						if st == "rejected" || st == "cancelled" || st == "complete" || st == "traded" {
							if st == "complete" {
								st = "Traded"
							}
							return Response{"Error": "The Given Order Status is " + st, "Reason": row["rejRsn"]}, nil
						}
					}
				}
			}
		}
	}
	body := map[string]any{"on": orderID, "am": amo}
	url, err := c.Config.GetURL(endpoint)
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("POST", url, c.queryWithServerID(), c.authHeaders("application/x-www-form-urlencoded"), body)
	if err != nil {
		return Response{"error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}

// ModifyOrderRequest mirrors Python modify_order arguments.
type ModifyOrderRequest struct {
	OrderID           string
	Price             string
	OrderType         string
	Quantity          string
	Validity          string
	InstrumentToken   string
	ExchangeSegment   string
	Product           string
	TradingSymbol     string
	TransactionType   string
	TriggerPrice      string // default "0"
	DD                string // default "NA"
	MarketProtection  string // default "0"
	DisclosedQuantity string // default "0"
	FilledQuantity    string // default "0"
	AMO               string // default "NO"
}

func (r *ModifyOrderRequest) applyDefaults() {
	if r.TriggerPrice == "" {
		r.TriggerPrice = "0"
	}
	if r.DD == "" {
		r.DD = "NA"
	}
	if r.MarketProtection == "" {
		r.MarketProtection = "0"
	}
	if r.DisclosedQuantity == "" {
		r.DisclosedQuantity = "0"
	}
	if r.FilledQuantity == "" {
		r.FilledQuantity = "0"
	}
	if r.AMO == "" {
		r.AMO = "NO"
	}
}

// ModifyOrder modifies a pending order. If only OrderID is supplied, other fields are hydrated from the order book.
func (c *Client) ModifyOrder(req ModifyOrderRequest) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	if req.OrderID == "" {
		return nil, &APIValueError{Msg: "order_id is mandatory"}
	}
	req.applyDefaults()

	if req.InstrumentToken != "" && req.ExchangeSegment != "" && req.TradingSymbol != "" && req.Product != "" {
		req.ExchangeSegment = ExchangeSegment[req.ExchangeSegment]
		req.Product = Product[req.Product]
		req.OrderType = OrderType[req.OrderType]
		return c.sendModify(req)
	}

	// Hydrate from order book.
	book, err := c.OrderReport()
	if err != nil {
		return Response{"Message": err.Error()}, nil
	}
	items, ok := book["data"].([]any)
	if !ok {
		return Response{"Message": "There is no Data in the Order Book"}, nil
	}
	for _, item := range items {
		row, ok := item.(map[string]any)
		if !ok {
			continue
		}
		if row["nOrdNo"] != req.OrderID {
			continue
		}
		if st, ok := row["ordSt"].(string); ok {
			if st == "rejected" || st == "cancelled" || st == "complete" || st == "traded" {
				if st == "complete" {
					st = "Traded"
				}
				return Response{
					"Error":  "The Given Order Status is " + st + ", So we can't proceed further",
					"Reason": row["rejRsn"],
				}, nil
			}
		}
		if req.TradingSymbol == "" {
			if v, ok := row["trdSym"].(string); ok {
				req.TradingSymbol = v
			}
		}
		if req.InstrumentToken == "" {
			if v, ok := row["tok"].(string); ok {
				req.InstrumentToken = v
			}
		}
		if req.Product == "" {
			if v, ok := row["prod"].(string); ok {
				req.Product = v
			}
		}
		if req.TransactionType == "" {
			if v, ok := row["trnsTp"].(string); ok {
				req.TransactionType = v
			}
		}
		if req.ExchangeSegment == "" {
			if v, ok := row["exSeg"].(string); ok {
				req.ExchangeSegment = v
			}
		}
		if req.TriggerPrice == "0" {
			if v, ok := row["trgPrc"].(string); ok {
				req.TriggerPrice = v
			}
		}
		return c.sendModify(req)
	}
	return Response{"Message": "The Given Order Number " + req.OrderID + " is not matching with any of the orders"}, nil
}

func (c *Client) sendModify(req ModifyOrderRequest) (Response, error) {
	body := map[string]any{
		"tk": req.InstrumentToken, "mp": req.MarketProtection, "pc": req.Product,
		"dd": req.DD, "dq": req.DisclosedQuantity, "vd": req.Validity,
		"ts": req.TradingSymbol, "tt": req.TransactionType, "pr": req.Price,
		"pt": req.OrderType, "fq": req.FilledQuantity, "am": req.AMO,
		"tp": req.TriggerPrice, "qt": req.Quantity, "no": req.OrderID,
		"es": req.ExchangeSegment, "os": OrderSource,
	}
	url, err := c.Config.GetURL("modify_order")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("POST", url, c.queryWithServerID(), c.authHeaders("application/x-www-form-urlencoded"), body)
	if err != nil {
		return Response{"error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}
