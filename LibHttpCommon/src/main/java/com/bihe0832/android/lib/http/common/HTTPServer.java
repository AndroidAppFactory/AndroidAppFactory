package com.bihe0832.android.lib.http.common;

import static com.bihe0832.android.lib.http.common.core.BaseConnection.CONNECT_TIMEOUT;
import static com.bihe0832.android.lib.http.common.core.BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8;
import static com.bihe0832.android.lib.http.common.core.BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD;
import static com.bihe0832.android.lib.http.common.core.HttpBasicRequest.HTTP_REQ_ENTITY_MERGE;

import android.content.Context;
import android.net.Network;
import android.text.TextUtils;

import com.bihe0832.android.lib.http.common.core.BaseConnection;
import com.bihe0832.android.lib.http.common.core.FileInfo;
import com.bihe0832.android.lib.http.common.core.HTTPConnection;
import com.bihe0832.android.lib.http.common.core.HTTPSConnection;
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest;
import com.bihe0832.android.lib.http.common.core.HttpFileUpload;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.request.HTTPRequestUtils;
import com.bihe0832.android.lib.thread.ThreadManager;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 网络请求分发、执行类
 */
public class HTTPServer {

    public static final String LOG_TAG = "bihe0832 REQUEST";
    public static final String BOUNDARY = UUID.randomUUID().toString();  //边界标识 随机生成

    //是否为测试版本
    private static final boolean DEBUG = true;
    private static volatile HTTPServer instance;

    public static HTTPServer getInstance() {
        if (instance == null) {
            synchronized (HTTPServer.class) {
                if (instance == null) {
                    instance = new HTTPServer();
                }
            }
        }
        return instance;
    }

    /**
     * 对post参数进行编码处理
     */
    public static String getFormDataString(Map<String, String> strParams) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : strParams.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) && TextUtils.isEmpty(entry.getValue())) {
                break;
            } else {
                stringBuffer.append(BaseConnection.HTTP_REQ_ENTITY_PREFIX).append(HTTPServer.BOUNDARY)
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_DISPOSITION).append(":").append("form-data")
                        .append(BaseConnection.HTTP_REQ_ENTITY_END).append("name").append(HTTP_REQ_ENTITY_MERGE)
                        .append("\"").append(entry.getKey()).append("\"")
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)// 参数头设置完以后需要两个换行，然后才是参数内容
                        .append(entry.getValue()).append(BaseConnection.HTTP_REQ_ENTITY_LINE_END);
            }
        }
        return stringBuffer.toString();
    }

    private byte[] executeRequest(HttpBasicRequest request, BaseConnection connection) {
        String url = request.getUrl();
        if (DEBUG) {
            ZLog.w(LOG_TAG, "=======================================");
            ZLog.w(LOG_TAG, request.getClass().toString());
            ZLog.w(LOG_TAG, "request url:" + url);
            ZLog.w(LOG_TAG, "connection url:" + connection.getURLConnection().getURL());
            if (request.data != null) {
                ZLog.w(LOG_TAG, "request data:" + new String(request.data));
            }
            ZLog.w(LOG_TAG, "=======================================");
        }
        request.setRequestTime(System.currentTimeMillis() / 1000);
        byte[] result = connection.doRequest(request);
        if (DEBUG) {
            ZLog.w(LOG_TAG, "=======================================");
            ZLog.w(LOG_TAG, request.getClass().toString());
            try {
                ZLog.w(LOG_TAG, "result byte array size:" + result.length);
                ZLog.w(LOG_TAG, "request data:" + new String(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
            ZLog.w(LOG_TAG, String.valueOf(connection.getResponseCode()));
            ZLog.w(LOG_TAG, connection.getResponseMessage());
            ZLog.w(LOG_TAG, "=======================================");
        }
        return result;
    }

    private byte[] executeRequest(HttpBasicRequest request, HttpByteResponseHandler handler, Network network) {
        String url = request.getUrl();
        BaseConnection connection = getConnection(url, network, request.getConnectTimeOut());
        byte[] result;
        result = executeRequest(request, connection);
        if (null == handler) {
            return result;
        } else {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                handler.onResponse(connection.getResponseCode(), result);
            } else {
                if (result != null && result.length > 0) {
                    if (DEBUG) {
                        ZLog.e(LOG_TAG, request.getClass().getName());
                    }
                    ZLog.e(LOG_TAG, "responseBody is null");
                    if (TextUtils.isEmpty(connection.getResponseMessage())) {
                        handler.onResponse(connection.getResponseCode(), new byte[0]);
                    } else {
                        handler.onResponse(connection.getResponseCode(), connection.getResponseMessage().getBytes());
                    }
                } else {
                    handler.onResponse(connection.getResponseCode(), result);
                }
            }
            return new byte[0];
        }
    }

    private void executeRequestInExecutor(final HttpBasicRequest request, HttpByteResponseHandler handler,
                                          Network network) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                executeRequest(request, handler, network);
            }
        });
    }

    private byte[] startRequest(Network network, final String url, byte[] bytes, final String contentType,
                                HttpByteResponseHandler handler) {
        final String finalContentType;
        if (TextUtils.isEmpty(contentType)) {
            finalContentType = HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD;
        } else {
            finalContentType = contentType;
        }
        HttpBasicRequest basicRequest = new HttpBasicRequest() {
            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public String getContentType() {
                return finalContentType;
            }
        };
        if (bytes != null) {
            basicRequest.data = bytes;
        }
        if (handler != null) {
            executeRequestInExecutor(basicRequest, handler, network);
            return new byte[0];
        } else {
            return executeRequest(basicRequest, null, network);
        }
    }

    public BaseConnection getConnection(String url, Network network, int timeOut) {
        ZLog.e(LOG_TAG, "getConnection:" + url);
        String finalUrl = HTTPRequestUtils.getRedirectUrl(url, timeOut);
        ZLog.e(LOG_TAG, "getConnection getRedirectUrl:" + finalUrl);
        BaseConnection connection = null;
        if (finalUrl.startsWith("https:")) {
            connection = new HTTPSConnection(finalUrl, network);
        } else {
            connection = new HTTPConnection(finalUrl, network);
        }
        return connection;
    }

    public String convertToString(byte[] source, String charsetName) {
        try {
            return new String(source, charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ZLog.e(LOG_TAG, "convertData data error" + e.toString());
            return "";
        }
    }

    public byte[] doFileUpload(Context context, Network network, final String requestUrl, final String strParams, int timeOut,
                               final List<FileInfo> fileParams) {
        return new HttpFileUpload().postRequest(context, HTTPServer.getInstance().getConnection(requestUrl, network, timeOut),
                strParams, fileParams);
    }

    public byte[] doFileUpload(Context context, final String requestUrl, final String strParams,
                               final List<FileInfo> fileParams) {
        return doFileUpload(context, null, requestUrl, strParams, CONNECT_TIMEOUT, fileParams);
    }

    public String doFileUpload(Context context, final String requestUrl, final String strParams,
                               final List<FileInfo> fileParams, String charSet) {
        return convertToString(doFileUpload(context, null, requestUrl, strParams, CONNECT_TIMEOUT, fileParams), charSet);
    }

    public void doByteRequest(HttpBasicRequest request, HttpByteResponseHandler handler, Network network) {
        executeRequestInExecutor(request, handler, network);
    }

    public byte[] doByteRequest(HttpBasicRequest request, Network network) {
        return executeRequest(request, null, network);
    }

    public void doRequest(HttpBasicRequest request, HttpResponseHandler handler, Network network,
                          String charSetName) {
        doByteRequest(request, new HttpByteResponseHandler() {
            @Override
            public void onResponse(int statusCode, byte[] response) {
                handler.onResponse(statusCode, convertToString(response, charSetName));
            }
        }, network);
    }

    public String doRequest(HttpBasicRequest request, Network network, String charSetName) {
        return convertToString(doByteRequest(request, network), charSetName);
    }

    public void doRequest(HttpBasicRequest request, HttpResponseHandler handler, Network network) {
        doRequest(request, handler, network, HTTP_REQ_VALUE_CHARSET_UTF8);
    }

    public String doRequest(HttpBasicRequest request, Network network) {
        return doRequest(request, network, HTTP_REQ_VALUE_CHARSET_UTF8);
    }

    public void doByteRequest(Network network, final String url, byte[] bytes, final String contentType,
                              HttpByteResponseHandler handler) {
        startRequest(network, url, bytes, contentType, handler);
    }

    public byte[] doByteRequest(Network network, final String url, byte[] bytes, final String contentType) {
        return startRequest(network, url, bytes, contentType, null);
    }

    public void doByteRequest(final String url, byte[] bytes, final String contentType,
                              HttpByteResponseHandler handler) {
        doByteRequest(null, url, bytes, contentType, handler);
    }

    public byte[] doByteRequest(final String url, byte[] bytes, final String contentType) {
        return startRequest(null, url, bytes, contentType, null);
    }

    public void doRequest(final String url, byte[] bytes, final String contentType,
                          HttpResponseHandler handler, String charSetName) {
        doByteRequest(null, url, bytes, contentType, new HttpByteResponseHandler() {
            @Override
            public void onResponse(int statusCode, byte[] response) {
                handler.onResponse(statusCode, convertToString(response, charSetName));
            }
        });
    }

    public String doRequest(final String url, byte[] bytes, final String contentType, String charSetName) {
        return convertToString(doByteRequest(null, url, bytes, contentType), charSetName);
    }

    public void doRequest(final String url, byte[] bytes, final String contentType, HttpResponseHandler handler) {
        doRequest(url, bytes, contentType, handler, HTTP_REQ_VALUE_CHARSET_UTF8);
    }

    public String doRequest(final String url, byte[] bytes, final String contentType) {
        return doRequest(url, bytes, contentType, HTTP_REQ_VALUE_CHARSET_UTF8);
    }

    public void doRequest(final String url, HttpResponseHandler handler) {
        doRequest(url, null, HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD, handler);
    }

    public String doRequest(final String url) {
        return doRequest(url, null, HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD);
    }

    @NotNull
    public String doRequest(@NotNull String url, @NotNull String params) {
        return doRequest(url, params.getBytes(), HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD);
    }
}