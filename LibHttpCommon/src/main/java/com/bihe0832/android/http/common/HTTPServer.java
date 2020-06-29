package com.bihe0832.android.http.common;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.bihe0832.android.lib.thread.ThreadManager;

import java.net.HttpURLConnection;

/**
 * 网络请求分发、执行类
 */
public class HTTPServer {

    private  static final String LOG_TAG = "bihe0832 REQUEST";

    //是否为测试版本
    private static final boolean DEBUG = true;
    private Handler mCallHandler;
    private static final int MSG_REQUEST = 0;

    private HandlerThread mRequestHandlerThread = null;

    private static volatile HTTPServer instance;
    public static HTTPServer getInstance () {
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

    private HTTPServer() {}

    public void init () {

        mCallHandler = new Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_HIGHER)) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REQUEST:
                        if(msg.obj != null && msg.obj instanceof HttpBasicRequest){
                            executeRequest((HttpBasicRequest)msg.obj);
                        }else{
                            Log.d(LOG_TAG,msg.toString());
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

    private void executeRequest(final HttpBasicRequest request){
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                executeRequestInExecutor(request);
            }
        });
    }

    private void executeRequestInExecutor(HttpBasicRequest request){
        request.setRequestTime(System.currentTimeMillis() / 1000);

        String url = request.getUrl();
        if(DEBUG){
            Log.w(LOG_TAG,"=======================================");
            Log.w(LOG_TAG,request.getClass().toString());
            Log.w(LOG_TAG,url);
            Log.w(LOG_TAG,"=======================================");
        }
        BaseConnection connection = null;
        if(url.startsWith("https:")){
            connection = new HTTPSConnection(url);
        }else{
            connection = new HTTPConnection(url);
        }

        String result = connection.doRequest(request);
        if(DEBUG){
            Log.w(LOG_TAG,"=======================================");
            Log.w(LOG_TAG,request.getClass().toString());
            Log.w(LOG_TAG,result);
            Log.w(LOG_TAG, String.valueOf(connection.getResponseCode()));
            Log.w(LOG_TAG,connection.getResponseMessage());
            Log.w(LOG_TAG,"=======================================");
        }

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
            request.getResponseHandler().onResponse(connection.getResponseCode(), result);
        }else{
            if (TextUtils.isEmpty(result)) {
                if(DEBUG) {
                    Log.e(LOG_TAG, request.getClass().getName());
                }
                Log.e(LOG_TAG,"responseBody is null");
                if(TextUtils.isEmpty(connection.getResponseMessage())){
                    request.getResponseHandler().onResponse(connection.getResponseCode(), "");
                }else{
                    request.getResponseHandler().onResponse(connection.getResponseCode(),connection.getResponseMessage());
                }
            } else {
                request.getResponseHandler().onResponse(connection.getResponseCode(), result);
            }
        }
    }
}