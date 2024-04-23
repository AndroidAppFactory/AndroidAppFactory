package com.bihe0832.android.lib.http.common.core;

import android.text.TextUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.request.HTTPRequestUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpURLConnection封装基类，网络请求，设置请求协议头、发送请求
 *
 * Created by zixie on 16/11/22.
 */
public abstract class BaseConnection {

    public static final String HTTP_REQ_PROPERTY_CHARSET = "Charset";
    public static final String HTTP_REQ_VALUE_CHARSET_UTF8 = "UTF-8";
    public static final String HTTP_REQ_VALUE_CHARSET_ISO_8599_1 = "ISO-8859-1";
    public static final String HTTP_REQ_PROPERTY_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HTTP_REQ_PROPERTY_CONTENT_TYPE = HTTPRequestUtils.HTTP_REQ_PROPERTY_CONTENT_TYPE;
    public static final String HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD = "application/x-www-form-urlencoded";
    public static final String HTTP_REQ_VALUE_CONTENT_TYPE_TEXT = "text/plain";
    public static final String HTTP_REQ_VALUE_CONTENT_TYPE_FORM = "multipart/form-data";     //内容类型
    public static final String HTTP_REQ_VALUE_CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";     //内容类型
    public static final String HTTP_REQ_ENTITY_MERGE = "=";
    public static final String HTTP_REQ_ENTITY_JOIN = "&";
    public static final String HTTP_REQ_ENTITY_PREFIX = "--";
    public static final String HTTP_REQ_ENTITY_END = "; ";
    public static final String HTTP_REQ_ENTITY_LINE_END = "\r\n";
    public static final String HTTP_REQ_PROPERTY_CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_REQ_METHOD_GET = "GET";
    public static final String HTTP_REQ_METHOD_POST = "POST";
    public static final String HTTP_REQ_COOKIE = "Cookie";
    public static final int CONNECT_TIMEOUT = HTTPRequestUtils.CONNECT_TIMEOUT;
    public static final int DEFAULT_READ_TIMEOUT = HTTPRequestUtils.DEFAULT_READ_TIMEOUT;

    private static final String LOG_TAG = "bihe0832 REQUEST";

    public BaseConnection() {

    }

    protected void setURLConnectionCommonPara(int connectTimeOut, int readTimeOut, boolean useCaches) {
        HttpURLConnection connection = getURLConnection();
        if (null == connection) {
            return;
        }
        connection.setConnectTimeout(connectTimeOut);
        connection.setReadTimeout(readTimeOut);
        connection.setUseCaches(useCaches);
    }


    protected void setURLConnectionRequestProperty(HashMap<String, String> requestProperty) {
        HttpURLConnection connection = getURLConnection();
        for (Map.Entry<String, String> entry : requestProperty.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) || TextUtils.isEmpty(entry.getValue())) {
                ZLog.d(LOG_TAG, "requestProperty is bad:" + entry.getKey());
            } else {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void setURLConnectionCookie(HashMap<String, String> cookieInfo) {
        HttpURLConnection connection = getURLConnection();
        if (null == connection) {
            return;
        }
        String cookieString = connection.getRequestProperty(HTTP_REQ_COOKIE);
        if (!TextUtils.isEmpty(cookieString)) {
            cookieString = cookieString + ";";
        } else {
            cookieString = "";
        }
        for (Map.Entry<String, String> entry : cookieInfo.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) || TextUtils.isEmpty(entry.getValue())) {
                ZLog.d(LOG_TAG, "cookie inf is bad:" + entry.getKey());
            } else {
                cookieString =
                        cookieString + entry.getKey() + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + entry.getValue() + ";";
            }
        }
        connection.setRequestProperty(HTTP_REQ_COOKIE, cookieString);
    }

    public String doRequest(HttpBasicRequest request) {
        if (null == getURLConnection()) {
            ZLog.e(LOG_TAG, "URLConnection is null");
            return "";
        }
        setURLConnectionCommonPara(request.getConnectTimeOut(), request.getReadTimeOut(), request.useCaches());
        HashMap<String, String> requestProperty = new HashMap<>();
        if (request.getRequestProperties() != null) {
            requestProperty.putAll(request.getRequestProperties());
        }
        requestProperty.put(HTTP_REQ_PROPERTY_CHARSET, HTTP_REQ_VALUE_CHARSET_UTF8);
        requestProperty.put(HTTP_REQ_PROPERTY_CONTENT_TYPE, request.getContentType());
        setURLConnectionRequestProperty(requestProperty);

        //检查cookie
        if (null != request.cookieInfo && request.cookieInfo.size() > 0) {
            setURLConnectionCookie(request.cookieInfo);
        }

        if (null == request.data) {
            return doGetRequest();
        } else {
            return doPostRequest(request.data);
        }
    }

    protected String doGetRequest() {
        String result = "";
        InputStream is = null;
        BufferedReader br = null;
        try {
            HttpURLConnection connection = getURLConnection();
            if (null == connection) {
                return "";
            }
            connection.setRequestMethod(HTTP_REQ_METHOD_GET);
            is = connection.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            is.close();
            result = os.toString("ISO-8859-1");
        } catch (javax.net.ssl.SSLHandshakeException ee) {
            ZLog.e(LOG_TAG, "javax.net.ssl.SSLPeerUnverifiedException");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
            return result;
        }
    }

    protected String doPostRequest(byte[] data) {
        BufferedReader br = null;
        InputStream inptStream = null;
        OutputStream outputStream = null;
        HttpURLConnection connection = getURLConnection();
        try {
            if (null == connection) {
                return "";
            }
            connection.setRequestMethod(HTTP_REQ_METHOD_POST);
            connection.setRequestProperty(HTTP_REQ_PROPERTY_CONTENT_LENGTH, String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            outputStream = connection.getOutputStream();
            outputStream.write(data);
            outputStream.flush();

            int response = connection.getResponseCode();            //获得服务器的响应码
            if (response == HttpURLConnection.HTTP_OK) {
                inptStream = connection.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = inptStream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                inptStream.close();
                return os.toString(HTTP_REQ_VALUE_CHARSET_ISO_8599_1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (inptStream != null) {
                    inptStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getResponseMessage() {

        HttpURLConnection connection = getURLConnection();
        if (null == connection) {
            return "";
        } else {
            try {
                return getURLConnection().getResponseMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    public int getResponseCode() {
        HttpURLConnection connection = getURLConnection();
        if (null == connection) {
            return -1;
        } else {
            try {
                return getURLConnection().getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    public abstract HttpURLConnection getURLConnection();


}
