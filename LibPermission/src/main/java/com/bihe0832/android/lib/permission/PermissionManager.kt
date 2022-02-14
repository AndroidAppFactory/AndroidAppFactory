package com.bihe0832.android.lib.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.ui.PermissionsActivity
import java.util.concurrent.ConcurrentHashMap

object PermissionManager {

    private const val TAG = "PermissionManager"

    const val PERMISSION_REQUEST_CODE = 0 // 系统权限管理页面的参数

    private var mContext: Context? = null
//    private var mOuterResultListener: OnPermissionResult? = null


    private val USER_DENY_KEY = "UserPermissionDenyKey"
    private val mPermissionDesc = ConcurrentHashMap<String, String>()
    private val mPermissionScene = ConcurrentHashMap<String, String>()
    private val mPermissionContent = ConcurrentHashMap<String, String>()

    private val mPermissionSettings = ConcurrentHashMap<String, String>().apply {
        put(Manifest.permission.SYSTEM_ALERT_WINDOW, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    }

    private val mOuterResultListenerList = ConcurrentHashMap<String, OnPermissionResult>()

    private fun getOuterPermissionResultListener(scene: String): OnPermissionResult? {
        return mOuterResultListenerList.get(scene)
    }

    private val mDefaultScene by lazy {
        mContext?.getString(R.string.com_bihe0832_permission_default_scene) ?: "完整"
    }

    private val mDefaultDesc by lazy {
        mContext?.getString(R.string.com_bihe0832_permission_default_desc) ?: "设备"
    }

    fun getPermissionCheckResultListener(): InnerOnPermissionResult {
        return mPermissionCheckResultListener
    }

    private val mPermissionCheckResultListener by lazy {
        object : InnerOnPermissionResult {
            override fun onSuccess(scene: String) {
                ZLog.d(TAG, "onSuccess")
                getOuterPermissionResultListener(scene)?.onSuccess()
                mOuterResultListenerList.remove(scene)
            }

            override fun onUserCancel(scene: String, permission: String) {
                ZLog.d(TAG, "onUserCancel")
                getOuterPermissionResultListener(scene)?.onUserCancel(scene, permission)
                mOuterResultListenerList.remove(scene)
            }

            override fun onUserDeny(scene: String, permission: String) {
                ZLog.d(TAG, "onUserDeny")
                getOuterPermissionResultListener(scene)?.onUserDeny(scene, permission)
                mOuterResultListenerList.remove(scene)
            }

            override fun onFailed(scene: String, msg: String) {
                ZLog.d(TAG, "onFailed:$msg")
                getOuterPermissionResultListener(scene)?.onFailed(msg)
                mOuterResultListenerList.remove(scene)
            }
        }
    }

    fun getPermissionKey(sceneid: String?, permission: String): String {
        sceneid?.let {
            return permission + sceneid
        }
        return permission
    }

    fun addPermissionScene(sceneid: String, permission: String, sceneDesc: String) {
        mPermissionScene.put(getPermissionKey(sceneid, permission), sceneDesc)
    }

    fun addPermissionDesc(sceneid: String, permission: String, permissionDesc: String) {
        mPermissionDesc.put(getPermissionKey(sceneid, permission), permissionDesc)
    }

    fun addPermissionContent(sceneid: String, permission: String, permissionDesc: String) {
        mPermissionContent.put(getPermissionKey(sceneid, permission), permissionDesc)
    }

    fun addPermissionScene(permissionScene: HashMap<String, String>) {
        mPermissionScene.putAll(permissionScene)
    }

    fun addPermissionDesc(permissionDesc: HashMap<String, String>) {
        mPermissionDesc.putAll(permissionDesc)
    }

    fun addPermissionContent(permissionDesc: HashMap<String, String>) {
        mPermissionContent.putAll(permissionDesc)
    }

    fun addPermissionSettings(permissionSettings: HashMap<String, String>) {
        mPermissionSettings.putAll(permissionSettings)
    }

    interface OnPermissionResult {
        fun onSuccess()
        fun onUserCancel(scene: String, permission: String)
        fun onUserDeny(scene: String, permission: String)
        fun onFailed(msg: String)
    }

    interface InnerOnPermissionResult {
        fun onSuccess(scene: String)
        fun onUserCancel(scene: String, permission: String)
        fun onUserDeny(scene: String, permission: String)
        fun onFailed(scene: String, msg: String)
    }

    fun hasPermission(context: Context?, permissions: String): Boolean {
        return !PermissionsChecker(context).lacksPermissions(permissions)
    }

    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        return !PermissionsChecker(context).lacksPermissions(*permissions)
    }

    fun checkPermission(context: Context?, vararg permissions: String) {
        checkPermission(context, false, *permissions)
    }

    fun checkPermission(context: Context?, canCancel: Boolean, vararg permissions: String) {
        checkPermission(context, "", canCancel, null, *permissions)
    }

    fun checkPermission(context: Context?, scene: String, canCancel: Boolean, result: OnPermissionResult?, vararg permissions: String) {
        checkPermission(context, scene, canCancel, PermissionsActivity::class.java, result, *permissions)
    }

    fun checkPermission(context: Context?, scene: String, canCancel: Boolean, permissionsActivityClass: Class<out PermissionsActivity>, result: OnPermissionResult?, vararg permissions: String) {
        result?.let {
            mOuterResultListenerList.put(scene, it)
        }
        if (null == context) {
            mPermissionCheckResultListener.onFailed(scene, "context is null")
        } else {
            mContext = context.applicationContext
            if (!PermissionsChecker(context).lacksPermissions(*permissions)) {
                mPermissionCheckResultListener.onSuccess(scene)
            } else {
                try {
                    val intent = Intent(context, permissionsActivityClass)
                    intent.putExtra(PermissionsActivity.EXTRA_PERMISSIONS, permissions)
                    intent.putExtra(PermissionsActivity.EXTRA_CAN_CANCEL, canCancel)
                    intent.putExtra(PermissionsActivity.EXTRA_SOURCE, scene)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    ActivityCompat.startActivity(context!!, intent, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mPermissionCheckResultListener.onFailed(scene, "start permission activity failed")
                }
            }
        }
    }

    fun getPermissionScene(scene: String, permission: String): String {
        return if (mPermissionScene.containsKey(getPermissionKey(scene, permission))) {
            return getPermissionScene(getPermissionKey(scene, permission))
        } else {
            getPermissionScene(permission)
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

    fun getPermissionDesc(scene: String, permission: String): String {
        return if (mPermissionDesc.containsKey(getPermissionKey(scene, permission))) {
            return getPermissionDesc(getPermissionKey(scene, permission))
        } else {
            getPermissionDesc(permission)
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

    fun getPermissionContent(scene: String, permission: String): String {
        return if (mPermissionContent.containsKey(getPermissionKey(scene, permission))) {
            return getPermissionContent(getPermissionKey(scene, permission))
        } else {
            getPermissionContent(permission)
        }
    }

    fun getPermissionContent(permission: String): String {
        if (mPermissionContent.containsKey(permission)) {
            mPermissionContent.get(permission)?.let {
                return it
            }
        }
        return ""
    }

    fun getPermissionDenyTime(permission: String): Long {
        return Config.readConfig(USER_DENY_KEY + permission, 0L)
    }

    fun getTitle(context: Context): String {
        return context.resources.getString(R.string.com_bihe0832_permission_title)
    }

    fun getNegtiveText(context: Context): String {
        return context.resources.getString(R.string.com_bihe0832_permission_negtive)
    }

    fun getPositiveText(context: Context): String {
        return context.resources.getString(R.string.com_bihe0832_permission_positive)
    }

    fun setUserDenyTime(permission: String) {
        Config.writeConfig(USER_DENY_KEY + permission, System.currentTimeMillis())
    }


    fun getPermissionSettings(permission: String): String {
        mPermissionSettings.get(permission)?.let {
            return it
        }
        return ""
    }

    private fun addHtmlWrapper(content: String): String {
        return "<font color ='" + (mContext?.resources?.getString(R.string.com_bihe0832_permission_color)
                ?: "#38ADFF") + "'><b> $content </b></font> "
    }

}