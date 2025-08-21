package com.bihe0832.android.base.compose.debug.cache;

import com.bihe0832.android.common.coroutines.AAFCoroutinesData

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/9/8.
 * Description: Description
 *
 */
class DebugCoroutinesData<T> : AAFCoroutinesData<T> {

    constructor(successData: T) : super(successData)

    constructor(errorCode: Int, msg: String?) : super(errorCode, msg)

    constructor(errorCode: Int, exception: Exception?) : super(errorCode, exception)

    constructor(flag: Int, errorCode: Int, exception: Exception?) : super(flag, errorCode, exception)

    constructor(flag: Int, errorCode: Int, exception: String?) : super(flag, errorCode, exception)

    override fun onSuccess(action: (success: T) -> Unit): DebugCoroutinesData<T> {
        data()?.let { action(it) }
        return this
    }

    fun onAAFError(action: (errCode: Int, exception: Exception?) -> Unit): DebugCoroutinesData<T> {
        error()?.let {
            if (it.flag == 1) {
                action(it.flag, it.exception)
            }

        }
        return this
    }

    fun onAAFLoginError(action: (errCode: Int, exception: Exception?) -> Unit): DebugCoroutinesData<T> {
        error()?.let {
            if (it.flag == 2) {
                action(it.flag, it.exception)
            }
        }
        return this
    }
}