package com.cnlaunch.physics.utils;

public class DataTools {
    public static final int LEN_SHORT = 2;

    public static String getText(byte[] data, int index, int len){
        String ret = "";
        try {
            ret = new String(data, index , len, "UTF-8");
        } catch (Exception e) {

        }
        return ret;
    }

    public static short bigEndByteToShort(byte[] data, int index){
        short value = 0;
       // value = (short)( ((short)data[index++] << 8) + (short)data[index]);
        value |= ((data[index + 0] & 0xff) << 8) | (data[index + 1] & 0xff);
        return value;
    }

    public static int bigEndByteToInt(byte[] buf, int pos){
        int value = 0;
        //value = (data[index++] << 24) + (data[index++] << 16) + (data[index++] << 8) + data[index] & 0xFF;
        value |= ((buf[pos] & 0xff) << 24) | ((buf[pos + 1] & 0xff) << 16) | ((buf[pos + 2] & 0xff) << 8) | (buf[pos + 3] & 0xff);
        return value;
    }

    public static void bigEndIntToByte(byte[] buf, int pos, int value) {
        buf[pos++] = (byte)(value >> 24);
        buf[pos++] = (byte)(value >> 16);
        buf[pos++] = (byte)(value >> 8);
        buf[pos++] = (byte)(value & 0xFF);
    }

    public static long bigEndByteToLong(byte[] buf, int pos){
        long value = 0;
        value |= ((buf[pos] & 0xff) << 24) | ((buf[pos + 1] & 0xff) << 16) | ((buf[pos + 2] & 0xff) << 8) | (buf[pos + 3] & 0xff);
        return value;
    }

    public static void bigEndIntToByte(byte[] buf, int pos, long value) {
        buf[pos++] = (byte)(value >> 24);
        buf[pos++] = (byte)(value >> 16);
        buf[pos++] = (byte)(value >> 8);
        buf[pos++] = (byte)(value & 0xFF);
    }

    public static void bigEndShortToByte(byte[] data, int pos, short value) {
        data[pos++] = (byte) (value >> 8);
        data[pos++] = (byte) (value & 0xFF);
    }

    public static void bigEndSortToByte(byte[] buf, int pos, short value) {
        buf[pos++] = (byte)(value >> 8);
        buf[pos++] = (byte)(value & 0xFF);
    }

    public static String bytesToHexString(byte[] src, int offset, int length) {
        if(src == null) {
            return"";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = offset; i < (offset + length); i++) {
            int v = src[i] & 0xFF;
            stringBuilder.append(String.format("%02x", v));
        }
        return stringBuilder.toString();
    }

    public static int readbigu16(byte[] buf, int pos) {
        int data = 0x0;
        data |= ((buf[pos + 0] & 0xff) << 8) | (buf[pos + 1] & 0xff);
        return data;
    }

    public static void u32little(byte[] buf, int pos, long data) {
        buf[pos] = (byte) (data & 0xff);
        buf[pos + 1] = (byte) ((data >> 8) & 0xff);
        buf[pos + 2] = (byte) ((data >> 16) & 0xff);
        buf[pos + 3] = (byte) ((data >> 24) & 0xff);
    }

    public static int getRemoteCrcByDataLength(byte[] data, int dataLength){
        int code = 0x00;
        for (int i = 0; i < dataLength; ++i) {
            code ^= data[i] & 0xFF;
        }
        return code;
    }
    public static int parseShortFromArrayAsLittle(byte[] data, int pos) {
        return data[pos] & 0xFF | ((data[pos + 1] & 0xFF) << 8);
    }

    public static int getCrcByData(byte[] data, int pos, int dataLength){
        int code = 0x00;
        for (int i = pos; i < (pos + dataLength); ++i) {
            code ^= data[i] & 0xFF;
        }
        return code;
    }
}
