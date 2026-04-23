package neoapi

import (
	"fmt"
	"strings"
)

// Response is the loose response type — mirrors Python dict.
type Response map[string]any

func nonEmpty(val, name string) error {
	if strings.TrimSpace(val) == "" {
		return &APIValueError{Msg: fmt.Sprintf("%s is mandatory", name)}
	}
	return nil
}

// ValidatePlaceOrder mirrors req_data_validation.place_order_validation.
func ValidatePlaceOrder(exchangeSegment, product, price, orderType, quantity, validity, tradingSymbol, transactionType string) error {
	for k, v := range map[string]string{
		"exchange_segment": exchangeSegment, "product": product, "price": price,
		"order_type": orderType, "quantity": quantity, "validity": validity,
		"trading_symbol": tradingSymbol, "transaction_type": transactionType,
	} {
		if err := nonEmpty(v, k); err != nil {
			return err
		}
	}
	if _, ok := ExchangeSegment[exchangeSegment]; !ok {
		return &APIValueError{Msg: "invalid exchange_segment: " + exchangeSegment}
	}
	if _, ok := Product[product]; !ok {
		return &APIValueError{Msg: "invalid product: " + product}
	}
	if _, ok := OrderType[orderType]; !ok {
		return &APIValueError{Msg: "invalid order_type: " + orderType}
	}
	tt := strings.ToUpper(transactionType)
	if tt != "B" && tt != "S" && tt != "BUY" && tt != "SELL" {
		return &APIValueError{Msg: "transaction_type must be B/S"}
	}
	return nil
}

// ValidateCancelOrder — order_id must be non-empty.
func ValidateCancelOrder(orderID string) error {
	return nonEmpty(orderID, "order_id")
}

// ValidateOrderHistory — order_id must be non-empty.
func ValidateOrderHistory(orderID string) error {
	return nonEmpty(orderID, "order_id")
}

// ValidateMargin — basic field presence check.
func ValidateMargin(exchangeSegment, price, orderType, product, quantity, instrumentToken, transactionType string) error {
	for k, v := range map[string]string{
		"exchange_segment": exchangeSegment, "price": price, "order_type": orderType,
		"product": product, "quantity": quantity, "instrument_token": instrumentToken,
		"transaction_type": transactionType,
	} {
		if err := nonEmpty(v, k); err != nil {
			return err
		}
	}
	if _, ok := ExchangeSegment[exchangeSegment]; !ok {
		return &APIValueError{Msg: "invalid exchange_segment: " + exchangeSegment}
	}
	return nil
}

// ValidateLimits — segment/exchange/product against allow-lists.
func ValidateLimits(segment, exchange, product string) error {
	if !contains(SegmentLimits, segment) {
		return &APIValueError{Msg: "invalid segment: " + segment}
	}
	if !contains(ExchangeLimits, exchange) {
		return &APIValueError{Msg: "invalid exchange: " + exchange}
	}
	if !contains(ProductLimits, product) {
		return &APIValueError{Msg: "invalid product: " + product}
	}
	return nil
}

func contains(list []string, v string) bool {
	for _, s := range list {
		if s == v {
			return true
		}
	}
	return false
}
