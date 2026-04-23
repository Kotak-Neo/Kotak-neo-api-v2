package neoapi

// Positions returns the current positions.
func (c *Client) Positions() (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	headers := c.authHeaders("application/x-www-form-urlencoded")
	headers["accept"] = "application/json"
	url, err := c.Config.GetURL("positions")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("GET", url, c.queryWithServerID(), headers, nil)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}

// Holdings returns portfolio holdings.
func (c *Client) Holdings() (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	headers := c.authHeaders("application/x-www-form-urlencoded")
	headers["accept"] = "*/*"
	url, err := c.Config.GetURL("holdings")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("GET", url, c.queryWithServerID(), headers, nil)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}

// Limits returns the trading limits. Defaults match the Python SDK ("ALL" for all three).
func (c *Client) Limits(segment, exchange, product string) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	if segment == "" {
		segment = "ALL"
	}
	if exchange == "" {
		exchange = "ALL"
	}
	if product == "" {
		product = "ALL"
	}
	if err := ValidateLimits(segment, exchange, product); err != nil {
		return Response{"Error": err.Error()}, nil
	}
	body := map[string]any{"seg": segment, "exch": exchange, "prod": product}
	url, err := c.Config.GetURL("limits")
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
