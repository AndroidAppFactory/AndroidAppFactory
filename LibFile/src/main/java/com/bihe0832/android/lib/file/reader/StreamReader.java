package com.bihe0832.android.lib.file.reader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/11/9.
 *         Description:
 */
public class StreamReader implements Reader {

    private final InputStream is;

    // Motorola / big endian byte order.
    public StreamReader(InputStream is) {
        this.is = is;
    }

    @Override
    public int getUInt16() throws IOException {
        return (is.read() << 8 & 0xFF00) | (is.read() & 0xFF);
    }

    @Override
    public short getUInt8() throws IOException {
        return (short) (is.read() & 0xFF);
    }

    @Override
    public long skip(long total) throws IOException {
        if (total < 0) {
            return 0;
        }

        long toSkip = total;
        while (toSkip > 0) {
            long skipped = is.skip(toSkip);
            if (skipped > 0) {
                toSkip -= skipped;
            } else {
                // Skip has no specific contract as to what happens when you reach the end of
                // the stream. To differentiate between temporarily not having more data and
                // having finished the stream, we read a single byte when we fail to skip any
                // amount of data.
                int testEofByte = is.read();
                if (testEofByte == -1) {
                    break;
                } else {
                    toSkip--;
                }
            }
        }
        return total - toSkip;
    }

    @Override
    public int read(byte[] buffer, int byteCount) throws IOException {
        int toRead = byteCount;
        int read;
        while (toRead > 0 && ((read = is.read(buffer, byteCount - toRead, toRead)) != -1)) {
            toRead -= read;
        }
        return byteCount - toRead;
    }
}