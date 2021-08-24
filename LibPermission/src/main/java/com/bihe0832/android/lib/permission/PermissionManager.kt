package com.bihe0832.android.lib.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import com.bihe0832.android.lib.log.ZLog

object PermissionManager {

    private const val TAG = "PermissionManager"

    const val PERMISSION_REQUEST_CODE = 0 // 系统权限管理页面的参数

    private var mContext: Context? = null
    private var mOuterResultListener: OnPermissionResult? = null

    private val mPermissionDesc = HashMap<String, String>()
    private val mPermissionScene = HashMap<String, String>()
    private val mPermissionSettings = HashMap<String, String>().apply {
        put(Manifest.permission.SYSTEM_ALERT_WINDOW, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    }

    private val mDefaultScene by lazy {
        mContext?.getString(R.string.permission_default_scene) ?: "完整"
    }

    private val mDefaultDesc by lazy {
        mContext?.getString(R.string.permission_default_desc) ?: "设备"
    }

    fun getPermissionCheckResultListener(): OnPermissionResult {
        return mLastPermissionCheckResultListener
    }

    private val mLastPermissionCheckResultListener by lazy {
        object : OnPermissionResult {
            override fun onSuccess() {
                ZLog.d(TAG, "onSuccess")
                mOuterResultListener?.onSuccess()
                mOuterResultListener = null
            }

            override fun onUserCancel() {
                ZLog.d(TAG, "onUserCancel")
                mOuterResultListener?.onUserCancel()
                mOuterResultListener = null
            }

            override fun onUserDeny() {
                ZLog.d(TAG, "onUserDeny")
                mOuterResultListener?.onUserDeny()
                mOuterResultListener = null
            }

            override fun onFailed(msg: String) {
                ZLog.d(TAG, "onFailed:$msg")
                mOuterResultListener?.onFailed(msg)
                mOuterResultListener = null
            }
        }
    }

    fun addPermissionScene(permissionScene: HashMap<String, String>) {
        mPermissionScene.putAll(permissionScene)
    }

    fun addPermissionDesc(permissionDesc: HashMap<String, String>) {
        mPermissionDesc.putAll(permissionDesc)
    }

    fun addPermissionSettings(permissionSettings: HashMap<String, String>) {
        mPermissionSettings.putAll(permissionSettings)
    }

    interface OnPermissionResult {
        fun onSuccess()
        fun onUserCancel()
        fun onUserDeny()
        fun onFailed(msg: String)
    }

    fun hasPermission(context: Context, permissions: String): Boolean {
        return PermissionsChecker(context).lacksPermissions(permissions)
    }

    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        return PermissionsChecker(context).lacksPermissions(*permissions)
    }

    fun checkPermission(context: Context, vararg permissions: String) {
        checkPermission(context, false, *permissions)
    }

    fun checkPermission(context: Context, canCancel: Boolean, vararg permissions: String) {
        checkPermission(context, canCancel, null, *permissions)
    }

    fun checkPermission(context: Context?, canCancel: Boolean, result: OnPermissionResult?, vararg permissions: String) {
        mOuterResultListener = result
        if (null == context) {
            mLastPermissionCheckResultListener.onFailed("context is null")
        } else {
            mContext = context.applicationContext
            if (!PermissionsChecker(context).lacksPermissions(*permissions)) {
                mLastPermissionCheckResultListener.onSuccess()
            } else {
                try {
                    val intent = Intent(context, PermissionsActivity::class.java)
                    intent.putExtra(PermissionsActivity.EXTRA_PERMISSIONS, permissions)
                    intent.putExtra(PermissionsActivity.EXTRA_CAN_CANCEL, canCancel)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    ActivityCompat.startActivity(context!!, intent, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mLastPermissionCheckResultListener.onFailed("start permission activity failed")
                }
            }
        }
    }

    fun getPermissionScene(permission: String): String {
        return if (mPermissionScene.containsKey(permission)) {
            if (mPermissionScene.get(permission) != null) {
                return addHtmlWrapper(mPermissionScene.get(permission)!!)
            } else {
                mDefaultScene
            }
        } else {
            mDefaultScene
        }
    }

    fun getPermissionDesc(permission: String): String {
        return if (mPermissionDesc.containsKey(permission)) {
            if (mPermissionDesc.get(permission) != null) {
                return addHtmlWrapper(mPermissionDesc.get(permission)!!)
            } else {
                mDefaultDesc
            }
        } else {
            mDefaultDesc
        }
    }

    fun getPermissionSettings(permission: String): String {
        mPermissionSettings.get(permission)?.let {
            return it
        }
        return ""
    }

    private fun addHtmlWrapper(content: String): String {
        return "<font color ='" + (mContext?.resources?.getString(R.string.permission_color)
                ?: "#38ADFF") + "'><b> $content </b></font> "
    }

}