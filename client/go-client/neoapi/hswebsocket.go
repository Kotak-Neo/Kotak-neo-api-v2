package neoapi

import (
	"encoding/binary"
	"errors"
)

// DecodeBinaryFrame parses a single binary market-data packet from the HSM WebSocket.
//
// The Kotak HSM protocol is length-prefixed: a 1-byte packet count, followed by
// per-packet [ 2-byte length | payload ] blocks. Each payload starts with a type byte
// and encodes fields as [ field-id | 4-byte big-endian int | ... ] with some variable-width
// fields (strings, 64-bit timestamps). This port preserves the framing and returns the
// raw type + payload bytes so callers can continue decoding; the field-level mapping lives
// in StockKeyMapping / IndexKeyMapping above and is applied by the caller.
//
// Returns a slice of frames. Each frame is {type: int, payload: []byte}.
func DecodeBinaryFrame(buf []byte) ([]map[string]any, error) {
	if len(buf) < 1 {
		return nil, errors.New("empty frame")
	}
	count := int(buf[0])
	pos := 1
	out := make([]map[string]any, 0, count)
	for i := 0; i < count; i++ {
		if pos+2 > len(buf) {
			return nil, errors.New("truncated frame header")
		}
		pktLen := int(binary.BigEndian.Uint16(buf[pos : pos+2]))
		pos += 2
		if pos+pktLen > len(buf) {
			return nil, errors.New("truncated packet payload")
		}
		payload := buf[pos : pos+pktLen]
		pos += pktLen
		if len(payload) < 1 {
			continue
		}
		out = append(out, map[string]any{
			"packet_type": int(payload[0]),
			"payload":     payload[1:],
		})
	}
	return out, nil
}
