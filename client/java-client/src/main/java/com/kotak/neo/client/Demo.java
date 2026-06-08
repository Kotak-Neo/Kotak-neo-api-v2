package com.kotak.neo.client;

public class Demo {
    public static void main(String[] args) {
        String env = System.getenv().getOrDefault("NEO_ENV", "uat");
        String consumer = System.getenv().getOrDefault("NEO_CONSUMER", "");
        String mobile = System.getenv("NEO_MOBILE");
        String ucc = System.getenv("NEO_UCC");
        String totp = System.getenv("NEO_TOTP");
        String mpin = System.getenv("NEO_MPIN");

        NeoAPI client = new NeoAPI(env, null, null, consumer);

        client.onOpen = m -> System.out.println("[ws] open : " + m);
        client.onMessage = m -> System.out.println("[ws] msg  : " + m);
        client.onError = e -> System.out.println("[ws] err  : " + e.getMessage());
        client.onClose = m -> System.out.println("[ws] close: " + m);

        if (mobile == null || ucc == null || totp == null || mpin == null) {
            System.out.println("Set NEO_MOBILE, NEO_UCC, NEO_TOTP, NEO_MPIN to exercise login.");
            System.out.println("Client constructed OK; skipping live calls.");
            return;
        }

        System.out.println("totp_login: " + client.totpLogin(mobile, ucc, totp));
        System.out.println("totp_validate: " + client.totpValidate(mpin));
        System.out.println("order_report: " + client.orderReport());
    }
}
