package com.bihe0832.android.lib.utils.encypt;


import android.util.Base64;
import com.bihe0832.android.lib.log.ZLog;
import java.io.UnsupportedEncodingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static byte ivBytes[] = new byte[]{0x4D, 0x4E, 0x41, 0x40, 0x32, 0x30, 0x31, 0x37, 0x47, 0x4F, 0x48, 0x45,
            0x41, 0x44, 0x21, 0x21};

    // 默认Key
    public static byte DEFAULT_KEY[] = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x31, 0x32, 0x33, 0x34, 0x35,
            0x35, 0x37, 0x38};

    private static byte[] doAESEncrypt(byte[] content, byte[] key, byte[] ivParaBytes, int mode) {
        try {
            if ((content == null) || key == null) {
                return null;
            }
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            //6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            final IvParameterSpec iv = new IvParameterSpec(ivParaBytes);
            cipher.init(mode, keySpec, iv);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static byte[] doAESEncryptWithDefaultIV(byte[] content, byte[] keyBytes, int mode) {
        try {
            return doAESEncrypt(content, keyBytes, ivBytes, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] doAESEncryptWithoutIV(byte[] content, byte[] keyBytes, int mode) {
        try {
            byte[] ivSpec = new byte[16];
            return doAESEncrypt(content, keyBytes, ivSpec, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte keyBytes[], byte[] content) {
        return doAESEncryptWithDefaultIV(content, keyBytes, Cipher.ENCRYPT_MODE);
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
        return doAESEncryptWithoutIV(content, keyBytes, Cipher.ENCRYPT_MODE);
    }

    public static byte[] decrypt(byte keyBytes[], byte[] content) {
        return doAESEncryptWithDefaultIV(content, keyBytes, Cipher.DECRYPT_MODE);
    }

    public static byte[] decryptWithoutIV(byte keyBytes[], byte[] content) {
        return doAESEncryptWithoutIV(content, keyBytes, Cipher.DECRYPT_MODE);
    }

    public static String encryptToHex(byte[] data, byte[] key, byte[] ivParaBytes) {
        try {
            byte[] result = doAESEncrypt(data, key, ivParaBytes, Cipher.ENCRYPT_MODE);
            return HexUtils.bytes2HexStr(result);
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static byte[] decryptHex(String data, byte[] key, byte[] ivParaBytes) {
        try {
            return doAESEncrypt(HexUtils.hexStr2Bytes(data), key, ivParaBytes, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static String encryptToBase64(byte[] data, byte[] key, byte[] ivParaBytes, int base64Flag) {
        try {
            byte[] result = doAESEncrypt(data, key, ivParaBytes, Cipher.ENCRYPT_MODE);
            return new String(Base64.encode(result, base64Flag), "UTF-8");
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }

    public static String encryptToBase64(byte[] data, byte[] key, byte[] ivParaBytes) {
        return encryptToBase64(data, key, ivParaBytes, Base64.URL_SAFE);
    }

    public static byte[] decryptBase64(String data, byte[] key, byte[] ivParaBytes) {
        return decryptBase64(data, key, ivParaBytes, Base64.URL_SAFE);
    }

    public static byte[] decryptBase64(String data, byte[] key, byte[] ivParaBytes, int base64Flag) {
        try {
            return doAESEncrypt(Base64.decode(data, base64Flag), key, ivParaBytes, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            ZLog.e("AESUtil", "AES 密文处理异常：" + e);
        }
        return null;
    }
}
