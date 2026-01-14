package com.bihe0832.android.app.log

import android.app.Activity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.log.LoggerFile.TYPE_HTML
import com.bihe0832.android.framework.log.LoggerFile.TYPE_TEXT
import com.bihe0832.android.framework.router.showH5File
import com.bihe0832.android.framework.router.showFileContent


/**
 * AAF 文件日志工具类
 *
 * 提供特殊日志的文件记录功能，支持：
 * - 按模块分类记录日志
 * - 支持文本和 HTML 格式
 * - 日志查看和分享功能
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 */
object AAFLoggerFile {

    /** 更新模块日志标识 */
    const val MODULE_UPDATE = "udpate"

    /** 服务器通信模块日志标识 */
    const val MODULE_SERVER = "server"

    /**
     * 记录文本日志
     *
     * @param filePath 日志文件路径
     * @param tag 日志标签
     * @param msg 日志内容
     */
    @Synchronized
    fun log(filePath: String, tag: String, msg: String) {
        LoggerFile.log(filePath, tag, msg)
    }

    /**
     * 记录 HTML 格式日志
     *
     * @param filePath 日志文件路径
     * @param tag 日志标签
     * @param msg 日志内容
     */
    @Synchronized
    fun logH5(filePath: String, tag: String, msg: String) {
        LoggerFile.logH5(filePath, tag, msg)
    }

    /**
     * 分享指定模块的日志文件
     *
     * @param module 模块名称
     */
    fun sendLogByModule(module: String) {
        AAFFileTools.sendFile(LoggerFile.getZixieFileLogPathByModule(module))
    }

    /**
     * 在应用内查看指定模块的日志
     *
     * @param module 模块名称
     */
    fun showLogByModule(module: String) {
        showFileContent(
            getLogPathByModuleName(module), isReversed = true, showLine = true
        )
    }

    /**
     * 使用系统应用打开指定模块的日志文件
     *
     * @param activity 当前 Activity
     * @param module 模块名称
     */
    fun showLogByModule(activity: Activity, module: String) {
        AAFFileTools.openFileWithTips(activity, getLogPathByModuleName(module))
    }

    /**
     * 在 WebView 中查看 HTML 格式的日志
     *
     * @param module 模块名称
     */
    fun showLocalH5LogByModule(module: String) {
        showH5File(
            getLogPathByModuleName(module, TYPE_HTML)
        )
    }

    /**
     * 获取指定模块的日志文件路径
     *
     * @param module 模块名称
     * @param type 日志类型（文本或 HTML）
     * @return 日志文件路径
     */
    fun getLogPathByModuleName(module: String, type: Int): String {
        return LoggerFile.getZixieFileLogPathByModule(module, ZixieContext.getLogFolder(), type)
    }

    /**
     * 获取指定模块的文本日志文件路径
     *
     * @param module 模块名称
     * @return 日志文件路径
     */
    fun getLogPathByModuleName(module: String): String {
        return getLogPathByModuleName(module, TYPE_TEXT)
    }

    /**
     * 记录服务器通信日志
     *
     * @param msg 日志内容
     */
    fun logServer(msg: String) {
        LoggerFile.log(getLogPathByModuleName(MODULE_SERVER), msg)
    }

    /**
     * 记录更新模块日志
     *
     * @param msg 日志内容
     */
    fun logUpdate(msg: String) {
        LoggerFile.log(getLogPathByModuleName(MODULE_UPDATE), msg)

    }
}