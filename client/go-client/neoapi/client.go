// Package neoapi is a Go client for Kotak Securities' Neo trading API.
//
// Example:
//
//	c, _ := neoapi.NewClient("prod", "")
//	c.Config.ConsumerKey = "your-consumer-key"
//	c.TotpLogin("+919999999999", "ABC12", "123456")
//	c.TotpValidate("1234")
//	resp, _ := c.PlaceOrder(neoapi.PlaceOrderRequest{ ... })
package neoapi

// Client is the top-level SDK entry point. Mirrors NeoAPI in the Python SDK.
type Client struct {
	Config *Configuration
	rest   *RESTClient
	ws     *NeoWebSocket

	// Callbacks for streaming feeds. Set before calling Subscribe or SubscribeToOrderFeed.
	OnOpen    func(msg string)
	OnMessage func(msg any)
	OnError   func(err error)
	OnClose   func(msg string)
}

// NewClient builds a Client.
//
// environment: "prod" or "uat".
// accessToken: optional pre-existing bearer token; leave empty to use the TOTP flow.
func NewClient(environment, accessToken string) (*Client, error) {
	cfg := NewConfiguration(environment)
	cfg.BearerToken = accessToken
	c := &Client{Config: cfg, rest: NewRESTClient(cfg)}
	return c, nil
}

// WithConsumerKey sets the consumer key header used during login.
func (c *Client) WithConsumerKey(key string) *Client {
	c.Config.ConsumerKey = key
	return c
}

// WithNeoFinKey sets the finkey header used on login requests.
func (c *Client) WithNeoFinKey(key string) *Client {
	c.Config.NeoFinKey = key
	return c
}

// authHeaders returns the Sid/Auth headers required for authenticated endpoints.
func (c *Client) authHeaders(contentType string) map[string]string {
	return map[string]string{
		"Sid":          c.Config.EditSID,
		"Auth":         c.Config.EditToken,
		"Content-Type": contentType,
	}
}

func (c *Client) requireLogin() error {
	if !c.Config.IsLoggedIn() {
		return &APIValueError{Msg: "Complete the 2fa process before accessing this application"}
	}
	return nil
}

func (c *Client) queryWithServerID() map[string]string {
	return map[string]string{"sId": c.Config.ServerID}
}
