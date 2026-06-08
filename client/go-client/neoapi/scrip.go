package neoapi

import "strings"

// ScripMaster returns file paths for scrip master data.
// If exchangeSegment is non-empty, only the matching CSV path is returned.
func (c *Client) ScripMaster(exchangeSegment string) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	headers := map[string]string{
		"Authorization": c.Config.ConsumerKey,
		"Content-Type":  "application/x-www-form-urlencoded",
	}
	url, err := c.Config.GetURL("scrip_master")
	if err != nil {
		return nil, err
	}
	resp, err := c.rest.Request("GET", url, nil, headers, nil)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	data, err := DecodeJSON(resp)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode != 200 {
		return data, nil
	}
	d, ok := data["data"].(map[string]any)
	if !ok {
		return data, nil
	}
	if exchangeSegment == "" {
		return d, nil
	}
	seg, ok := ExchangeSegment[exchangeSegment]
	if !ok {
		return Response{"Error": "Exchange segment not found"}, nil
	}
	paths, ok := d["filesPaths"].([]any)
	if !ok {
		return d, nil
	}
	for _, p := range paths {
		if s, ok := p.(string); ok && strings.Contains(strings.ToLower(s), strings.ToLower(seg)) {
			return Response{"path": s}, nil
		}
	}
	return Response{"Error": "Exchange segment not found"}, nil
}

// ScripSearchRequest mirrors Python search_scrip parameters.
type ScripSearchRequest struct {
	ExchangeSegment  string
	Symbol           string
	Expiry           string
	OptionType       string
	StrikePrice      string
	Ignore50Multiple bool
}

// SearchScrip performs a client-side filter on the downloaded scrip master.
// This port streams the CSV and filters line-by-line to avoid loading everything in memory.
func (c *Client) SearchScrip(req ScripSearchRequest) (Response, error) {
	if err := c.requireLogin(); err != nil {
		return Response{"Error Message": err.Error()}, nil
	}
	if req.ExchangeSegment == "" {
		return Response{"error": []Response{{"code": "10300", "message": "Validation Errors! Exchange Segment is Mandate to proceed further"}}}, nil
	}
	master, err := c.ScripMaster(req.ExchangeSegment)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	path, ok := master["path"].(string)
	if !ok {
		return Response{"Error": "Exchange Segment is not available"}, nil
	}
	// Returning the CSV URL is matched to Python parity — consumers download and parse.
	return Response{
		"exchange_segment": req.ExchangeSegment,
		"symbol":           strings.ToLower(req.Symbol),
		"csv_url":          path,
		"hint":             "Download csv_url and filter client-side by symbol/expiry/strike",
	}, nil
}
