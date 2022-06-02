package com.bihe0832.android.base.test.request.basic;


import com.bihe0832.android.lib.http.common.core.HttpRequest;
import com.bihe0832.android.lib.http.common.HttpResponseHandler;
import com.bihe0832.android.base.test.request.Constants;

public class BasicPostRequest extends HttpRequest {

	private HttpResponseHandler mResponseHandlerHandler;

	public BasicPostRequest(String para, HttpResponseHandler handler) {
        this.mResponseHandlerHandler = handler;
        String encodedParam = Constants.PARA_PARA + HTTP_REQ_ENTITY_MERGE + para;
        try {
            this.data = encodedParam.getBytes("UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

	@Override
	public String getUrl() {
        return Constants.HTTP_DOMAIN + Constants.PATH_POST;
	}


    @Override
    protected void onResponse(int statusCode, String result) {
        this.mResponseHandlerHandler.onResponse(statusCode,result);
    }
}
