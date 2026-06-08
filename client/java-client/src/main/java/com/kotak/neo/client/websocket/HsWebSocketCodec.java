package com.kotak.neo.client.websocket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary frame parser for the HSM market-data WebSocket.
 * Matches the structural port in the Go / .NET / TypeScript clients.
 */
public final class HsWebSocketCodec {
    private HsWebSocketCodec() {}

    public static class Frame {
        public int packetType;
        public byte[] payload;
    }

    public static List<Frame> decode(byte[] buf) {
        List<Frame> out = new ArrayList<>();
        if (buf == null || buf.length < 1) return out;
        int count = buf[0] & 0xFF;
        int pos = 1;
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < count; i++) {
            if (pos + 2 > buf.length) break;
            bb.position(pos);
            int len = bb.getShort() & 0xFFFF;
            pos += 2;
            if (pos + len > buf.length) break;
            if (len < 1) { pos += len; continue; }
            Frame f = new Frame();
            f.packetType = buf[pos] & 0xFF;
            f.payload = new byte[len - 1];
            System.arraycopy(buf, pos + 1, f.payload, 0, len - 1);
            out.add(f);
            pos += len;
        }
        return out;
    }
}
