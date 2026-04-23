package neoapi

import (
	"strings"

	"github.com/golang-jwt/jwt/v5"
)

// Configuration holds per-session state — tokens, sids, server routing, keys.
type Configuration struct {
	Host         string
	BearerToken  string
	ViewToken    string
	SID          string
	UserID       string
	EditToken    string
	EditSID      string
	EditRID      string
	ServerID     string
	NeoFinKey    string
	DataCenter   string
	BaseURL      string
	TOTPSessID   string
	ConsumerKey  string
}

// NewConfiguration creates a Configuration for the given environment ("prod" or "uat").
func NewConfiguration(environment string) *Configuration {
	return &Configuration{Host: environment}
}

// ExtractUserID decodes the JWT view-token and returns the `sub` claim.
func (c *Configuration) ExtractUserID(viewToken string) (string, error) {
	if viewToken == "" {
		return "", &APIValueError{Msg: "view_token is empty — call totp_login first"}
	}
	parser := jwt.NewParser(jwt.WithoutClaimsValidation())
	var claims jwt.MapClaims
	_, _, err := parser.ParseUnverified(viewToken, &claims)
	if err != nil {
		return "", err
	}
	if sub, ok := claims["sub"].(string); ok {
		c.UserID = sub
		return sub, nil
	}
	return "", &APIKeyError{Msg: "sub claim missing from token"}
}

// GetDomain returns the base URL for the current host.
// When sessionInit is true, the session-init root is returned instead.
func (c *Configuration) GetDomain(sessionInit bool) (string, error) {
	host := strings.ToLower(strings.TrimSpace(c.Host))
	if host != "prod" && host != "uat" {
		return "", &APIValueError{Msg: "environment must be 'prod' or 'uat'"}
	}
	if sessionInit {
		return BaseURL, nil
	}
	if host == "prod" {
		if c.BaseURL != "" {
			return c.BaseURL, nil
		}
		return ProdBaseURL, nil
	}
	return UATBaseURL, nil
}

// GetURL returns the full URL for the named endpoint, e.g. "place_order".
func (c *Configuration) GetURL(apiInfo string) (string, error) {
	host := strings.ToLower(strings.TrimSpace(c.Host))
	domain, err := c.GetDomain(false)
	if err != nil {
		return "", err
	}
	var path string
	if host == "prod" {
		path = ProdPaths[apiInfo]
	} else {
		path = UATPaths[apiInfo]
	}
	if path == "" {
		return "", &APIValueError{Msg: "unknown endpoint: " + apiInfo}
	}
	return strings.TrimRight(domain, "/") + "/" + path, nil
}

// GetNeoFinKey — tracking key header; falls back to defaults per environment.
func (c *Configuration) GetNeoFinKey() string {
	host := strings.ToLower(strings.TrimSpace(c.Host))
	if c.NeoFinKey != "" {
		return c.NeoFinKey
	}
	if host == "prod" {
		return "neotradeapi"
	}
	return "bQJNkL5z8m4aGcRgjDvXhHfSx7VpZnE"
}

// IsLoggedIn reports whether a full 2FA flow has completed.
func (c *Configuration) IsLoggedIn() bool {
	return c.EditToken != "" && c.EditSID != ""
}
