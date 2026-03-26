package com.bihe0832.android.common.message.data.db;

import android.content.Context
import com.bihe0832.android.common.message.data.MessageInfoItem

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/6/11.
 * Description: Description
 *
 */
object MessageDBManager {

    private var mApplicationContext: Context? = null
    private var commonDBHelperInstance: MessageDBHelper? = null

    fun init(context: Context) {
        mApplicationContext = context
        if (null == commonDBHelperInstance) {
            commonDBHelperInstance = MessageDBHelper(mApplicationContext)
        }
    }

    fun getAll(): List<MessageInfoItem> {
        try {
            commonDBHelperInstance?.let {
                return MessageTableModel.getAllData(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mutableListOf()
    }

    fun getData(messageID: String): MessageInfoItem? {
        try {
            commonDBHelperInstance?.let {
                return MessageTableModel.getData(it, messageID)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun saveData(info: MessageInfoItem): Boolean {
        try {
            commonDBHelperInstance?.let {
                return MessageTableModel.saveData(it, info)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteData(data: MessageInfoItem): Boolean {
        try {
            commonDBHelperInstance?.let {
                return MessageTableModel.clearData(it, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}