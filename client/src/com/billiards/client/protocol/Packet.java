package com.billiards.client.protocol;

import java.io.*;

/** Copy Packet utility cho client */
public class Packet {
    public static void writeType(DataOutputStream out, PacketType type) throws IOException { out.writeByte(type.getCode()); }
    public static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8"); out.writeInt(bytes.length); out.write(bytes);
    }
    public static void writeBoolean(DataOutputStream out, boolean value) throws IOException { out.writeByte(value ? 1 : 0); }
    public static void writeInt(DataOutputStream out, int value) throws IOException { out.writeInt(value); }
    public static void writeDouble(DataOutputStream out, double value) throws IOException { out.writeDouble(value); }
    public static void writeLong(DataOutputStream out, long value) throws IOException { out.writeLong(value); }
    public static void flush(DataOutputStream out) throws IOException { out.flush(); }

    public static PacketType readType(DataInputStream in) throws IOException { return PacketType.fromByte(in.readByte()); }
    public static String readString(DataInputStream in) throws IOException {
        int len = in.readInt(); byte[] bytes = new byte[len]; in.readFully(bytes); return new String(bytes, "UTF-8");
    }
    public static boolean readBoolean(DataInputStream in) throws IOException { return in.readByte() != 0; }
    public static int readInt(DataInputStream in) throws IOException { return in.readInt(); }
    public static double readDouble(DataInputStream in) throws IOException { return in.readDouble(); }
    public static long readLong(DataInputStream in) throws IOException { return in.readLong(); }
}
