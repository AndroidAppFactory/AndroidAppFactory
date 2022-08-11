package com.bihe0832.android.lib.request;

import android.net.Uri;
import android.text.TextUtils;

import com.bihe0832.android.lib.router.Routers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zixie on 16/11/21.
 */
public class URLUtils {

    public static final String HTTP_REQ_ENTITY_START = "?";
    public static final String HTTP_REQ_ENTITY_MERGE = "=";
    public static final String HTTP_REQ_ENTITY_JOIN = "&";
    public static final String HTTP_REQ_ENTITY_SPECIAL = "#";

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

    public static String encode(String origValue) {
        return Routers.encode(origValue);
    }

    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        } else {
            int start = url.lastIndexOf("/");
            int finish = url.indexOf(URLUtils.HTTP_REQ_ENTITY_START);
            if (start != -1) {
                if (finish != -1) {
                    return url.substring(start + 1, finish);
                } else {
                    return url.substring(start + 1);
                }
            } else {
                return "";
            }
        }
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
        if (TextUtils.isEmpty(para)) {
            return url;
        }
        String resultURL = "";
        if (url.contains(HTTP_REQ_ENTITY_START)) {
            if (url.contains(HTTP_REQ_ENTITY_SPECIAL)) {
                resultURL = url.substring(0, url.indexOf(HTTP_REQ_ENTITY_START) + 1) + para;
                if (para.endsWith(HTTP_REQ_ENTITY_JOIN)) {
                    resultURL = resultURL + url.substring(url.indexOf(HTTP_REQ_ENTITY_START) + 1);
                } else {
                    resultURL =
                            resultURL + HTTP_REQ_ENTITY_JOIN + url.substring(url.indexOf(HTTP_REQ_ENTITY_START) + 1);
                }
            } else {
                resultURL = url + HTTP_REQ_ENTITY_JOIN + para;
            }
        } else {
            resultURL = url + HTTP_REQ_ENTITY_START + para;
        }
        return resultURL;
    }

    public static String marge(String url, HashMap<String, String> para) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        if (null == para) {
            return url;
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> entry : para.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.toString();
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
