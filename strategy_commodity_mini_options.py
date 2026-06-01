#DISCLAIMER:
#1) This sample code is for learning purposes only.
#2) Always be very careful when dealing with codes in which you can place orders in your account.
#3) The actual results may or may not be similar to backtested results. The historical results do not guarantee any profits or losses in the future.
#4) You are responsible for any losses/profits that occur in your account in case you plan to take trades in your account.
#5) TFU and Aseem Singhal do not take any responsibility of you running these codes on your account and the corresponding profits and losses that might occur.
#6) The running of the code properly is dependent on a lot of factors such as internet, broker, what changes you have made, etc. So it is always better to keep checking the trades as technology error can come anytime.
#7) This is NOT a tip providing service/code.
#8) This is NOT a software. Its a tool that works as per the inputs given by you.
#9) Slippage is dependent on market conditions.
#10) Commodity and automatic API trading are subject to market risks

import datetime
import time
import pandas as pd
import os
import json
import sys
from neo_api_client import NeoAPI

def print_banner():
    """Print an enhanced welcome banner"""
    print("\n" + "="*90)
    print("🚀" + " "*10 + "KOTAK NEO MINI COMMODITY OPTIONS STRATEGY" + " "*10 + "🚀")
    print("="*90)
    print("⚠️  Educational Purpose - Please Trade Responsibly")
    print("="*90 + "\n")

def print_section(title, emoji="📊"):
    """Print enhanced section headers"""
    print(f"\n{emoji} {title}")
    print("─" * (len(title) + 4))

def print_config():
    """Display current configuration with enhanced formatting"""
    print_section("TRADING CONFIGURATION", "⚙️")

    print(f"  🏆 Commodity          : {commodity}")
    print(f"  📍 Strike Type        : {strike_type.upper()}")
    print(f"  ⏰ Entry Time         : {startTime}")
    print(f"  🎯 Trade Based On     : {trade_based_on.upper()}")
    print(f"  🛑 SL Based On        : {sl_based_on.upper()}")

    if sl_based_on == 'point':
        print(f"  🛑 SL Points          : {SL_point} points")
        print(f"  🎯 Target Points      : {target_point} points")
    else:
        print(f"  🛑 SL Percentage      : {SL_percentage}%")
        print(f"  🎯 Target Percentage  : {target_percentage}%")

    print(f"  📦 Quantity           : {qty} qty")
    print(f"  📝 Paper Trading      : {'✅ YES' if papertrading == 0 else '❌ NO (LIVE)'}")
    print(f"  📋 Product Type       : {producttpye}")
    print(f"  🏦 Broker             : KOTAK NEO")

    if trade_based_on == "premium":
        print(f"  💰 Premium Target     : ₹{premium}")

    if for_every_x_point > 0:
        print(f"  📈 Trailing SL        : Every {for_every_x_point} pts trail by {trail_by_y_point} pts")

def print_trade_alert(message, alert_type="info"):
    """Print formatted trade alerts with emojis"""
    emoji_map = {
        "buy": "🟢",
        "sell": "🔴",
        "exit": "🚪",
        "target": "🎯",
        "stop": "🛑",
        "info": "ℹ️",
        "warning": "⚠️",
        "success": "✅",
        "error": "❌",
        "money": "💰",
        "time": "⏰",
        "crude": "🛢️",
        "gold": "🥇",
        "silver": "🥈"
    }
    emoji = emoji_map.get(alert_type, "ℹ️")
    timestamp = datetime.datetime.now().strftime('%H:%M:%S')
    print(f"{emoji} [{timestamp}] {message}")

def initializeKotakAPI():
    """Initialize Kotak Neo API client"""
    print_section("INITIALIZING KOTAK NEO API", "🔌")
    
    try:
        # Read stored credentials
        with open("kotak_consumer_key.txt", 'r') as f:
            consumer_key = f.read().strip()
        with open("kotak_trading_token.txt", 'r') as f:
            trading_token = f.read().strip()
        
        # Initialize client
        client = NeoAPI(environment='prod', consumer_key=consumer_key)
        print_trade_alert("Kotak Neo API client initialized", "success")
        
        return client, trading_token
        
    except FileNotFoundError as e:
        print_trade_alert(f"Error: Missing credential file - {e}", "error")
        print_trade_alert("Run kotak_neo_login.py first to generate credentials", "warning")
        sys.exit()
    except Exception as e:
        print_trade_alert(f"Error initializing Kotak API: {e}", "error")
        sys.exit()

def getCommoditySpot():
    """Get commodity spot symbol based on selection"""
    commodity_map = {
        "CRUDEOIL": "CRUDEOILM",      # Crude Oil Mini
        "GOLD": "GOLDM",               # Gold Mini
        "SILVER": "SILVERM",           # Silver Mini
        "COPPER": "COPPERM",           # Copper Mini
        "NATURALGASM": "NATURALGASM",  # Natural Gas Mini
        "LEAD": "LEADM",               # Lead Mini
        "ZINC": "ZINCM",               # Zinc Mini
        "NICKEL": "NICKELM"            # Nickel Mini
    }
    return commodity_map.get(commodity, commodity)

def getCommodityExpiry():
    """Get commodity expiry date (next month contract)"""
    today = datetime.date.today()
    
    # For commodities, expiry is typically on specific dates
    # Using next month's contract
    if today.month == 12:
        expiry_month = 1
        expiry_year = today.year + 1
    else:
        expiry_month = today.month + 1
        expiry_year = today.year
    
    # Approximate expiry date (usually mid-month or end of month)
    expiry_date = datetime.date(expiry_year, expiry_month, 15)
    
    return expiry_date.strftime('%d%b%y').upper()

def getCommodityOptionFormat(commodity, expiry, strike, option_type):
    """Format commodity option symbol for Kotak Neo"""
    # Format: CRUDEOILM15JUN24C6500
    return f"{commodity}{expiry}{option_type}{strike}"

def getQuotes(symbol, client):
    """Get current LTP for a symbol"""
    try:
        # Get quotes from Kotak Neo API
        quotes_resp = client.quotes(
            instrument_tokens=[{"instrument_token": symbol, "exchange_segment": "mcx_fo"}],
            quote_type="ltp"
        )
        
        if quotes_resp and "data" in quotes_resp:
            data = quotes_resp["data"]
            if data and "ltp" in data:
                return float(data["ltp"])
        
        print_trade_alert(f"Could not fetch LTP for {symbol}", "warning")
        return -1
        
    except Exception as e:
        print_trade_alert(f"Error fetching quotes for {symbol}: {e}", "error")
        return -1

def manualLTP(symbol, client):
    """Get LTP manually from Kotak Neo"""
    try:
        return getQuotes(symbol, client)
    except Exception as e:
        print_trade_alert(f"Error getting manual LTP: {e}", "error")
        return -1

def getCommodityStrikeLevels():
    """Get strike levels based on commodity type"""
    strike_levels = {
        "CRUDEOIL": {"step": 100, "range": 10},   # 100 point steps
        "GOLD": {"step": 100, "range": 10},       # 100 point steps
        "SILVER": {"step": 50, "range": 10},      # 50 point steps
        "COPPER": {"step": 50, "range": 10},      # 50 point steps
        "NATURALGASM": {"step": 10, "range": 10}, # 10 point steps
        "LEAD": {"step": 25, "range": 10},        # 25 point steps
        "ZINC": {"step": 25, "range": 10},        # 25 point steps
        "NICKEL": {"step": 50, "range": 10}       # 50 point steps
    }
    return strike_levels.get(commodity, {"step": 100, "range": 10})

def findStrikePriceATM(client):
    """Find ATM strike prices for commodity"""
    emoji_map = {"CRUDEOIL": "🛢️", "GOLD": "🥇", "SILVER": "🥈"}
    emoji = emoji_map.get(commodity, "📊")
    
    print_section(f"FINDING ATM STRIKE PRICES - {commodity}", emoji)
    
    # Get current commodity LTP
    commodity_spot = getCommoditySpot()
    ltp = getQuotes(commodity_spot, client)
    
    if ltp == -1:
        print_trade_alert(f"Could not fetch {commodity} spot price. Exiting...", "error")
        sys.exit()
    
    print_trade_alert(f"Current {commodity} LTP: ₹{ltp:,.2f}", "info")

    # Get strike levels
    strike_config = getCommodityStrikeLevels()
    step = strike_config["step"]
    
    # Calculate ATM strike
    closest_Strike = int(round(ltp / step, 0) * step)
    
    print_trade_alert(f"ATM Strike calculated: {closest_Strike}", "success")

    closest_Strike_CE = closest_Strike + otm
    closest_Strike_PE = closest_Strike - otm

    print_trade_alert(f"Call Strike ({otm} OTM): {closest_Strike_CE}", "info")
    print_trade_alert(f"Put Strike ({otm} OTM): {closest_Strike_PE}", "info")

    # Get expiry date
    intExpiry = getCommodityExpiry()

    # Get option symbols
    atmCE = getCommodityOptionFormat(commodity, intExpiry, closest_Strike_CE, "C")
    atmPE = getCommodityOptionFormat(commodity, intExpiry, closest_Strike_PE, "P")

    print_trade_alert(f"Call Symbol: {atmCE}", "success")
    print_trade_alert(f"Put Symbol: {atmPE}", "success")

    takeEntry(closest_Strike_CE, closest_Strike_PE, atmCE, atmPE, client)

def findStrikePricePremium(client):
    """Find strikes based on premium"""
    emoji_map = {"CRUDEOIL": "🛢️", "GOLD": "🥇", "SILVER": "🥈"}
    emoji = emoji_map.get(commodity, "📊")
    
    print_section(f"FINDING STRIKES BY PREMIUM - {commodity}", emoji)

    commodity_spot = getCommoditySpot()
    strikeList = []

    ltp = getQuotes(commodity_spot, client)
    if ltp == -1:
        print_trade_alert(f"Could not fetch {commodity} spot price. Exiting...", "error")
        sys.exit()

    print_trade_alert(f"Current {commodity} LTP: ₹{ltp:,.2f}", "info")
    print_trade_alert(f"Target Premium: ₹{premium}", "money")

    # Get expiry date
    intExpiry = getCommodityExpiry()
    
    # Get strike levels
    strike_config = getCommodityStrikeLevels()
    step = strike_config["step"]
    range_count = strike_config["range"]

    # Generate strike list
    base_strike = int(ltp / step) * step
    for i in range(-range_count, range_count + 1):
        strike = base_strike + (i * step)
        if strike > 0:
            strikeList.append(strike)

    print_trade_alert(f"Scanning {len(strikeList)} strike prices...", "info")

    # FOR CALLS
    print_section("SCANNING CALL OPTIONS", "🔍")
    prev_diff = 10000
    closest_Strike_CE = strikeList[0]
    
    for strike in strikeList:
        symbol = getCommodityOptionFormat(commodity, intExpiry, strike, "C")
        ltp_option = manualLTP(symbol, client)
        if ltp_option > 0:
            diff = abs(ltp_option - premium)
            print(f"    Strike {strike}: ₹{ltp_option:.1f} (diff: ₹{diff:.1f})")
            if (diff < prev_diff):
                closest_Strike_CE = strike
                prev_diff = diff
        time.sleep(0.5)
    
    print_trade_alert(f"Selected Call Strike: {closest_Strike_CE}", "success")

    # FOR PUTS
    print_section("SCANNING PUT OPTIONS", "🔍")
    prev_diff = 10000
    closest_Strike_PE = strikeList[0]
    
    for strike in strikeList:
        symbol = getCommodityOptionFormat(commodity, intExpiry, strike, "P")
        ltp_option = manualLTP(symbol, client)
        if ltp_option > 0:
            diff = abs(ltp_option - premium)
            print(f"    Strike {strike}: ₹{ltp_option:.1f} (diff: ₹{diff:.1f})")
            if (diff < prev_diff):
                closest_Strike_PE = strike
                prev_diff = diff
        time.sleep(0.5)

    print_trade_alert(f"Selected Put Strike: {closest_Strike_PE}", "success")

    atmCE = getCommodityOptionFormat(commodity, intExpiry, closest_Strike_CE, "C")
    atmPE = getCommodityOptionFormat(commodity, intExpiry, closest_Strike_PE, "P")

    print_trade_alert(f"Final Call Symbol: {atmCE}", "success")
    print_trade_alert(f"Final Put Symbol: {atmPE}", "success")

    takeEntry(closest_Strike_CE, closest_Strike_PE, atmCE, atmPE, client)

def takeEntry(closest_Strike_CE, closest_Strike_PE, atmCE, atmPE, client):
    """Execute entry orders"""
    global PnL
    print_section("TRADE ENTRY EXECUTION", "⚡")

    ce_entry_price = manualLTP(atmCE, client)
    pe_entry_price = manualLTP(atmPE, client)
    PnL = ce_entry_price + pe_entry_price

    print_trade_alert(f"Call Entry Price: ₹{ce_entry_price:.2f}", "money")
    print_trade_alert(f"Put Entry Price: ₹{pe_entry_price:.2f}", "money")
    print_trade_alert(f"Total Premium Received: ₹{PnL:.2f}", "success")

    df['CE_Entry_Price'] = [ce_entry_price]
    df['PE_Entry_Price'] = [pe_entry_price]

    if sl_based_on == "point":
        ceSL = round(ce_entry_price + SL_point, 1)
        peSL = round(pe_entry_price + SL_point, 1)
        ceTarget = round(ce_entry_price - target_point, 1)
        peTarget = round(pe_entry_price - target_point, 1)
    else:
        ceSL = round(ce_entry_price * (1 + SL_percentage / 100), 1)
        peSL = round(pe_entry_price * (1 + SL_percentage / 100), 1)
        ceTarget = round(ce_entry_price * (1 - target_percentage / 100), 1)
        peTarget = round(pe_entry_price * (1 - target_percentage / 100), 1)

    print_section("ORDER LEVELS", "📋")
    print(f"    🛑 Call Stop Loss: ₹{ceSL:.1f}")
    print(f"    🎯 Call Target: ₹{ceTarget:.1f}")
    print(f"    🛑 Put Stop Loss: ₹{peSL:.1f}")
    print(f"    🎯 Put Target: ₹{peTarget:.1f}")

    # SELL AT MARKET PRICE
    print_section("PLACING ORDERS", "📤")
    oidentryCE = placeOrder1(atmCE, "SELL", qty, "MKT", ce_entry_price, "regular", papertrading, producttpye, client)
    oidentryPE = placeOrder1(atmPE, "SELL", qty, "MKT", pe_entry_price, "regular", papertrading, producttpye, client)

    print_trade_alert(f"Call Order placed - ID: {oidentryCE}", "info")
    print_trade_alert(f"Put Order placed - ID: {oidentryPE}", "info")

    exitPosition(atmCE, ceSL, ceTarget, ce_entry_price, atmPE, peSL, peTarget, pe_entry_price, qty, client)

def exitPosition(atmCE, ceSL, ceTarget, ce_entry_price, atmPE, peSL, peTarget, pe_entry_price, qty, client):
    """Monitor and exit positions"""
    global PnL
    print_section("POSITION MONITORING", "👁️")

    traded = "No"
    originalEntryCE = ce_entry_price
    originalEntryPE = pe_entry_price
    ce_exit_done = False
    pe_exit_done = False
    print_trade_alert("Starting real-time position monitoring...", "info")

    while traded == "No":
        dt = datetime.datetime.now()
        try:
            ltp = manualLTP(atmCE, client)
            ltp1 = manualLTP(atmPE, client)

            if ltp == -1 or ltp1 == -1:
                time.sleep(1)
                continue

            time_str = dt.strftime('%H:%M:%S')
            print(f"\r  [{time_str}] Call: ₹{ltp:.1f} (SL: ₹{ceSL:.1f}) | Put: ₹{ltp1:.1f} (SL: ₹{peSL:.1f}) | Temp PnL: ₹{PnL:.1f}", end="", flush=True)

            # Call Exit Logic
            if ((ltp > ceSL) or (ltp < ceTarget) or (dt.hour >= 23 and dt.minute >= 30)) and ce_exit_done == False:
                print(f"\n\n🚨 CALL EXIT TRIGGERED!")
                if ltp > ceSL:
                    print_trade_alert("Call Stop Loss Hit!", "stop")
                elif ltp < ceTarget:
                    print_trade_alert("Call Target Achieved!", "target")
                else:
                    print_trade_alert("End of Day Exit - Call", "time")

                oidexitCE = placeOrder1(atmCE, "BUY", qty, "MKT", ltp, "regular", papertrading, producttpye, client)
                PnL = PnL - ltp
                print("Current PnL is: ", PnL)
                df["CE_Exit_Price"] = [ltp]
                print("The OID of Exit Call is: ", oidexitCE)
                ce_exit_done = True

            # Put Exit Logic
            if ((ltp1 > peSL) or (ltp1 < peTarget) or (dt.hour >= 23 and dt.minute >= 30)) and pe_exit_done == False:
                print(f"\n\n🚨 PUT EXIT TRIGGERED!")
                if ltp1 > peSL:
                    print_trade_alert("Put Stop Loss Hit!", "stop")
                elif ltp1 < peTarget:
                    print_trade_alert("Put Target Achieved!", "target")
                else:
                    print_trade_alert("End of Day Exit - Put", "time")
                
                oidexitPE = placeOrder1(atmPE, "BUY", qty, "MKT", ltp1, "regular", papertrading, producttpye, client)
                PnL = PnL - ltp1
                print("Current PnL is: ", PnL)
                df["PE_Exit_Price"] = [ltp1]
                print("The OID of Exit Put is: ", oidexitPE)
                pe_exit_done = True

            # Trail SL Call
            if ltp < originalEntryCE - for_every_x_point:
                originalEntryCE = originalEntryCE - for_every_x_point
                ceSL = ceSL - trail_by_y_point

            # Trail SL Put
            if ltp1 < originalEntryPE - for_every_x_point:
                originalEntryPE = originalEntryPE - for_every_x_point
                peSL = peSL - trail_by_y_point

            # Exit condition
            if ce_exit_done == True and pe_exit_done == True:
                print(f"\n\n✅ BOTH POSITIONS CLOSED SUCCESSFULLY")
                print_trade_alert(f"Final PnL: ₹{PnL:.2f}", "success")
                break

            time.sleep(1)

        except Exception as e:
            print_trade_alert(f"Error in position monitoring: {e}", "warning")
            time.sleep(1)

def placeOrder1(inst, t_type, qty, order_type, price, variety, papertrading=0, producttype="intraday_fno", client=None):
    """Place order through Kotak Neo API"""
    try:
        ddate = datetime.datetime.now().strftime('%Y-%m-%d')
        dtime = datetime.datetime.now().strftime('%H:%M:%S')
        trade_log = f"{ddate},{dtime},{inst},{t_type},{qty},{order_type},{price},{variety},{papertrading},{producttype}\n"
        
        with open("commodity_options_results.txt", "a") as f:
            f.write(trade_log)
        
        order_emoji = "🔴" if t_type == "SELL" else "🟢"
        print_trade_alert(f"{order_emoji} Order: {inst} {t_type} {qty} @ ₹{price:.2f}", "success")
        
    except Exception as e:
        print_trade_alert(f"Error logging trade: {e}", "warning")

    if papertrading == 0:
        return 0
    
    # Place actual order via Kotak Neo API
    try:
        if client:
            order_response = client.place_order(
                exchange_segment="mcx_fo",
                product=producttype.split('_')[0].upper(),
                price=str(int(price)),
                order_type="MKT" if order_type == "MKT" else "L",
                quantity=str(qty),
                validity="DAY",
                trading_symbol=inst,
                transaction_type=t_type,
                amo="NO"
            )
            
            if order_response and "data" in order_response:
                return order_response["data"].get("order_id", 0)
        
        return 0
    except Exception as e:
        print_trade_alert(f"Error placing order: {e}", "error")
        return 0

def checkTime_tofindStrike(client):
    """Wait for entry time"""
    print_section("WAITING FOR ENTRY TIME", "⏰")

    x = 1
    while x == 1:
        dt = datetime.datetime.now()
        if (dt.time() >= startTime):
            print_trade_alert(f"Entry time reached: {dt.strftime('%H:%M:%S')}", "success")
            x = 2
            if trade_based_on == "premium":
                findStrikePricePremium(client)
            else:
                findStrikePriceATM(client)
        else:
            time_diff = datetime.datetime.combine(datetime.date.today(), startTime) - dt
            remaining = str(time_diff).split('.')[0]

            print(f"\r  ⏰ Current: {dt.strftime('%H:%M:%S')} | Entry: {startTime} | Remaining: {remaining}", end="", flush=True)
            time.sleep(1)

def print_final_summary():
    """Print final trading summary"""
    print_section("TRADING SESSION SUMMARY", "📊")

    emoji_map = {"CRUDEOIL": "🛢️", "GOLD": "🥇", "SILVER": "🥈"}
    emoji = emoji_map.get(commodity, "📊")
    
    print(f"    {emoji} Commodity: {commodity}")
    print(f"    📅 Date: {datetime.date.today()}")
    print(f"    🎯 Strategy: {trade_based_on.upper()}")
    print(f"    💰 Total PnL: ₹{PnL:.2f}")
    print(f"    📝 Paper Trading: {'Yes' if papertrading == 0 else 'No'}")
    print(f"    🏦 Broker: KOTAK NEO")

####################__INPUT__#####################
# TIME TO FIND THE STRIKE
entryHour   = 9
entryMinute = 15
entrySecond = 0
startTime = datetime.time(entryHour, entryMinute, entrySecond)

# COMMODITY SELECTION
# Options: "CRUDEOIL", "GOLD", "SILVER", "COPPER", "NATURALGASM", "LEAD", "ZINC", "NICKEL"
commodity = "CRUDEOIL"  # Mini contracts
strike_type = "ATM"  # ATM or CUSTOM
otm = 100  # OTM points (adjust based on commodity volatility)
SL_point = 50
target_point = 50
SL_percentage = 5
target_percentage = 10
for_every_x_point = 50
trail_by_y_point = 10
PnL = 0
premium = 50  # Target premium for premium-based strategy
trade_based_on = "atm"  # "premium" or "atm"
sl_based_on = "point"  # "point" or "percent"
producttpye = "intraday_fno"  # "intraday_eq","positional_eq","intraday_fno","positional_fno"
df = pd.DataFrame(columns=['Date', 'CE_Entry_Price', 'CE_Exit_Price', 'PE_Entry_Price', 'PE_Exit_Price', 'PnL'])
df["Date"] = [datetime.date.today()]
qty = 10  # Adjust based on commodity contract size
papertrading = 0  # If paper trading is 0, then paper trading will be done. If paper trading is 1, then live trade

##################################################

def main():
    """Main function to start the trading bot"""
    global PnL

    print_banner()
    
    # Initialize Kotak Neo API
    print_section("CONNECTING TO KOTAK NEO", "🔗")
    client, trading_token = initializeKotakAPI()
    
    print_config()

    # Start the trading process
    checkTime_tofindStrike(client)

    # Save final results
    df["PnL"] = [PnL]

    # Save results to CSV
    df.to_csv('commodity_options_template.csv', mode='a', index=True, header=True)

    print_final_summary()
    print_trade_alert("Results saved to commodity_options_template.csv", "success")
    print("="*90)


if __name__ == "__main__":
    main()
