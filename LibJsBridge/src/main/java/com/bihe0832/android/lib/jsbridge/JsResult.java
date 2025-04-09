package com.bihe0832.android.lib.jsbridge;

public class JsResult {

	// 成功
	public static final int Result_OK = 0;
	// 失败
	public static final int Result_Fail = -1;
	// 函数为空
	public static final int Code_None = -2;
	// 客户端内部逻辑错误
	public static final int Code_Java_Exception = -3;
	// 请求参数不合法
	public static final int Code_IllegalArgument = -4;
	// 接口有权限限制，当前没有权限
	public static final int AUTHORIZE_FAIL = -5;
	// 服务器返回失败
	public static final int SERVER_BUSY = -6;
	// 处理异常，一般是客户端主动掉H5接口时
	public static final int Code_Busy = -100;
	// 当前的客户端版本没有此接口
	public static final int NOT_SUPPORT = -7;
	//未安装或未安装特定版本应用
	public static final int NOT_INSTALLED = -8 ;
	
}
