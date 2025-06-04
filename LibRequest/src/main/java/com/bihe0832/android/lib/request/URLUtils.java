package com.bihe0832.android.lib.request;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zixie on 16/11/21.
 */
public class URLUtils {
    public static final String HTTP_REQ_ENTITY_START = "?";
    public static final String HTTP_REQ_ENTITY_MERGE = "=";
    public static final String HTTP_REQ_ENTITY_JOIN = "&";
    public static final String HTTP_REQ_ENTITY_SPECIAL = "#";

    /**
     * @param source 传入带参数的url,如:https://www.qq.com/ui/oa/test.html?name=hao&id=123
     * @return 去掉参数的url, 如:https://www.qq.com/ui/oa/test.html
     */
    public static String getNoQueryUrl(String source) {
        String dest = source;
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

    public static String encode(String value) {
        String encoded = "";

        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
        }

        StringBuffer buf = new StringBuffer(encoded.length());

        for (int i = 0; i < encoded.length(); ++i) {
            char focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
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
        try {
            Uri uri = Uri.parse(url);
            final String query = uri.getEncodedQuery();
            if (TextUtils.isEmpty(query)) {
                return "";
            }
            final String encodedKey = uri.encode(name, null);
            final int length = query.length();
            int start = 0;
            do {
                int nextAmpersand = query.indexOf(URLUtils.HTTP_REQ_ENTITY_JOIN, start);
                int end = nextAmpersand != -1 ? nextAmpersand : length;
                int separator = query.indexOf(URLUtils.HTTP_REQ_ENTITY_MERGE, start);
                if (separator > end || separator == -1) {
                    separator = end;
                }
                if (separator - start == encodedKey.length()
                        && query.regionMatches(start, encodedKey, 0, encodedKey.length())) {
                    if (separator == end) {
                        return "";
                    } else {
                        return query.substring(separator + 1, end);
                    }
                }
                // Move start to end of name.
                if (nextAmpersand != -1) {
                    start = nextAmpersand + 1;
                } else {
                    break;
                }
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getFileName(String url) {
        String noQueryUrl = getNoQueryUrl(url);
        if (TextUtils.isEmpty(noQueryUrl)) {
            return "";
        } else {
            int start = noQueryUrl.lastIndexOf("/");
            int finish = noQueryUrl.indexOf(URLUtils.HTTP_REQ_ENTITY_START);
            if (start != -1) {
                if (finish != -1) {
                    return noQueryUrl.substring(start + 1, finish);
                } else {
                    return noQueryUrl.substring(start + 1);
                }
            } else {
                return "";
            }
        }
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
        return URLUtil.isHttpsUrl(pInput) || URLUtil.isHttpUrl(pInput);
    }

    public static boolean isURL(String source) {
        return URLUtil.isValidUrl(source);
    }

}
