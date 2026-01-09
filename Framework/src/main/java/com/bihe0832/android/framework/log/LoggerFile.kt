package com.bihe0832.android.framework.log

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
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
    private var mDefaultHeader = ""

    private val mLogFiles = ConcurrentHashMap<String, File?>()
    private val mBufferedWriters = ConcurrentHashMap<String, BufferedWriter?>()
    private val fileNameMap = ConcurrentHashMap<String, String>()

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
    fun init(context: Context, openLog: Boolean, duration: Long, default: String) {
        mContext = context
        mCanSaveSpecialFile = openLog
        mDuration = if (duration > DEFAULT_DURATION) {
            duration
        } else {
            DEFAULT_DURATION
        }
        mDefaultHeader = default
    }

    @Synchronized
    fun init(context: Context, openLog: Boolean, duration: Long) {
        init(context, openLog, duration, "")
    }

    @Synchronized
    fun init(context: Context, openLog: Boolean) {
        init(context, openLog, 7 * DateUtil.MILLISECOND_OF_DAY, "")
    }

    private fun checkOldFile(file: File) {
        file.parentFile?.let { FileUtils.deleteOldAsync(it, mDuration) }
    }

    @Synchronized
    private fun innerReset(fileName: String, initMsg: String) {
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
                    bufferSave(fileName, TYPE_TEXT, "", initMsg)
                } catch (e: Exception) {
                    ZLog.e(TAG, "ZLog FLIE ERROR !!!! [filePath:$fileName] $e")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun defaultReset(fileName: String, type: Int) {
        val msg = if (type == TYPE_HTML) {
            getH5LogHeader() + getH5Sort() + getH5Content()
        } else {
            mDefaultHeader
        }
        innerReset(fileName, msg)
    }

    fun getH5LogHeader(): String {
        return getH5LogHeader("<title>" + APKUtils.getAppName(mContext) + "</title>\n")
    }

    fun getH5LogHeader(userHead: String): String {
        return " <!DOCTYPE HTML>\n" +
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
                "      background-color: #fff;\n" +
                "      color: #333;\n" +
                "      font-size: 12px;\n" +
                "      margin-top: 10px;\n" +
                "    }\n" +
                "    div{       \n" +
                "      width: 100%;       \n" +
                "      color: #333;\n" +
                "      line-height: 2em;\n" +
                "      border-bottom: 1px solid #333;\n" +
                "    }  \n" +
                "    .container {\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        border-bottom: 0;\n" +
                "    }\n" +
                "    .button-container{\n" +
                "      display: flex;\n" +
                "        align-items: center;\n" +
                "        justify-content: center;\n" +
                "        display: flex;\n" +
                "        flex-direction: row;\n" +
                "        border-bottom: 0;\n" +
                "    }\n" +
                "    button {\n" +
                "      width:40%; \n" +
                "      height: 40px;\n" +
                "      margin-left: 20px;\n" +
                "      margin-right:  20px;\n" +
                "      margin-top: 5px;\n" +
                "      margin-bottom: 5px;" +
                "    }\n" +
                "    </style>\n" +
                "    <script>\n" +
                "      function reverseOrder() {\n" +
                "          var container = document.getElementById('logInfoContainer');\n" +
                "          container.style.flexDirection = 'column-reverse';\n" +
                "      }\n" +
                "      function normalOrder() {\n" +
                "          var container = document.getElementById('logInfoContainer');\n" +
                "          container.style.flexDirection = 'column';\n" +
                "      }\n" +
                "  </script>\n" +
                userHead +
                "</head>\n" +
                "<body>\n"
    }

    fun getH5Sort(): String {
        return "  <div class=\"button-container\" >\n" +
                "    <button onclick=\"reverseOrder()\">倒序展示</button>\n" +
                "    <button onclick=\"normalOrder()\">正序展示</button>\n" +
                "  </div>\n"
    }

    fun getH5Content(): String {
        return "<div class=\"container\" id=\"logInfoContainer\">"
    }

    fun getH5LogLine(tag: String, msg: String?): String {
        return "<div>$tag ${
            msg?.replace(
                "\n", "<BR>"
            )
        }</div>"
    }

    private fun bufferSave(fileName: String, type: Int, tag: String, msg: String?) {
        mLoggerFileHandler.post {
            try {
                if (type == TYPE_HTML) {
                    mBufferedWriters[fileName]?.write(
                        getH5LogLine(tag, msg)
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
        return getZixieFileLogPathByModule(module, ZixieContext.getLogFolder(), TYPE_TEXT)
    }

    fun getZixieFileLogPathByModule(module: String, type: Int): String {
        return getZixieFileLogPathByModule(module, ZixieContext.getLogFolder(), type)
    }

    fun getZixieFileLogPathByModule(module: String, folder: String, type: Int): String {
        var path = ""
        if (fileNameMap.containsKey(module + type)) {
            path = fileNameMap[module + type] ?: ""
        }
        if (TextUtils.isEmpty(path)) {
            val ext = if (type == TYPE_HTML) {
                ".html"
            } else {
                ".txt"
            }
            path = FileUtils.getFolderPathWithSeparator(folder) + "${module}_${
                DateUtil.getCurrentDateEN("yyyyMMdd")
            }$ext"
            fileNameMap[module + type] = path
        }
        return path
    }

    fun initFile(fileName: String, headerInfo: String, needClear: Boolean) {
        if (needClear) {
            FileUtils.writeToFile(fileName, "", false)
            if (mBufferedWriters[fileName] != null) {
                try {
                    mBufferedWriters[fileName]?.close()
                    mBufferedWriters.remove(fileName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        innerReset(fileName, headerInfo)
    }

    fun logFile(filePath: String, type: Int, tag: String, msg: String) {
        try {
            ZLog.d(FileUtils.getFileNameWithoutEx(filePath), msg)
            if (mCanSaveSpecialFile) {
                defaultReset(filePath, type)
                bufferSave(filePath, type, tag, msg)
            }
        } catch (e: java.lang.Exception) {
            ZLog.e(TAG, "log ERROR !!!! $e")
            e.printStackTrace()
        }
    }

    fun logH5(filePath: String, tag: String, msg: String) {
        logFile(filePath, TYPE_HTML, tag, msg)
    }

    fun logH5(filePath: String, msg: String) {
        logH5(filePath, DateUtil.getCurrentDateEN("MM-dd HH:mm:ss"), msg)
    }

    fun log(filePath: String, tag: String, msg: String) {
        logFile(filePath, TYPE_TEXT, tag, msg)
    }

    fun log(filePath: String, msg: String) {
        log(filePath, DateUtil.getCurrentDateEN("MM-dd HH:mm:ss"), msg)
    }

    fun getAudioH5LogData(filePath: String, type: String): String {
        return "<audio controls style=\"height: 1em;\">\n" + "  <source src=\"file://${filePath}\" type=\"${type}\">\n" + "  Your browser does not support the audio element.\n" + "</audio>"
    }
}