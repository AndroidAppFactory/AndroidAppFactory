package com.bihe0832.android.lib.file.content;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 文件滑动写入与读取
 *
 * @author code@bihe0832.com
 *         Created on 2024/2/27.
 *         Description:
 */
public class RandomAccessFileUtils {


    public static boolean createFile(String filePath, long fileSize) {
        try {
            File newFile = new File(filePath);
            if (newFile.exists()) {
                ZLog.d("File " + filePath + " has exist");
                return false;
            } else {
                newFile.createNewFile();
            }
            // 创建一个 RandomAccessFile 对象
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            raf.setLength(fileSize);
            raf.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeDataToFile(String filePath, long offset, byte[] bytes) {
        try {
            if (!FileUtils.INSTANCE.checkFileExist(filePath)) {
                ZLog.d("File " + filePath + " not exist");
                return false;
            }
            // 创建一个 RandomAccessFile 对象
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            // 移动文件指针到指定位置
            raf.seek(offset); // 在这里，我们移动文件指针到文件的第 10 个字节
            // 写入二进制内容
            raf.write(bytes);
            // 关闭 RandomAccessFile
            raf.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] readDataFromFile(String filePath, long offset, int length) {
        // 创建一个指定长度的二进制内容

        if (!FileUtils.INSTANCE.checkFileExist(filePath)) {
            ZLog.d("File " + filePath + " not exist");
            return null;
        }

        try {
            byte[] bytes = new byte[length];
            // 创建一个 RandomAccessFile 对象
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            // 移动文件指针到指定位置
            raf.seek(offset);
            raf.read(bytes);
            raf.close();
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
