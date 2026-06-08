import { NeoAPI } from "../src";

async function main(): Promise<void> {
  const env = process.env.NEO_ENV || "uat";
  const consumer = process.env.NEO_CONSUMER || "";
  const mobile = process.env.NEO_MOBILE;
  const ucc = process.env.NEO_UCC;
  const totp = process.env.NEO_TOTP;
  const mpin = process.env.NEO_MPIN;

  const client = new NeoAPI({ environment: env, consumerKey: consumer });

  client.onOpen = (m) => console.log("[ws] open:", m);
  client.onMessage = (m) => console.log("[ws] msg:", m);
  client.onError = (e) => console.log("[ws] err:", e);
  client.onClose = (m) => console.log("[ws] close:", m);

  if (!mobile || !ucc || !totp || !mpin) {
    console.log("Set NEO_MOBILE, NEO_UCC, NEO_TOTP, NEO_MPIN to exercise login.");
    console.log("Client constructed OK; skipping live calls.");
    return;
  }

  console.log("totp_login:", await client.totpLogin(mobile, ucc, totp));
  console.log("totp_validate:", await client.totpValidate(mpin));
  console.log("order_report:", await client.orderReport());
}

main().catch(console.error);
