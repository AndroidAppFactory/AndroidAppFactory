package com.bihe0832.android.lib.utils.encypt;

public class HexUtils {

    private static final char[] digits = new char[]{'0', '1', '2', '3', '4',//
            '5', '6', '7', '8', '9',//
            'A', 'B', 'C', 'D', 'E',//
            'F'};

    public static final byte[] emptybytes = new byte[0];

    /**
     * 将单个字节转成Hex String
     *
     * @return String Hex String
     */
    public static String byte2HexStr(byte b) {
        char[] buf = new char[2];
        buf[1] = digits[b & 0xF];
        b = (byte) (b >>> 4);
        buf[0] = digits[b & 0xF];
        return new String(buf);
    }

    /**
     * 将字节数组转成Hex String
     *
     * @return String
     */
    public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        char[] buf = new char[2 * bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            buf[2 * i + 1] = digits[b & 0xF];
            b = (byte) (b >>> 4);
            buf[2 * i + 0] = digits[b & 0xF];
        }
        return new String(buf);
    }

    /**
     * 将单个hex Str转换成字节
     *
     * @return byte
     */
    public static byte hexStr2Byte(String str) {
        if (str != null && str.length() == 1) {
            return char2Byte(str.charAt(0));
        } else {
            return 0;
        }
    }

    /**
     * 字符到字节
     *
     * @return byte
     */
    public static byte char2Byte(char ch) {
        if (ch >= '0' && ch <= '9') {
            return (byte) (ch - '0');
        } else if (ch >= 'a' && ch <= 'f') {
            return (byte) (ch - 'a' + 10);
        } else if (ch >= 'A' && ch <= 'F') {
            return (byte) (ch - 'A' + 10);
        } else {
            return 0;
        }
    }

    public static byte[] hexStr2Bytes(String str) {
        if (str == null || str.equals("")) {
            return emptybytes;
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            char high = str.charAt(i * 2);
            char low = str.charAt(i * 2 + 1);
            bytes[i] = (byte) (char2Byte(high) * 16 + char2Byte(low));
        }
        return bytes;
    }

    public static String stringToHex(String str) {

        return bytes2HexStr(str.getBytes());
    }

    public static String hexToString(String hex) {
        return new String(hexStr2Bytes(hex));
    }
}
