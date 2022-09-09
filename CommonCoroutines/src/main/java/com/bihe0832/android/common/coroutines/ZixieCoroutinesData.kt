package com.bihe0832.android.common.coroutines

/**
 * @author zixie code@bihe0832.com Created on 2022/2/10.
 */

open class ZixieCoroutinesData<T> {

    data class Error(val flag: Int, val exception: ZixieCoroutinesException?)

    private var SUCCESS: T? = null
    protected var ERROR: Error? = null

    constructor(successData: T) {
        SUCCESS = successData
    }

    constructor(error: Error?) {
        ERROR = error
    }

    constructor(errorCode: Int, msg: String?) {
        ERROR = Error(0, ZixieCoroutinesException(errorCode, msg))
    }

    constructor(errorCode: Int, exception: Exception?) {
        ERROR = Error(0, ZixieCoroutinesException(errorCode, exception))
    }

    constructor(flag: Int, errorCode: Int, msg: String?) {
        ERROR = Error(flag, ZixieCoroutinesException(errorCode, msg))
    }

    constructor(flag: Int, errorCode: Int, exception: Exception?) {
        ERROR = Error(flag, ZixieCoroutinesException(errorCode, exception))
    }

    fun data(): T? {
        return SUCCESS
    }

    fun error(): Error? {
        return ERROR
    }

    open fun onSuccess(action: (success: T) -> Unit): ZixieCoroutinesData<T> {
        data()?.let {
            action(it)
        }
        return this
    }


    open fun onError(action: (flag: Int, errCode: Int, exception: Exception?) -> Unit) {
        error()?.let {
            action(it.flag, it.exception?.getCode() ?: -1, it.exception)
        }
    }
}
