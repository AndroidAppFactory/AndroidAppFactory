package com.bihe0832.android.lib.request;

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
 *         Created on 2019-08-01.
 *         Description: 根据URL获取当前HTTP网页的title
 */
public class HTTPRequestUtils {

    private static final Pattern TITLE_TAG = Pattern
            .compile("\\<title>(.*)\\</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static String getPageTitle(String url) {
        try {
            URL e = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) e.openConnection();
            conn.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

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
                if (headerName != null && headerName.equalsIgnoreCase("Content-Type")) {
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
