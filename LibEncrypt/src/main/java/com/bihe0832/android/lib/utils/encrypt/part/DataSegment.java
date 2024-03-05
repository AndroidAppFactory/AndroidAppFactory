package com.bihe0832.android.lib.utils.encrypt.part;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/2/28.
 *         Description:
 */
public class DataSegment {

    private int start = -1;
    private String signatureValue = "";
    private int totalLength = -1;
    private byte[] content = null;

    public DataSegment(int start, byte[] contentData, int totalLength, String signatureValue) {
        this.start = start;
        this.content = new byte[contentData.length];
        System.arraycopy(contentData, 0, content, 0, contentData.length);
        this.totalLength = totalLength;
        this.signatureValue = signatureValue;
    }

    public int getStart() {
        return start;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public byte[] getContent() {
        return content;
    }
}
