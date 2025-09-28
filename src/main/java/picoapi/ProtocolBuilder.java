package picoapi;

import java.util.ArrayList;
import java.util.List;

public class ProtocolBuilder {
    
    /**
     * Build protocol packet with default frame rate (100ms)
     */
    public static byte[] build(Object... inputs) {
        return build(100, inputs);
    }
    
    /**
     * Build protocol packet with specified frame rate
     * @param frameRateMs Frame rate in milliseconds (0-65535)
     * @param inputs CharPacket objects or arrays to include in the packet
     */
    public static byte[] build(int frameRateMs, Object... inputs) {
        List<Byte> result = new ArrayList<>();

        // Protocol header
        result.add((byte) 0xAA);
        result.add((byte) 0x55);

        List<Byte> body = new ArrayList<>();

        for (Object input : inputs) {
            if (input instanceof CharPacket) {
                for (byte b : ((CharPacket) input).toBytes()) {
                    body.add(b);
                }
            } else if (input instanceof CharPacket[]) {
                for (CharPacket cp : (CharPacket[]) input) {
                    for (byte b : cp.toBytes()) {
                        body.add(b);
                    }
                }
            } else {
                throw new IllegalArgumentException("Unsupported type: " + input.getClass());
            }
        }

        // Receiver bug: it treats bodyLen as fullLen (total packet length)
        // So we need to put total packet length in the 3rd byte
        int totalDataSize = 2 + body.size(); // 2 bytes for frame rate + data body  
        int fullPacketLength = 3 + totalDataSize; // header(2) + length(1) + data
        result.add((byte) fullPacketLength);

        // Add frame rate (2 bytes, high byte first)
        result.add((byte) ((frameRateMs >> 8) & 0xFF));
        result.add((byte) (frameRateMs & 0xFF));

        // Add data body
        result.addAll(body);

        // Convert to byte[]
        byte[] packet = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) {
            packet[i] = result.get(i);
        }
        return packet;
    }

}
