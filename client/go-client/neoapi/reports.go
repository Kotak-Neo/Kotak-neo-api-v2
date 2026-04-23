package neoapi

// OrderReport fetches the order book.
func (c *Client) OrderReport() (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	headers := c.authHeaders("application/x-www-form-urlencoded")
	headers["accept"] = "application/json"
	url, err := c.Config.GetURL("order_book")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("GET", url, c.queryWithServerID(), headers, nil)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}

// OrderHistory returns the status history for a specific order.
func (c *Client) OrderHistory(orderID string) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	if err := ValidateOrderHistory(orderID); err != nil {
		return Response{"Error": err.Error()}, nil
	}
	body := map[string]any{"nOrdNo": orderID}
	url, err := c.Config.GetURL("order_history")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("POST", url, c.queryWithServerID(),
		c.authHeaders("application/x-www-form-urlencoded"), body)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}

// TradeReport returns trades. If orderID is non-empty, results are filtered to that order.
func (c *Client) TradeReport(orderID string) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	headers := c.authHeaders("application/x-www-form-urlencoded")
	headers["accept"] = "application/json"
	url, err := c.Config.GetURL("trade_report")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("GET", url, c.queryWithServerID(), headers, nil)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	data, err := DecodeJSON(resp)
	if err != nil {
		return nil, err
	}
	if orderID == "" {
		return data, nil
	}
	rows, ok := data["data"].([]any)
	if !ok {
		return Response{"Error": "There is no trades available with the given order id"}, nil
	}
	for _, r := range rows {
		if row, ok := r.(map[string]any); ok && row["nOrdNo"] == orderID {
			return Response{"stat": data["stat"], "stCode": data["stCode"], "data": row}, nil
		}
	}
	return Response{"Error": "There is no trades available with the given order id"}, nil
}
