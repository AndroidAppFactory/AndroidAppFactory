package com.bihe0832.android.lib.aaf.tools;


/**
 * @author zixie code@bihe0832.com
 * Created on 2020-02-24.
 * Description: 所有的网络请求都使用MnaNetworkCallback来处理
 */
abstract class AAFDataCallback<T> {

    abstract fun onSuccess(result: T?)

    open fun onError(errorCode: Int, msg: String) {

    }

}
