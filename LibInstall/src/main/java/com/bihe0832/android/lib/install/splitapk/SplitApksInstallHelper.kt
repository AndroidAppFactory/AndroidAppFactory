package com.bihe0832.android.lib.install.splitapk

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.os.Build
import android.os.RemoteException
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.install.InstallErrorCode
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("StaticFieldLeak")
object SplitApksInstallHelper {
    private const val TAG = "SplitApksInstallHelper:::"
    private var mBroadcastReceiver: SplitApksInstallBroadcastReceiver? = null
    private var mPackageInstaller: PackageInstaller? = null
    private var mInstallListenerMap = ConcurrentHashMap<String, InstallListener>()
    private var mContext: Context? = null
    private var hasInit = false
    private var hasUnregister = false

    @Synchronized
    private fun init(context: Context) {
        if (hasInit) {
            if (hasUnregister) {
                mBroadcastReceiver?.let {
                    try {
                        context.registerReceiver(it, IntentFilter(it.getIntentFilterFlag(context)))
                        hasUnregister = false
                    } catch (e: java.lang.Exception) {
                        ZLog.e("registerReceiver failed:${e.message}")
                    }
                }
            }
            return
        }
        mContext = context.applicationContext
        hasInit = true
        mPackageInstaller = context.packageManager.packageInstaller
        mBroadcastReceiver = SplitApksInstallBroadcastReceiver().apply {
            setEventObserver(object :
                SplitApksInstallBroadcastReceiver.EventObserver {
                override fun onConfirmationPending(sessionId: String?, packageName: String?) {
                    ZLog.e("onConfirmationPending")
                    mInstallListenerMap.get(sessionId)?.onInstallStart()
                }

                override fun onInstallationSucceeded(sessionId: String?, packageName: String?) {
                    try {
                        mInstallListenerMap.get(sessionId)?.onInstallSuccess()
                        mInstallListenerMap.remove(sessionId)
                        checkReceiver(context)
                    } catch (e: java.lang.Exception) {
                        ZLog.e("onInstallationSucceeded unregisterReceiver failed:${e.message}")
                    }
                }

                override fun onInstallationFailed(sessionId: String?, packageName: String?) {
                    try {
                        mInstallListenerMap.get(sessionId)
                            ?.onInstallFailed(InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION)
                        mInstallListenerMap.remove(sessionId)
                        checkReceiver(context)
                    } catch (e: java.lang.Exception) {
                        ZLog.e("onInstallationFailed unregisterReceiver failed:${e.message}")
                    }
                }
            })
        }
        mBroadcastReceiver?.let {
            try {
                context.registerReceiver(it, IntentFilter(it.getIntentFilterFlag(context)))
                hasUnregister = false
            } catch (e: java.lang.Exception) {
                ZLog.e("registerReceiver failed:${e.message}")
            }
        }
    }

    fun checkReceiver(context: Context) {
        if (mInstallListenerMap.isEmpty()) {
            context.unregisterReceiver(mBroadcastReceiver)
            hasUnregister = true
        }
    }

    fun installApk(
        context: Context,
        files : ArrayList<String>?,
        packageName: String,
        listener: InstallListener
    ) {
        init(context)
        if (files.isNullOrEmpty()) {
            listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND)
            checkReceiver(context)
        } else {
            listener.onInstallPrepare()
            if (files.size > 0) {
                installApk(context, files, packageName, listener)
            } else {
                listener.onInstallFailed(InstallErrorCode.BAD_APK_TYPE)
                checkReceiver(context)
            }
        }
    }



    private fun installApk(
        context: Context,
        files: ArrayList<String>,
        packageName: String,
        listener: InstallListener
    ): Int {
        val nameSizeMap = HashMap<String, Long>()
        val filenameToPathMap = HashMap<String, String>()
        var totalSize: Long = 0
        var sessionId = 0
        try {
            for (file in files) {
                val listOfFile = File(file)
                if (listOfFile.isFile) {
                    ZLog.d("$TAG installApk: " + listOfFile.name)
                    nameSizeMap[listOfFile.name] = listOfFile.length()
                    filenameToPathMap[listOfFile.name] = file
                    totalSize += listOfFile.length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener.onInstallFailed(InstallErrorCode.UNKNOWN_EXCEPTION)
            checkReceiver(context)
            return -1
        }
        val installParams = makeSessionParams(totalSize, packageName)
        try {
            sessionId = runInstallCreate(installParams)
            for ((key, value) in nameSizeMap) {
                runInstallWrite(value, sessionId, key, filenameToPathMap[key])
            }
            doCommitSession(sessionId)
            listener.onInstallStart()
            mInstallListenerMap.put(sessionId.toString(), listener)
            ThreadManager.getInstance().start({
                installTimeOut(context, sessionId.toString())
            }, 60)
            ZLog.d("$TAG Success")
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        if (sessionId < 0) {
            listener.onInstallFailed(InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION)
            checkReceiver(context)
        }
        return sessionId
    }

    private fun installTimeOut(context: Context, sessionId: String) {
        mInstallListenerMap.get(sessionId)?.onInstallTimeOut()
        mInstallListenerMap.remove(sessionId)
        checkReceiver(context)
    }

    private fun runInstallCreate(sessionParams: SessionParams): Int {
        if (sessionParams == null) {
            ZLog.d(TAG, "doCreateSession: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!param is null")
            return 0
        }
        val sessionId = doCreateSession(sessionParams)
        ZLog.d("$TAG Success: created install session [$sessionId]")
        return sessionId
    }

    private fun doCreateSession(params: SessionParams): Int {
        var sessionId = 0
        try {
            sessionId = mPackageInstaller!!.createSession(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sessionId
    }

    private fun runInstallWrite(size: Long, sessionId: Int, splitName: String, path: String?): Int {
        var sizeBytes: Long = -1
        sizeBytes = size
        return doWriteSession(sessionId, path, sizeBytes, splitName)
    }


    private fun doWriteSession(
        sessionId: Int,
        inPath: String?,
        sizeBytes: Long,
        splitName: String
    ): Int {
        var inPath = inPath
        var sizeBytes = sizeBytes
        if ("-" == inPath) {
            inPath = null
        } else if (inPath != null) {
            val file = File(inPath)
            if (file.isFile) {
                sizeBytes = file.length()
            }
        }

        var session: PackageInstaller.Session? = null
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        return try {
            session = mPackageInstaller?.openSession(sessionId)
            if (inPath != null) {
                inputStream = FileInputStream(inPath)
            }
            out = session?.openWrite(splitName, 0, sizeBytes)
            var total = 0
            val buffer = ByteArray(65536)
            var c: Int
            while (inputStream!!.read(buffer).also { c = it } != -1) {
                total += c
                out?.write(buffer, 0, c)
            }
            out?.let {
                session?.fsync(it)
            }

            ZLog.d("$TAG Success: streamed $total bytes")
            PackageInstaller.STATUS_SUCCESS
        } catch (e: IOException) {
            ZLog.d("$TAG Error: failed to write; " + e.message)
            PackageInstaller.STATUS_FAILURE
        } finally {
            try {
                out?.close()
                inputStream?.close()
                session?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun doCommitSession(sessionId: Int): Int {
        var session: PackageInstaller.Session? = null
        return try {
            try {
                session = mPackageInstaller?.openSession(sessionId)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val callbackIntent = Intent(mBroadcastReceiver!!.getIntentFilterFlag(mContext))
            val pendingIntent = PendingIntent.getBroadcast(mContext, 0, callbackIntent, 0)
            session?.commit(pendingIntent.intentSender)
            session?.close()
            ZLog.d("$TAG install request sent")
            ZLog.d("$TAG doCommitSession: " + mPackageInstaller?.mySessions)
            ZLog.d("$TAG doCommitSession: after session commit ")
            1
        } finally {
            session?.close()
        }
    }

    private fun makeSessionParams(totalSize: Long, packageName: String): SessionParams {
        val sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL)
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
            sessionParams.setInstallReason(PackageManager.INSTALL_REASON_USER)
        }
        sessionParams.setAppPackageName(packageName)
        sessionParams.setSize(totalSize)
        return sessionParams
    }
}