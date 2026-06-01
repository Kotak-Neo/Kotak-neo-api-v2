from neo_api_client import NeoAPI
import time

"""
In order to get started with Kotak Neo API we would like you to do the following things first.
1. Checkout our API docs: https://www.kotaksecurities.com/platform/kotak-neo-trade-api/
2. Create an APP using API Dashboard: https://www.kotaksecurities.com/ (Login → Invest → Trade API → API Dashboard)
3. Register TOTP (Time-based One-Time Password) using Google Authenticator
Once you have created an APP and registered TOTP you can start using the below SDK
"""

#### Generate Session Token using Consumer Key and TOTP (Login Flow)

# pip install "git+https://github.com/Kotak-Neo/Kotak-neo-api-v2.git@v2.0.1#egg=neo_api_client"

"""
1. Input parameters
"""
# Get these credentials from Kotak Neo API Dashboard
consumer_key = "YOUR_TOKEN_HERE"  # Token generated in API Dashboard
mobile_number = "YOUR_REGISTERED_MOBILE"  # Mobile number registered with Kotak
mpin = "YOUR_6_DIGIT_MPIN"  # Your 6-digit trading PIN
totp_secret = "YOUR_TOTP_SECRET"  # Secret key for TOTP (if storing locally)

# Environment (use 'prod' for production)
environment = "prod"

"""
2. Initialize NeoAPI Client
"""
try:
    client = NeoAPI(environment=environment, consumer_key=consumer_key)
    print("NeoAPI client initialized successfully")
except Exception as e:
    print(f"Error initializing NeoAPI client: {e}")
    exit()

"""
3. Generate TOTP Code (if you have the secret key stored)
   If not, manually enter the 6-digit code from Google Authenticator
"""
# Option 1: If you have pyotp installed
# import pyotp
# totp = pyotp.TOTP(totp_secret)
# current_totp = totp.now()

# Option 2: Manually enter TOTP
current_totp = input("Enter 6-digit TOTP code from Google Authenticator: ")

"""
4. Login to Generate Session Token
"""
try:
    response = client.login(
        mobile_number=mobile_number,
        totp=current_totp,
        mpin=mpin
    )
    
    print("Login Response:", response)
    
    # Extract session tokens from response
    if "success" in response and response["success"]:
        data = response.get("data", {})
        trading_token = data.get("token")
        trading_sid = data.get("sid")
        base_url = data.get("base_url")
        client_code = data.get("client_code")  # Your UCC
        
        print(f"\n✓ Login Successful!")
        print(f"Trading Token: {trading_token}")
        print(f"Client Code: {client_code}")
        
        # Save credentials to files for later use
        with open("kotak_trading_token.txt", 'w') as file:
            file.write(trading_token)
        with open("kotak_client_code.txt", 'w') as file:
            file.write(client_code)
        with open("kotak_trading_sid.txt", 'w') as file:
            file.write(trading_sid)
        with open("kotak_base_url.txt", 'w') as file:
            file.write(base_url)
            
        print("\n✓ Credentials saved to files")
        
    else:
        error = response.get("message", "Unknown error")
        print(f"✗ Login Failed: {error}")
        exit()
        
except Exception as e:
    print(f"✗ Error during login: {e}")
    exit()

"""
5. Now you can use the client for trading and data API calls
   Example: Fetch holdings, place orders, etc.
"""
print("\n--- You can now use the NeoAPI client for trading ---")
print("Example usage:")
print("  - client.place_order(...)")
print("  - client.cancel_order(...)")
print("  - client.fetch_holdings(...)")
print("  - client.fetch_orderbook(...)")
