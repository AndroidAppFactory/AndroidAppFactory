package com.bihe0832.android.lib.jsbridge;

public class JsResult {


	public static final int Result_OK = 0;
	
	public static final int Result_Fail = -1;
	
	public static final int Code_None = -2;
	
	public static final int Code_Java_Exception = -3;
	
	public static final int Code_IllegalArgument = -4;

	public static final int AUTHORIZE_FAIL = -5;

	public static final int SERVER_BUSY = -6;//服务器返回失败

	public static final int Code_Busy = -100;
	
	public static final int NOT_SUPPORT = -7;
	
	public static final int NOT_INSTALLED = -8 ; //未安装或未安装特定版本应用
	
}
