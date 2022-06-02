package com.bihe0832.android.base.debug.request.advanced;


import com.bihe0832.android.lib.aaf.tools.AAFDataCallback;
import com.bihe0832.android.lib.http.advanced.HttpAdvancedRequest;
import com.bihe0832.android.base.debug.request.Constants;

import org.jetbrains.annotations.NotNull;


public class AdvancedGetRequest extends HttpAdvancedRequest<TestResponse> {

    private String mPara = "";

    private AAFDataCallback mAdvancedResponseHandlerHandler;

	public AdvancedGetRequest(String para, AAFDataCallback<TestResponse> handler) {
        this.mPara = para;
        this.mAdvancedResponseHandlerHandler = handler;
    }

    @NotNull
    @Override
    public AAFDataCallback getAdvancedResponseHandler() {
        return mAdvancedResponseHandlerHandler;
    }

    @Override
	public String getUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.PARA_PARA + HTTP_REQ_ENTITY_MERGE + mPara);
        return getBaseUrl()+"?"+builder.toString();
	}

    private String getBaseUrl(){
        return Constants.HTTP_DOMAIN + Constants.PATH_GET;
    }

}
