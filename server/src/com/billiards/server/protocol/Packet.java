package com.billiards.server.protocol;

import java.io.*;

/**
 * Lớp đóng gói dữ liệu gửi/nhận qua mạng.
 * 
 * Định dạng gói tin: [1 byte: PacketType] [... data ...]
 * 
 * Sử dụng DataInputStream / DataOutputStream để đọc/ghi dữ liệu.
 * Tất cả dữ liệu dạng big-endian (chuẩn Java).
 */
public class Packet {

    // ============================================================
    // WRITER – Gửi gói tin ra DataOutputStream
    // ============================================================

    /** Ghi header: type của gói tin */
    public static void writeType(DataOutputStream out, PacketType type) throws IOException {
        out.writeByte(type.getCode());
    }

    public static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static void writeBoolean(DataOutputStream out, boolean value) throws IOException {
        out.writeByte(value ? 1 : 0);
    }

    public static void writeInt(DataOutputStream out, int value) throws IOException {
        out.writeInt(value);
    }

    public static void writeDouble(DataOutputStream out, double value) throws IOException {
        out.writeDouble(value);
    }

    public static void writeLong(DataOutputStream out, long value) throws IOException {
        out.writeLong(value);
    }

    public static void flush(DataOutputStream out) throws IOException {
        out.flush();
    }

    // ============================================================
    // READER – Đọc gói tin từ DataInputStream
    // ============================================================

    /** Đọc 1 byte type từ stream, trả về PacketType tương ứng */
    public static PacketType readType(DataInputStream in) throws IOException {
        byte b = in.readByte();
        return PacketType.fromByte(b);
    }

    public static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > 65536) throw new IOException("Invalid string length: " + length);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    public static boolean readBoolean(DataInputStream in) throws IOException {
        return in.readByte() != 0;
    }

    public static int readInt(DataInputStream in) throws IOException {
        return in.readInt();
    }

    public static double readDouble(DataInputStream in) throws IOException {
        return in.readDouble();
    }

    public static long readLong(DataInputStream in) throws IOException {
        return in.readLong();
    }
}
