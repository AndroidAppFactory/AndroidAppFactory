package com.bihe0832.android.framework.log

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.time.DateUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: 用户处理特殊日志
 *
 */
object LoggerFile {

    private const val TAG = "LoggerFile"

    private var mCanSaveSpecialFile = false
    private var mContext: Context? = null

    private val mLogFiles = ConcurrentHashMap<String, File?>()
    private val mBufferedWriters = ConcurrentHashMap<String, BufferedWriter?>()
    private const val DEFAULT_DURATION = DateUtil.MILLISECOND_OF_HOUR
    private const val DEFAULT_LOG_FILE_SIZE = FileUtils.SPACE_MB * 3
    private const val MAX_LOG_FILE_SIZE = FileUtils.SPACE_MB * 10

    private var mDuration = DEFAULT_DURATION
    private val mLoggerHandlerThread by lazy {
        HandlerThread("THREAD_ZIXIE_LOG_FILE", 5).also {
            it.start()
        }
    }

    private val mLoggerFileHandler by lazy { Handler(mLoggerHandlerThread.looper) }

    @Synchronized
    fun init(context: Context, isDebug: Boolean, duration: Long) {
        mContext = context
        mCanSaveSpecialFile = isDebug
        mDuration = if (duration > DEFAULT_DURATION) {
            duration
        } else {
            DEFAULT_DURATION
        }
    }

    @Synchronized
    fun init(context: Context, isDebug: Boolean) {
        init(context, isDebug, 7 * DateUtil.MILLISECOND_OF_DAY)
    }

    @Synchronized
    private fun reset(fileName: String) {
        if (mCanSaveSpecialFile) {
            if (mLogFiles[fileName] != null && mBufferedWriters[fileName] != null) {

            } else {
                try {
                    var file = File(fileName)
                    if (FileUtils.checkFileExist(fileName)) {
                        if (file.length() > MAX_LOG_FILE_SIZE) {
                            FileUtils.copyFile(
                                file, File(
                                    "${file.parentFile.absolutePath}${File.separator}${
                                        FileUtils.getFileNameWithoutEx(fileName)
                                    }_${DateUtil.getCurrentDateEN("HHmm")}.txt"
                                ), true
                            )
                        }
                    }
                    if (!FileUtils.checkFileExist(fileName)) {
                        file.createNewFile()
                    }
                    checkOldFile(file)
                    val bufferedWriter =
                        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), "UTF-8"))
                    mLogFiles[fileName] = file
                    ZLog.e(TAG, "ZLog Add New File !!!! $fileName")
                    mBufferedWriters[fileName] = bufferedWriter
                } catch (e: Exception) {
                    ZLog.e(TAG, "ZLog FLIE ERROR !!!! $e")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkOldFile(file: File) {
        FileUtils.deleteOldAsync(file.parentFile, mDuration)
    }

    private fun bufferSave(fileName: String, tag: String, msg: String?) {
        mLoggerFileHandler.post {
            try {
                mBufferedWriters[fileName]?.write("$tag $msg")
                mBufferedWriters[fileName]?.newLine()
                mBufferedWriters[fileName]?.flush()
            } catch (e: java.lang.Exception) {
                ZLog.e(TAG, "ZLog FLIE  ERROR !!!! $e")
                e.printStackTrace()
            }
        }
    }

    fun getZixieFileLogPathByModule(module: String): String {
        return ZixieContext.getLogFolder() + "${module}_${DateUtil.getCurrentDateEN("yyyyMMdd")}.txt"
    }

    fun log(filePath: String, msg: String) {
        ZLog.info(FileUtils.getFileNameWithoutEx(filePath), msg)
        logFile(filePath, DateUtil.getCurrentDateEN(), msg)
    }


    fun log(filePath: String, tag: String, msg: String) {
        ZLog.info(FileUtils.getFileNameWithoutEx(filePath), msg)
        logFile(filePath, tag, msg)
    }

    fun logFile(filePath: String, msg: String) {
        logFile(filePath, DateUtil.getCurrentDateEN(), msg)
    }

    fun logFile(filePath: String, tag: String, msg: String) {
        try {
            if (mCanSaveSpecialFile) {
                reset(filePath)
                bufferSave(filePath, tag, msg)
            }
        } catch (e: java.lang.Exception) {
            ZLog.e(TAG, "log ERROR !!!! $e")
            e.printStackTrace()
        }
    }
}