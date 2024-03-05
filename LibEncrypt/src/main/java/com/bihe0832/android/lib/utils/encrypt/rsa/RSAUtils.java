package com.bihe0832.android.lib.utils.encrypt.rsa;

import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * Summary
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/8/28.
 *         Description:
 */
public class RSAUtils {

    public static final String MOD_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final String MOD_PKCS_1 = "RSA/ECB/PKCS1Padding";

    public static final String PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----";
    public static final String PUBLIC_KEY_END = "-----END PUBLIC KEY-----";


    // 加密方法
    public static byte[] encrypt(String mod, PublicKey publicKey, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(mod);
            int blockSize = 128;
            // 使用"RSA/ECB/PKCS1Padding"模式进行加密
            blockSize = ((RSAKey) publicKey).getModulus().bitLength() / 8 - 11;
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[blockSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] encryptedDataBlock = cipher.doFinal(buffer, 0, bytesRead);
                outputStream.write(encryptedDataBlock);
            }
            return outputStream.toByteArray().clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(String mod, PrivateKey privateKey, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(mod);
            int blockSize = 128;
            // 使用"RSA/ECB/PKCS1Padding"模式进行加密
            blockSize = ((RSAKey) privateKey).getModulus().bitLength() / 8;
            // 使用"RSA/ECB/PKCS1Padding"模式进行解密
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[blockSize];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] decryptedDataBlock = cipher.doFinal(buffer, 0, bytesRead);
                outputStream.write(decryptedDataBlock);
            }
            return outputStream.toByteArray().clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPublicKeyContent(PublicKey publicKey, int wrapSize) {
        try {
            if (null != publicKey) {
                String publicKeyBase64 = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
                if (wrapSize > 0) {
                    return RSAUtils.wrapString(publicKeyBase64, wrapSize);
                } else {
                    return publicKeyBase64;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String getPublicKeyPemString(PublicKey publicKey) {
        return PUBLIC_KEY_BEGIN + "\n" + getPublicKeyContent(publicKey, 64) + PUBLIC_KEY_END + "\n";
    }


    public static String getPublicKeyByteString(PublicKey publicKey) {
        try {

            StringBuffer result = new StringBuffer();
            if (null != publicKey && null != publicKey.getEncoded()) {
                publicKey.getEncoded();
                for (byte b : publicKey.getEncoded()) {
                    result.append(String.format("%02X ", b));
                }
            }
            return result.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String transPublicKeyByteStringToWindows(String keyString) {
        return keyString.replace(
                "30 82 01 22 30 0D 06 09 2A 86 48 86 F7 0D 01 01 01 05 00 03 82 01 0F 00 30 82 01 0A 02 82 01 01 00 ",
                "30 82 01 0a 02 82 01 01 00 ");
    }

    public static String wrapString(String source, int wrapLength) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < source.length()) {
            int endIndex = Math.min(index + wrapLength, source.length());
            result.append(source.substring(index, endIndex)).append("\n");
            index = endIndex;
        }
        return result.toString();
    }

    public static PublicKey pemStringToRSAPublicKey(String publicKeyPem) {
        try {
            String publicKeyPemFormatted = publicKeyPem
                    .replace(PUBLIC_KEY_BEGIN, "")
                    .replace(PUBLIC_KEY_END, "")
                    .replaceAll("\\s+", "");

            byte[] publicKeyBytes = Base64.decode(publicKeyPemFormatted, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey pemStringToRSAPrivateKey(String privateKeyPem) {
        try {
            String privateKeyPemFormatted = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] privateKeyBytes = Base64.decode(privateKeyPemFormatted, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptSecretKeyWithRSAPublicKey(String mod, PublicKey publicKey, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(mod);
            cipher.init(Cipher.WRAP_MODE, publicKey);
            return cipher.wrap(secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public static byte[] signDataWithRSAPrivateKey(PrivateKey privateKey, String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            byte[] dataBytes = data.getBytes();
            signature.update(dataBytes);

            byte[] signatureBytes = signature.sign();
            return signatureBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifySignatureWithRSAPublicKey(PublicKey publicKey, String data, byte[] signatureBytes) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);

            byte[] dataBytes = data.getBytes();
            signature.update(dataBytes);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}