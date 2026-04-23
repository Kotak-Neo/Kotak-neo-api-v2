using Kotak.Neo;

string env = Environment.GetEnvironmentVariable("NEO_ENV") ?? "uat";
string consumer = Environment.GetEnvironmentVariable("NEO_CONSUMER") ?? "";
string? mobile = Environment.GetEnvironmentVariable("NEO_MOBILE");
string? ucc = Environment.GetEnvironmentVariable("NEO_UCC");
string? totp = Environment.GetEnvironmentVariable("NEO_TOTP");
string? mpin = Environment.GetEnvironmentVariable("NEO_MPIN");

using var client = new NeoApi(environment: env, consumerKey: consumer);

client.OnOpen += m => Console.WriteLine($"[ws] open : {m}");
client.OnMessage += m => Console.WriteLine($"[ws] msg  : {m}");
client.OnError += e => Console.WriteLine($"[ws] err  : {e.Message}");
client.OnClose += m => Console.WriteLine($"[ws] close: {m}");

if (string.IsNullOrEmpty(mobile) || string.IsNullOrEmpty(ucc) || string.IsNullOrEmpty(totp) || string.IsNullOrEmpty(mpin))
{
    Console.WriteLine("Set NEO_MOBILE, NEO_UCC, NEO_TOTP, NEO_MPIN to exercise login.");
    Console.WriteLine("Client constructed OK; skipping live calls.");
    return;
}

Console.WriteLine("totp_login: " + await client.TotpLoginAsync(mobile, ucc, totp));
Console.WriteLine("totp_validate: " + await client.TotpValidateAsync(mpin));
Console.WriteLine("order_report: " + await client.OrderReportAsync());
