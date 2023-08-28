package com.bihe0832.android.lib.utils.encrypt;


import android.util.Base64;
import com.bihe0832.android.lib.log.ZLog;
import java.io.UnsupportedEncodingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static final byte[] IV_BYTES = new byte[]{0x4D, 0x4E, 0x41, 0x40, 0x32, 0x30, 0x31, 0x37, 0x47, 0x4F, 0x48,
            0x45, 0x41, 0x44, 0x21, 0x21};
    // 默认Key
    public static byte[] DEFAULT_KEY = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x31, 0x32, 0x33, 0x34, 0x35,
            0x35, 0x37, 0x38};

    public static AESEncryptResult doAESEncrypt(SecretKey key, byte[] ivParaBytes, int mode, byte[] content) {
        try {
            if ((content == null) || key == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");// 创建密码器
            if (null != ivParaBytes) {
                final IvParameterSpec iv = new IvParameterSpec(ivParaBytes);
                cipher.init(mode, key, iv);
                return new AESEncryptResult(ivParaBytes, cipher.doFinal(content));
            } else {
                cipher.init(mode, key);
                if (mode == Cipher.ENCRYPT_MODE) {
                    return new AESEncryptResult(cipher.getIV(), cipher.doFinal(content));
                } else {
                    return new AESEncryptResult(null, cipher.doFinal(content));
                }
            }
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static byte[] doAESEncryptWithDefaultIV(SecretKey key, int mode, byte[] content) {
        try {
            return doAESEncrypt(key, IV_BYTES, mode, content).result;
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }


    private static byte[] doAESEncrypt(byte[] key, byte[] ivParaBytes, int mode, byte[] content) {
        try {
            if (key == null) {
                return null;
            }
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            return doAESEncrypt(keySpec, ivParaBytes, mode, content).result;
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static byte[] doAESEncryptWithIV(byte[] keyBytes, byte[] ivSpec, int mode, byte[] content) {
        try {
            return doAESEncrypt(keyBytes, ivSpec, mode, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] doAESEncryptWithDefaultIV(byte[] keyBytes, int mode, byte[] content) {
        try {
            return doAESEncryptWithIV(keyBytes, IV_BYTES, mode, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] doAESEncryptWithoutIV(byte[] keyBytes, int mode, byte[] content) {
        try {
            byte[] ivSpec = new byte[16];
            return doAESEncryptWithIV(keyBytes, ivSpec, mode, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte keyBytes[], byte[] content) {
        return doAESEncryptWithDefaultIV(keyBytes, Cipher.ENCRYPT_MODE, content);
    }

    public static byte[] encrypt(byte keyBytes[], String content) {
        byte[] cByte = null;
        try {
            cByte = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return encrypt(keyBytes, cByte);
    }

    public static byte[] encryptWithoutIV(byte keyBytes[], byte[] content) {
        return doAESEncryptWithoutIV(keyBytes, Cipher.ENCRYPT_MODE, content);
    }

    public static byte[] decrypt(byte keyBytes[], byte[] content) {
        return doAESEncryptWithDefaultIV(keyBytes, Cipher.DECRYPT_MODE, content);
    }

    public static byte[] decryptWithoutIV(byte keyBytes[], byte[] content) {
        return doAESEncryptWithoutIV(keyBytes, Cipher.DECRYPT_MODE, content);
    }

    public static String encryptToHex(byte[] data, byte[] key, byte[] ivParaBytes) {
        try {
            byte[] result = doAESEncrypt(key, ivParaBytes, Cipher.ENCRYPT_MODE, data);
            return HexUtils.bytes2HexStr(result);
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static byte[] decryptHex(byte[] key, byte[] ivParaBytes, String data) {
        try {
            return doAESEncrypt(key, ivParaBytes, Cipher.DECRYPT_MODE, HexUtils.hexStr2Bytes(data));
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static String encryptToBase64(byte[] key, byte[] ivParaBytes, byte[] data, int base64Flag) {
        try {
            byte[] result = doAESEncrypt(key, ivParaBytes, Cipher.ENCRYPT_MODE, data);
            return new String(Base64.encode(result, base64Flag), "UTF-8");
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static String encryptToBase64(byte[] key, byte[] ivParaBytes, byte[] data) {
        return encryptToBase64(key, ivParaBytes, data, Base64.URL_SAFE);
    }

    public static byte[] decryptBase64(byte[] key, byte[] ivParaBytes, String data) {
        return decryptBase64(key, ivParaBytes, data, Base64.URL_SAFE);
    }

    public static byte[] decryptBase64(byte[] key, byte[] ivParaBytes, String data, int base64Flag) {
        try {
            return doAESEncrypt(key, ivParaBytes, Cipher.DECRYPT_MODE, Base64.decode(data, base64Flag));
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

}
