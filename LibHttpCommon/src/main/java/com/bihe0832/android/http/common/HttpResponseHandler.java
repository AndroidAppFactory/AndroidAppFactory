package com.bihe0832.android.http.common;

/**
 * 网络请求基类中将请求结果处理后的返回内容转化为业务逻辑相关错误
 */
public interface HttpResponseHandler {
    void onResponse(int statusCode, String response);
}
