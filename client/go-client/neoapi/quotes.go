package neoapi

import (
	"net/url"
	"strings"
)

// QuoteInstrument identifies a single instrument for a quotes lookup.
type QuoteInstrument struct {
	InstrumentToken string
	ExchangeSegment string
}

// Quotes fetches snapshot quotes for the given instruments. No login required.
// quoteType is one of: "ltp", "ohlc", "52w", "circuit_limits", "market_depth", "scrip_details", "all" (default).
func (c *Client) Quotes(instruments []QuoteInstrument, quoteType string) (Response, error) {
	if len(instruments) == 0 {
		return Response{"error": []Response{{"message": "Validation Errors! instrument_tokens are missing"}}}, nil
	}
	if quoteType == "" {
		quoteType = "all"
	}
	parts := make([]string, 0, len(instruments))
	for _, in := range instruments {
		parts = append(parts, in.ExchangeSegment+"|"+in.InstrumentToken)
	}
	neoSymbols := strings.Join(parts, ",")

	endpointURL, err := c.Config.GetURL("quotes_neo_symbol")
	if err != nil {
		return nil, err
	}
	endpointURL = strings.ReplaceAll(endpointURL, "{neo_symbols}", url.PathEscape(neoSymbols))
	endpointURL = strings.ReplaceAll(endpointURL, "{quote_type}", quoteType)

	headers := map[string]string{
		"Authorization": c.Config.ConsumerKey,
		"Content-Type":  "application/x-www-form-urlencoded",
	}
	resp, err := c.rest.Request("GET", endpointURL, nil, headers, nil)
	if err != nil {
		return Response{"Error": err.Error()}, nil
	}
	return DecodeJSON(resp)
}
