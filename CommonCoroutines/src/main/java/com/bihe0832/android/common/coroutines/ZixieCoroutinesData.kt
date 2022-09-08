package com.bihe0832.android.common.coroutines

/**
 * @author zixie code@bihe0832.com Created on 2022/2/10.
 */

open class ZixieCoroutinesData<T> {

    data class Error(val flag: Int, val errCode: Int, val msg: String?, val exception: Exception?)

    private var SUCCESS: T? = null
    protected var ERROR: Error? = null

    constructor(successData: T) {
        SUCCESS = successData
    }

    constructor(errorCode: Int, msg: String?) {
        ERROR = Error(0, errorCode, msg, null)
    }

    constructor(errorCode: Int, exception: Exception?) {
        ERROR = Error(0, errorCode, "", exception)
    }

    constructor(errorCode: Int, msg: String?, exception: Exception?) {
        ERROR = Error(0, errorCode, msg, exception)
    }

    constructor(type: Int, errorCode: Int, msg: String?, exception: Exception?) {
        ERROR = Error(type, errorCode, msg, exception)
    }

    fun onSuccess(action: (success: T) -> Unit): ZixieCoroutinesData<T> {
        SUCCESS?.let {
            action(it)
        }
        return this
    }

    fun onSuccess(): T? {
        return SUCCESS
    }

    fun onError(): Error? {
        return ERROR
    }

    protected open fun onInnerError(action: (flag: Int, errCode: Int, msg: String?, exception: Exception?) -> Unit) {
        ERROR?.let {
            action(it.flag, it.errCode, it.msg, it.exception)
        }
    }

    fun onError(action: (errCode: Int, msg: String?, exception: Exception?) -> Unit) {
        ERROR?.let {
            action(it.errCode, it.msg, it.exception)
        }
    }
}
