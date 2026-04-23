// Binary frame parser for the HSM market-data WebSocket.
// Mirrors the structural port in the Go client's hswebsocket.go.

export interface DecodedFrame {
  packet_type: number;
  payload: Buffer;
}

export function decodeBinaryFrame(buf: Buffer): DecodedFrame[] {
  if (buf.length < 1) return [];
  const count = buf.readUInt8(0);
  let pos = 1;
  const out: DecodedFrame[] = [];
  for (let i = 0; i < count; i++) {
    if (pos + 2 > buf.length) break;
    const pktLen = buf.readUInt16BE(pos);
    pos += 2;
    if (pos + pktLen > buf.length) break;
    const payload = buf.slice(pos, pos + pktLen);
    pos += pktLen;
    if (payload.length < 1) continue;
    out.push({ packet_type: payload.readUInt8(0), payload: payload.slice(1) });
  }
  return out;
}
