package com.bihe0832.android.lib.jsbridge;


import static com.bihe0832.android.lib.jsbridge.BaseJsBridge.TAG;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.utils.apk.APKUtils;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * 调用的方法为js形如：window.location.href="jsb://getAppInfo/1/ZixieCallback?packagename=com.tencent.mm";
 * 分别是：jsb://方法名/序列号/回调方法?参数key=value 回调方法如不需要可以不用。但是需要调用的方法名和系列号必须要
 */
public abstract class BaseJsBridgeProxy {

    private static final String ACTIVITY_STATE_CHANGE_CALLBACK = "activityStateCallback";

    private final int PAGE_CONTROL_RELOAD = 0;
    private final int PAGE_CONTROL_GO_BACK = 1;
    private final int PAGE_CONTROL_GO_FORWARD = 2;

    protected Activity mActivity;

    private boolean canPullRefresh = true;

    public BaseJsBridgeProxy(Activity activity) {
        mActivity = activity;
    }

    protected abstract BaseJsBridge getJsBridge();

    protected abstract void reload();

    protected abstract void goForward();

    public abstract void goBack();

    public abstract boolean canGoBack();

    public abstract boolean canGoForward();

    /**
     * 负责url的调用处理逻辑
     *
     * @param url
     */
    public void invoke(final String url) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                invokeAsync(url);
            }
        });
    }

    private void callNativeMethod(Uri uri, String hostAsMethodName, int seqid, String callbackName) {
        try {
            ZLog.e(TAG, uri.toString());
            if (!TextUtils.isEmpty(hostAsMethodName)) {
                Object obj = this;
                ZLog.e(TAG, this.getClass().getName());
                Method method = this.getClass()
                        .getMethod(hostAsMethodName, Uri.class, Integer.TYPE, String.class, String.class);
                method.invoke(obj, uri, seqid, hostAsMethodName, callbackName);

            } else {
                if (!TextUtils.isEmpty(callbackName)) {
                    getJsBridge().responseFail(callbackName, seqid, hostAsMethodName, JsResult.Code_None,
                            "callbackName is bad");
                }
            }
        } catch (NoSuchMethodException e) {
            ZLog.e(TAG, "JSBridge method 404：" + e);
            if (e.getCause() != null) {
                ZLog.d(TAG, e.getCause().toString());
            }
            if (!TextUtils.isEmpty(callbackName)) {
                getJsBridge().responseFail(callbackName, seqid, hostAsMethodName, JsResult.NOT_SUPPORT,
                        "JSBridge method 404, please update apk version:" + e);
            }
        } catch (Exception ex) {
            ZLog.e(TAG, "JSBridge method has error:" + ex);
            if (ex.getCause() != null) {
                ZLog.d(TAG, ex.getCause().toString());
            }
            if (!TextUtils.isEmpty(callbackName)) {
                getJsBridge().responseFail(callbackName, seqid, hostAsMethodName, JsResult.Code_Java_Exception,
                        "JSBridge method has error, connect JSBridge Native api developer:" + ex);
            }
        }

    }

    private void invokeAsync(String url) {
        Uri uri = Uri.parse(url);
        //将URI中的host作为方法名，path中的第一个作为回call的方法名，如果没有回call的方法名，则不回call
        String hostAsMethodName = uri.getHost();
        if (TextUtils.isEmpty(hostAsMethodName)) {
            return;
        }
        List<String> paths = uri.getPathSegments();
        int seqid = 0;//系列号，任何请求都要系列号，因为是异步调用，不然无法关联上
        String callbackName = null;
        if (paths != null && paths.size() > 0) {
            try {
                seqid = Integer.parseInt(paths.get(0));
            } catch (Exception e) {
                e.printStackTrace();
                seqid = 0;
            }
            if (paths.size() > 1) {
                callbackName = paths.get(1);
            }
        }

        if (hostAsMethodName.equals(BaseJsBridge.CALL_BATCH_NAME)) {
            try {
                String param = uri.getQueryParameter("param");
                JSONArray jsonArray = new JSONArray(param);
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String method = jsonObject.getString("method");
                    int seqidOfCall = jsonObject.getInt("seqid");
                    String callback = "";
                    if (jsonObject.has("ZixieCallback")) {
                        callback = jsonObject.optString("ZixieCallback");
                    }
                    if (TextUtils.isEmpty(callback)) {
                        callback = "";
                    }

                    StringBuilder uriBuilder = new StringBuilder();
                    uriBuilder.append(BaseJsBridge.JS_BRIDGE_SCHEME).append(method).append("/").append(seqidOfCall)
                            .append("/").append(callback).append("?");

                    if (jsonObject.has("args")) {
                        JSONObject args = jsonObject.getJSONObject("args");
                        if (args != null) {
                            Iterator iterator = args.keys();
                            while (iterator.hasNext()) {
                                String key = (String) iterator.next();
                                String value = Uri.decode(args.getString(key));
                                uriBuilder.append(key).append("=").append(Uri.encode(value)).append("&");
                            }
                        }
                    }
                    Uri uriForCall = Uri.parse(uriBuilder.toString());
                    callAMethod(uriForCall, method, seqidOfCall, callback);
                }
            } catch (Exception ex) {
                ZLog.e(TAG, "JSBridge method has error:" + ex);
                if (ex.getCause() != null) {
                    ZLog.d(TAG, ex.getCause().toString());
                }
                if (!TextUtils.isEmpty(callbackName)) {
                    getJsBridge().responseFail(callbackName, seqid, hostAsMethodName, JsResult.Code_Java_Exception,
                            "JSBridge method has error, connect JSBridge Native api developer:" + ex);
                }
            }
        } else {
            callAMethod(uri, hostAsMethodName, seqid, callbackName);
        }
    }

    /**
     * @param uri 为伪协议的详细内容
     * @param hostAsMethodName 为函数名称，主要用于回调时返回给web端
     * @param seqid 为序列号，主要用于回调时返回给web端
     * @param callbackName 回调方法，回调时回调的web端方法名
     */
    protected void callAMethod(Uri uri, String hostAsMethodName, int seqid, String callbackName) {
        callNativeMethod(uri, hostAsMethodName, seqid, callbackName);
    }

    public void pageControl(final Uri uri, final int seqid, final String method, final String function) {
        int type = PAGE_CONTROL_RELOAD;
        try {
            type = Integer.parseInt(uri.getQueryParameter("type"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        final int loadType = type;
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (loadType == PAGE_CONTROL_GO_BACK) {
                    goBack();
                } else if (loadType == PAGE_CONTROL_GO_FORWARD) {
                    goForward();
                } else {
                    // ⚠️ 此处的reload 只会走webview的内部逻辑，不会走 loadURL的完整逻辑，如果还有外部逻辑要处理，建议用自定义接口
                    reload();
                }
            }
        });
        getJsBridge().response(function, seqid, method, "");
    }

    public void toast(final Uri uri, final int seqid, String method, final String callbackFun) {
        int def_duration = 0;
        try {
            String durationStr = uri.getQueryParameter("duration");
            if (!TextUtils.isEmpty(durationStr)) {
                def_duration = Integer.parseInt(durationStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        final int duration = def_duration;
        final String text = uri.getQueryParameter("text");
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            ThreadManager.getInstance().runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show(mActivity, text, duration == 1 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                }
            });

        } else {
            ToastUtil.show(mActivity, text, duration == 1 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        }

    }

    public void onResume() {
        ZLog.e(TAG, "JSBridge onResume");
        getJsBridge().response(ACTIVITY_STATE_CHANGE_CALLBACK, 0, ACTIVITY_STATE_CHANGE_CALLBACK, "onResume", null,
                BaseJsBridge.ResponseType.Event);
    }

    public void onPause() {
        ZLog.e(TAG, "JSBridge onPause");
        getJsBridge().response(ACTIVITY_STATE_CHANGE_CALLBACK, 0, ACTIVITY_STATE_CHANGE_CALLBACK, "onPause", null,
                BaseJsBridge.ResponseType.Event);
    }

    public void onRefresh() {
        ZLog.e(TAG, "JSBridge onRefresh");
        getJsBridge().response(ACTIVITY_STATE_CHANGE_CALLBACK, 0, ACTIVITY_STATE_CHANGE_CALLBACK, "onRefresh", null,
                BaseJsBridge.ResponseType.Event);
    }

    public void getAppInfo(final Uri uri, final int seqid, final String method, final String callbackFun) {
        final String packageName = uri.getQueryParameter("packagename");

        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(callbackFun)) {
            getJsBridge().responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument);
            return;
        }

        JSONObject result = new JSONObject();
        String pkgName = packageName.trim();
        PackageInfo item = APKUtils.getInstalledPackage(mActivity, pkgName);
        JSONObject json = new JSONObject();
        try {
            if (null != item) {
                json.put("install", 1);
                json.put("verCode", item.versionCode);
                json.put("verName", item.versionName == null ? "" : item.versionName);
            } else {
                json.put("install", 0);
            }
            result.put(pkgName, json);
        } catch (Exception e) {
            ZLog.e(TAG, "JSBridge method has error:" + e);
            if (e.getCause() != null) {
                ZLog.d(TAG, e.getCause().toString());
            }
            getJsBridge().responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception,
                    "JSBridge method has error:" + e);
        }
        getJsBridge().response(callbackFun, seqid, method, result.toString());
    }

    public void disablePullRefresh(final Uri uri, final int seqid, String method, final String callbackFun) {
        canPullRefresh = false;
        getJsBridge().response(callbackFun, seqid, method, String.valueOf(canPullRefresh));
    }

    public void enablePullRefresh(final Uri uri, final int seqid, String method, final String callbackFun) {
        canPullRefresh = true;
        getJsBridge().response(callbackFun, seqid, method, String.valueOf(canPullRefresh));
    }

    public boolean canPullToRefresh() {
        return canPullRefresh;
    }

    public void closePage(final Uri uri, final int seqid, String method, final String callbackFun) {
        if (null != mActivity) {
            getJsBridge().response(callbackFun, seqid, method, String.valueOf(JsResult.Result_OK));
            mActivity.finish();
        } else {
            getJsBridge().responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument);
        }
    }
}
