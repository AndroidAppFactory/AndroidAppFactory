package com.bihe0832.android.http.common;


public abstract class HttpRequest extends HttpBasicRequest {

	protected abstract void onResponse(int statusCode, String errorResponse);

	@Override
	public HttpResponseHandler getResponseHandler() {
		return mHttpResponseHandler;
	}

	public HttpResponseHandler mHttpResponseHandler = new HttpResponseHandler() {

		@Override
		public void onResponse(int statusCode, String responseBody) {
            HttpRequest.this.onResponse(statusCode, responseBody);
		}
	};
}
