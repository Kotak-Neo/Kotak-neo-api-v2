package neoapi

import (
	"encoding/json"
	"strings"
	"sync"
	"time"

	"github.com/gorilla/websocket"
)

// Instrument is a single subscription target.
type Instrument struct {
	InstrumentToken string `json:"instrument_token"`
	ExchangeSegment string `json:"exchange_segment"`
}

// NeoWebSocket manages the two streaming connections (market data + order feed).
// It mirrors the public shape of the Python NeoWebSocket class.
type NeoWebSocket struct {
	sid         string
	token       string
	serverID    string
	dataCenter  string
	marketConn  *websocket.Conn
	orderConn   *websocket.Conn
	marketOpen  bool
	orderOpen   bool
	subList     []Instrument
	quotesIndex bool

	OnOpen    func()
	OnMessage func(msg any)
	OnError   func(err error)
	OnClose   func()

	mu sync.Mutex
}

// NewNeoWebSocket constructs a socket. Callbacks are wired later by the parent Client.
func NewNeoWebSocket(sid, token, serverID, dataCenter string) *NeoWebSocket {
	return &NeoWebSocket{sid: sid, token: token, serverID: serverID, dataCenter: dataCenter}
}

// GetLiveFeed subscribes to the given instruments. Opens the socket lazily.
func (w *NeoWebSocket) GetLiveFeed(instruments []Instrument, isIndex, isDepth bool) error {
	subType := ReqTypeValues["SCRIP_SUBS"]
	if isIndex {
		subType = ReqTypeValues["INDEX_SUBS"]
		w.quotesIndex = true
	}
	if isDepth {
		subType = ReqTypeValues["DEPTH_SUBS"]
	}

	w.mu.Lock()
	w.subList = append(w.subList, instruments...)
	w.mu.Unlock()

	if !w.marketOpen {
		if err := w.openMarketConn(); err != nil {
			return err
		}
	}

	scrips := formatScrips(instruments)
	payload := map[string]any{"type": subType, "scrips": scrips, "channelnum": 2}
	return w.sendMarket(payload)
}

// UnsubscribeList stops streaming the given instruments.
func (w *NeoWebSocket) UnsubscribeList(instruments []Instrument, isIndex, isDepth bool) error {
	unsubType := ReqTypeValues["SCRIP_UNSUBS"]
	if isIndex {
		unsubType = ReqTypeValues["INDEX_UNSUBS"]
	}
	if isDepth {
		unsubType = ReqTypeValues["DEPTH_UNSUBS"]
	}
	if w.marketConn == nil {
		return &APIValueError{Msg: "Socket Connection has been closed"}
	}
	scrips := formatScrips(instruments)
	payload := map[string]any{"type": unsubType, "scrips": scrips, "channelnum": 2}
	return w.sendMarket(payload)
}

// GetOrderFeed opens the order-feed socket.
func (w *NeoWebSocket) GetOrderFeed() error {
	if w.orderOpen {
		return nil
	}
	return w.openOrderConn()
}

func (w *NeoWebSocket) openMarketConn() error {
	conn, _, err := websocket.DefaultDialer.Dial(WebSocketURL, nil)
	if err != nil {
		return err
	}
	w.marketConn = conn
	w.marketOpen = true

	handshake := map[string]any{"type": "cn", "Authorization": w.token, "Sid": w.sid}
	if err := w.sendMarket(handshake); err != nil {
		return err
	}
	if w.OnOpen != nil {
		w.OnOpen()
	}
	go w.readMarket()
	go w.marketHeartbeat()
	return nil
}

func (w *NeoWebSocket) openOrderConn() error {
	url := OrderFeedURL
	switch strings.ToLower(w.dataCenter) {
	case "adc":
		url = OrderFeedURLADC
	case "e21":
		url = OrderFeedURLE21
	case "e22":
		url = OrderFeedURLE22
	case "e41":
		url = OrderFeedURLE41
	case "e43":
		url = OrderFeedURLE43
	}
	conn, _, err := websocket.DefaultDialer.Dial(url, nil)
	if err != nil {
		return err
	}
	w.orderConn = conn
	w.orderOpen = true

	handshake := map[string]any{
		"type": "CONNECTION", "Authorization": w.token, "Sid": w.sid, "source": "WEB",
	}
	buf, _ := json.Marshal(handshake)
	if err := w.orderConn.WriteMessage(websocket.TextMessage, buf); err != nil {
		return err
	}
	if w.OnOpen != nil {
		w.OnOpen()
	}
	go w.readOrder()
	go w.orderHeartbeat()
	return nil
}

func (w *NeoWebSocket) sendMarket(payload map[string]any) error {
	buf, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	w.mu.Lock()
	defer w.mu.Unlock()
	return w.marketConn.WriteMessage(websocket.TextMessage, buf)
}

func (w *NeoWebSocket) readMarket() {
	for w.marketOpen {
		_, msg, err := w.marketConn.ReadMessage()
		if err != nil {
			w.marketOpen = false
			if w.OnError != nil {
				w.OnError(err)
			}
			if w.OnClose != nil {
				w.OnClose()
			}
			return
		}
		var anyMsg any
		if err := json.Unmarshal(msg, &anyMsg); err == nil {
			if w.OnMessage != nil {
				w.OnMessage(map[string]any{"type": "stock_feed", "data": anyMsg})
			}
		} else {
			// Raw binary packet — hand it to the codec.
			decoded, err := DecodeBinaryFrame(msg)
			if err == nil && w.OnMessage != nil {
				w.OnMessage(map[string]any{"type": "stock_feed", "data": decoded})
			}
		}
	}
}

func (w *NeoWebSocket) readOrder() {
	for w.orderOpen {
		_, msg, err := w.orderConn.ReadMessage()
		if err != nil {
			w.orderOpen = false
			if w.OnError != nil {
				w.OnError(err)
			}
			if w.OnClose != nil {
				w.OnClose()
			}
			return
		}
		var anyMsg any
		_ = json.Unmarshal(msg, &anyMsg)
		if w.OnMessage != nil {
			w.OnMessage(map[string]any{"type": "order_feed", "data": anyMsg})
		}
	}
}

func (w *NeoWebSocket) marketHeartbeat() {
	ticker := time.NewTicker(29 * time.Second)
	defer ticker.Stop()
	for range ticker.C {
		if !w.marketOpen {
			return
		}
		_ = w.sendMarket(map[string]any{"type": "hb"})
	}
}

func (w *NeoWebSocket) orderHeartbeat() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()
	for range ticker.C {
		if !w.orderOpen {
			return
		}
		buf, _ := json.Marshal(map[string]any{"type": "HB"})
		_ = w.orderConn.WriteMessage(websocket.TextMessage, buf)
	}
}

// Close shuts down both sockets.
func (w *NeoWebSocket) Close() {
	if w.marketConn != nil {
		_ = w.marketConn.Close()
		w.marketOpen = false
	}
	if w.orderConn != nil {
		_ = w.orderConn.Close()
		w.orderOpen = false
	}
}

func formatScrips(instruments []Instrument) string {
	parts := make([]string, 0, len(instruments))
	for _, i := range instruments {
		parts = append(parts, i.ExchangeSegment+"|"+i.InstrumentToken)
	}
	return strings.Join(parts, "&")
}

// Subscribe bridges the Client API to the underlying socket.
func (c *Client) Subscribe(instruments []Instrument, isIndex, isDepth bool) error {
	if err := c.requireLogin(); err != nil {
		return err
	}
	if c.ws == nil {
		c.ws = NewNeoWebSocket(c.Config.EditSID, c.Config.EditToken, c.Config.ServerID, c.Config.DataCenter)
		c.wireWS()
	}
	return c.ws.GetLiveFeed(instruments, isIndex, isDepth)
}

// Unsubscribe removes instruments from the live feed.
func (c *Client) Unsubscribe(instruments []Instrument, isIndex, isDepth bool) error {
	if err := c.requireLogin(); err != nil {
		return err
	}
	if c.ws == nil {
		return &APIValueError{Msg: "no active subscription"}
	}
	return c.ws.UnsubscribeList(instruments, isIndex, isDepth)
}

// SubscribeToOrderFeed starts streaming order/trade updates.
func (c *Client) SubscribeToOrderFeed() error {
	if err := c.requireLogin(); err != nil {
		return err
	}
	if c.ws == nil {
		c.ws = NewNeoWebSocket(c.Config.EditSID, c.Config.EditToken, c.Config.ServerID, c.Config.DataCenter)
		c.wireWS()
	}
	return c.ws.GetOrderFeed()
}

func (c *Client) wireWS() {
	c.ws.OnOpen = func() {
		if c.OnOpen != nil {
			c.OnOpen("The Session has been Opened!")
		}
	}
	c.ws.OnMessage = func(msg any) {
		if c.OnMessage != nil {
			c.OnMessage(msg)
		}
	}
	c.ws.OnError = func(err error) {
		if c.OnError != nil {
			c.OnError(err)
		}
	}
	c.ws.OnClose = func() {
		if c.OnClose != nil {
			c.OnClose("The Session has been Closed!")
		}
	}
}
