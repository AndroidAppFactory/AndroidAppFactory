package com.bihe0832.android.http.common;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;

import java.net.HttpURLConnection;

/**
 * 网络请求分发、执行类
 */
public class HTTPServer {

    private static final String LOG_TAG = "bihe0832 REQUEST";

    //是否为测试版本
    private static final boolean DEBUG = true;
    private Handler mCallHandler;
    private static final int MSG_REQUEST = 0;
    private static volatile HTTPServer instance;

    public static HTTPServer getInstance() {
        if (instance == null) {
            synchronized (HTTPServer.class) {
                if (instance == null) {
                    instance = new HTTPServer();
                    instance.init();
                }
            }
        }
        return instance;
    }

    private HTTPServer() {
    }

    public void init() {

        mCallHandler = new Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_HIGHER)) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REQUEST:
                        if (msg.obj != null && msg.obj instanceof HttpBasicRequest) {
                            executeRequestInExecutor((HttpBasicRequest) msg.obj);
                        } else {
                            ZLog.d(LOG_TAG, msg.toString());
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }


    public void doRequest(HttpBasicRequest request) {
        Message msg = mCallHandler.obtainMessage();
        msg.what = MSG_REQUEST;
        msg.obj = request;
        mCallHandler.sendMessage(msg);
    }

    public String doRequestAsync(final String url) {
        BaseConnection connection = getConnection(url);
        HttpBasicRequest basicRequest = new HttpBasicRequest() {
            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public HttpResponseHandler getResponseHandler() {
                return null;
            }
        };
        String result = executeRequest(basicRequest, connection);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return result;
        } else {
            ZLog.e(LOG_TAG, "Code:" + connection.getResponseCode() + ";Messag:" + connection.getResponseMessage());
            ZLog.e(LOG_TAG, "responseBody is null");
            return "";
        }
    }

    private void executeRequestInExecutor(final HttpBasicRequest request) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                executeRequest(request);
            }
        });
    }

    private void executeRequest(HttpBasicRequest request) {

        String url = request.getUrl();
        BaseConnection connection = getConnection(url);
        String result = executeRequest(request, connection);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            request.getResponseHandler().onResponse(connection.getResponseCode(), result);
        } else {
            if (TextUtils.isEmpty(result)) {
                if (DEBUG) {
                    ZLog.e(LOG_TAG, request.getClass().getName());
                }
                ZLog.e(LOG_TAG, "responseBody is null");
                if (TextUtils.isEmpty(connection.getResponseMessage())) {
                    request.getResponseHandler().onResponse(connection.getResponseCode(), "");
                } else {
                    request.getResponseHandler().onResponse(connection.getResponseCode(), connection.getResponseMessage());
                }
            } else {
                request.getResponseHandler().onResponse(connection.getResponseCode(), result);
            }
        }
    }

    private BaseConnection getConnection(String url) {
        BaseConnection connection = null;
        if (url.startsWith("https:")) {
            connection = new HTTPSConnection(url);
        } else {
            connection = new HTTPConnection(url);
        }
        return connection;
    }

    public String executeRequest(HttpBasicRequest request, BaseConnection connection) {
        String url = request.getUrl();
        if (DEBUG) {
            ZLog.w(LOG_TAG, "=======================================");
            ZLog.w(LOG_TAG, request.getClass().toString());
            ZLog.w(LOG_TAG, url);
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
}