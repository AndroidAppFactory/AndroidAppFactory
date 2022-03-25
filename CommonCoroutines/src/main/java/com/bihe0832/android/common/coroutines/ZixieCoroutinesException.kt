package com.bihe0832.android.common.coroutines

/**
 * @author hardyshi code@bihe0832.com Created on 2022/2/10.
 */
const val Coroutines_ERROR_CODE_EXCEPTION = -1
const val Coroutines_ERROR_DATA_NULL = -2

open class ZixieCoroutinesException : Exception {

    private var code = Coroutines_ERROR_CODE_EXCEPTION

    constructor(code: Int, msg: String?) : super(msg) {
        this.code = code
    }

    constructor(code: Int) : super("") {
        this.code = code
    }

    fun getCode(): Int {
        return code
    }

    override fun toString(): String {
        return "ZixieCoroutinesException{" +
                "code=" + code +
                ",msg=" + message +
                '}'
    }
}