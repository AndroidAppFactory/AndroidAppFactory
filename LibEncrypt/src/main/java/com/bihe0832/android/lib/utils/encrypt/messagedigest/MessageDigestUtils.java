package com.bihe0832.android.lib.utils.encrypt.messagedigest;

import android.text.TextUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.encrypt.HexUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2022/8/15.
 *         Description: Description
 */
public class MessageDigestUtils {

    private static final int BUFFER_SIZE = 2048;

    private static final ThreadLocal<Map<String, MessageDigest>> sMessageDigestHolder = new ThreadLocal<Map<String, MessageDigest>>() {
        @Override
        protected Map<String, MessageDigest> initialValue() {
            return new HashMap<>();
        }
    };

    public static MessageDigest getMessageDigest(String digestType) {
        Map<String, MessageDigest> digestMap = sMessageDigestHolder.get();
        MessageDigest digest = digestMap.get(digestType);
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance(digestType);
                digestMap.put(digestType, digest);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
        digest.reset();
        ZLog.e(String.valueOf(digest.hashCode()));
        return digest;
    }

    public static String getDigestData(byte[] bytes, String digestType) {
        try {
            MessageDigest digest = getMessageDigest(digestType);
            if (digest == null) {
                return "";
            }
            byte[] hash = digest.digest(bytes);
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

    public static String getDigestData(String string, String digestType) {
        try {
            return getDigestData(string.getBytes("UTF-8"), digestType);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFileDigestData(String fileName, String digestType) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        File sourceFile = new File(fileName);
        if (null != sourceFile && sourceFile.exists() && sourceFile.length() > 0) {
            return getFilePartDigestData(sourceFile, digestType, 0, sourceFile.length());
        }
        return "";
    }

    public static String getFileDigestData(File sourceFile, String digestType) {
        String ret = "";
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(sourceFile));
            ret = getInputStreamDigestData(is, digestType);
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

        return ret;
    }

    public static String getFilePartDigestData(String fileName, String digestType, long start, long end) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return getFilePartDigestData(new File(fileName), digestType, start, end);
    }

    public static String getFilePartDigestData(File sourceFile, String digestType, long start, long end) {
        String ret = "";
        if (null != sourceFile && sourceFile.exists() && sourceFile.length() > 0) {
            BufferedInputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(sourceFile));
                ret = getInputStreamPartDigestData(is, digestType, start, end);
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

    public static String getInputStreamPartDigestData(InputStream bis, String digestType, long start, long end) {
        try {
            MessageDigest digest = getMessageDigest(digestType);
            if (digest == null) {
                return "";
            }

            if (start < 0) {
                start = 0;
            }
            if (end < start) {
                return "";
            }
            long length = end - start;
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            long totalRead = 0;
            bis.skip(start);
            while ((read = bis.read(buffer, 0, (int) Math.min(buffer.length, length - totalRead))) != -1
                    && totalRead < length) {
                digest.update(buffer, 0, read);
                totalRead += read;
            }
            byte[] md5Bytes = digest.digest();
            return HexUtils.bytes2HexStr(md5Bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getInputStreamDigestData(InputStream is, String digestType) {
        try {
            MessageDigest digest = getMessageDigest(digestType);
            if (digest == null) {
                return "";
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
            byte[] md5Bytes = digest.digest();
            return HexUtils.bytes2HexStr(md5Bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


}
