package com.bihe0832.android.base.debug.request.basic;


import com.bihe0832.android.lib.http.common.HttpResponseHandler;
import com.bihe0832.android.base.debug.request.Constants;
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest;

public class BasicPostRequest extends HttpBasicRequest {


	public BasicPostRequest(String para) {
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

}
