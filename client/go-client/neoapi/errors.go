package neoapi

import "fmt"

// APIError is the base error type returned by the SDK for HTTP-level failures.
type APIError struct {
	Status int
	Reason string
	Body   string
}

func (e *APIError) Error() string {
	if e.Body != "" {
		return fmt.Sprintf("(%d) %s: %s", e.Status, e.Reason, e.Body)
	}
	return fmt.Sprintf("(%d) %s", e.Status, e.Reason)
}

// NewAPIError builds an APIError.
func NewAPIError(status int, reason, body string) *APIError {
	return &APIError{Status: status, Reason: reason, Body: body}
}

// APIValueError — invalid parameter value.
type APIValueError struct{ Msg string }

func (e *APIValueError) Error() string { return e.Msg }

// APITypeError — parameter type mismatch.
type APITypeError struct{ Msg string }

func (e *APITypeError) Error() string { return e.Msg }

// APIAttributeError — missing required attribute.
type APIAttributeError struct{ Msg string }

func (e *APIAttributeError) Error() string { return e.Msg }

// APIKeyError — missing required key in response/request.
type APIKeyError struct{ Msg string }

func (e *APIKeyError) Error() string { return e.Msg }
