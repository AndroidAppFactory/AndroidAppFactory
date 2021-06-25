package com.bihe0832.android.framework.log

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.DateUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-03-05.
 * Description: 用户处理特殊日志
 *
 */
object LoggerFile {
    private var mCanSaveSpecialFile = false
    private var mContext: Context? = null
    private val mLogFiles = ConcurrentHashMap<String, File?>()
    private val mBufferedWriters = ConcurrentHashMap<String, BufferedWriter?>()

    private val mLoggerHandlerThread by lazy {
        HandlerThread("THREAD_ZIXIE_LOG_FILE", 5).also {
            it.start()
        }
    }

    private val mLoggerFileHandler by lazy { Handler(mLoggerHandlerThread.looper) }

    @Synchronized
    fun init(context: Context, isDebug: Boolean) {
        mContext = context
        mCanSaveSpecialFile = isDebug
        ZLog.setDebug(isDebug)
    }

    @Synchronized
    private fun reset(fileName: String) {
        if (mCanSaveSpecialFile) {
            if (mLogFiles[fileName] != null && mBufferedWriters[fileName] != null) {

            } else {
                try {
                    var file = File(getFilePathByName(fileName))
                    if (!FileUtils.checkFileExist(getFilePathByName(fileName))) {
                        file.createNewFile()
                    }
                    val bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8"))
                    mLogFiles[fileName] = file
                    mBufferedWriters[fileName] = bufferedWriter
                } catch (e: Exception) {
                    ZLog.e("ZLog FLIE ERROR !!!!")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun bufferSave(fileName: String, msg: String?) {
        mLoggerFileHandler.post {
            try {
                mBufferedWriters[fileName]?.write("${DateUtil.getCurrentDateEN()} $msg")
                mBufferedWriters[fileName]?.newLine()
                mBufferedWriters[fileName]?.flush()
            } catch (e: java.lang.Exception) {
                ZLog.e("ZLog FLIE  ERROR !!!!")
                e.printStackTrace()
            }
        }
    }

    private fun getFilePathByName(module: String): String {
        return ZixieContext.getLogFolder() + "/${module}_${DateUtil.getCurrentDateEN("yyyyMMdd")}.txt"
    }

    fun log(module: String, msg: String) {
        ZLog.info(module, msg)
        try {
            if (mCanSaveSpecialFile) {
                reset(module)
                bufferSave(module, msg)
            }
        } catch (e: java.lang.Exception) {
            ZLog.e("Logger ERROR !!!!")
            e.printStackTrace()
        }
    }

    fun openLog(module: String) {
        try { //设置intent的data和Type属性
            mContext?.let {
                FileUtils.openFile(it, getFilePathByName(module), "*/*")
            }
        } catch (e: java.lang.Exception) { //当系统没有携带文件打开软件，提示
            e.printStackTrace()
        }
    }

    fun sendLog(module: String) {
        try { //设置intent的data和Type属性
            mContext?.let { context ->
                try { //设置intent的data和Type属性
                    FileUtils.sendFile(context, getFilePathByName(module), "*/*")
                } catch (e: java.lang.Exception) { //当系统没有携带文件打开软件，提示
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception) { //当系统没有携带文件打开软件，提示
            ZLog.e("Logger ERROR !!!!")
            e.printStackTrace()
        }
    }

}