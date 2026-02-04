package com.bihe0832.android.base.compose.debug.download

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.DownloadFileUtils
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.download.file.DownloadFileManager
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.intent.IntentUtils

private const val TAG = "DownloadTest"

/**
 * LibDownload 完整测试页面
 * 
 * 测试覆盖：
 * - 下载状态 (DownloadStatus)
 * - 暂停类型 (DownloadPauseType)
 * - 单任务控制
 * - 批量控制
 * - 网络状态模拟
 * - 状态查询
 * - 自动化测试
 */
@Preview
@Composable
fun DebugDownloadTestView() {
    DebugContent {
        DebugComposeItem(
            "下载及安装Debug调试",
            "DebugDownloadView"
        ) { DebugDownloadView() }
        // ========== 🚀 自动化测试 ==========
        DebugTips("🚀 自动化测试")

        DebugItem("一键执行全部测试") { context ->
            runAllTests(context)
        }

        // ========== 📥 任务创建 ==========
        DebugTips("📥 任务创建")
        
        DebugItem("添加测试任务(WiFi only)") { context ->
            addTestTask(context, useMobile = false, tag = "WiFi")
        }
        
        DebugItem("添加测试任务(允许移动网络)") { context ->
            addTestTask(context, useMobile = true, tag = "Mobile")
        }
        
        DebugItem("批量添加3个任务") { context ->
            addBatchTasks(context, 3)
        }
        
        DebugItem("添加小文件任务(快速完成)") { context ->
            addSmallFileTask(context)
        }

        // ========== 🎮 单任务控制 ==========
        DebugTips("🎮 单任务控制")
        
        DebugItem("暂停第一个下载中任务(USER)") {
            pauseFirstDownloadingTask(DownloadPauseType.PAUSED_BY_USER)
        }
        
        DebugItem("暂停第一个下载中任务(ALL)") {
            pauseFirstDownloadingTask(DownloadPauseType.PAUSED_BY_ALL)
        }
        
        DebugItem("恢复第一个暂停任务") {
            resumeFirstPausedTask()
        }
        
        DebugItem("删除第一个任务") {
            deleteFirstTask()
        }

        // ========== 📋 批量控制 ==========
        DebugTips("📋 批量控制")
        
        DebugItem("pauseAll(可自动恢复)") {
            logAction("pauseAll(PAUSED_BY_ALL)")
            printAllTasksStatus("执行前")
            DownloadFileUtils.pauseAll(true, true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("pauseAll(用户暂停)") {
            logAction("pauseAll(PAUSED_BY_USER)")
            printAllTasksStatus("执行前")
            DownloadFileUtils.pauseAll(true, false)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("pauseDownloading(可恢复)") {
            logAction("pauseDownloading(PAUSED_BY_ALL)")
            printAllTasksStatus("执行前")
            DownloadFileManager.pauseDownloadingTask(DownloadPauseType.PAUSED_BY_ALL, true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("pauseDownloading(用户)") {
            logAction("pauseDownloading(PAUSED_BY_USER)")
            printAllTasksStatus("执行前")
            DownloadFileManager.pauseDownloadingTask(DownloadPauseType.PAUSED_BY_USER, true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("pauseWaiting(可恢复)") {
            logAction("pauseWaiting(PAUSED_BY_ALL)")
            printAllTasksStatus("执行前")
            DownloadFileManager.pauseWaitingTask(DownloadPauseType.PAUSED_BY_ALL, true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("pauseWaiting(用户)") {
            logAction("pauseWaiting(PAUSED_BY_USER)")
            printAllTasksStatus("执行前")
            DownloadFileManager.pauseWaitingTask(DownloadPauseType.PAUSED_BY_USER, true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("resumeAll") {
            logAction("resumeAll")
            printAllTasksStatus("执行前")
            DownloadFileUtils.resumeAll(true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("resumeFailedTask") {
            logAction("resumeFailedTask")
            printAllTasksStatus("执行前")
            DownloadFileManager.resumeFailedTask(true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("resumePauseTask(含USER)") {
            logAction("resumePauseTask(includeUserPaused=true)")
            printAllTasksStatus("执行前")
            DownloadFileManager.resumePauseTask(true, true)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }
        
        DebugItem("resumePauseTask(排除USER)") {
            logAction("resumePauseTask(includeUserPaused=false)")
            printAllTasksStatus("执行前")
            DownloadFileManager.resumePauseTask(true, false)
            ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
        }

        // ========== 🌐 网络控制 ==========
        DebugTips("🌐 网络控制")
        
        DebugItem("关闭 WiFi (跳转设置)") { context ->
            logAction("closeWifi - openSettings")
            printAllTasksStatus("关闭前")
            ZLog.d(TAG, "跳转到 WiFi 设置，请手动关闭 WiFi")
            IntentUtils.startSettings(context, Settings.ACTION_WIFI_SETTINGS)
        }
        
        DebugItem("打开 WiFi") {
            logAction("openWifi")
            printAllTasksStatus("打开前")
            WifiManagerWrapper.openWifi()
            ZLog.d(TAG, "WiFi 打开中，等待网络恢复...")
            ThreadManager.getInstance().start({ 
                printAllTasksStatus("打开后")
            }, 3000)
        }
        
        DebugItem("检查任务状态") {
            logAction("checkStatus")
            printAllTasksStatus("当前状态")
            printPausedTasksByType()
        }

        // ========== 🔍 状态查询 ==========
        DebugTips("🔍 状态查询")
        
        DebugItem("打印所有任务状态") {
            printAllTasksStatus("当前")
        }
        
        DebugItem("打印各状态任务数统计") {
            printTaskStatistics()
        }
        
        DebugItem("打印暂停任务(按类型分类)") {
            printPausedTasksByType()
        }
        
        DebugItem("打印 hasPauseAll 状态") {
            val hasPauseAll = DownloadFileManager.hasPauseAll()
            ZLog.d(TAG, "========== hasPauseAll = $hasPauseAll ==========")
        }

        // ========== 🧹 清理 ==========
        DebugTips("🧹 清理")
        
        DebugItem("删除所有任务") {
            deleteAllTasks()
        }
        
        DebugItem("重置 hasPauseAll (resumeAll)") {
            logAction("重置 hasPauseAll")
            DownloadFileUtils.resumeAll(true)
            ZLog.d(TAG, "hasPauseAll 已重置为 false")
        }
    }
}

// ==================== 辅助函数 ====================

private fun logAction(action: String) {
    ZLog.d(TAG, "")
    ZLog.d(TAG, "========== 操作: $action ==========")
}

private fun logStep(step: Int, name: String) {
    ZLog.d(TAG, "")
    ZLog.d(TAG, ">>>>>>>>>> Step $step: $name >>>>>>>>>>")
}

private fun logResult(passed: Boolean, message: String) {
    val status = if (passed) "[PASS]" else "[FAIL]"
    ZLog.d(TAG, "$status $message")
}

private val testDownloadListener = object : SimpleDownloadListener() {
    override fun onWait(item: DownloadItem) {
        ZLog.d(TAG, "  [回调] onWait: ${getShortUrl(item.downloadURL)}")
    }

    override fun onStart(item: DownloadItem) {
        ZLog.d(TAG, "  [回调] onStart: ${getShortUrl(item.downloadURL)}")
    }

    override fun onProgress(item: DownloadItem) {
        // 每 20% 输出一次进度
        ZLog.d(TAG, "  [回调] onProgress: ${getShortUrl(item.downloadURL)}, progress=${item.getProcessDesc()}")
    }

    override fun onPause(item: DownloadItem, pauseType: Int) {
        ZLog.d(TAG, "  [回调] onPause: ${getShortUrl(item.downloadURL)}, pauseType=${getPauseTypeName(pauseType)}")
    }

    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
        ZLog.d(TAG, "  [回调] onFail: ${getShortUrl(item.downloadURL)}, errorCode=$errorCode, msg=$msg")
    }

    override fun onComplete(filePath: String, item: DownloadItem): String {
        ZLog.d(TAG, "  [回调] onComplete: ${getShortUrl(item.downloadURL)}, filePath=$filePath")
        return filePath
    }
}

private fun addTestTask(context: Context, useMobile: Boolean, tag: String) {
    logAction("添加测试任务($tag)")
    val url = URL_YYB_TTS
    ZLog.d(TAG, "URL: $url")
    ZLog.d(TAG, "useMobile: $useMobile")
    
    DownloadFile.download(
        context,
        url,
        useMobile,
        testDownloadListener
    )
    
    ThreadManager.getInstance().start({ printAllTasksStatus("添加后") }, 1000)
}

private fun addBatchTasks(context: Context, count: Int) {
    logAction("批量添加 $count 个任务")
    
    val urls = listOf(URL_YYB_WZ, URL_YYB_TTS, URL_YYB_CHANNEL)
    
    for (i in 0 until count.coerceAtMost(urls.size)) {
        ZLog.d(TAG, "添加任务 ${i + 1}: ${urls[i]}")
        DownloadFile.download(context, urls[i], true, testDownloadListener)
    }
    
    ThreadManager.getInstance().start({ printAllTasksStatus("添加后") }, 5000)
}

private fun addSmallFileTask(context: Context) {
    logAction("添加小文件任务")
    ZLog.d(TAG, "URL: $URL_CONFIG")
    
    DownloadFile.download(context, URL_CONFIG, true, testDownloadListener)
    
    ThreadManager.getInstance().start({ printAllTasksStatus("添加后") }, 500)
}

private fun pauseFirstDownloadingTask(pauseType: Int) {
    logAction("暂停第一个下载中任务(${getPauseTypeName(pauseType)})")
    printAllTasksStatus("执行前")
    
    val downloadingTasks = DownloadFileManager.getDownloadingTask()
    if (downloadingTasks.isEmpty()) {
        ZLog.d(TAG, "⚠️ 没有下载中的任务")
        return
    }
    
    val task = downloadingTasks.first()
    ZLog.d(TAG, "暂停任务: ${getShortUrl(task.downloadURL)}")
    DownloadFileManager.pauseTask(task.downloadID, pauseType)
    
    ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
}

private fun resumeFirstPausedTask() {
    logAction("恢复第一个暂停任务")
    printAllTasksStatus("执行前")
    
    val pausedTasks = DownloadFileManager.getAllTask().filter { 
        it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED 
    }
    
    if (pausedTasks.isEmpty()) {
        ZLog.d(TAG, "⚠️ 没有暂停的任务")
        return
    }
    
    val task = pausedTasks.first()
    ZLog.d(TAG, "恢复任务: ${getShortUrl(task.downloadURL)}, 原 pauseType=${getPauseTypeName(task.pauseType)}")
    DownloadFileUtils.resumeDownload(task.downloadID, true)
    
    ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
}

private fun deleteFirstTask() {
    logAction("删除第一个任务")
    printAllTasksStatus("执行前")
    
    val allTasks = DownloadFileManager.getAllTask()
    if (allTasks.isEmpty()) {
        ZLog.d(TAG, "⚠️ 没有任务")
        return
    }
    
    val task = allTasks.first()
    ZLog.d(TAG, "删除任务: ${getShortUrl(task.downloadURL)}")
    DownloadFileUtils.deleteTask(task.downloadID, true)
    
    ThreadManager.getInstance().start({ printAllTasksStatus("执行后") }, 500)
}



private fun deleteAllTasks() {
    logAction("删除所有任务")
    
    val allTasks = DownloadFileManager.getAllTask()
    ZLog.d(TAG, "待删除任务数: ${allTasks.size}")
    
    allTasks.forEach { task ->
        ZLog.d(TAG, "  删除: ${getShortUrl(task.downloadURL)}")
        DownloadFileUtils.deleteTask(task.downloadID, true)
    }
    
    ThreadManager.getInstance().start({
        ZLog.d(TAG, "删除后任务数: ${DownloadFileManager.getAllTask().size}")
    }, 500)
}

private fun printAllTasksStatus(label: String) {
    val allTasks = DownloadFileManager.getAllTask()
    ZLog.d(TAG, "---------- $label 状态 (共 ${allTasks.size} 个任务) ----------")
    
    if (allTasks.isEmpty()) {
        ZLog.d(TAG, "  (无任务)")
        return
    }
    
    allTasks.forEachIndexed { index, task ->
        ZLog.d(TAG, "  任务${index + 1}: ${getShortUrl(task.downloadURL)}")
        ZLog.d(TAG, "         status=${getStatusName(task.status)}")
        ZLog.d(TAG, "         pauseType=${getPauseTypeName(task.pauseType)}")
        ZLog.d(TAG, "         progress=${task.process}%")
        ZLog.d(TAG, "         useMobile=${task.isDownloadWhenUseMobile}")
    }
}

private fun printTaskStatistics() {
    logAction("任务统计")
    
    val allTasks = DownloadFileManager.getAllTask()
    val downloading = DownloadFileManager.getDownloadingTask().size
    val waiting = DownloadFileManager.getWaitingTask().size
    val finished = DownloadFileManager.getFinishedTask().size
    val paused = allTasks.count { it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED }
    val failed = allTasks.count { it.status == DownloadStatus.STATUS_DOWNLOAD_FAILED }
    
    ZLog.d(TAG, "========== 任务统计 ==========")
    ZLog.d(TAG, "  总任务数: ${allTasks.size}")
    ZLog.d(TAG, "  下载中: $downloading")
    ZLog.d(TAG, "  等待中: $waiting")
    ZLog.d(TAG, "  已暂停: $paused")
    ZLog.d(TAG, "  已失败: $failed")
    ZLog.d(TAG, "  已完成: $finished")
    ZLog.d(TAG, "  hasPauseAll: ${DownloadFileManager.hasPauseAll()}")
    ZLog.d(TAG, "==============================")
}

private fun printPausedTasksByType() {
    logAction("暂停任务分类")
    
    val pausedTasks = DownloadFileManager.getAllTask().filter { 
        it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED 
    }
    
    ZLog.d(TAG, "========== 暂停任务分类 (共 ${pausedTasks.size} 个) ==========")
    
    val byMobile = pausedTasks.filter { it.pauseType == DownloadPauseType.PAUSED_BY_MOBILE_NETWORK }
    val byUser = pausedTasks.filter { it.pauseType == DownloadPauseType.PAUSED_BY_USER }
    val byAll = pausedTasks.filter { it.pauseType == DownloadPauseType.PAUSED_BY_ALL }
    val byPending = pausedTasks.filter { it.pauseType == DownloadPauseType.PAUSED_PENDING_START }
    val byNetworkError = pausedTasks.filter { it.pauseType == DownloadPauseType.PAUSED_BY_NETWORK_ERROR }
    
    ZLog.d(TAG, "  PAUSED_BY_MOBILE_NETWORK: ${byMobile.size}")
    byMobile.forEach { ZLog.d(TAG, "    - ${getShortUrl(it.downloadURL)}") }
    
    ZLog.d(TAG, "  PAUSED_BY_USER: ${byUser.size}")
    byUser.forEach { ZLog.d(TAG, "    - ${getShortUrl(it.downloadURL)}") }
    
    ZLog.d(TAG, "  PAUSED_BY_ALL: ${byAll.size}")
    byAll.forEach { ZLog.d(TAG, "    - ${getShortUrl(it.downloadURL)}") }
    
    ZLog.d(TAG, "  PAUSED_PENDING_START: ${byPending.size}")
    byPending.forEach { ZLog.d(TAG, "    - ${getShortUrl(it.downloadURL)}") }
    
    ZLog.d(TAG, "  PAUSED_BY_NETWORK_ERROR: ${byNetworkError.size}")
    byNetworkError.forEach { ZLog.d(TAG, "    - ${getShortUrl(it.downloadURL)}") }
    
    ZLog.d(TAG, "================================================")
}

private fun getStatusName(status: Int): String {
    return when (status) {
        DownloadStatus.NO_DOWNLOAD -> "NO_DOWNLOAD"
        DownloadStatus.STATUS_DOWNLOAD_WAITING -> "WAITING"
        DownloadStatus.STATUS_DOWNLOAD_STARTED -> "STARTED"
        DownloadStatus.STATUS_DOWNLOADING -> "DOWNLOADING"
        DownloadStatus.STATUS_HAS_DOWNLOAD -> "HAS_DOWNLOAD"
        DownloadStatus.STATUS_DOWNLOAD_SUCCEED -> "SUCCEED"
        DownloadStatus.STATUS_DOWNLOAD_PAUSED -> "PAUSED"
        DownloadStatus.STATUS_DOWNLOAD_FAILED -> "FAILED"
        DownloadStatus.STATUS_DOWNLOAD_DELETE -> "DELETE"
        else -> "UNKNOWN($status)"
    }
}

private fun getPauseTypeName(pauseType: Int): String {
    return when (pauseType) {
        0 -> "NONE"
        DownloadPauseType.PAUSED_BY_MOBILE_NETWORK -> "PAUSED_BY_MOBILE_NETWORK"
        DownloadPauseType.PAUSED_BY_USER -> "PAUSED_BY_USER"
        DownloadPauseType.PAUSED_BY_ALL -> "PAUSED_BY_ALL"
        DownloadPauseType.PAUSED_PENDING_START -> "PAUSED_PENDING_START"
        DownloadPauseType.PAUSED_BY_NETWORK_ERROR -> "PAUSED_BY_NETWORK_ERROR"
        else -> "UNKNOWN($pauseType)"
    }
}

private fun getShortUrl(url: String?): String {
    return FileUtils.getFileName(url)
}

// ==================== 自动化测试 ====================

/**
 * 等待至少有指定数量的任务进入下载状态（非 WAITING）
 * @param minCount 最少需要的任务数
 * @param maxWaitMs 最大等待时间（毫秒）
 * @return 是否等待成功
 */
private fun waitForTasksReady(minCount: Int, maxWaitMs: Long = 10000): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < maxWaitMs) {
        val allTasks = DownloadFileManager.getAllTask()
        // 统计非 WAITING 状态的任务（已开始下载、暂停、完成等都算）
        val readyTasks = allTasks.filter { 
            it.status != DownloadStatus.STATUS_DOWNLOAD_WAITING 
        }
        if (allTasks.size >= minCount && readyTasks.size >= minCount) {
            ZLog.d(TAG, "  任务准备就绪: 总数=${allTasks.size}, 已开始=${readyTasks.size}")
            return true
        }
        Thread.sleep(500)
    }
    val allTasks = DownloadFileManager.getAllTask()
    ZLog.d(TAG, "  等待超时: 总数=${allTasks.size}, 状态=${allTasks.map { getStatusName(it.status) }}")
    return false
}

/**
 * 等待至少有一个任务进入下载中状态
 * @param maxWaitMs 最大等待时间（毫秒）
 * @return 是否等待成功
 */
private fun waitForDownloading(maxWaitMs: Long = 10000): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < maxWaitMs) {
        val downloading = DownloadFileManager.getDownloadingTask()
        if (downloading.isNotEmpty()) {
            ZLog.d(TAG, "  任务已开始下载: ${getShortUrl(downloading.first().downloadURL)}")
            return true
        }
        Thread.sleep(500)
    }
    ZLog.d(TAG, "  等待下载中任务超时")
    return false
}

/**
 * 等待至少有一个任务下载完成
 * @param maxWaitMs 最大等待时间（毫秒）
 * @return 是否等待成功
 */
private fun waitForFinished(maxWaitMs: Long = 15000): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < maxWaitMs) {
        val finished = DownloadFileManager.getFinishedTask()
        if (finished.isNotEmpty()) {
            ZLog.d(TAG, "  任务已完成: ${getShortUrl(finished.first().downloadURL)}")
            return true
        }
        Thread.sleep(500)
    }
    ZLog.d(TAG, "  等待下载完成超时")
    return false
}

/**
 * 等待任务状态变化（用于暂停/恢复后的验证）
 * @param taskId 任务ID
 * @param expectedStatus 期望的状态
 * @param maxWaitMs 最大等待时间
 * @return 是否等待成功
 */
private fun waitForTaskStatus(taskId: Long, expectedStatus: Int, maxWaitMs: Long = 5000): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < maxWaitMs) {
        val task = DownloadFileManager.getAllTask().find { it.downloadID == taskId }
        if (task?.status == expectedStatus) {
            return true
        }
        Thread.sleep(300)
    }
    return false
}

private fun runAllTests(context: Context) {
    ZLog.d(TAG, "")
    ZLog.d(TAG, "============================================")
    ZLog.d(TAG, "       LibDownload 自动化测试开始")
    ZLog.d(TAG, "============================================")
    ZLog.d(TAG, "测试时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
    ZLog.d(TAG, "============================================")
    
    val testResults = mutableListOf<Pair<String, Boolean>>()
    val startTime = System.currentTimeMillis()
    
    ThreadManager.getInstance().start {
        try {
            // Step 1: 清理环境
            logStep(1, "清理环境")
            deleteAllTasksSync()
            Thread.sleep(500)
            val step1Pass = DownloadFileManager.getAllTask().isEmpty()
            testResults.add("清理环境" to step1Pass)
            logResult(step1Pass, "清理环境 - 任务数: ${DownloadFileManager.getAllTask().size}")
            
            // Step 2: 基础下载测试
            logStep(2, "基础下载测试")
            DownloadFile.download(context, URL_CONFIG, true, testDownloadListener)
            val step2Pass = waitForFinished(15000)  // 等待小文件下载完成
            val finishedCount = DownloadFileManager.getFinishedTask().size
            testResults.add("基础下载测试" to step2Pass)
            logResult(step2Pass, "基础下载测试 - 完成任务数: $finishedCount")
            deleteAllTasksSync()
            Thread.sleep(500)
            
            // Step 3: 单任务暂停/恢复测试
            logStep(3, "单任务暂停/恢复测试")
            DownloadFile.download(context, URL_YYB_WZ, true, testDownloadListener)
            
            if (!waitForDownloading()) {
                testResults.add("单任务暂停/恢复测试" to false)
                logResult(false, "  等待下载中任务超时")
            } else {
                val downloadingBefore = DownloadFileManager.getDownloadingTask()
                val taskId = downloadingBefore.first().downloadID
                DownloadFileManager.pauseTask(taskId, DownloadPauseType.PAUSED_BY_USER)
                waitForTaskStatus(taskId, DownloadStatus.STATUS_DOWNLOAD_PAUSED)
                
                val taskAfterPause = DownloadFileManager.getAllTask().find { it.downloadID == taskId }
                val pauseCorrect = taskAfterPause?.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED &&
                        taskAfterPause.pauseType == DownloadPauseType.PAUSED_BY_USER
                logResult(pauseCorrect, "  暂停状态: ${getPauseTypeName(taskAfterPause?.pauseType ?: 0)}")
                
                DownloadFileUtils.resumeDownload(taskId, true)
                waitForDownloading(10000)
                
                val taskAfterResume = DownloadFileManager.getAllTask().find { it.downloadID == taskId }
                val resumeCorrect = taskAfterResume?.status == DownloadStatus.STATUS_DOWNLOADING ||
                        taskAfterResume?.status == DownloadStatus.STATUS_DOWNLOAD_WAITING
                logResult(resumeCorrect, "  恢复后状态: ${getStatusName(taskAfterResume?.status ?: 0)}")
                
                testResults.add("单任务暂停/恢复测试" to (pauseCorrect && resumeCorrect))
            }
            deleteAllTasksSync()
            Thread.sleep(500)
            
            // Step 4: 批量控制测试
            logStep(4, "批量控制测试")
            DownloadFile.download(context, URL_YYB_WZ, true, testDownloadListener)
            DownloadFile.download(context, URL_YYB_TTS, true, testDownloadListener)
            
            if (!waitForTasksReady(2)) {
                testResults.add("批量控制测试" to false)
                logResult(false, "  等待任务就绪超时")
            } else {
                DownloadFileUtils.pauseAll(true, true)
                Thread.sleep(500)
                
                val pausedAfterPauseAll = DownloadFileManager.getAllTask().count { 
                    it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED && 
                    it.pauseType == DownloadPauseType.PAUSED_BY_ALL 
                }
                logResult(pausedAfterPauseAll > 0, "  pauseAll 后 PAUSED_BY_ALL 数: $pausedAfterPauseAll")
                
                DownloadFileUtils.resumeAll(true)
                waitForDownloading(10000)
                
                val activeAfterResume = DownloadFileManager.getDownloadingTask().size + 
                    DownloadFileManager.getWaitingTask().size
                logResult(activeAfterResume > 0, "  resumeAll 后活跃任务数: $activeAfterResume")
                
                testResults.add("批量控制测试" to (pausedAfterPauseAll > 0 && activeAfterResume > 0))
            }
            deleteAllTasksSync()
            Thread.sleep(500)
            
            // Step 5: USER 暂停优先级测试
            logStep(5, "USER 暂停优先级测试")
            DownloadFile.download(context, URL_YYB_WZ, true, testDownloadListener)
            DownloadFile.download(context, URL_YYB_TTS, true, testDownloadListener)
            
            // 等待任务准备就绪（非 WAITING 状态）
            if (!waitForTasksReady(2)) {
                ZLog.d(TAG, "  ⚠️ 等待任务就绪超时，跳过此测试")
                testResults.add("USER 暂停优先级测试" to false)
            } else {
                val tasks = DownloadFileManager.getAllTask()
                ZLog.d(TAG, "  当前任务数: ${tasks.size}")
                if (tasks.size >= 2) {
                    // 任务1 用 USER 暂停
                    DownloadFileManager.pauseTask(tasks[0].downloadID, DownloadPauseType.PAUSED_BY_USER)
                    // 任务2 用 ALL 暂停
                    DownloadFileManager.pauseTask(tasks[1].downloadID, DownloadPauseType.PAUSED_BY_ALL)
                    Thread.sleep(500)
                    
                    // 执行 pauseAll(ALL)，USER 不应该被覆盖
                    DownloadFileUtils.pauseAll(true, true)
                    Thread.sleep(500)
                    
                    val task1AfterPauseAll = DownloadFileManager.getAllTask().find { it.downloadID == tasks[0].downloadID }
                    val userNotOverwritten = task1AfterPauseAll?.pauseType == DownloadPauseType.PAUSED_BY_USER
                    logResult(userNotOverwritten, "  USER 暂停未被覆盖: ${getPauseTypeName(task1AfterPauseAll?.pauseType ?: 0)}")
                    
                    // resumePauseTask(false) 排除 USER
                    DownloadFileManager.resumePauseTask(pauseOnMobile = true, includeUserPaused = false)
                    Thread.sleep(3000)
                    
                    val task1AfterResume = DownloadFileManager.getAllTask().find { it.downloadID == tasks[0].downloadID }
                    val task2AfterResume = DownloadFileManager.getAllTask().find { it.downloadID == tasks[1].downloadID }
                    
                    val userStillPaused = task1AfterResume?.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED
                    val allResumed = task2AfterResume?.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED
                    logResult(userStillPaused, "  USER 任务仍暂停: ${getStatusName(task1AfterResume?.status ?: 0)}")
                    logResult(allResumed, "  ALL 任务已恢复: ${getStatusName(task2AfterResume?.status ?: 0)}")
                    
                    testResults.add("USER 暂停优先级测试" to (userNotOverwritten && userStillPaused))
                } else {
                    ZLog.d(TAG, "  ⚠️ 任务数不足（需要 >= 2），跳过此测试")
                    testResults.add("USER 暂停优先级测试" to false)
                }
            }
            deleteAllTasksSync()
            Thread.sleep(500)
            
            // Step 6: 网络暂停类型测试（模拟方式，真实WiFi操作请使用手动测试）
            logStep(6, "网络暂停类型测试")
            DownloadFile.download(context, URL_YYB_WZ, true, testDownloadListener)
            
            // 等待任务进入下载状态
            if (!waitForDownloading()) {
                ZLog.d(TAG, "  ⚠️ 等待下载中任务超时，跳过此测试")
                testResults.add("网络暂停类型测试" to false)
            } else {
                val netTask = DownloadFileManager.getAllTask().firstOrNull()
                ZLog.d(TAG, "  当前任务数: ${DownloadFileManager.getAllTask().size}")
                if (netTask != null) {
                    ZLog.d(TAG, "  测试任务: ${getShortUrl(netTask.downloadURL)}")
                    // 模拟断网暂停
                    DownloadFileManager.pauseTask(netTask.downloadID, DownloadPauseType.PAUSED_BY_NETWORK_ERROR)
                    Thread.sleep(500)
                    val afterNetError = DownloadFileManager.getAllTask().find { it.downloadID == netTask.downloadID }
                    val netErrorCorrect = afterNetError?.pauseType == DownloadPauseType.PAUSED_BY_NETWORK_ERROR
                    ZLog.d(TAG, "  断网暂停后: status=${getStatusName(afterNetError?.status ?: 0)}, pauseType=${getPauseTypeName(afterNetError?.pauseType ?: 0)}")
                    logResult(netErrorCorrect, "  PAUSED_BY_NETWORK_ERROR 设置正确")
                    
                    // 模拟网络恢复（调用 checkDownloadWhenNetChanged）
                    DownloadFileManager.checkDownloadWhenNetChanged()
                    Thread.sleep(5000)
                    val afterRecover = DownloadFileManager.getAllTask().find { it.downloadID == netTask.downloadID }
                    val recoverCorrect = afterRecover?.status == DownloadStatus.STATUS_DOWNLOADING || 
                                         afterRecover?.status == DownloadStatus.STATUS_DOWNLOAD_WAITING ||
                                         afterRecover?.status == DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                    ZLog.d(TAG, "  网络恢复后: status=${getStatusName(afterRecover?.status ?: 0)}, pauseType=${getPauseTypeName(afterRecover?.pauseType ?: 0)}")
                    logResult(recoverCorrect, "  checkDownloadWhenNetChanged 恢复正确")
                    
                    testResults.add("网络暂停类型测试" to (netErrorCorrect && recoverCorrect))
                } else {
                    ZLog.d(TAG, "  ⚠️ 没有找到任务，跳过此测试")
                    testResults.add("网络暂停类型测试" to false)
                }
            }
            deleteAllTasksSync()
            Thread.sleep(500)
            
            // Step 7: 状态查询测试
            logStep(7, "状态查询测试")
            DownloadFile.download(context, URL_YYB_WZ, true, testDownloadListener)
            Thread.sleep(3000)
            
            val queryAllTask = DownloadFileManager.getAllTask()
            val queryDownloading = DownloadFileManager.getDownloadingTask()
            val queryWaiting = DownloadFileManager.getWaitingTask()
            val queryFinished = DownloadFileManager.getFinishedTask()
            val queryByMobile = DownloadFileManager.getTasksPausedByMobileNetwork()
            val queryByNetError = DownloadFileManager.getTasksPausedByNetworkError()
            
            ZLog.d(TAG, "  getAllTask: ${queryAllTask.size}")
            ZLog.d(TAG, "  getDownloadingTask: ${queryDownloading.size}")
            ZLog.d(TAG, "  getWaitingTask: ${queryWaiting.size}")
            ZLog.d(TAG, "  getFinishedTask: ${queryFinished.size}")
            ZLog.d(TAG, "  getTasksPausedByMobileNetwork: ${queryByMobile.size}")
            ZLog.d(TAG, "  getTasksPausedByNetworkError: ${queryByNetError.size}")
            
            testResults.add("状态查询测试" to (queryAllTask.isNotEmpty()))
            logResult(queryAllTask.isNotEmpty(), "状态查询测试")
            deleteAllTasksSync()
            Thread.sleep(500)
            
            // Step 8: 最终清理验证
            logStep(8, "最终清理验证")
            val step8Pass = DownloadFileManager.getAllTask().isEmpty()
            testResults.add("最终清理验证" to step8Pass)
            logResult(step8Pass, "所有任务已清理")
            
        } catch (e: Exception) {
            ZLog.e(TAG, "测试异常: ${e.message}")
            e.printStackTrace()
        } finally {
            // 确保测试结束后清理所有任务
            ZLog.d(TAG, "")
            ZLog.d(TAG, "🧹 测试结束，清理所有任务...")
            deleteAllTasksSync()
            Thread.sleep(300)
            val remainingTasks = DownloadFileManager.getAllTask()
            if (remainingTasks.isEmpty()) {
                ZLog.d(TAG, "✅ 清理完成，所有任务已删除")
            } else {
                ZLog.d(TAG, "⚠️ 仍有 ${remainingTasks.size} 个任务未清理")
            }
        }
        
        // 输出测试报告
        val totalTime = System.currentTimeMillis() - startTime
        val passCount = testResults.count { it.second }
        val totalCount = testResults.size
        
        ZLog.d(TAG, "")
        ZLog.d(TAG, "============================================")
        ZLog.d(TAG, "       LibDownload 自动化测试报告")
        ZLog.d(TAG, "============================================")
        testResults.forEachIndexed { index, (name, passed) ->
            val status = if (passed) "[PASS]" else "[FAIL]"
            ZLog.d(TAG, "$status Step ${index + 1}: $name")
        }
        ZLog.d(TAG, "============================================")
        ZLog.d(TAG, "测试结果: $passCount/$totalCount 通过")
        ZLog.d(TAG, "总耗时: ${totalTime / 1000.0} 秒")
        ZLog.d(TAG, "============================================")
    }
}

private fun deleteAllTasksSync() {
    DownloadFileManager.getAllTask().forEach { task ->
        DownloadFileUtils.deleteTask(task.downloadID, true)
    }
}
