package com.bihe0832.android.base.compose.debug.request.basic;


import com.bihe0832.android.base.compose.debug.request.Constants;
import com.bihe0832.android.lib.http.common.HttpResponseHandler;
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest;

public class BasicGetRequest extends HttpBasicRequest {

    private String mPara = "";

    public BasicGetRequest(String para, HttpResponseHandler handler) {
        this.mPara = para;
    }

    @Override
    public boolean useCaches() {
        return true;
    }

    @Override
    public String getUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.PARA_PARA + HTTP_REQ_ENTITY_MERGE + mPara);
        return getBaseUrl() + "?" + builder.toString();
    }

    private String getBaseUrl() {
        return Constants.HTTP_DOMAIN + Constants.PATH_GET;
    }


}
