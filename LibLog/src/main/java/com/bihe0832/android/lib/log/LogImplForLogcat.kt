package com.bihe0832.android.lib.log

import android.util.Log

/**
 * Created by zixie on 2017/8/31.
 */
object LogImplForLogcat : LogImpl {

    override fun getName(): String {
        return "LogcatForZixie"
    }

    override fun i(tag: String?, msg: String?) {
        Log.i(tag, msg ?: "")
    }

    override fun d(tag: String?, msg: String?) {
        Log.d(tag, msg?: "")
    }

    override fun w(tag: String?, msg: String?) {
        Log.w(tag, msg?: "")
    }

    override fun e(tag: String?, msg: String?) {
        Log.e(tag, msg?: "")
    }

    override fun info(tag: String?, msg: String?) {
        Log.w(tag, msg?: "")
    }
}