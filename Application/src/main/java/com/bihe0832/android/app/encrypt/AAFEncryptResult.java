package com.bihe0832.android.app.encrypt;

import com.bihe0832.android.lib.utils.encrypt.AESEncryptResult;

/**
 * Summary
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/8/28.
 *         Description:
 */
public class AAFEncryptResult extends AESEncryptResult {

    public byte[] keyEncryptData;

    public AAFEncryptResult(byte[] keyEncryptData, byte[] iv, byte[] result) {
        super(iv, result);
        this.keyEncryptData = keyEncryptData;
    }
}
