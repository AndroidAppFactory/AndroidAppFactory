package com.jygaming.android.framework.channel

import android.content.Context
import android.text.TextUtils
import android.util.Log
import java.io.IOException
import java.util.*


/**
 * 使用规则：
 *  在项目assets目录下添加文件channel.ini
 *  在 channel.ini中添加记录CHANNEL=XXX 其中XXX就是对应的channel值
 */

object ChannelTools {

    private const val CHANNEL_ID_KEY = "CHANNEL"
    private const val CHANNEL_ID_FILE = "channel.ini"
    private var mDefaultChannel = "000000"
    private var mChannel = ""

    fun init(context: Context) {
        mChannel = readChannelFromIni(context)
    }

    fun init(context: Context,channel : String) {
        mDefaultChannel = channel
        mChannel = readChannelFromIni(context)
    }

    fun getChannel(): String {
        if (TextUtils.isEmpty(mChannel)) {
            mChannel = mDefaultChannel
        }
        return mChannel
    }

    private fun readChannelFromIni(context: Context): String {
        return try {
            val inputStream = context?.resources?.assets?.open(CHANNEL_ID_FILE)
            val properties = Properties()
            properties.load(inputStream)
            inputStream?.close()
            properties.getProperty(CHANNEL_ID_KEY, "")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Channel", "CHANNEL ID ERROR")
            ""
        }
    }
}