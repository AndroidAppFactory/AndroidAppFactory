package com.bihe0832.android.lib.utils.encrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZlibUtil {

    public static byte[] uncompress(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        //定义byte数组用来放置解压后的数据
        byte[] output = new byte[0];
        Inflater decompresser = new Inflater();
        decompresser.reset();
        //设置当前输入解压
        decompresser.setInput(data, 0, data.length);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }


    public static byte[] compress(byte[] data){

        if (data == null || data.length <= 0) {
            return null;
        }
        //定义byte数组用来放置解压后的数据
        byte[] output = new byte[0];
        Deflater decompresser = new Deflater();
        decompresser.reset();
        //设置当前输入解压
        decompresser.setInput(data, 0, data.length);
        decompresser.finish();
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.deflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }
}
