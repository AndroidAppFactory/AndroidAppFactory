package me.yokeyword.fragmentation.helper

import java.lang.Exception

/**
 * Created by YoKey on 17/2/5.
 */
interface ExceptionHandler {
    fun onException(e: Exception)
}