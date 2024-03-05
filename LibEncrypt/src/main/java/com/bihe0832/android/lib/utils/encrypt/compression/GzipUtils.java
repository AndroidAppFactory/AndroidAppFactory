package com.bihe0832.android.lib.utils.encrypt.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2022/4/10.
 *         Description: Description
 */
public class GzipUtils {

    public static byte[] compress(String data) {
        return compress(data.getBytes());
    }

    public static byte[] compress(byte[] data) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.close();
            byte[] compressed = bos.toByteArray();
            bos.close();
            return compressed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] uncompress(byte[] compressed) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
            GZIPInputStream gis = new GZIPInputStream(bis);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            gis.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String uncompressToString(byte[] compressed, Charset charset) {
        return new String(uncompress(compressed), charset);
    }

    public static String uncompressToString(byte[] compressed) {
        return new String(uncompress(compressed), Charset.forName("UTF-8"));
    }

    public static String uncompressToString(String compressed) {
        return uncompressToString(compressed.getBytes(Charset.forName("ISO-8859-1")));
    }
}