package neoapi

import "strings"

// TotpLogin — step 1 of 2FA. Returns view token + sid on success and stashes them in Configuration.
func (c *Client) TotpLogin(mobileNumber, ucc, totp string) (Response, error) {
	if mobileNumber == "" || ucc == "" || totp == "" {
		return Response{"error": []Response{{"message": "mobile_number, ucc or totp missing"}}}, nil
	}
	domain, err := c.Config.GetDomain(true)
	if err != nil {
		return nil, err
	}
	urlStr := strings.TrimRight(domain, "/") + "/" + ProdPaths["totp_login"]
	if strings.EqualFold(strings.TrimSpace(c.Config.Host), "uat") {
		urlStr = strings.TrimRight(domain, "/") + "/" + UATPaths["totp_login"]
	}

	headers := map[string]string{
		"Authorization": c.Config.ConsumerKey,
		"neo-fin-key":   c.Config.GetNeoFinKey(),
		"Content-Type":  "application/json",
	}
	body := map[string]string{"mobileNumber": mobileNumber, "ucc": ucc, "totp": totp}
	resp, err := c.rest.Request("POST", urlStr, nil, headers, body)
	if err != nil {
		return nil, err
	}
	data, err := DecodeJSON(resp)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode >= 200 && resp.StatusCode <= 299 {
		if d, ok := data["data"].(map[string]any); ok {
			if v, ok := d["token"].(string); ok {
				c.Config.ViewToken = v
			}
			if v, ok := d["sid"].(string); ok {
				c.Config.SID = v
			}
		}
	}
	return data, nil
}

// TotpValidate — step 2 of 2FA. Takes mpin, returns edit token and writes auth state into Configuration.
func (c *Client) TotpValidate(mpin string) (Response, error) {
	if mpin == "" {
		return Response{"error": []Response{{"message": "mpin is missing"}}}, nil
	}
	domain, err := c.Config.GetDomain(true)
	if err != nil {
		return nil, err
	}
	urlStr := strings.TrimRight(domain, "/") + "/" + ProdPaths["totp_validate"]
	if strings.EqualFold(strings.TrimSpace(c.Config.Host), "uat") {
		urlStr = strings.TrimRight(domain, "/") + "/" + UATPaths["totp_validate"]
	}

	headers := map[string]string{
		"Authorization": c.Config.ConsumerKey,
		"sid":           c.Config.SID,
		"Auth":          c.Config.ViewToken,
		"neo-fin-key":   c.Config.GetNeoFinKey(),
		"Content-Type":  "application/json",
	}
	body := map[string]string{"mpin": mpin}
	resp, err := c.rest.Request("POST", urlStr, nil, headers, body)
	if err != nil {
		return nil, err
	}
	data, err := DecodeJSON(resp)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode >= 200 && resp.StatusCode <= 299 {
		if d, ok := data["data"].(map[string]any); ok {
			if v, ok := d["token"].(string); ok {
				c.Config.EditToken = v
			}
			if v, ok := d["sid"].(string); ok {
				c.Config.EditSID = v
			}
			if v, ok := d["rid"].(string); ok {
				c.Config.EditRID = v
			}
			if v, ok := d["hsServerId"].(string); ok {
				c.Config.ServerID = v
			}
			if v, ok := d["dataCenter"].(string); ok {
				c.Config.DataCenter = v
			}
			if v, ok := d["baseUrl"].(string); ok {
				c.Config.BaseURL = v
			}
		}
	}
	return data, nil
}

// Logout — clears local session state. Mirrors Python: no HTTP call, just clears tokens.
func (c *Client) Logout() Response {
	if !c.Config.IsLoggedIn() {
		return Response{"Error Message": "Complete the 2fa process before accessing this application"}
	}
	c.Config.BearerToken = ""
	c.Config.EditSID = ""
	c.Config.EditToken = ""
	return Response{"State": "OK", "message": "You have been successfully logged out"}
}
