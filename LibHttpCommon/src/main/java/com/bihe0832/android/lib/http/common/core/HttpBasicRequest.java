package com.bihe0832.android.lib.http.common.core;


import android.text.TextUtils;
import com.bihe0832.android.lib.http.common.HTTPServer;
import com.bihe0832.android.lib.http.common.HttpResponseHandler;
import com.bihe0832.android.lib.log.ZLog;
import java.util.HashMap;
import java.util.Map;


public abstract class HttpBasicRequest {

    public static final String LOG_TAG = HTTPServer.LOG_TAG;

    public static final String HTTP_REQ_ENTITY_MERGE = BaseConnection.HTTP_REQ_ENTITY_MERGE;
    public static final String HTTP_REQ_ENTITY_JOIN = BaseConnection.HTTP_REQ_ENTITY_JOIN;

    protected long requestTime = 0;
    public byte[] data = null;
    public HashMap<String, String> cookieInfo = new HashMap<>();

    public abstract String getUrl();

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public String getContentType() {
        return BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD;
    }

    public Map<String, String> getRequestProperties() {
        return new HashMap<String, String>();
    }

    public byte[] getFormData(Map<String, String> strParams) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : strParams.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) && TextUtils.isEmpty(entry.getValue())) {
                break;
            } else {
                stringBuffer.append(entry.getKey())
                        .append(HttpBasicRequest.HTTP_REQ_ENTITY_MERGE)
                        .append(entry.getValue())
                        .append(HttpBasicRequest.HTTP_REQ_ENTITY_JOIN);

            }
        }
        String result = stringBuffer.toString();
        ZLog.e(LOG_TAG, "getFormDataString = \n" + result);
        try {
            return stringBuffer.toString().getBytes(BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
