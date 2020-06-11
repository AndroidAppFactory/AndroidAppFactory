package com.bihe0832.android.lib.sqlite.impl

import android.content.Context

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/6/11.
 * Description: Description
 *
 */
object CommonDBManager {

    private var mApplicationContext: Context? = null
    private var commonDBHelperInstance: CommonDBHelper? = null

    fun init(context: Context) {
        mApplicationContext = context
        commonDBHelperInstance = CommonDBHelper(mApplicationContext)
    }

    fun getData(key: String): String {
        try {
            commonDBHelperInstance?.let {
                return CommonTableModel.getData(it, key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun saveData(key: String, value: String): Boolean {
        try {
            commonDBHelperInstance?.let {
                return CommonTableModel.saveData(it, key, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteData(key: String): Boolean {
        try {
            commonDBHelperInstance?.let {
                return CommonTableModel.clearData(it, key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteAll(): Int {
        try {
            commonDBHelperInstance?.let {
                return CommonTableModel.deleteAll(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
}