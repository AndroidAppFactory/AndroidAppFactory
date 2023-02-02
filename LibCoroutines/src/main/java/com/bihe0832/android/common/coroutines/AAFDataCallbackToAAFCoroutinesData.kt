package com.bihe0832.android.common.coroutines

import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/2/2.
 * Description: Description
 *
 */
class AAFDataCallbackToAAFCoroutinesData<T>(private var continuation: Continuation<AAFCoroutinesData<T>>) : AAFDataCallback<T>() {

    override fun onSuccess(result: T?) {
        if (result != null) {
            continuation.resume(AAFCoroutinesData(result))
        } else {
            continuation.resume(AAFCoroutinesData(-1, Coroutines_ERROR_DATA_NULL, ""))
        }
    }

    override fun onError(code: Int, msg: String) {
        continuation.resume(AAFCoroutinesData(code, code, msg))
    }
}