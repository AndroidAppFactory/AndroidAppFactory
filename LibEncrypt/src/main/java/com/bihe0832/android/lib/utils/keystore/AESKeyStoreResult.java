package com.bihe0832.android.lib.utils.keystore;

/**
 * Summary
 *
 * @author hardyshi code@bihe0832.com
 *         Created on 2023/8/25.
 *         Description:
 */
public class AESKeyStoreResult {

    public byte[] iv = null;
    public byte[] result = null;

    public AESKeyStoreResult(byte[] iv, byte[] result) {
        this.iv = iv;
        this.result = result;
    }

}
