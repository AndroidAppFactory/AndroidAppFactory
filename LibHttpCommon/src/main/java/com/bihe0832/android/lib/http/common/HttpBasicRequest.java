package com.bihe0832.android.lib.http.common;


import static com.bihe0832.android.lib.http.common.BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE;

import java.util.HashMap;


public abstract class HttpBasicRequest {

    public static final String LOG_TAG = "bihe0832 REQUEST";

    public static final String HTTP_REQ_ENTITY_MERGE = "=";
    public static final String HTTP_REQ_ENTITY_JOIN = "&";

    protected long requestTime = 0;
    public byte[] data = null;
    public HashMap<String, String> cookieInfo = new HashMap<>();

    public abstract String getUrl();

    public abstract HttpResponseHandler getResponseHandler();

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public String getContentType() {
        return HTTP_REQ_VALUE_CONTENT_TYPE;
    }
}
