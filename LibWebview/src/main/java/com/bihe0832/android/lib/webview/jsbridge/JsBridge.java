package com.bihe0832.android.lib.webview.jsbridge;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.bihe0832.android.lib.thread.ThreadManager;
import com.tencent.smtt.sdk.WebView;

import org.json.JSONObject;

import java.util.Map;


public class JsBridge {

    private static final String TAG = "JsBridge";
    // 默认值
    public static final int ST_PAGE_ORIGINAL = 2000;

    public WebView mWebView;
    public Context mContext;

    public static final String JS_BRIDGE_SCHEME = "jsb://";
    public static final String CALL_BATCH_NAME = "callBatch";

    public static final String FILE_CHOOSER_CALLBACK_FUNCTION_NAME = "fileChooserCallback"; //文件选择的回调

    public enum ResponseType {
        Method, // 默认，直接调用js的方法
        Event // 采用事件分发的方式，传给js
    }

    public JsBridge(WebView webView, Context context) {
        this.mWebView = webView;
        this.mContext = context;
    }



    public void response(String function, int seqid, String method, String result) {
        response(function, seqid, method, result,null);
    }

    public void response(String function, int seqid, String method, String result, Map<String, String> extMap) {
        response(function, seqid, method, result,extMap, ResponseType.Method);
    }

    public void response(String function, int seqid, String method, String result, Map<String, String> extMap, ResponseType type){
        if(TextUtils.isEmpty(function)){
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("result", JsResult.Result_OK);
            json.put("data", result);
            if(!TextUtils.isEmpty(method)){
                json.put("method", method);
            }
            json.put("seqid", seqid);
            if (extMap != null) {
                for(String key:extMap.keySet()){
                    json.put(key, extMap.get(key));
                }
            }

            callback(function, json.toString(), type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void responseFail(String callbackFun, int seqid, String method, int code) {
        responseFail(callbackFun, seqid, method, code, null);
    }

    public void responseFail(String callbackFun, int seqid, String method, int code, Map<String, String> extMap) {
        responseFail(callbackFun, seqid, method, code, null, ResponseType.Method);
    }

    public void responseFail(String callbackFun, int seqid, String method, int code, Map<String, String> extMap, ResponseType type){
        if(TextUtils.isEmpty(callbackFun)){
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("result", JsResult.Result_Fail);
            json.put("code", code);
            json.put("method", method);
            json.put("seqid", seqid);
            if (extMap != null) {
                for(String key:extMap.keySet()){
                    json.put(key, extMap.get(key));
                }
            }
            callback(callbackFun, json.toString(), type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callback(final String function, final String result, final ResponseType type) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            // 非主线程，post到主线程执行
            ThreadManager.getInstance().runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    callbackUiThread(function, result, type);
                }
            });
        }
        else {
            callbackUiThread(function, result, type);
        }
    }

    public void callbackUiThread(String function, String result, ResponseType type) {
        Log.e(TAG, "callbackUiThread " + result);
        StringBuffer sb = new StringBuffer("javascript:");
        switch (type) {
            case Method:
                sb.append("if(!!").append("window."+function).append("){");
                sb.append(function);
                sb.append("(");
                sb.append(result);
                sb.append(")}");
                break;
            case Event:
                sb.append("var event = document.createEvent('Events');");
                sb.append("event.initEvent('" + function + "');");
                sb.append("event.data = " + result + ";");
                sb.append("window.dispatchEvent(event);");
                break;
        }
        if (mWebView != null) {
            try {
                // 加一个catch， 避免空指针
                mWebView.loadUrl(sb.toString());
            } catch (Exception e) {

            }
        }
    }
}
