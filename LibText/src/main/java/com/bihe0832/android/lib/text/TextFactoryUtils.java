package com.bihe0832.android.lib.text;

import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hardyshi on 16/11/21.
 */
public class TextFactoryUtils {

    public static final String STRING_HTML_SPACE = "&nbsp;";

    /**
     * 过滤字符串的空格
     * */
    public static String trimSpace(String str) {
        if(null == str) {
            return null;
        }
        String dest = "";
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(str);
        dest = m.replaceAll("");
        return dest;
    }

    /**
     * 分割字符串
     * @param line			原始字符串
     * @param seperator		分隔符
     * @return				分割结果
     */
    public static String[] split(String line, String seperator) {
        if (line == null || seperator == null || seperator.length() == 0)
            return null;
        ArrayList<String> list = new ArrayList<String>();
        int pos1 = 0;
        int pos2;
        for (; ; ) {
            pos2 = line.indexOf(seperator, pos1);
            if (pos2 < 0) {
                list.add(line.substring(pos1));
                break;
            }
            list.add(line.substring(pos1, pos2));
            pos1 = pos2 + seperator.length();
        }
        // 去掉末尾的空串，和String.split行为保持一致
        for (int i = list.size() - 1; i >= 0 && list.get(i).length() == 0; --i) {
            list.remove(i);
        }
        return list.toArray(new String[0]);
    }

    /**
     * 指定长度的随机字符串
     *
     * @param len
     *            随机字符串长度
     * @return 获取到的随机字符串
     */
    public static String getRandomString(int len) {
        String returnStr = "";
        char[] ch = new char[len];
        Random rd = new Random();
        for (int i = 0; i < len; i++) {
            ch[i] = (char) (rd.nextInt(9) + 97);
        }
        returnStr = new String(ch);
        return returnStr;
    }

    public static byte[] getBytesUTF8(String str) {
       	try {
       		return str.getBytes("UTF-8");
       	} catch (UnsupportedEncodingException e) {
       		return null;
       	}
    }

    public static String getSpecialText(String text, int color){
        return "<font color='"+color + "'>" + text + "</font>";
    }

    public static Spannable getLinkText(String text){
        Spannable s = (Spannable) Html.fromHtml(text);
        for (URLSpan u: s.getSpans(0, s.length(), URLSpan.class)) {
            s.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0);
        }
        return s;
    }

    public static Spanned getSpannedTextByHtml(String text) {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }
}
