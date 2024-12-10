package com.bihe0832.android.framework.log

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.apk.APKUtils
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
@SuppressLint("StaticFieldLeak")
object LoggerFile {

    val TYPE_HTML = 1
    val TYPE_TEXT = 2

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
    private fun reset(fileName: String, type: Int) {
        if (mCanSaveSpecialFile) {
            if (mLogFiles[fileName] != null && mBufferedWriters[fileName] != null) {

            } else {
                try {
                    val file = File(fileName)
                    if (FileUtils.checkFileExist(fileName)) {
                        if (file.length() > MAX_LOG_FILE_SIZE) {
                            FileUtils.copyFile(
                                file, File(
                                    "${file.parentFile.absolutePath}${File.separator}${
                                        FileUtils.getFileNameWithoutEx(
                                            fileName
                                        )
                                    }_${DateUtil.getCurrentDateEN("HHmm")}." + FileUtils.getExtensionName(
                                        fileName
                                    )
                                ), true
                            )
                        }
                    }
                    val hasExist = FileUtils.checkFileExist(fileName)
                    if (!hasExist) {
                        file.createNewFile()
                    }
                    checkOldFile(file)
                    val bufferedWriter =
                        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), "UTF-8"))
                    mLogFiles[fileName] = file
                    ZLog.e(TAG, "ZLog Add New File !!!! $fileName")
                    mBufferedWriters[fileName] = bufferedWriter
                    if (!hasExist && type == TYPE_HTML) {
                        bufferSave(
                            fileName, TYPE_TEXT, "", " <!DOCTYPE HTML>\n" +
                                    "<head>\n" +
                                    "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                                    "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1,maximum-scale=1,user-scalable=0\" />\n" +
                                    "  <meta name=\"apple-mobile-web-app-capable\" content=\"yes\" />\n" +
                                    "  <meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" />\n" +
                                    "  <meta name=\"format-detection\" content=\"telephone=no\" />\n" +
                                    "  <meta http-equiv=\"Pragma\" content=\"no-cache\">\n" +
                                    "  <meta http-equiv=\"Cache-Control\" content=\"no-cache, must-revalidate\">\n" +
                                    "  <meta http-equiv=\"Expires\" content=\"0\">\n" +
                                    "  <link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.bihe0832.com/css/global.css\" />\n" +
                                    "  <style type=\"text/css\">\n" +
                                    "   body {\n" +
                                    "      line-height: 1;\n" +
                                    "      font-family: Microsoft Yahei;\n" +
                                    "      color: #333;\n" +
                                    "      background: #fff;\n" +
                                    "      font-size: 0.9em;\n" +
                                    "      margin-top: 10px;\n" +
                                    "      margin-left: 6px;\n" +
                                    "      margin-right: 6px;\n" +
                                    "    }\n" +
                                    "    div{       \n" +
                                    "      width: 100%;       \n" +
                                    "      color: #333;\n" +
                                    "      line-height: 2em;\n" +
                                    "      border-bottom: 0.5px solid #333;\n" +
                                    "    }  \n" +
                                    "    </style>\n" +
                                    "  <title>" + APKUtils.getAppName(mContext) +
                                    "</title>\n" +
                                    "</head>\n" +
                                    "<body>"
                        )
                    }
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

    private fun bufferSave(fileName: String, type: Int, tag: String, msg: String?) {
        mLoggerFileHandler.post {
            try {
                if (type == TYPE_HTML) {
                    mBufferedWriters[fileName]?.write(
                        "<div>$tag ${
                            msg?.replace(
                                "\n",
                                "<BR>"
                            )
                        }</div>"
                    )
                } else {
                    mBufferedWriters[fileName]?.write("$tag $msg")
                }
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
        logFile(filePath, TYPE_TEXT, DateUtil.getCurrentDateEN(), msg)
    }


    fun log(filePath: String, tag: String, msg: String) {
        ZLog.info(FileUtils.getFileNameWithoutEx(filePath), msg)
        logFile(filePath, TYPE_TEXT, tag, msg)
    }

    fun logFile(filePath: String, msg: String) {
        logFile(filePath, TYPE_TEXT, DateUtil.getCurrentDateEN(), msg)
    }

    fun logFile(filePath: String, type: Int, tag: String, msg: String) {
        try {
            if (mCanSaveSpecialFile) {
                reset(filePath, type)
                bufferSave(filePath, type, tag, msg)
            }
        } catch (e: java.lang.Exception) {
            ZLog.e(TAG, "log ERROR !!!! $e")
            e.printStackTrace()
        }
    }

    fun getAudioH5LogData(filePath: String, type: String): String {
        return "这是一个测试<audio controls style=\"height: 1em;\">\n" +
                "  <source src=\"file://${filePath}\" type=\"${type}\">\n" +
                "  Your browser does not support the audio element.\n" +
                "</audio>"
    }
}