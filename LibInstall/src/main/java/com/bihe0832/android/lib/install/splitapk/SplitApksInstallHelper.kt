package com.bihe0832.android.lib.install.splitapk

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.os.Build
import com.bihe0832.android.lib.install.InstallErrorCode
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils.TAG
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 降级回调接口
 * 当 Session API 安装失败时触发，允许调用方使用其他方式安装
 */
fun interface FallbackCallback {
    /**
     * Session 安装失败，需要降级处理
     * @param errorCode 错误码
     */
    fun onFallback(errorCode: Int)
}

/**
 * Split APKs 安装助手
 *
 * 使用 PackageInstaller API 安装 APK，支持：
 * - 单个 APK 安装
 * - Split APKs（拆分 APK）安装
 *
 * 适配说明：
 * - Android 12+ (API 31+): PendingIntent 需要 FLAG_MUTABLE
 * - Android 13+ (API 33+): 广播接收器需要 RECEIVER_NOT_EXPORTED
 * - Android 14+ (API 34+): mutable PendingIntent 需要显式 Intent
 * - Android 15+ (API 35+): 被安装的 APK targetSdkVersion >= 24
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-09
 * Refactored on 2025-01-07
 */
@SuppressLint("StaticFieldLeak")
object SplitApksInstallHelper {

    // 安装状态广播 Action 后缀
    private const val ACTION_INSTALL_STATUS_SUFFIX = ".action.SPLIT_APKS_INSTALL_STATUS"

    // 安装监听器映射表，key 为 sessionId
    private val installListenerMap = ConcurrentHashMap<Int, InstallListenerWrapper>()

    // 广播接收器
    private var installReceiver: BroadcastReceiver? = null

    // 应用上下文
    private var appContext: Context? = null

    // 是否已初始化
    private var isInitialized = false

    // 广播接收器是否已注销
    private var isReceiverUnregistered = true

    /**
     * 获取安装状态广播 Action
     */
    private fun getInstallStatusAction(context: Context): String {
        return context.packageName + ACTION_INSTALL_STATUS_SUFFIX
    }

    /**
     * 初始化
     */
    @Synchronized
    private fun init(context: Context) {
        if (isInitialized && !isReceiverUnregistered) {
            return
        }

        appContext = context.applicationContext
        isInitialized = true

        // 创建广播接收器
        if (installReceiver == null) {
            installReceiver = createInstallReceiver()
        }

        // 注册广播接收器
        registerReceiver(context)
    }

    /**
     * 创建安装结果广播接收器
     */
    private fun createInstallReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (context == null || intent == null) return

                val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
                val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: ""
                val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME) ?: ""

                ZLog.d(TAG, "onReceive: status=$status, sessionId=$sessionId, package=$packageName, message=$message")

                when (status) {
                    PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                        // 需要用户确认，启动确认界面
                        handlePendingUserAction(context, intent, sessionId)
                    }

                    PackageInstaller.STATUS_SUCCESS -> {
                        ZLog.d(TAG, "Installation succeeded for session $sessionId")
                        notifySuccess(sessionId)
                    }

                    PackageInstaller.STATUS_FAILURE,
                    PackageInstaller.STATUS_FAILURE_ABORTED,
                    PackageInstaller.STATUS_FAILURE_BLOCKED,
                    PackageInstaller.STATUS_FAILURE_CONFLICT,
                    PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                    PackageInstaller.STATUS_FAILURE_INVALID,
                    PackageInstaller.STATUS_FAILURE_STORAGE -> {
                        ZLog.e(TAG, "Installation failed for session $sessionId: ${getErrorMessage(status, message)}")
                        notifyFailed(sessionId, status, message)
                    }

                    else -> {
                        ZLog.e(TAG, "Unknown status $status for session $sessionId")
                        notifyFailed(sessionId, status, message)
                    }
                }
            }
        }
    }

    /**
     * 处理需要用户确认的情况
     * 
     * 安全说明：
     * - 验证 Intent 来源，防止 Intent Redirection 攻击
     * - 只接受来自系统 PackageInstaller 的 Intent
     */
    private fun handlePendingUserAction(context: Context, intent: Intent, sessionId: Int) {
        ZLog.d(TAG, "Requesting user confirmation for session $sessionId")

        val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }

        if (confirmIntent != null) {
            // 安全检查：验证 Intent 是否来自系统 PackageInstaller
            if (!isValidPackageInstallerIntent(confirmIntent)) {
                ZLog.e(TAG, "Security check failed: Intent is not from system PackageInstaller")
                notifyFailed(sessionId, InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION, "Invalid intent source")
                return
            }
            
            try {
                confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(confirmIntent)
                installListenerMap[sessionId]?.listener?.onInstallStart()
            } catch (e: Exception) {
                ZLog.e(TAG, "Failed to start confirmation activity: ${e.message}")
                notifyFailed(sessionId, InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION, e.message ?: "")
            }
        } else {
            ZLog.e(TAG, "Confirmation intent is null")
            notifyFailed(sessionId, InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION, "Confirmation intent is null")
        }
    }
    
    /**
     * 验证 Intent 是否来自系统 PackageInstaller
     * 
     * 防止 Intent Redirection 攻击：
     * - 检查 Intent 的目标组件是否属于系统包（com.android.* 或 com.google.android.*）
     * - 检查 action 是否为 PackageInstaller 相关的 action
     * 
     * @param intent 待验证的 Intent
     * @return true 如果是合法的系统 Intent，false 否则
     */
    private fun isValidPackageInstallerIntent(intent: Intent): Boolean {
        // 检查目标组件
        val component = intent.component
        if (component != null) {
            val packageName = component.packageName
            // 只允许系统包
            val isSystemPackage = packageName.startsWith("com.android.") ||
                    packageName.startsWith("com.google.android.") ||
                    packageName == "android"
            
            if (!isSystemPackage) {
                ZLog.e(TAG, "Invalid package: $packageName")
                return false
            }
            
            ZLog.d(TAG, "Valid system package: $packageName")
            return true
        }
        
        // 如果没有组件，检查 action
        val action = intent.action
        if (action != null) {
            // PackageInstaller 相关的合法 action
            val validActions = listOf(
                "android.content.pm.action.CONFIRM_INSTALL",
                "android.content.pm.action.CONFIRM_PERMISSIONS",
                "android.intent.action.INSTALL_PACKAGE",
                "com.android.packageinstaller.action.CONFIRM_INSTALL"
            )
            
            if (validActions.any { action.contains(it, ignoreCase = true) || it.contains(action, ignoreCase = true) }) {
                ZLog.d(TAG, "Valid action: $action")
                return true
            }
        }
        
        // 默认拒绝未知来源的 Intent
        ZLog.e(TAG, "Unknown intent source: component=$component, action=$action")
        return false
    }

    /**
     * 注册广播接收器
     */
    private fun registerReceiver(context: Context) {
        if (!isReceiverUnregistered) return

        installReceiver?.let { receiver ->
            try {
                val filter = IntentFilter(getInstallStatusAction(context))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.applicationContext.registerReceiver(
                        receiver,
                        filter,
                        Context.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    context.applicationContext.registerReceiver(receiver, filter)
                }
                isReceiverUnregistered = false
                ZLog.d(TAG, "Receiver registered")
            } catch (e: Exception) {
                ZLog.e(TAG, "Failed to register receiver: ${e.message}")
            }
        }
    }

    /**
     * 注销广播接收器
     */
    private fun unregisterReceiver(context: Context) {
        if (isReceiverUnregistered) return

        installReceiver?.let { receiver ->
            try {
                context.applicationContext.unregisterReceiver(receiver)
                isReceiverUnregistered = true
                ZLog.d(TAG, "Receiver unregistered")
            } catch (e: Exception) {
                ZLog.e(TAG, "Failed to unregister receiver: ${e.message}")
            }
        }
    }

    /**
     * 检查是否需要注销广播接收器
     */
    private fun checkAndUnregisterReceiver(context: Context) {
        if (installListenerMap.isEmpty()) {
            unregisterReceiver(context)
        }
    }

    /**
     * 通知安装成功
     */
    private fun notifySuccess(sessionId: Int) {
        ThreadManager.getInstance().runOnUIThread {
            installListenerMap[sessionId]?.listener?.onInstallSuccess()
            installListenerMap.remove(sessionId)
            appContext?.let { checkAndUnregisterReceiver(it) }
        }
    }

    /**
     * 通知安装失败
     */
    private fun notifyFailed(sessionId: Int, errorCode: Int, message: String) {
        ZLog.e(TAG, "Install failed: errorCode=$errorCode, message=$message")
        ThreadManager.getInstance().runOnUIThread {
            val wrapper = installListenerMap[sessionId]
            wrapper?.listener?.onInstallFailed(
                when (errorCode) {
                    PackageInstaller.STATUS_FAILURE_ABORTED -> InstallErrorCode.PERMISSION_DENY
                    PackageInstaller.STATUS_FAILURE_BLOCKED -> InstallErrorCode.PERMISSION_DENY
                    PackageInstaller.STATUS_FAILURE_INVALID -> InstallErrorCode.BAD_APK_TYPE
                    else -> InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION
                }
            )
            installListenerMap.remove(sessionId)
            appContext?.let { checkAndUnregisterReceiver(it) }
        }
    }

    /**
     * 获取错误信息
     */
    private fun getErrorMessage(status: Int, message: String): String {
        return when (status) {
            PackageInstaller.STATUS_FAILURE -> "Installation failed: $message"
            PackageInstaller.STATUS_FAILURE_ABORTED -> "Installation aborted"
            PackageInstaller.STATUS_FAILURE_BLOCKED -> "Installation blocked"
            PackageInstaller.STATUS_FAILURE_CONFLICT -> "Installation conflict"
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> "APK incompatible"
            PackageInstaller.STATUS_FAILURE_INVALID -> "Invalid APK"
            PackageInstaller.STATUS_FAILURE_STORAGE -> "Insufficient storage"
            else -> "Unknown error: $status"
        }
    }

    /**
     * 安装 APK 文件列表
     *
     * @param context 上下文
     * @param files APK 文件路径列表
     * @param timeoutSeconds 超时时间（秒）
     * @param listener 安装监听器
     */
    fun installApk(
        context: Context,
        files: ArrayList<String>?,
        timeoutSeconds: Int,
        listener: InstallListener
    ) {
        installApkWithFallback(context, files, timeoutSeconds, listener, null)
    }

    /**
     * 安装 APK 文件列表（支持降级回调）
     *
     * @param context 上下文
     * @param files APK 文件路径列表
     * @param timeoutSeconds 超时时间（秒）
     * @param listener 安装监听器
     * @param fallbackCallback 降级回调，当 Session 创建/写入失败时触发，允许调用方使用其他方式安装
     */
    fun installApkWithFallback(
        context: Context,
        files: ArrayList<String>?,
        timeoutSeconds: Int,
        listener: InstallListener,
        fallbackCallback: FallbackCallback?
    ) {
        ZLog.d(TAG, "installApk: files=${files?.size}, timeout=$timeoutSeconds, hasFallback=${fallbackCallback != null}")

        if (files.isNullOrEmpty()) {
            ZLog.e(TAG, "Files list is null or empty")
            listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND)
            return
        }

        // 验证文件是否存在
        val validFiles = files.filter { path ->
            val file = File(path)
            val exists = file.exists() && file.isFile
            if (!exists) {
                ZLog.e(TAG, "File not found or not a file: $path")
            }
            exists
        }

        if (validFiles.isEmpty()) {
            ZLog.e(TAG, "No valid files found")
            listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND)
            return
        }

        // 初始化
        init(context)

        // 通知准备安装
        listener.onInstallPrepare()

        // 在后台线程执行安装
        ThreadManager.getInstance().start {
            doInstall(context, ArrayList(validFiles), timeoutSeconds, listener, fallbackCallback)
        }
    }

    /**
     * 执行安装（在后台线程）
     */
    private fun doInstall(
        context: Context,
        files: ArrayList<String>,
        timeoutSeconds: Int,
        listener: InstallListener,
        fallbackCallback: FallbackCallback? = null
    ) {
        var sessionId = -1
        var session: PackageInstaller.Session? = null

        try {
            val packageInstaller = context.packageManager.packageInstaller

            // 计算总大小
            var totalSize = 0L
            val fileInfoMap = HashMap<String, Long>()
            for (filePath in files) {
                val file = File(filePath)
                fileInfoMap[file.name] = file.length()
                totalSize += file.length()
                ZLog.d(TAG, "File: ${file.name}, size: ${file.length()}")
            }

            // 创建安装会话参数
            val sessionParams = createSessionParams(totalSize)

            // 创建会话
            sessionId = packageInstaller.createSession(sessionParams)
            ZLog.d(TAG, "Created session: $sessionId")

            // 保存监听器
            installListenerMap[sessionId] = InstallListenerWrapper(listener, timeoutSeconds)

            // 打开会话
            session = packageInstaller.openSession(sessionId)

            // 写入所有 APK 文件
            for (filePath in files) {
                val file = File(filePath)
                val writeResult = writeApkToSession(session, file)
                if (writeResult != PackageInstaller.STATUS_SUCCESS) {
                    throw IOException("Failed to write APK: ${file.name}")
                }
            }

            // 提交会话
            commitSession(context, session, sessionId)

            ZLog.d(TAG, "Session committed: $sessionId")

            // 设置超时
            setupTimeout(context, sessionId, timeoutSeconds)

        } catch (e: Exception) {
            ZLog.e(TAG, "Install error: ${e.message}")
            e.printStackTrace()

            // 清理
            session?.close()
            if (sessionId >= 0) {
                installListenerMap.remove(sessionId)
                try {
                    context.packageManager.packageInstaller.abandonSession(sessionId)
                } catch (ex: Exception) {
                    ZLog.e(TAG, "Failed to abandon session: ${ex.message}")
                }
            }

            // 如果有降级回调，则触发降级；否则通知失败
            if (fallbackCallback != null) {
                ZLog.d(TAG, "Session install failed, triggering fallback")
                ThreadManager.getInstance().runOnUIThread {
                    fallbackCallback.onFallback(InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION)
                }
            } else {
                // 通知失败
                ThreadManager.getInstance().runOnUIThread {
                    listener.onInstallFailed(InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION)
                }
            }

            checkAndUnregisterReceiver(context)
        }
    }

    /**
     * 创建会话参数
     */
    private fun createSessionParams(totalSize: Long): SessionParams {
        return SessionParams(SessionParams.MODE_FULL_INSTALL).apply {
            setSize(totalSize)
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
                setInstallReason(PackageManager.INSTALL_REASON_USER)
            }
        }
    }

    /**
     * 将 APK 写入会话
     */
    private fun writeApkToSession(session: PackageInstaller.Session, file: File): Int {
        var inputStream: FileInputStream? = null
        var outputStream: java.io.OutputStream? = null

        return try {
            inputStream = FileInputStream(file)
            outputStream = session.openWrite(file.name, 0, file.length())

            val buffer = ByteArray(65536)
            var bytesRead: Int
            var totalWritten = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalWritten += bytesRead
            }

            session.fsync(outputStream)

            ZLog.d(TAG, "Written ${file.name}: $totalWritten bytes")
            PackageInstaller.STATUS_SUCCESS

        } catch (e: IOException) {
            ZLog.e(TAG, "Failed to write ${file.name}: ${e.message}")
            PackageInstaller.STATUS_FAILURE
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                ZLog.e(TAG, "Failed to close output stream: ${e.message}")
            }
            try {
                inputStream?.close()
            } catch (e: IOException) {
                ZLog.e(TAG, "Failed to close input stream: ${e.message}")
            }
        }
    }

    /**
     * 提交会话
     */
    private fun commitSession(context: Context, session: PackageInstaller.Session, sessionId: Int) {
        // 创建 PendingIntent
        // Android 14+ 要求 mutable PendingIntent 使用显式 Intent
        val intent = Intent(getInstallStatusAction(context)).apply {
            setPackage(context.packageName)
        }

        // Android 12+ 需要 FLAG_MUTABLE
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            pendingIntentFlags
        )

        // 提交
        session.commit(pendingIntent.intentSender)
        session.close()
    }

    /**
     * 设置安装超时
     */
    private fun setupTimeout(context: Context, sessionId: Int, timeoutSeconds: Int) {
        // timeoutSeconds <= 0 时不设置超时
        if (timeoutSeconds <= 0) {
            ZLog.d(TAG, "setupTimeout: timeoutSeconds <= 0, skip timeout setup")
            return
        }
        
        ThreadManager.getInstance().start({
            if (installListenerMap.containsKey(sessionId)) {
                ZLog.d(TAG, "Installation timeout for session $sessionId")
                ThreadManager.getInstance().runOnUIThread {
                    installListenerMap[sessionId]?.listener?.onInstallTimeOut()
                    installListenerMap.remove(sessionId)
                    checkAndUnregisterReceiver(context)
                }
            }
        }, timeoutSeconds)
    }

    /**
     * 安装监听器包装类
     */
    private data class InstallListenerWrapper(
        val listener: InstallListener,
        val timeoutSeconds: Int
    )
}
