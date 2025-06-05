# Kotak Neo Python SDK Development

- API version: 1.0.1
- Package version: 2.0.0

## Requirements.

Python 3.10+

## Installation & Usage
### pip install

If the python package is hosted on a repository, you can install directly using:

```sh
pip install "git+https://github.com/Kotak-Neo/Kotak-neo-api-v2.git#egg=neo_api_client"
```

If you are updating your package please use below command to install
```sh
pip install --force-reinstall "git+https://github.com/Kotak-Neo/Kotak-neo-api-v2"
```
(you may need to run `pip` with root permission: `sudo pip install -e "`)

Then import the package:
```python
import neo_api_client
```

### Setuptools

Install via [Setuptools]

```sh
python setup.py install --user
```
(or `sudo python setup.py install` to install the package for all users)

Then import the package:
```python
import neo_api_client
```

## Getting Started

Please follow the [installation procedure](#installation--usage) and then refer to the sample code below for various API requests:

```python
from neo_api_client import NeoAPI
from neo_api_client import BaseUrl

# ucc: Unique Client Code which you will find in mobile application/website under profile section
base_url = BaseUrl(ucc='').get_base_url()

# Either you pass consumer_key and consumer_secret or you pass acsess_token 
# access_token: It is optional. If you have barrier token then pass and consumer_key and consumer_secret will be optional.
# environment: By default it's uat. You can pass prod to connect to live server
# neo_fin_key: Key you recieve at the time of api registration on your registered email id
# base_url: You'll get by calling base_url api
client = NeoAPI(consumer_key="", consumer_secret="", environment='prod', access_token=None, neo_fin_key=None, base_url=base_url)


# Login using TOTP

# Complete your TOTP registration from Kotak Securities website. Follow steps mentioned below.

# Visit https://www.kotaksecurities.com/platform/kotak-neo-trade-api/ and select Register for Totp.
# Totp registration is a one time step where you can register for totp on your mobile and start receiving totps.

# Step 1 - Verify your mobile no with OTP

# Step 2 - Select account, for which you want to register for totp

# Step 3 - Select option to register for totp

# Step 4 - You will receive a QR code, which is valid for 5 minutes

# Step 5 - Open any authenticator app, and scan the QR code

# Step 6 - You will start receiving the Totps on the authenticator apps

# Step 7 - Submit the totp on the QR code page to complete the Totp registration

# mobile_number: registered mobile number
# ucc: Unique Client Code which you will find in mobile application/website under profile section
# totp: Time-based One-Time Password recieved on google authenticator application
# totp_login generates the view token and session id used to generate trade token
client.totp_login(mobile_number="", ucc="", totp='')

# mpin: mpin for your neo account
# totp_validate generates the trade token
client.totp_validate(mpin="")


# Alternate Login Method - Login using QR code (User can either login with TOTP or with QR flow)

# ucc: Unique Client Code which you will find in mobile application/website under profile section
# qr_code_get_link returns a qrcode
client.qr_code_get_link(ucc='')

# ott: You will recieve ott by scanning qr code through kotak's application 
# ucc: Unique Client Code which you will find in mobile application/website under profile section
# trade token is generated on passing ott to qr_code_generate_session 
client.qr_code_generate_session(ott='', ucc='')


# Once you have session token after completing 2FA, you can place the order by using below function
# exchange_segment: Expected values are nse_cm, bse_cm, nse_fo, bse_fo, cde_fo. 
# For exchange_segment nse_cm expected product=NRML, CNC, MIS, CO, BO. Add order_type=L, MKT, SL, SL-M. 
# For exchange_segment bse_cm expected product=NRML, CNC, MIS, CO, BO. Add order_type=L, MKT, SL, SL-M.
# For exchange_segment nse_fo expected product=NRML, MIS, BO. Add order_type=L, MKT, SL, SL-M.
# For exchange_segment bse_fo expected product=NRML, MIS. Add order_type=L, MKT, SL, SL-M.

# For exchange_segment cde_fo expected product=NRML, MIS. Add order_type=L, MKT, SL, SL-M.

# product: Expected values are NRML, CNC, MIS, CO, BO
# price: scrip price
# order_type: Expected values are L, MKT, SL, SL-M
# quantity: The stock quantity(If suppose one lot size of a stock is 25, then in disclosed_quantity you have to pass 25 and not 1)
# validity: Expected values are DAY, IOC, GTC, EOS, GTD
# trading_symbol: scrip trading symbol. You will get this value from master scrip file
# transaction_type: Expected values are B, S
# amo: Expected values are either YES or NO
# disclosed_quantity: by default 0. Disclosed quantity (DQ) is a feature that allows traders to only disclose a portion of their order quantity to the market. This feature is useful when trading large quantities of shares. For example, if your quantity is 10, then disclosed_quantity can be between 0 to 10 (only disclosed _quantity will be visible in market depth)
# market_protection: by default 0. Market protection, also known as Market Price Protection (MPP), is a feature that protects investors from sudden price movements in the stock market. For example, if the Last Traded Price (LTP) is ₹100 and a 5% Market Protection is applied, your order will be placed as a limit order at ₹105. This means your buy order will only be executed if sellers are available at ₹105 or below. If no sellers are available within this range, your order will remain open as a limit order.
# pf: by default N
# trigger_price: Price on which ypur order will be open to the market
# tag: Give your own tag to track the order
# scrip_token: Applicable only for Bracket Order
# square_off_type: Applicable only for Bracket Order. Expected Values are 'Absolute' and 'Ticks'.
# stop_loss_type: Applicable only for bracket Order. Expected Values are 'Absolute' and 'Ticks'.
# stop_loss_value: Applicable only for Bracket Order
# square_off_value: Applicable only for Bracket Order
# last_traded_price: Applicable only for Bracket Order
# trailing_stop_loss: Applicable only for Bracket Order. Expected Values are 'Y' and 'N'.
# trailing_sl_value: Applicable only for Bracket Order. Expected Values are 'Y' and 'N'.	
client.place_order(
    exchange_segment="",
    product="",
    price="",
    order_type="",
    quantity="",
    validity="",
    trading_symbol="",
    transaction_type="",
    amo="NO",
    disclosed_quantity="0",
    market_protection="0",
    pf="N",
    trigger_price="0",
    tag=None,
    scrip_token=None,
    square_off_type=None,
    stop_loss_type=None,
    stop_loss_value=None,
    square_off_value=None,
    last_traded_price=None,
    trailing_stop_loss=None,
    trailing_sl_value=None,
)


						
# Modify an order
# order_id: Order number you'll recieve from the response after placing the order
# price: scrip price
# quantity: The stock quantity(If suppose one lot size of a stock is 25, then in disclosed_quantity you have to pass 25 and not 1)
# disclosed_quantity: by default 0. Disclosed quantity (DQ) is a feature that allows traders to only disclose a portion of their order quantity to the market. This feature is useful when trading large quantities of shares. For example, if your quantity is 10, then disclosed_quantity can be between 0 to 10 (only disclosed _quantity will be visible in market depth)
# trigger_price: Price on which ypur order will be open to the market
# validity: Expected values are DAY, IOC, GTC, EOS, GTD
# order_type: Expected values are L, MKT, SL, SL-M
client.modify_order(order_id = "", price = "7.0", quantity = "2", disclosed_quantity = "0", trigger_price = "0", validity = "DAY", order_type='')

# Cancel an order
# order_id: Order number you'll recieve from the response after placing the order
client.cancel_order(order_id = "")

# order_id: Order number you'll recieve from the response after placing the order
# isVerify: isVerify is an optional param. Default value is 'False'. If isVerify is True, we will first check the status of the given order. If the order status is not 'rejected', 'cancelled', 'traded', or 'completed', we will proceed to cancel the order using the cancel_order function. Otherwise, we will display the order status to the user instead.
# amo: It specifies whether its an after market order. Expected values are YES and NO
# This request will check whether your order is rejected, cancelled, complete or traded. If any of this is true, your order will not be cancelled. This will be rejected with the rejection reson. 
# If this is not the case, the your order will be cancelled.
client.cancel_order(order_id = "", amo = "", isVerify=True)


# Cancel cover order
# order_id: Order number you'll recieve from the response after placing the order
client.cancel_cover_order(order_id = "")
# order_id: Order number you'll recieve from the response after placing the order
# isVerify: isVerify is an optional param. Default value is 'False'. If isVerify is True, we will first check the status of the given order. If the order status is not 'rejected', 'cancelled', 'traded', or 'completed', we will proceed to cancel the order using the cancel_order function. Otherwise, we will display the order status to the user instead.
# This request will check whether your order is rejected, cancelled, complete or traded. If any of this is true, your order will not be cancelled. This will be rejected with the rejection reson. 
# amo: It specifies whether its an after market order. Expected values are YES and NO
client.cancel_cover_order(order_id = "", amo = "", isVerify=False)

# Cancel bracket order
# Cancel cover order
# order_id: Order number you'll recieve from the response after placing the order
client.cancel_bracket_order(order_id = "")
# order_id: Order number you'll recieve from the response after placing the order
# isVerify: isVerify is an optional param. Default value is 'False'. If isVerify is True, we will first check the status of the given order. If the order status is not 'rejected', 'cancelled', 'traded', or 'completed', we will proceed to cancel the order using the cancel_order function. Otherwise, we will display the order status to the user instead.
# This request will check whether your order is rejected, cancelled, complete or traded. If any of this is true, your order will not be cancelled. This will be rejected with the rejection reson. 
# amo: It specifies whether its an after market order. Expected values are YES and NO
client.cancel_bracket_order(order_id = "", amo = "", isVerify=False)


# Get Order Book
# The function retrieves a list of orders in the order book.
client.order_report()

# Get Order History
# The function retrieves the order history for a given order ID.
# order_id: Order number you'll recieve from the response after placing the order
client.order_history(order_id = "")

# Get Trade Book
# The function retrieves a filtered list of trades.
client.trade_report()

# Get Detailed Trade Report for specific order id. 
# The function retrieves a filtered list of trades.
# order_id: Order number you'll recieve from the response after placing the order
client.trade_report(order_id = "")

# Get Positions
# The function retrieves a list of positions
client.positions()

# Get Portfolio Holdings
# The function retrieves the current holdings for the portfolio
client.holdings()

# Get Limits
# The function retrieves the limits available for the given segment, exchange and product
# segment: Expected values are CASH, CUR, FO, ALL. Default value is 'ALL'
# exchange: Expected values are ALL, NSE, BSE
# product: Expected values are NRML, CNC, MIS, ALL
client.limits(segment="", exchange="", product="")

# Get Margin required for Equity orders. 
# The function calculates the margin required for a given trade.
# exchange_segment: Expected values are nse_cm, bse_cm, nse_fo, bse_fo, cde_fo. 
# price: scrip price
# order_type: Expected values are L, MKT, SL, SL-M
# product: Expected values are NRML, CNC, MIS, CO, BO
# quantity: The stock quantity
# instrument_token: The instrument token of the stock to trade.
# transaction_type: Expected values are B, S
client.margin_required(exchange_segment = "", price = "", order_type= "", product = "",   quantity = "", instrument_token = "",  transaction_type = "")

# Get Scrip Master CSV file
# The function retrieves the list of scrips available in the given exchange segment
client.scrip_master()

# Get Scrip Master CSV file for specific Exchange Segment. 
# exchange_segment: Section of a stock exchange. Its a mandatory param. Expected values are nse_cm, bse_cm, nse_fo, bse_fo, cde_fo
client.scrip_master(exchange_segment = "")

# Search for the Scrip details from Scrip master file
# exchange_segment: Section of a stock exchange. Its a mandatory param. Expected values are nse_cm, bse_cm, nse_fo, bse_fo, cde_fo
client.search_scrip(exchange_segment="", symbol="", expiry="", option_type="",
                    strike_price="")

# Get quote details
instrument_tokens = [
    {"instrument_token": "", "exchange_segment": ""},
    {"instrument_token": "", "exchange_segment": ""},
    {"instrument_token": "", "exchange_segment": ""}
]
# quote_type: Expected values are `all`, `depth`, `ohlc`, `ltp`, `oi`, `52w`, `circuit_limits`, `scrip_details`
    # By default, `quote_type` is set as `all`, which means you will get the complete data.
    # Quotes API can be accessed by access token without completing login.
# instrument_tokens: This is a list of dictionaries.
    # instrument_token: The instrument token of the stock
    # exchange_segment: Expected values are nse_cm, bse_cm, nse_fo, bse_fo, cde_fo
client.quotes(instrument_tokens = instrument_tokens, quote_type = "")

def on_message(message):
    print(message)
    
def on_error(error_message):
    print(error_message)

def on_close(message):
    print(message)
    
def on_open(message):
    print(message)

# Setup Callbacks for websocket events (Optional)
client.on_message = on_message  # called when message is received from websocket
client.on_error = on_error  # called when any error or exception occurs in code or websocket
client.on_close = on_close  # called when websocket connection is closed
client.on_open = on_open  # called when websocket successfully connects

# Subscribe method will get you the live feed details of the given tokens.
# By Default isIndex is set as False and you want to get the live feed to index scrips set the isIndex flag as True 
# By Default isDepth is set as False and you want to get the depth information set the isDepth flag as True
# instrument_tokens: This is a list of dictionaries.
    # instrument_token: The instrument token of the stock which you will get from master scrip file
    # exchange_segment: Expected values are nse_cm, bse_cm, nse_fo, bse_fo, cde_fo
# isIndex: If you want to subscribe to index data, set isIndex to True
# isDepth: If you want to subscribe to depth data, set isDepth to True
client.subscribe(instrument_tokens = instrument_tokens, isIndex=False, isDepth=False)

# Un_Subscribes the given tokens. First the tokens will be checked weather that is subscribed. If not Subscribed we will send you the error message else we will unsubscribe the give tokens
# instrument_tokens: This is a list of dictionaries.
    # instrument_token: The instrument token of the stock
    # exchange_segment: Expected values are nse_cm, bse_cm, nse_fo, bse_fo, cde_fo
# isIndex: If you want to unsubscribe to index data, set isIndex to True
# isDepth: If you want to unsubscribe to depth data, set isDepth to True
client.un_subscribe(instrument_tokens=instrument_tokens, isIndex=False, isDepth=False)

#Order Feed 
# This function subscribes to order feed
client.subscribe_to_orderfeed()

#Terminate user's Session
client.logout()
```


## Documentation for API Endpoints

| Class                  | Method                                                                                     | Description              |
|------------------------|--------------------------------------------------------------------------------------------|--------------------------|
| *Base Url*             | [**neo_api_client.BaseUrl**](docs/Base_url.md#base_url)                                    | Base Url                 |
| *LoginAPI*             | [**neo_api_client.SessionINIT**](docs/Session_init.md#session_init)                        | Initialise Session       |
| *TOTP LoginAPI*        | [**neo_api_client.Totp_login**](docs/Totp_login.md#totp_login)                             | TOTP Login               |
| *TOTP LoginAPI*        | [**neo_api_client.Totp_validation**](docs/Totp_validate.md#totp_validate)                  | TOTP Validation          |
| *QR Code LoginAPI*     | [**neo_api_client.Qr_code_link**](docs/Qr_code_link.md#qr_code_link)                       | QR Code Get Link         |
| *QR Code LoginAPI*     | [**neo_api_client.Qr_code_session**](docs/Qr_code_session.md#qr_code_session)              | QR Code Generate Session |
| *Place Order*          | [**neo_api_client.placeorder**](docs/Place_Order.md#place_order)                           | Place Order              |
| *Modify Order*         | [**neo_api_client.modifyorder**](docs/Modify_Order.md#modify_order)                        | Modify Order             |
| *Cancel Order*         | [**neo_api_client.cancelorder**](docs/Cancel_Order.md#cancel_order)                        | Cancel Order             |
| *Cancel Order*         | [**neo_api_client.cancelcoverorder**](docs/Cancel_Cover_Order.md#cancel_cover_order)       | Cancel Cover Order       |
| *Cancel Order*         | [**neo_api_client.cancelbracketorder**](docs/Cancel_Bracket_Order.md#cancel_bracket_order) | Cancel Bracket Order     |
| *Order Report*         | [**neo_api_client.orderreport**](docs/Order_report.md#order_report)                        | Order Report             |
| *Order History*        | [**neo_api_client.orderhistory**](docs/Order_history.md#order_history)                     | Order Report             |
| *Trade Report*         | [**neo_api_client.tradereport**](docs/Trade_report.md#trade_report)                        | Trade Report             |
| *Positions*            | [**neo_api_client.positions**](docs/Positions.md#positions)                                | Positions                |
| *Holdings*             | [**neo_api_client.holdings**](docs/Holdings.md#holdings)                                   | Holdings                 |
| *Limits*               | [**neo_api_client.limits**](docs/Limits.md#limits)                                         | Limits                   |
| *Margin Required*      | [**neo_api_client.margin_required**](docs/Margin_Required.md#margin_required)              | Margin Required          |
| *Scrip Master*         | [**neo_api_client.scrip_master**](docs/Scrip_Master.md#scrip_master)                       | Scrip Master             |
| *Search Scrip*         | [**neo_api_client.scrip_search**](docs/Scrip_Search.md#scrip_search)                       | Scrip Search             |
| *Quotes*               | [**neo_api_client.quotes**](docs/Quotes.md#quotes)                                         | Quotes                   |
| *Subscribe*            | [**neo_api_client.subscribe**](docs/webSocket.md#websocket)                                | Subscribe                |
| *Subscribe Order Feed* | [**neo_api_client.subscribeorderfeed**](docs/webSocket_orderfeed.md#websocket_orderfeed)   | Subscribe                |

