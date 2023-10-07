package com.bihe0832.android.lib.file.content;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/18.
 *         Description:
 */
public class FileContentPattern {

    public static int findPattern(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int findPattern(byte[] data, byte[] pattern, int[] failure) {
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    public static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

    public static int findPattern(String filePath, String dataToFind) {

        byte[] searchBytes = dataToFind.getBytes();
        byte[] buffer = new byte[searchBytes.length * 2];
        int[] failure = computeFailure(searchBytes);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            int bytesRead;
            int position = 0;

            bytesRead = bis.read(buffer, 0, buffer.length);
            while (bytesRead != -1) {

                int aa = findPattern(buffer, searchBytes, failure);
                if (aa > 0) {
                    return position + aa - searchBytes.length;
                }
                position += bytesRead;
                int length = buffer.length - searchBytes.length;
                System.arraycopy(buffer, length, buffer, 0, length);
                bytesRead = bis.read(buffer, length, buffer.length - length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
