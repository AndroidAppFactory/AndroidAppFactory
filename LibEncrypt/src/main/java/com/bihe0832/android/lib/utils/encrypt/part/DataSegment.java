package com.bihe0832.android.lib.utils.encrypt.part;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/2/28.
 *         Description:
 */
public class DataSegment {

    protected String dataKey = "";
    protected int dataNo = -1;
    protected byte[] content = null;

    public DataSegment(String dataKey, int dataNo, byte[] contentData) {
        this.dataKey = dataKey;
        this.dataNo = dataNo;
        this.content = new byte[contentData.length];
        System.arraycopy(contentData, 0, content, 0, contentData.length);
    }

    public String getDataKey() {
        return dataKey;
    }

    public int getDataNo() {
        return dataNo;
    }

    public byte[] getContent() {
        return content;
    }
}
