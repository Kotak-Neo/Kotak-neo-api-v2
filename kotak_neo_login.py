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
consumer_key = "YOUR_TOKEN_HERE"           # Token generated in API Dashboard
mobile_number = "YOUR_REGISTERED_MOBILE"   # Mobile number registered with Kotak (with country code, e.g., +91XXXXXXXXXX)
ucc = "YOUR_UCC_CODE"                      # Unique Client Code (5 characters, e.g., AB123)
mpin = "YOUR_6_DIGIT_MPIN"                # Your 6-digit trading PIN

# Environment (use 'prod' for production)
environment = "prod"

"""
2. Initialize NeoAPI Client
"""
try:
    client = NeoAPI(environment=environment, consumer_key=consumer_key)
    print("✓ NeoAPI client initialized successfully")
except Exception as e:
    print(f"✗ Error initializing NeoAPI client: {e}")
    exit()

"""
3. Step 1: TOTP Login - Get TOTP code from Google Authenticator
"""
current_totp = input("Enter 6-digit TOTP code from Google Authenticator: ")

"""
4. Step 2: Call totp_login with mobile_number, UCC, and TOTP
"""
try:
    print("\n--- Attempting TOTP Login ---")
    totp_login_response = client.totp_login(
        mobile_number=mobile_number,
        ucc=ucc,
        totp=current_totp
    )
    
    print("TOTP Login Response:", totp_login_response)
    
    # Check if login was successful
    if "success" in totp_login_response and totp_login_response["success"]:
        print("✓ TOTP Login Successful!")
    else:
        error = totp_login_response.get("message", "Unknown error")
        print(f"✗ TOTP Login Failed: {error}")
        exit()
        
except Exception as e:
    print(f"✗ Error during TOTP login: {e}")
    exit()

"""
5. Step 3: Validate TOTP with MPIN
"""
try:
    print("\n--- Attempting TOTP Validation with MPIN ---")
    totp_validate_response = client.totp_validate(mpin=mpin)
    
    print("TOTP Validate Response:", totp_validate_response)
    
    # Check if validation was successful
    if "success" in totp_validate_response and totp_validate_response["success"]:
        data = totp_validate_response.get("data", {})
        trading_token = data.get("token")
        client_code = data.get("ucc", ucc)
        
        print(f"\n✓ Login Successful!")
        print(f"Trading Token: {trading_token}")
        print(f"Client Code: {client_code}")
        
        # Save credentials to files for later use
        with open("kotak_trading_token.txt", 'w') as file:
            file.write(trading_token)
        with open("kotak_client_code.txt", 'w') as file:
            file.write(client_code)
        with open("kotak_consumer_key.txt", 'w') as file:
            file.write(consumer_key)
            
        print("\n✓ Credentials saved to files:")
        print("  - kotak_trading_token.txt")
        print("  - kotak_client_code.txt")
        print("  - kotak_consumer_key.txt")
        
    else:
        error = totp_validate_response.get("message", "Unknown error")
        print(f"✗ TOTP Validation Failed: {error}")
        exit()
        
except Exception as e:
    print(f"✗ Error during TOTP validation: {e}")
    exit()

"""
6. Now you can use the client for trading and data API calls
   Example: Fetch holdings, place orders, etc.
"""
print("\n" + "="*60)
print("--- You can now use the NeoAPI client for trading ---")
print("="*60)
print("\nExample usage:")
print("  - client.place_order(...)")
print("  - client.cancel_order(...)")
print("  - client.holdings()")
print("  - client.positions()")
print("  - client.order_report()")
print("  - client.trade_report()")
