package com.bihe0832.android.lib.utils.encypt;

import android.util.Log;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    // iv同C语言中iv
    private static byte ivBytes[] = new byte[]{0x4D, 0x4E, 0x41, 0x40, 0x32, 0x30, 0x31, 0x37, 0x47, 0x4F, 0x48, 0x45,
            0x41, 0x44, 0x21, 0x21};

    // 默认Key
    public static byte DEFAULT_KEY[] = {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x31, 0x32, 0x33, 0x34, 0x35,
            0x35, 0x37, 0x38};

    public static byte[] encrypt(byte keyBytes[], byte[] content) {
        return docrypt(content, keyBytes, Cipher.ENCRYPT_MODE);
    }

    public static byte[] encrypt(byte keyBytes[], String content) {
        byte[] cByte = null;
        try {
            cByte = content.getBytes("UTF-8");
        } catch(UnsupportedEncodingException e) {
            return null;
        }
        return docrypt(cByte, keyBytes, Cipher.ENCRYPT_MODE);
    }

    public static byte[] decrypt(byte keyBytes[], byte[] content) {
        return docrypt(content, keyBytes, Cipher.DECRYPT_MODE);
    }

    public static byte[] docrypt(byte[] content, byte[] keyBytes, int mode) {
        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
            final IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(mode, key, iv);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] docrypt(byte[] content, byte[] keyBytes, byte[] ivParaBytes , int mode ) {
        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
            final IvParameterSpec iv = new IvParameterSpec(ivParaBytes);
            cipher.init(mode, key, iv);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加密
     *
     * @param data 需要加密的内容
     * @param key 加密密码
     * @return
     */
    public static String encrypt(String data, byte[] key , byte[] ivParaBytes) {
        return doAES(data, key, ivParaBytes ,Cipher.ENCRYPT_MODE);
    }

    /**
     * 解密
     *
     * @param data 待解密内容
     * @param key 解密密钥
     * @return
     */
    public static String decrypt(String data, byte[] key , byte[] ivParaBytes) {
        return doAES(data, key, ivParaBytes ,Cipher.DECRYPT_MODE);
    }

    /**
     * 加解密
     */
    private static String doAES(String data, byte[] key, byte[] ivParaBytes ,int mode) {
        try {
            if ((data == null) || key == null) {
                return null;
            }
            //判断是加密还是解密
            boolean encrypt = mode == Cipher.ENCRYPT_MODE;
            byte[] content;
            //true 加密内容 false 解密内容
            if (encrypt) {
                content = data.getBytes("UTF-8");
            } else {
                content = HexUtils.hexStr2Bytes(data);
            }
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            //6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            final IvParameterSpec iv = new IvParameterSpec(ivParaBytes);
            cipher.init(mode, keySpec, iv);
            byte[] result = cipher.doFinal(content);
            if (encrypt) {
                //将二进制转换成16进制
                return HexUtils.bytes2HexStr(result);
            } else {
                return new String(result, "UTF-8");
            }
        } catch (Exception e) {
            Log.e("AESUtil", "AES 密文处理异常", e);
        }
        return null;
    }
}
