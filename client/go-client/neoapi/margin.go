package neoapi

// MarginRequiredRequest mirrors the Python margin_required parameters.
type MarginRequiredRequest struct {
	ExchangeSegment  string
	Price            string
	OrderType        string
	Product          string
	Quantity         string
	InstrumentToken  string
	TransactionType  string
	TriggerPrice     string
	BrokerName       string // default "KOTAK"
	BranchID         string // default "ONLINE"
	StopLossType     string
	StopLossValue    string
	SquareOffType    string
	SquareOffValue   string
	TrailingStopLoss string
	TrailingSLValue  string
}

// MarginRequired calculates the margin needed for a trade.
func (c *Client) MarginRequired(req MarginRequiredRequest) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	if err := ValidateMargin(req.ExchangeSegment, req.Price, req.OrderType, req.Product,
		req.Quantity, req.InstrumentToken, req.TransactionType); err != nil {
		return Response{"Error": err.Error()}, nil
	}
	if req.BrokerName == "" {
		req.BrokerName = "KOTAK"
	}
	if req.BranchID == "" {
		req.BranchID = "ONLINE"
	}

	body := map[string]any{
		"exSeg":          ExchangeSegment[req.ExchangeSegment],
		"prc":            req.Price,
		"prcTp":          OrderType[req.OrderType],
		"prod":           Product[req.Product],
		"qty":            req.Quantity,
		"tok":            req.InstrumentToken,
		"trnsTp":         req.TransactionType,
		"trgPrc":         req.TriggerPrice,
		"brkName":        req.BrokerName,
		"brnchId":        req.BranchID,
		"slAbsOrTks":     req.StopLossType,
		"slVal":          req.StopLossValue,
		"sqrOffAbsOrTks": req.SquareOffType,
		"sqrOffVal":      req.SquareOffValue,
		"trailSL":        req.TrailingStopLoss,
		"tSLTks":         req.TrailingSLValue,
	}
	url, err := c.Config.GetURL("margin")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("POST", url, c.queryWithServerID(),
		c.authHeaders("application/x-www-form-urlencoded"), body)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	data, err := DecodeJSON(resp)
	if err != nil {
		return nil, err
	}
	return Response{"data": data}, nil
}
