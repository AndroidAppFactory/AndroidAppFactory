package com.bihe0832.android.common.coroutines

/**
 * @author zixie code@bihe0832.com Created on 2022/2/10.
 */

open class ZixieCoroutinesData<T> {

    data class Error(val errCode: Int, val msg: String?, val exception: Exception?)

    private var SUCCESS: T? = null
    private var ERROR: Error? = null

    constructor(successData: T) {
        SUCCESS = successData
    }

    constructor(errorCode: Int, msg: String?) {
        ERROR = Error(errorCode, msg, null)
    }

    constructor(errorCode: Int, exception: Exception?) {
        ERROR = Error(errorCode, "", exception)
    }

    constructor(errorCode: Int, msg: String?, exception: Exception?) {
        ERROR = Error(errorCode, msg, exception)
    }

    fun onSuccess(action: (success: T) -> Unit) {
        SUCCESS?.let {
            action(it)
        }
    }

    fun onError(action: (errCode: Int, msg: String?, exception: Exception?) -> Unit) {
        ERROR?.let {
            action(it.errCode, it.msg, it.exception)
        }
    }

}
