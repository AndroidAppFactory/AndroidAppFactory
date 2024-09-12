package com.bihe0832.android.lib.pinyin;

import android.content.Context;
import com.bihe0832.android.lib.pinyin.bean.PinyinMapDict;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从Asset中的文本文件构建词典的辅助类
 *
 * 词典格式为：每行一个词和对应的拼音，拼音在前，词在后，空格分隔，拼音间以'分隔
 * 例：  CHONG'QING 重庆
 *
 * Created by guyacong on 2016/12/23.
 */
public class FilePinyinMapDict extends PinyinMapDict {

    final Context mContext;

    final Map<String, String[]> mDict;

    public FilePinyinMapDict(Context context, String filePath) {
        mContext = context.getApplicationContext();
        mDict = new ConcurrentHashMap<>();
        init(filePath);
    }

    @Override
    public Map<String, String[]> mapping() {
        return mDict;
    }

    private void init(String filePath) {
        BufferedReader reader = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] keyAndValue = line.split("\\s+");
                if (keyAndValue.length == 2) {
                    String[] pinyinStrs = keyAndValue[0].split("'");
                    mDict.put(keyAndValue[1], pinyinStrs);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
