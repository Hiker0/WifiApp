package com.alllen.mylibrary.utils;

/**
 * Created by Johnson on 2017-04-14.
 */

public class Utils {

    static public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    static void byteMerge(byte[] tar, int tarstar, byte[] src, int srcstar, int len) {
        for (int i = 0; i < len; i++) {
            tar[tarstar + i] = src[srcstar + i];
        }
    }

    public static void putShort(byte b[], short s, int index) {
        b[index + 1] = (byte) (s >> 8);
        b[index + 0] = (byte) (s >> 0);
    }

    public static short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
    }
}
