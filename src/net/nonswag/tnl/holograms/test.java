package net.nonswag.tnl.holograms;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class test {

    public static void main(String[] args) throws IOException {
        String address = "thenextlvl.net";
        int port = 25565;
        InetSocketAddress host = new InetSocketAddress(address, port);
        Socket socket = new Socket();
        socket.connect(host, 3000);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        byte[] handshakeMessage = createHandshakeMessage(address, port);
        writeVarInt(output, handshakeMessage.length);
        output.write(handshakeMessage);
        output.writeByte(0x01);
        output.writeByte(0x00);
        int size = readVarInt(input);
        int packetId = readVarInt(input);
        if (packetId == -1) {
            throw new IOException("Premature end of stream.");
        }
        if (packetId != 0x00) {
            throw new IOException("Invalid packetID");
        }
        int length = readVarInt(input);
        if (length == -1) {
            throw new IOException("Premature end of stream.");
        }
        if (length == 0) {
            throw new IOException("Invalid string length.");
        }
        byte[] in = new byte[length];
        input.readFully(in);
        String json = new String(in);
        long now = System.currentTimeMillis();
        output.writeByte(0x09);
        output.writeByte(0x01);
        output.writeLong(now);
        readVarInt(input);
        packetId = readVarInt(input);
        if (packetId == -1) {
            throw new IOException("Premature end of stream.");
        }
        if (packetId != 0x01) {
            throw new IOException("Invalid packetID");
        }
        long pingTime = input.readLong();
        System.out.println(json);
    }

    public static byte[] createHandshakeMessage(String host, int port) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(buffer);
        handshake.writeByte(0x00);
        writeVarInt(handshake, -1);
        writeString(handshake, host, Charset.defaultCharset());
        handshake.writeShort(port);
        writeVarInt(handshake, 1);
        return buffer.toByteArray();
    }

    public static void writeString(DataOutputStream out, String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }
}
