package com.bihe0832.android.lib.request;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hardyshi on 16/11/21.
 */
public class URLUtils {

    public static final String HTTP_REQ_ENTITY_START = "?";
    public static final String HTTP_REQ_ENTITY_MERGE = "=";
    public static final String HTTP_REQ_ENTITY_JOIN = "&";

    /**
     * @param source 传入带参数的url,如:http://www.qq.com/ui/oa/test.html?name=hao&id=123
     * @return 去掉参数的url, 如:http://www.qq.com/ui/oa/test.html
     */
    public static String getNoQueryUrl(String source) {
        String dest = null;
        try {
            URL sUrl = new URL(source);
            URL dUrl = new URL(sUrl.getProtocol(), sUrl.getHost(),
                    sUrl.getPort(), sUrl.getPath());
            dest = dUrl.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return dest;
    }

    public static String getUrlEncodeValue(String origValue) {
        if (origValue == null) {
            origValue = "";
        }
        return encode(origValue);

    }

    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        } else {
            int start = url.lastIndexOf("/");
            int finish = url.indexOf(URLUtils.HTTP_REQ_ENTITY_START);
            if (start != -1) {
                if(finish != -1){
                    return url.substring(start + 1, finish);
                }else {
                    return url.substring(start + 1);
                }
            } else {
                return "";
            }
        }
    }

    public static String encode(String value) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuffer buf = new StringBuffer(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7'
                    && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }

    /**
     * 获取url 指定name的value;
     *
     * @param url
     * @param name
     * @return
     */
    public static String getValueByName(String url, String name) {
        String result = "";
        int index = url.indexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.contains(name)) {
                result = str.replace(name + "=", "");
                break;
            }
        }
        return result;
    }

    public static String marge(String url, String para) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        if (url.contains(HTTP_REQ_ENTITY_START)) {
            url += HTTP_REQ_ENTITY_JOIN;
        } else {
            url += HTTP_REQ_ENTITY_START;
        }
        if (!TextUtils.isEmpty(para)) {
            url += para;
        }
        return url;
    }

    //判断是否url
    public static boolean isHTTPUrl(String pInput) {
        if (pInput == null) {
            return false;
        }
        String regEx = "^(http|https)//://([a-zA-Z0-9//.//-]+(//:[a-zA-"
                + "Z0-9//.&%//$//-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{"
                + "2}|[1-9]{1}[0-9]{1}|[1-9])//.(25[0-5]|2[0-4][0-9]|[0-1]{1}"
                + "[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)//.(25[0-5]|2[0-4][0-9]|"
                + "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)//.(25[0-5]|2[0-"
                + "4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0"
                + "-9//-]+//.)*[a-zA-Z0-9//-]+//.[a-zA-Z]{2,4})(//:[0-9]+)?(/"
                + "[^/][a-zA-Z0-9//.//,//?//'///////+&%//$//=~_//-@]*)*$";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(pInput);

        boolean res = matcher.matches();

        if (!res) {
            String lower = pInput.toLowerCase();
            res = lower.startsWith("http://") || lower.startsWith("https://");
        }
        return res;
    }
}
