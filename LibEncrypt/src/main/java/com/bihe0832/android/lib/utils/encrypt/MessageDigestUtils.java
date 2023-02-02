package com.bihe0832.android.lib.utils.encrypt;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/8/15.
 * Description: Description
 */
public class MessageDigestUtils {

    private static final int BUFFER_SIZE = 2048;

    public static String getDigestData(String string, String digestType) {
        try {
            return getDigestData(string.getBytes("UTF-8"), digestType);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDigestData(byte[] bytes, String digestType) {
        try {
            byte[] hash = MessageDigest.getInstance(digestType).digest(bytes);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String getFileDigestData(String fileName, String digestType) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return getFileDigestData(new File(fileName), digestType);
    }

    public static String getFileDigestData(File sourceFile, String digestType) {
        String ret = "";
        if (null != sourceFile && sourceFile.exists() && sourceFile.length() > 0) {
            return getFileDigestData(sourceFile, digestType, 0, sourceFile.length());
        }
        return ret;
    }

    public static String getFileDigestData(String fileName, String digestType, long start, long end) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return getFileDigestData(new File(fileName), digestType, start, end);
    }

    public static String getFileDigestData(File sourceFile, String digestType, long start, long end) {
        String ret = "";
        if (null != sourceFile && sourceFile.exists() && sourceFile.length() > 0) {
            BufferedInputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(sourceFile));
                ret = getInputStreamDigestData(is, digestType, start, end);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ret;
    }

    public static String getInputStreamDigestData(InputStream bis, String digestType, long start, long end) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(digestType);
            if (start < 1) {
                start = 1;
            }
            bis.skip(start - 1);
            if (end < start) {
                return "";
            }

            int total = (int) (end - start + 1);
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = 0; i < total / BUFFER_SIZE; i++) {
                bis.read(buffer, 0, BUFFER_SIZE);
                md5.update(buffer, 0, BUFFER_SIZE);
            }
            if (total % BUFFER_SIZE > 0) {
                bis.read(buffer, 0, total % BUFFER_SIZE);
                md5.update(buffer, 0, total % BUFFER_SIZE);
            }
            byte[] md5Bytes = md5.digest();
            return HexUtils.bytes2HexStr(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getInputStreamDigestData(InputStream is, String digestType) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(digestType);

            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            int readSize = 0;
            while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                if (len > 0) {
                    md5.update(buffer, 0, len);
                    readSize += len;
                }
            }
            if (readSize == 0)
                return "";
            byte[] md5Bytes = md5.digest();
            return HexUtils.bytes2HexStr(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
