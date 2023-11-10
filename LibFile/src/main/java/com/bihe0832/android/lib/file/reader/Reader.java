package com.bihe0832.android.lib.file.reader;

import java.io.IOException;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/11/9.
 *         Description:
 */
public interface Reader {

    int getUInt16() throws IOException;

    short getUInt8() throws IOException;

    long skip(long total) throws IOException;

    int read(byte[] buffer, int byteCount) throws IOException;
}