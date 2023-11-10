package com.bihe0832.android.lib.file.reader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/11/9.
 *         Description:
 */
public class RandomAccessReader {

    private final ByteBuffer data;

    public RandomAccessReader(byte[] data, int length) {
        this.data = (ByteBuffer) ByteBuffer.wrap(data)
                .order(ByteOrder.BIG_ENDIAN)
                .limit(length);
    }

    public void order(ByteOrder byteOrder) {
        this.data.order(byteOrder);
    }

    public int length() {
        return data.remaining();
    }

    public int getInt32(int offset) {
        return data.getInt(offset);
    }

    public short getInt16(int offset) {
        return data.getShort(offset);
    }
}