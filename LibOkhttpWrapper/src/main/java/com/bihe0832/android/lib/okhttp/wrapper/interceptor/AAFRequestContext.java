package com.bihe0832.android.lib.okhttp.wrapper.interceptor;

/**
 * @author zixie code@bihe0832.com Created on 2025/3/27. Description: Description
 */
public class AAFRequestContext {

    private String mRequestId = "";

    public AAFRequestContext(String id) {
        mRequestId = id;
    }

    public String getRequestId() {
        return mRequestId;
    }
}
