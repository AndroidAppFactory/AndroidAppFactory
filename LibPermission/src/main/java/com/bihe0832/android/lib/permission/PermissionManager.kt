package com.bihe0832.android.lib.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.ui.PermissionsActivity
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.utils.apk.APKUtils
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
    private val mPermissionIcon = ConcurrentHashMap<String, Int>()

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

    fun addPermissionIcon(sceneid: String, permission: String, icon: Int) {
        mPermissionIcon.put(getPermissionKey(sceneid, permission), icon)
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

    fun addPermissionIcon(permissionScene: HashMap<String, Int>) {
        mPermissionIcon.putAll(permissionScene)
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
        checkPermission(context, scene, canCancel, PermissionsActivityV2::class.java, result, *permissions)
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


    fun getPermissionIcon(scene: String, permission: String): Int {
        return if (mPermissionIcon.containsKey(getPermissionKey(scene, permission))) {
            return getPermissionIcon(getPermissionKey(scene, permission));
        } else {
            getPermissionIcon(permission)
        }
    }

    fun getPermissionIcon(permission: String): Int {
        return if (mPermissionIcon.containsKey(permission)) {
            return mPermissionIcon.get(permission) ?: R.mipmap.icon
        } else {
            R.mipmap.icon
        }
    }

    fun getPermissionScene(scene: String, permission: String, needSpecial: Boolean): String {
        return if (mPermissionScene.containsKey(getPermissionKey(scene, permission))) {
            return getPermissionScene(getPermissionKey(scene, permission), needSpecial)
        } else {
            getPermissionScene(permission, needSpecial)
        }
    }

    fun getPermissionScene(permission: String, needSpecial: Boolean): String {
        return if (mPermissionScene.containsKey(permission)) {
            if (mPermissionScene.get(permission) != null) {
                return if (needSpecial) {
                    addHtmlWrapper(mPermissionScene.get(permission)!!)
                } else {
                    mPermissionScene.get(permission)!!
                }
            } else {
                mDefaultScene
            }
        } else {
            mDefaultScene
        }
    }

    fun getPermissionDesc(scene: String, permission: String, needSpecial: Boolean): String {
        return if (mPermissionDesc.containsKey(getPermissionKey(scene, permission))) {
            return getPermissionDesc(getPermissionKey(scene, permission), needSpecial)
        } else {
            getPermissionDesc(permission, needSpecial)
        }
    }

    fun getPermissionDesc(permission: String, needSpecial: Boolean): String {
        return if (mPermissionDesc.containsKey(permission)) {
            if (mPermissionDesc.get(permission) != null) {
                return if (needSpecial) {
                    addHtmlWrapper(mPermissionDesc.get(permission)!!)
                } else {
                    mPermissionDesc.get(permission)!!
                }
            } else {
                mDefaultDesc
            }
        } else {
            mDefaultDesc
        }
    }

    fun getPermissionContent(context: Context, scene: String, permission: String, needSpecial: Boolean): String {
        return if (mPermissionContent.containsKey(getPermissionKey(scene, permission))) {
            return getPermissionContent(context, getPermissionKey(scene, permission), needSpecial)
        } else {
            getPermissionContent(context, permission, needSpecial)
        }
    }

    fun getPermissionContent(context: Context, permission: String, needSpecial: Boolean): String {
        if (mPermissionContent.containsKey(permission)) {
            mPermissionContent.get(permission)?.let {
                return it
            }
        }
        return getDefaultPermissionContent(context, permission, needSpecial)
    }

    fun getPermissionScene(sceneID: String, tempPermissionList: List<String>, needSpecial: Boolean): String {
        var scene = ""
        tempPermissionList.forEach {
            scene = scene + getPermissionScene(sceneID, it, needSpecial) + "、"
        }
        scene = scene.substring(0, scene.length - 1)

        return scene
    }

    fun getPermissionDesc(sceneID: String, tempPermissionList: List<String>, needSpecial: Boolean): String {
        var desc = ""
        tempPermissionList.forEach {
            desc = desc + getPermissionDesc(sceneID, it, needSpecial) + "、"
        }
        desc = desc.substring(0, desc.length - 1)

        return desc
    }

    fun getPermissionContent(context: Context, sceneID: String, tempPermissionList: List<String>, needSpecial: Boolean): String {
        return if (tempPermissionList.size > 1) {
            getDefaultPermissionContent(context, getPermissionScene(sceneID, tempPermissionList, needSpecial), getPermissionDesc(sceneID, tempPermissionList, needSpecial))
        } else if (tempPermissionList.size > 0) {
            getPermissionContent(context, sceneID, tempPermissionList.get(0), needSpecial)
        } else {
            ""
        }
    }

    fun getDefaultPermissionContent(context: Context, showPermission: String, needSpecial: Boolean): String {
        return getDefaultPermissionContent(context, getPermissionScene(showPermission, needSpecial), getPermissionDesc(showPermission, needSpecial))
    }

    fun getDefaultPermissionContent(context: Context, sceneText: String, permissionDesc: String): String {
        return String.format(context.getString(R.string.com_bihe0832_permission_default_content), APKUtils.getAppName(context), sceneText, permissionDesc)
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

    fun getPermissionDenyTime(permission: String): Long {
        return Config.readConfig(USER_DENY_KEY + permission, 0L)
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
                ?: "#38ADFF") + "'><b>$content</b></font>"
    }


}