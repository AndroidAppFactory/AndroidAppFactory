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
 * Created by zixie on 2017/9/15.
 */

public class MD5 {

    private static final int BUFFER_SIZE = 2048;
    public static String getMd5(String string) {
        try {
            return getMd5(string.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getMd5(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(bytes);
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


    public static String getFileMD5(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return getFileMD5(new File(fileName));
    }

    public static String getFileMD5(File sourceFile) {
        String ret = "";
        if (null != sourceFile && sourceFile.exists() && sourceFile.length() > 0) {
            BufferedInputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(sourceFile));
                ret = getInputStreamMd5(is);
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

    public static String getInputStreamMd5(InputStream is) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

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

    public static String getFileMD5(String fileName, long start, long end) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return getFileMD5(new File(fileName), start, end);
    }

    public static String getFileMD5(File sourceFile, long start, long end) {
        String ret = "";
        if (null != sourceFile && sourceFile.exists() && sourceFile.length() > 0) {
            BufferedInputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(sourceFile));
                ret = getInputStreamMd5(is, start, end);
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

    public static String getInputStreamMd5(InputStream bis, long start, long end) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
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
}