package main

import (
	"fmt"
	"log"
	"os"

	"github.com/kotak-neo/neoapi/neoapi"
)

// Example mirrors neo_api_client/demo.py.
//
// Run with:
//
//	NEO_ENV=prod NEO_CONSUMER=<base64> NEO_MOBILE=+91... NEO_UCC=XXXXX NEO_TOTP=123456 NEO_MPIN=1234 \
//	  go run ./examples
func main() {
	env := envOr("NEO_ENV", "uat")
	consumer := os.Getenv("NEO_CONSUMER")
	mobile := os.Getenv("NEO_MOBILE")
	ucc := os.Getenv("NEO_UCC")
	totp := os.Getenv("NEO_TOTP")
	mpin := os.Getenv("NEO_MPIN")

	c, err := neoapi.NewClient(env, "")
	if err != nil {
		log.Fatal(err)
	}
	c.WithConsumerKey(consumer)

	c.OnOpen = func(m string) { fmt.Println("[ws] open:", m) }
	c.OnMessage = func(m any) { fmt.Println("[ws] msg :", m) }
	c.OnError = func(e error) { fmt.Println("[ws] err :", e) }
	c.OnClose = func(m string) { fmt.Println("[ws] close:", m) }

	if mobile == "" || ucc == "" || totp == "" || mpin == "" {
		fmt.Println("Set NEO_MOBILE, NEO_UCC, NEO_TOTP, NEO_MPIN to exercise login.")
		fmt.Println("Client constructed OK; skipping live calls.")
		return
	}

	if resp, err := c.TotpLogin(mobile, ucc, totp); err != nil {
		log.Fatal(err)
	} else {
		fmt.Println("totp_login:", resp)
	}
	if resp, err := c.TotpValidate(mpin); err != nil {
		log.Fatal(err)
	} else {
		fmt.Println("totp_validate:", resp)
	}

	if resp, err := c.OrderReport(); err == nil {
		fmt.Println("order_report:", resp)
	}
}

func envOr(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}
