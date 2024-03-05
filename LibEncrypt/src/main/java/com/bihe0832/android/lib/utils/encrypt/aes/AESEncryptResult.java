package com.bihe0832.android.lib.utils.encrypt.aes;

/**
 * Summary
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/8/25.
 *         Description:
 */
public class AESEncryptResult {

    public byte[] iv = null;
    public byte[] result = null;

    public AESEncryptResult(byte[] iv, byte[] result) {
        this.iv = iv;
        this.result = result;
    }
}
