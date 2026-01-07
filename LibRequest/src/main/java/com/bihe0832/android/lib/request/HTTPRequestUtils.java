package com.bihe0832.android.lib.request;

import android.text.TextUtils;

import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-08-01.
 * Description: 根据URL获取当前HTTP网页的title
 */
public class HTTPRequestUtils {

    private static final Pattern TITLE_TAG = Pattern
            .compile("\\<title>(.*)\\</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static final String USER_AGENT_COMMON_ZIXIE = "Mozilla/5.0 (Linux; Android 10; UNKnown) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 Mobile Safari/537.36/";

    /**
     * 建立连接的超时时间
     */
    public static final int CONNECT_TIMEOUT = 5 * 1000;
    /**
     * 建立到资源的连接后从 input 流读入时的超时时间
     */
    public static final int DEFAULT_READ_TIMEOUT = 10 * 1000;

    public static final String HTTP_REQ_PROPERTY_CONTENT_TYPE = "Content-Type";


    public static String getPageTitle(String url) {
        try {
            URL e = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) e.openConnection();
            conn.addRequestProperty("User-Agent", USER_AGENT_COMMON_ZIXIE);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(DEFAULT_READ_TIMEOUT);

            HTTPRequestUtils.ContentType contentType = getContentTypeHeader(conn);
            if (contentType != null && contentType.contentType.equals("text/html")) {
                Charset charset = getCharset(contentType);
                if (charset == null) {
                    charset = Charset.defaultCharset();
                }

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
                boolean n = false;
                int totalRead = 0;
                char[] buf = new char[1024];

                StringBuilder content;
                int n1;
                for (content = new StringBuilder(); /*totalRead < 2048 &&*/
                     (n1 = reader.read(buf, 0, buf.length)) != -1; totalRead += n1) {
                    content.append(buf, 0, n1);
                }

                reader.close();
                Matcher matcher = TITLE_TAG.matcher(content);
                return matcher.find() ? matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim() : "unknown(-2)";
            } else {
                return "unknown(-1)";
            }
        } catch (Exception var12) {
            var12.printStackTrace();
            return "unknown(-3)";
        }
    }

    public static HTTPRequestUtils.ContentType getContentTypeHeader(URLConnection conn) {
        try {
            int e = 0;
            boolean moreHeaders = true;

            do {
                String headerName = conn.getHeaderFieldKey(e);
                String headerValue = conn.getHeaderField(e);
                if (headerName != null && headerName.equalsIgnoreCase(HTTP_REQ_PROPERTY_CONTENT_TYPE)) {
                    return new HTTPRequestUtils.ContentType(headerValue);
                }
                ++e;
                moreHeaders = headerName != null || headerValue != null;
            } while (moreHeaders);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return null;
    }

    public static long getContentLength(URLConnection conn) {
        try {
            if (BuildUtils.INSTANCE.getSDK_INT() >= android.os.Build.VERSION_CODES.N) {
                return conn.getContentLengthLong();
            } else {
                return conn.getContentLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getRedirectUrl(String url) {
        return getRedirectUrl(url, CONNECT_TIMEOUT);
    }

    //   可能第一步得获取重定向的url
    public static String getRedirectUrl(String url, int timeOut) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(timeOut);
            String redirectUrl = conn.getHeaderField("Location");
            if (TextUtils.isEmpty(redirectUrl)) {
                return url;
            }
            return getRedirectUrl(redirectUrl, timeOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    public static Charset getCharset(HTTPRequestUtils.ContentType contentType) {
        try {
            return contentType != null && contentType.charsetName != null && Charset
                    .isSupported(contentType.charsetName) ? Charset.forName(contentType.charsetName) : null;
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static final class ContentType {

        private static final Pattern CHARSET_HEADER = Pattern.compile("charset=([-_a-zA-Z0-9]+)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        private String contentType;
        private String charsetName;

        private ContentType(String headerValue) {
            try {
                if (headerValue == null) {
                    throw new IllegalArgumentException("ContentType must be constructed with a not-null headerValue");
                }

                int e = headerValue.indexOf(";");
                if (e != -1) {
                    this.contentType = headerValue.substring(0, e);
                    Matcher matcher = CHARSET_HEADER.matcher(headerValue);
                    if (matcher.find()) {
                        this.charsetName = matcher.group(1);
                    }
                } else {
                    this.contentType = headerValue;
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }

        }
    }
}
