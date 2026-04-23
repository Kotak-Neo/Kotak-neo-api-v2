package neoapi

import (
	"bytes"
	"encoding/json"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"
)

// RESTClient is the underlying HTTP transport for the SDK.
type RESTClient struct {
	HTTPClient *http.Client
	Config     *Configuration
}

// NewRESTClient returns a REST client with a 30s default timeout.
func NewRESTClient(cfg *Configuration) *RESTClient {
	return &RESTClient{
		HTTPClient: &http.Client{Timeout: 30 * time.Second},
		Config:     cfg,
	}
}

// Request executes a request matching the Python rest.py contract.
//  - "application/json"                  → body is JSON-marshalled
//  - "application/x-www-form-urlencoded" → body is wrapped as jData=<json>
//  - GET / DELETE with queryParams       → params appended to URL
func (c *RESTClient) Request(method, targetURL string, queryParams map[string]string, headers map[string]string, body any) (*http.Response, error) {
	method = strings.ToUpper(method)
	if headers == nil {
		headers = map[string]string{}
	}
	if _, ok := headers["Content-Type"]; !ok {
		headers["Content-Type"] = "application/json"
	}

	if len(queryParams) > 0 {
		values := url.Values{}
		for k, v := range queryParams {
			values.Set(k, v)
		}
		sep := "?"
		if strings.Contains(targetURL, "?") {
			sep = "&"
		}
		targetURL = targetURL + sep + values.Encode()
	}

	var reqBody io.Reader
	contentType := headers["Content-Type"]
	if method == "POST" || method == "PUT" || method == "PATCH" || method == "DELETE" {
		switch {
		case strings.Contains(strings.ToLower(contentType), "json"):
			if body != nil {
				buf, err := json.Marshal(body)
				if err != nil {
					return nil, NewAPIError(0, "marshal error", err.Error())
				}
				reqBody = bytes.NewReader(buf)
			}
		case strings.Contains(strings.ToLower(contentType), "x-www-form-urlencoded"):
			form := url.Values{}
			if body != nil {
				buf, err := json.Marshal(body)
				if err != nil {
					return nil, NewAPIError(0, "marshal error", err.Error())
				}
				form.Set("jData", string(buf))
			}
			reqBody = strings.NewReader(form.Encode())
		default:
			return nil, NewAPIError(0, "invalid Content-Type", contentType)
		}
	}

	req, err := http.NewRequest(method, targetURL, reqBody)
	if err != nil {
		return nil, NewAPIError(0, "build request", err.Error())
	}
	for k, v := range headers {
		req.Header.Set(k, v)
	}
	req.Header.Set("User-Agent", "NeoTradeApi-go/1.0.0")

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return nil, NewAPIError(0, "http error", err.Error())
	}
	return resp, nil
}

// DecodeJSON reads and JSON-decodes a response body into a Response map.
func DecodeJSON(resp *http.Response) (Response, error) {
	defer resp.Body.Close()
	buf, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	if len(buf) == 0 {
		return Response{}, nil
	}
	var out Response
	if err := json.Unmarshal(buf, &out); err != nil {
		// Not all responses are object-shaped; wrap list-shaped responses.
		var list []any
		if listErr := json.Unmarshal(buf, &list); listErr == nil {
			return Response{"data": list}, nil
		}
		return Response{"raw": string(buf)}, nil
	}
	return out, nil
}
