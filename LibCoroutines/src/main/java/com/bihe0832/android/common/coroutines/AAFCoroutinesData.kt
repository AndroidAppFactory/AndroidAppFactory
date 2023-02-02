package com.bihe0832.android.common.coroutines

/**
 * @author zixie code@bihe0832.com Created on 2022/2/10.
 */

open class AAFCoroutinesData<T> {

    data class Error(val flag: Int, val exception: AAFCoroutinesException?)

    private var SUCCESS: T? = null
    protected var ERROR: Error? = null

    constructor(successData: T) {
        SUCCESS = successData
    }

    constructor(error: Error?) {
        ERROR = error
    }

    constructor(errorCode: Int, msg: String?) {
        ERROR = Error(0, AAFCoroutinesException(errorCode, msg))
    }

    constructor(errorCode: Int, exception: Exception?) {
        ERROR = Error(0, AAFCoroutinesException(errorCode, exception))
    }

    constructor(flag: Int, errorCode: Int, msg: String?) {
        ERROR = Error(flag, AAFCoroutinesException(errorCode, msg))
    }

    constructor(flag: Int, errorCode: Int, exception: Exception?) {
        ERROR = Error(flag, AAFCoroutinesException(errorCode, exception))
    }

    fun data(): T? {
        return SUCCESS
    }

    fun error(): Error? {
        return ERROR
    }

    open fun onSuccess(action: (success: T) -> Unit): AAFCoroutinesData<T> {
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
