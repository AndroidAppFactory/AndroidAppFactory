package com.bihe0832.android.lib.utils.encrypt;

import java.io.File;
import java.io.InputStream;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/8/15.
 * Description: Description
 */
public class SHA256 {
    public static final String MESSAGE_DIGEST_TYPE_SHA256 = "SHA-256";

    public static String getSHA256(String string) {
        return MessageDigestUtils.getDigestData(string, MESSAGE_DIGEST_TYPE_SHA256);
    }

    public static String getSHA256(byte[] bytes) {
        return MessageDigestUtils.getDigestData(bytes, MESSAGE_DIGEST_TYPE_SHA256);
    }

    public static String getFileSHA256(String fileName) {
        return MessageDigestUtils.getFileDigestData(fileName, MESSAGE_DIGEST_TYPE_SHA256);
    }

    public static String getFileSHA256(File sourceFile) {
        return MessageDigestUtils.getFileDigestData(sourceFile, MESSAGE_DIGEST_TYPE_SHA256);
    }

    public static String getInputStreamSHA256(InputStream is) {
        return MessageDigestUtils.getInputStreamDigestData(is, MESSAGE_DIGEST_TYPE_SHA256);
    }

    public static String getFileSHA256(String fileName, long start, long end) {
        return MessageDigestUtils.getFileDigestData(fileName, MESSAGE_DIGEST_TYPE_SHA256, start, end);
    }

    public static String getFileSHA256(File sourceFile, long start, long end) {
        return MessageDigestUtils.getFileDigestData(sourceFile, MESSAGE_DIGEST_TYPE_SHA256, start, end);
    }

    public static String getInputStreamSHA256(InputStream bis, long start, long end) {
        return MessageDigestUtils.getInputStreamDigestData(bis, MESSAGE_DIGEST_TYPE_SHA256, start, end);
    }
}
