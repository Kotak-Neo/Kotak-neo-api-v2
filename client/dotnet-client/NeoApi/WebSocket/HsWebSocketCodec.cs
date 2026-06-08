using System.Buffers.Binary;

namespace Kotak.Neo.WebSocket;

public record DecodedFrame(int PacketType, byte[] Payload);

public static class HsWebSocketCodec
{
    /// <summary>
    /// Parses a binary market-data frame (packet-count prefixed, length-delimited).
    /// </summary>
    public static List<DecodedFrame> Decode(byte[] buf)
    {
        var result = new List<DecodedFrame>();
        if (buf.Length < 1) return result;
        int count = buf[0];
        int pos = 1;
        for (int i = 0; i < count; i++)
        {
            if (pos + 2 > buf.Length) break;
            int len = BinaryPrimitives.ReadUInt16BigEndian(buf.AsSpan(pos, 2));
            pos += 2;
            if (pos + len > buf.Length) break;
            if (len < 1) { pos += len; continue; }
            int type = buf[pos];
            var payload = new byte[len - 1];
            Buffer.BlockCopy(buf, pos + 1, payload, 0, len - 1);
            pos += len;
            result.Add(new DecodedFrame(type, payload));
        }
        return result;
    }
}
