package com.bihe0832.android.lib.http.common;

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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
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
                stringBuffer.append(BaseConnection.HTTP_REQ_ENTITY_PREFIX)
                        .append(HTTPServer.BOUNDARY)
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_DISPOSITION).append(":").append("form-data")
                        .append(BaseConnection.HTTP_REQ_ENTITY_END)
                        .append("name").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(entry.getKey()).append("\"")
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)// 参数头设置完以后需要两个换行，然后才是参数内容
                        .append(entry.getValue())
                        .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END);
            }
        }
        return stringBuffer.toString();
    }

    private String executeRequest(HttpBasicRequest request, BaseConnection connection) {
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
        String result = connection.doRequest(request);
        if (DEBUG) {
            ZLog.w(LOG_TAG, "=======================================");
            ZLog.w(LOG_TAG, request.getClass().toString());
            ZLog.w(LOG_TAG, result);
            ZLog.w(LOG_TAG, String.valueOf(connection.getResponseCode()));
            ZLog.w(LOG_TAG, connection.getResponseMessage());
            ZLog.w(LOG_TAG, "=======================================");
        }
        return result;
    }

    private String executeRequest(HttpBasicRequest request, HttpResponseHandler handler, Network network,
            boolean needConvert) {
        String url = request.getUrl();
        BaseConnection connection = getConnection(url, network);
        String result;
        if (needConvert) {
            result = convertOriginToUTF8Data(executeRequest(request, connection));
        } else {
            result = executeRequest(request, connection);
        }
        if (null == handler) {
            return result;
        } else {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                handler.onResponse(connection.getResponseCode(), result);
            } else {
                if (TextUtils.isEmpty(result)) {
                    if (DEBUG) {
                        ZLog.e(LOG_TAG, request.getClass().getName());
                    }
                    ZLog.e(LOG_TAG, "responseBody is null");
                    if (TextUtils.isEmpty(connection.getResponseMessage())) {
                        handler.onResponse(connection.getResponseCode(), "");
                    } else {
                        handler.onResponse(connection.getResponseCode(), connection.getResponseMessage());
                    }
                } else {
                    handler.onResponse(connection.getResponseCode(), result);
                }
            }
            return "";
        }
    }

    private void executeRequestInExecutor(final HttpBasicRequest request, HttpResponseHandler handler, Network network,
            final boolean needConvert) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                executeRequest(request, handler, network, needConvert);
            }
        });
    }

    private String doRequest(Network network, final String url, byte[] bytes, final String contentType,
            HttpResponseHandler handler,
            boolean needConvert) {
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
            executeRequestInExecutor(basicRequest, handler, network, needConvert);
            return "";
        } else {
            return executeRequest(basicRequest, null, network, needConvert);
        }
    }

    public BaseConnection getConnection(String url, Network network) {
        ZLog.e(LOG_TAG, "getConnection:" + url);
        String finalUrl = HTTPRequestUtils.getRedirectUrl(url);
        ZLog.e(LOG_TAG, "getConnection getRedirectUrl:" + finalUrl);
        BaseConnection connection = null;
        if (finalUrl.startsWith("https:")) {
            connection = new HTTPSConnection(finalUrl, network);
        } else {
            connection = new HTTPConnection(finalUrl, network);
        }
        return connection;
    }

    public String convertOriginToUTF8Data(String source) {
        return convertOriginData(source, BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8);
    }

    public String convertOriginData(String source, String charsetName) {

        try {
            return new String(source.getBytes(Charset.forName(BaseConnection.HTTP_REQ_VALUE_CHARSET_ISO_8599_1)),
                    charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ZLog.e(LOG_TAG, "convertData data error" + e.toString());
            return "";
        }
    }

    public String doFileUpload(Context context, Network network, final String requestUrl, final String strParams,
            final List<FileInfo> fileParams) {
        return new HttpFileUpload()
                .postRequest(context, HTTPServer.getInstance().getConnection(requestUrl, network), strParams,
                        fileParams);
    }

    public void doOriginRequestAsync(HttpBasicRequest request, HttpResponseHandler handler, Network network) {
        executeRequestInExecutor(request, handler, network, false);
    }

    public String doOriginRequestSync(HttpBasicRequest request, Network network) {
        return executeRequest(request, null, network, false);
    }

    public void doRequestAsync(HttpBasicRequest request, HttpResponseHandler handler, Network network) {
        executeRequestInExecutor(request, handler, network, true);
    }

    public String doRequestSync(HttpBasicRequest request, Network network) {
        return executeRequest(request, null, network, true);
    }

    public String doFileUpload(Context context, final String requestUrl, final String strParams,
            final List<FileInfo> fileParams) {
        return doFileUpload(context, null, requestUrl, strParams, fileParams);
    }

    public String doOriginRequestSync(Network network, final String url, byte[] bytes, final String contentType) {
        return doRequest(network, url, bytes, contentType, null, false);
    }

    public String doOriginRequestSync(final String url, byte[] bytes, final String contentType) {
        return doOriginRequestSync(null, url, bytes, contentType);
    }

    public String doRequestSync(Network network, final String url, byte[] bytes, final String contentType) {
        return convertOriginToUTF8Data(doRequest(network, url, bytes, contentType, null, true));
    }

    public String doRequestSync(final String url, byte[] bytes, final String contentType) {
        return doRequestSync(null, url, bytes, contentType);
    }

    public void doOriginRequestAsync(Network network, final String url, byte[] bytes, final String contentType,
            HttpResponseHandler handler) {
        doRequest(network, url, bytes, contentType, handler, false);
    }

    public void doOriginRequestAsync(final String url, byte[] bytes, final String contentType,
            HttpResponseHandler handler) {
        doOriginRequestAsync(null, url, bytes, contentType, handler);
    }

    public void doRequestAsync(Network network, final String url, byte[] bytes, final String contentType,
            HttpResponseHandler handler) {
        doRequest(network, url, bytes, contentType, handler, true);
    }

    public void doRequestAsync(final String url, byte[] bytes, final String contentType,
            HttpResponseHandler handler) {
        doRequestAsync(null, url, bytes, contentType, handler);
    }

    public String doRequestSync(final String url) {
        return convertOriginToUTF8Data(doOriginRequestSync(url));
    }

    public String doOriginRequestSync(final String url) {
        return doOriginRequestSync(url, (byte[]) null, HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD);
    }

    public void doOriginRequestAsync(final String url, HttpResponseHandler handler) {
        doOriginRequestAsync(url, (byte[]) null, HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD, handler);
    }

    public void doRequestAsync(final String url, HttpResponseHandler handler) {
        doRequestAsync(url, (byte[]) null, HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD, handler);
    }

    public String doOriginRequestSync(final String url, final String params) {
        byte[] bytes = null;

        try {
            bytes = params.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doOriginRequestSync(url, bytes, HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD);
    }

    public String doRequestSync(final String url, final String params) {
        return convertOriginToUTF8Data(doOriginRequestSync(url, params));
    }
}