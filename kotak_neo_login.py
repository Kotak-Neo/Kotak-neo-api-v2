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
consumer_key = "fb564892-aecf-42ec-95fd-d5e1cc2477b7"           # Token generated in API Dashboard
mobile_number = "+919929936431"   # Mobile number registered with Kotak (10 digits, e.g., 9876543210)
ucc = "V77SV"                      # Unique Client Code (5 characters, e.g., AB123)
mpin = "975200"                # Your 6-digit trading PIN

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
    
    print("TOTP Login Response received...")
    
    # Check if login was successful - look for 'data' key
    if totp_login_response and "data" in totp_login_response:
        data = totp_login_response.get("data", {})
        status = data.get("status", "")
        
        if status == "success" or "token" in data:
            print("✓ TOTP Login Successful!")
            greeting_name = data.get("greetingName", "User")
            print(f"Welcome, {greeting_name}!")
        else:
            error = data.get("message", "Unknown error")
            print(f"✗ TOTP Login Failed: {error}")
            exit()
    elif "error" in totp_login_response:
        error_msg = totp_login_response.get("error", "Unknown error")
        print(f"✗ TOTP Login Failed: {error_msg}")
        exit()
    else:
        print(f"✗ Unexpected response format: {totp_login_response}")
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
    
    print("TOTP Validate Response received...")
    
    # Check if validation was successful
    if totp_validate_response and "data" in totp_validate_response:
        data = totp_validate_response.get("data", {})
        status = data.get("status", "")
        
        if status == "success" or "token" in data:
            trading_token = data.get("token", "")
            client_code = data.get("ucc", ucc)
            sid = data.get("sid", "")
            
            print(f"\n✓ Login Successful!")
            print(f"Client Code: {client_code}")
            print(f"Token received: {trading_token[:50]}...")  # Show first 50 chars
            
            # Save credentials to files for later use
            with open("kotak_trading_token.txt", 'w') as file:
                file.write(trading_token)
            with open("kotak_client_code.txt", 'w') as file:
                file.write(client_code)
            with open("kotak_consumer_key.txt", 'w') as file:
                file.write(consumer_key)
            with open("kotak_session_id.txt", 'w') as file:
                file.write(sid)
                
            print("\n✓ Credentials saved to files:")
            print("  - kotak_trading_token.txt")
            print("  - kotak_client_code.txt")
            print("  - kotak_consumer_key.txt")
            print("  - kotak_session_id.txt")
        else:
            error = data.get("message", "Unknown error")
            print(f"✗ TOTP Validation Failed: {error}")
            exit()
    elif "error" in totp_validate_response:
        error_msg = totp_validate_response.get("error", "Unknown error")
        print(f"✗ TOTP Validation Failed: {error_msg}")
        exit()
    else:
        print(f"✗ Unexpected response format: {totp_validate_response}")
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
print("\nYour credentials have been saved and can be reused for future sessions.")
