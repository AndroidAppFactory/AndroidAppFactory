package com.bihe0832.http.advanced;


import com.bihe0832.android.http.common.HttpBasicRequest;
import com.bihe0832.android.http.common.HttpResponseHandler;
import com.bihe0832.android.lib.gson.JsonHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 *
 * 网络请求实例类的基类，所有具体的网络请求都是他的实现
 *
 */

public abstract class HttpAdvancedRequest<T> extends HttpBasicRequest {

	private  static final String LOG_TAG = "bihe0832 REQUEST";

	public static final int RET_SUCC      =   0;
	public static final int RET_FAIL      =   1;

	public final static int Succ = 0;
	public final static int Error = -1;
	public final static int NetWorkException = 100000;  //调用网络请求异常
	public final static int NetWorkTimeout = 100101;  //调用网络请求超时
	public final static int HttpSatutsError = 100102;   //HTTP请求状态异常
	public final static int HttpRespNull = 100103;      //HTTP响应为空
	public final static int HttpRespParseError = 100104;//HTTP响应解析错误

	public interface AdvancedResponseHandler<T> {
		@NotNull
		void onRequestSuccess(T response);
		void onRequestFailure(int statusCode, String response);
	}

	@NotNull
	public abstract AdvancedResponseHandler getAdvancedResponseHandler();

	private Class<T> getTClass(){
		try {
			Type type = getClass().getGenericSuperclass();
			Type[] params = ((ParameterizedType) type).getActualTypeArguments();
			Class<T> reponseClass = (Class) params[0];
			return reponseClass;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public HttpResponseHandler getResponseHandler() {
		return mHttpResponseHandler;
	}

	public HttpResponseHandler mHttpResponseHandler = new HttpResponseHandler() {

		@Override
		public void onResponse(int statusCode, String response) {
			int code = NetWorkException;
			if (statusCode == 0) {
				code = NetWorkException;
				getAdvancedResponseHandler().onRequestFailure(code, response);
			} else if (statusCode == -1) {
				code = NetWorkException;
				getAdvancedResponseHandler().onRequestFailure(code, response);
			}else if (statusCode > 300) {
				code = HttpSatutsError;
				getAdvancedResponseHandler().onRequestFailure(code, response);
			} else {
				try {
					T resultObj =  JsonHelper.INSTANCE.fromJson(response,getTClass());
					if(resultObj != null){
						getAdvancedResponseHandler().onRequestSuccess(resultObj);
					}else {
						getAdvancedResponseHandler().onRequestFailure(HttpRespParseError, response);
					}
				}catch (Exception e){
					e.printStackTrace();
					getAdvancedResponseHandler().onRequestFailure(HttpRespParseError, response);
				}
			}
		}
	};
}
