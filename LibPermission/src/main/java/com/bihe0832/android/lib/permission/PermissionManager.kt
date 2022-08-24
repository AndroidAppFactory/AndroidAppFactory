package com.bihe0832.android.lib.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.ActivityCompat
import android.text.TextUtils
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

    // 权限组与权限的对应
    private val mPermissionGroup = ConcurrentHashMap<String, List<String>>()

    // 权限组与图标的对应
    private val mPermissionIcon = ConcurrentHashMap<String, Int>()

    // 权限组与权限使用场景的对应
    private val mPermissionScene = ConcurrentHashMap<String, String>()

    // 权限组与权限描述的对应
    private val mPermissionDesc = ConcurrentHashMap<String, String>()

    // 权限组与权限文案的对应
    private val mPermissionContent = ConcurrentHashMap<String, String>()

    // 权限组与设置对应的权限页面的对应
    private val mPermissionSettings = ConcurrentHashMap<String, String>().apply {
        put(Manifest.permission.SYSTEM_ALERT_WINDOW, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    }

    private val mOuterResultListenerList = ConcurrentHashMap<String, OnPermissionResult>()


    private val mDefaultScene by lazy {
        mContext?.getString(R.string.com_bihe0832_permission_default_scene) ?: "完整"
    }

    private val mDefaultDesc by lazy {
        mContext?.getString(R.string.com_bihe0832_permission_default_desc) ?: "设备"
    }


    private fun getOuterPermissionResultListener(scene: String): OnPermissionResult? {
        return mOuterResultListenerList.get(scene)
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

            override fun onUserCancel(scene: String, permissionGroupID: String, permission: String) {
                ZLog.d(TAG, "onUserCancel")
                getOuterPermissionResultListener(scene)?.onUserCancel(scene, permissionGroupID, permission)
                mOuterResultListenerList.remove(scene)
            }

            override fun onUserDeny(scene: String, permissionGroupID: String, permission: String) {
                ZLog.d(TAG, "onUserDeny")
                getOuterPermissionResultListener(scene)?.onUserDeny(scene, permissionGroupID, permission)
                mOuterResultListenerList.remove(scene)
            }

            override fun onFailed(scene: String, msg: String) {
                ZLog.d(TAG, "onFailed:$msg")
                getOuterPermissionResultListener(scene)?.onFailed(msg)
                mOuterResultListenerList.remove(scene)
            }
        }
    }

    fun getPermissionKey(sceneid: String?, permissionGroupID: String): String {
        sceneid?.let {
            return permissionGroupID + sceneid
        }
        return permissionGroupID
    }

    fun addPermissionGroup(sceneid: String, permissionGroupID: String, permissions: List<String>) {
        mPermissionGroup.put(getPermissionKey(sceneid, permissionGroupID), permissions)
    }

    fun addPermissionGroupScene(sceneid: String, permissionGroupID: String, sceneDesc: String) {
        mPermissionScene.put(getPermissionKey(sceneid, permissionGroupID), sceneDesc)
    }

    fun addPermissionGroupDesc(sceneid: String, permissionGroupID: String, permissionDesc: String) {
        mPermissionDesc.put(getPermissionKey(sceneid, permissionGroupID), permissionDesc)
    }

    fun addPermissionGroupContent(sceneid: String, permissionGroupID: String, permissionDesc: String) {
        mPermissionContent.put(getPermissionKey(sceneid, permissionGroupID), permissionDesc)
    }

    fun addPermissionGroupIcon(sceneid: String, permissionGroupID: String, icon: Int) {
        mPermissionIcon.put(getPermissionKey(sceneid, permissionGroupID), icon)
    }

    fun addPermissionGroupScene(permissionScene: HashMap<String, String>) {
        mPermissionScene.putAll(permissionScene)
    }

    fun addPermissionGroupDesc(permissionDesc: HashMap<String, String>) {
        mPermissionDesc.putAll(permissionDesc)
    }

    fun addPermissionGroupContent(permissionDesc: HashMap<String, String>) {
        mPermissionContent.putAll(permissionDesc)
    }

    fun addPermissionGroupIcon(permissionScene: HashMap<String, Int>) {
        mPermissionIcon.putAll(permissionScene)
    }

    fun addPermissionSettings(permissionSettings: HashMap<String, String>) {
        mPermissionSettings.putAll(permissionSettings)
    }

    interface OnPermissionResult {
        fun onSuccess()
        fun onUserCancel(scene: String, permissionGroupID: String, permission: String)
        fun onUserDeny(scene: String, permissionGroupID: String, permission: String)
        fun onFailed(msg: String)
    }

    interface InnerOnPermissionResult {
        fun onSuccess(scene: String)
        fun onUserCancel(scene: String, permissionGroupID: String, permission: String)
        fun onUserDeny(scene: String, permissionGroupID: String, permission: String)
        fun onFailed(scene: String, msg: String)
    }

    fun hasPermissionGroup(context: Context?, permissionGroupID: String): Boolean {
        return if (mPermissionGroup.containsKey(permissionGroupID)) {
            !PermissionsChecker(context).lacksPermissions(mPermissionGroup.get(permissionGroupID))
        } else {
            !PermissionsChecker(context).lacksPermission(permissionGroupID)
        }
    }

    fun hasPermissionGroup(context: Context, permissionGroupIDList: List<String>): Boolean {
        for (permissionGroupID in permissionGroupIDList) {
            if (!hasPermissionGroup(context, permissionGroupID)) {
                return false
            }
        }
        return true
    }

    fun checkPermission(context: Context?, permissionGroupID: String) {
        checkPermission(context, false, mutableListOf<String>().apply {
            add(permissionGroupID)
        })
    }

    fun checkPermission(context: Context?, permissionGroupIDList: List<String>) {
        checkPermission(context, false, permissionGroupIDList)
    }

    fun checkPermission(context: Context?, scene: String, permissionGroupIDList: List<String>) {
        checkPermission(context, scene, false, null, permissionGroupIDList)
    }

    fun checkPermission(context: Context?, canCancel: Boolean, permissionGroupIDList: List<String>) {
        checkPermission(context, "", canCancel, null, permissionGroupIDList)
    }

    fun checkPermission(context: Context?, scene: String, canCancel: Boolean, result: OnPermissionResult?, permissionGroupIDList: List<String>) {
        checkPermission(context, scene, canCancel, PermissionsActivityV2::class.java, result, permissionGroupIDList)
    }

    fun checkPermission(context: Context?, scene: String, canCancel: Boolean, permissionsActivityClass: Class<out PermissionsActivity>, result: OnPermissionResult?, permissionGroupIDList: List<String>) {
        result?.let {
            mOuterResultListenerList.put(scene, it)
        }
        if (null == context) {
            mPermissionCheckResultListener.onFailed(scene, "context is null")
        } else {
            mContext = context.applicationContext
            if (hasPermissionGroup(context, permissionGroupIDList)) {
                mPermissionCheckResultListener.onSuccess(scene)
            } else {
                try {
                    val intent = Intent(context, permissionsActivityClass)
                    intent.putExtra(PermissionsActivity.EXTRA_PERMISSIONS, permissionGroupIDList.toTypedArray())
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

    fun getPermissionGroup(scene: String, permissionGroupID: String): List<String> {
        return if (mPermissionGroup.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionGroup(getPermissionKey(scene, permissionGroupID), false);
        } else {
            getPermissionGroup(permissionGroupID, true)
        }
    }

    private fun getPermissionGroup(permissionGroupID: String, isPermission: Boolean): List<String> {
        var permissionList = mutableListOf<String>()
        if (mPermissionGroup.containsKey(permissionGroupID)) {
            mPermissionGroup.get(permissionGroupID)?.let {
                permissionList.addAll(it)
            }
        }
        if (permissionList.isEmpty() && isPermission) {
            permissionList.add(permissionGroupID)
        }
        return permissionList
    }


    fun getPermissionIcon(scene: String, permissionGroupID: String): Int {
        return if (mPermissionIcon.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionIcon(getPermissionKey(scene, permissionGroupID));
        } else {
            getPermissionIcon(permissionGroupID)
        }
    }

    fun getPermissionIcon(permissionGroupID: String): Int {
        return if (mPermissionIcon.containsKey(permissionGroupID)) {
            return mPermissionIcon.get(permissionGroupID) ?: R.mipmap.icon
        } else {
            R.mipmap.icon
        }
    }

    fun getPermissionScene(scene: String, permissionGroupID: String, needSpecial: Boolean): String {
        return if (mPermissionScene.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionScene(getPermissionKey(scene, permissionGroupID), needSpecial)
        } else {
            getPermissionScene(permissionGroupID, needSpecial)
        }
    }

    fun getPermissionScene(permissionGroupID: String, needSpecial: Boolean): String {
        return if (mPermissionScene.containsKey(permissionGroupID)) {
            if (mPermissionScene.get(permissionGroupID) != null) {
                return if (needSpecial) {
                    addHtmlWrapper(mPermissionScene.get(permissionGroupID)!!)
                } else {
                    mPermissionScene.get(permissionGroupID)!!
                }
            } else {
                ""
            }
        } else {
            ""
        }
    }

    fun getPermissionDesc(scene: String, permissionGroupID: String, needSpecial: Boolean): String {
        return if (mPermissionDesc.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionDesc(getPermissionKey(scene, permissionGroupID), needSpecial)
        } else {
            getPermissionDesc(permissionGroupID, needSpecial)
        }
    }

    fun getPermissionDesc(permissionGroupID: String, needSpecial: Boolean): String {
        return if (mPermissionDesc.containsKey(permissionGroupID)) {
            if (mPermissionDesc.get(permissionGroupID) != null) {
                return if (needSpecial) {
                    addHtmlWrapper(mPermissionDesc.get(permissionGroupID)!!)
                } else {
                    mPermissionDesc.get(permissionGroupID)!!
                }
            } else {
                ""
            }
        } else {
            ""
        }
    }

    fun getPermissionContent(context: Context, scene: String, permissionGroupID: String, needSpecial: Boolean): String {
        return if (mPermissionContent.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionContent(context, getPermissionKey(scene, permissionGroupID), needSpecial)
        } else {
            getPermissionContent(context, permissionGroupID, needSpecial)
        }
    }

    fun getPermissionContent(context: Context, permissionGroupID: String, needSpecial: Boolean): String {
        if (mPermissionContent.containsKey(permissionGroupID)) {
            mPermissionContent.get(permissionGroupID)?.let {
                return it
            }
        }
        return getDefaultPermissionContent(context, permissionGroupID, needSpecial)
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

    fun getDefaultPermissionContent(context: Context, showPermissionGroupID: String, needSpecial: Boolean): String {
        return getDefaultPermissionContent(context, getPermissionScene(showPermissionGroupID, needSpecial), getPermissionDesc(showPermissionGroupID, needSpecial))
    }

    fun getDefaultPermissionContent(context: Context, sceneText: String, permissionDesc: String): String {
        return String.format(
                context.getString(R.string.com_bihe0832_permission_default_content),
                APKUtils.getAppName(context),
                if (TextUtils.isEmpty(sceneText)) {
                    mDefaultScene
                } else {
                    sceneText
                },
                if (TextUtils.isEmpty(permissionDesc)) {
                    mDefaultDesc
                } else {
                    permissionDesc
                }
        )
    }

    fun getTitle(context: Context): String {
        return context.resources.getString(R.string.com_bihe0832_permission_title)
    }

    fun getNegativeText(context: Context): String {
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