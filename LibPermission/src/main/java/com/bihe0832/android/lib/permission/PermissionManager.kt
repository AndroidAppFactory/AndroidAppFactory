package com.bihe0832.android.lib.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.ui.PermissionsActivity
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.apk.APKUtils
import java.util.concurrent.ConcurrentHashMap
import com.bihe0832.android.lib.aaf.res.R as ResR

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
        ThemeResourcesManager.getString(ResR.string.com_bihe0832_permission_default_scene) ?: "完整"
    }

    private val mDefaultDesc by lazy {
        ThemeResourcesManager.getString(ResR.string.com_bihe0832_permission_default_desc) ?: "设备"
    }


    private fun getOuterPermissionResultListener(scene: String): OnPermissionResult? {
        return mOuterResultListenerList.get(scene)
    }

    fun logPermissionConfigInfo() {
        ZLog.d(TAG, "----------logPermissionConfigInfo start ----------")
        mPermissionGroup.forEach { (key, value) ->
            logPermission("mPermissionGroup", key, value.toString())
        }
        ZLog.d(TAG, "\n\n")
        mPermissionScene.forEach { (key, value) ->
            logPermission("mPermissionScene", key, value)
        }
        ZLog.d(TAG, "\n\n")
        mPermissionDesc.forEach { (key, value) ->
            logPermission("mPermissionDesc", key, value)
        }
        ZLog.d(TAG, "\n\n")
        mPermissionContent.forEach { (key, value) ->
            logPermission("mPermissionContent", key, value)
        }
        ZLog.d(TAG, "\n\n")
        mPermissionSettings.forEach { (key, value) ->
            logPermission("mPermissionSettings", key, value)
        }
        ZLog.d(TAG, "----------logPermissionConfigInfo end ----------")
    }

    fun logPermission(type: String, key: String, value: String) {
        ZLog.d(TAG, "$type: \n\tkey is [$key],  data is [${value}]")
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
        if (!TextUtils.isEmpty(sceneid)) {
            return permissionGroupID + "_" + sceneid
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

    fun isAllPermissionOK(context: Context, permissionKey: String): Boolean {
        return if (mPermissionGroup.containsKey(permissionKey)) {
            !PermissionsChecker(context).lacksPermissions(mPermissionGroup.get(permissionKey))
        } else {
            !PermissionsChecker(context).lacksPermission(permissionKey)
        }
    }

    fun isAllPermissionOK(context: Context, scene: String, permissionGroupID: String): Boolean {
        val groupKey = getPermissionKey(scene, permissionGroupID)
        return isAllPermissionOK(context, groupKey)
    }

    fun isAllPermissionOK(context: Context, permissionKeyList: List<String>): Boolean {
        for (permissionKey in permissionKeyList) {
            if (!isAllPermissionOK(context, permissionKey)) {
                return false
            }
        }
        return true
    }

    fun isAllPermissionOK(context: Context, scene: String, permissionGroupIDList: List<String>): Boolean {
        for (permissionKey in permissionGroupIDList) {
            if (!isAllPermissionOK(context, scene, permissionKey)) {
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

    fun checkPermission(
        context: Context?,
        scene: String,
        canCancel: Boolean,
        result: OnPermissionResult?,
        permissionGroupIDList: List<String>,
    ) {
        checkPermission(context, scene, canCancel, PermissionsActivityV2::class.java, result, permissionGroupIDList)
    }

    fun checkPermission(
        context: Context?,
        scene: String,
        canCancel: Boolean,
        permissionsActivityClass: Class<out PermissionsActivity>,
        result: OnPermissionResult?,
        permissionGroupIDList: List<String>,
    ) {
        result?.let {
            mOuterResultListenerList.put(scene, it)
        }
        if (null == context) {
            mPermissionCheckResultListener.onFailed(scene, "context is null")
        } else {
            mContext = context.applicationContext
            if (isAllPermissionOK(context, permissionGroupIDList)) {
                mPermissionCheckResultListener.onSuccess(scene)
            } else {
                try {
                    startPermissionActivity(context, scene, canCancel, permissionsActivityClass, permissionGroupIDList)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mPermissionCheckResultListener.onFailed(scene, "start permission activity failed")
                }
            }
        }
    }

    fun startPermissionActivity(
        context: Context?,
        scene: String,
        canCancel: Boolean,
        permissionsActivityClass: Class<out PermissionsActivity>,
        permissionGroupIDList: List<String>,
    ) {
        val intent = Intent(context, permissionsActivityClass)
        intent.putExtra(PermissionsActivity.EXTRA_PERMISSIONS, permissionGroupIDList.toTypedArray())
        intent.putExtra(PermissionsActivity.EXTRA_CAN_CANCEL, canCancel)
        intent.putExtra(PermissionsActivity.EXTRA_SOURCE, scene)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        ActivityCompat.startActivity(context!!, intent, null)
    }

    fun getPermissionsByGroupID(scene: String, permissionGroupID: String): List<String> {
        return if (mPermissionGroup.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionsByGroupID(getPermissionKey(scene, permissionGroupID), false);
        } else {
            getPermissionsByGroupID(permissionGroupID, true)
        }
    }

    private fun getPermissionsByGroupID(permissionKey: String, isPermission: Boolean): List<String> {
        var permissionList = mutableListOf<String>()
        if (mPermissionGroup.containsKey(permissionKey)) {
            mPermissionGroup.get(permissionKey)?.let {
                permissionList.addAll(it)
            }
        }
        if (permissionList.isEmpty() && isPermission) {
            permissionList.add(permissionKey)
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
            return mPermissionIcon.get(permissionGroupID) ?: ResR.mipmap.icon
        } else {
            ResR.mipmap.icon
        }
    }

    fun getPermissionScene(
        scene: String,
        permissionGroupID: String,
        useDefault: Boolean,
        needSpecial: Boolean,
    ): String {
        return if (mPermissionScene.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionScene(getPermissionKey(scene, permissionGroupID), needSpecial)
        } else if (useDefault) {
            getPermissionScene(permissionGroupID, needSpecial)
        } else {
            return ""
        }
    }

    fun getPermissionScene(permissionGroupID: String, needSpecial: Boolean): String {
        return if (mPermissionScene.containsKey(permissionGroupID)) {
            if (mPermissionScene.get(permissionGroupID) != null) {
                return if (needSpecial) {
                    addPermissionHtmlWrapper(mPermissionScene.get(permissionGroupID)!!)
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

    fun getPermissionDesc(scene: String, permissionGroupID: String, useDefault: Boolean, needSpecial: Boolean): String {
        return if (mPermissionDesc.containsKey(getPermissionKey(scene, permissionGroupID))) {
            return getPermissionDesc(getPermissionKey(scene, permissionGroupID), needSpecial)
        } else if (useDefault) {
            getPermissionDesc(permissionGroupID, needSpecial)
        } else {
            ""
        }
    }

    fun getPermissionDesc(permissionGroupID: String, needSpecial: Boolean): String {
        return if (mPermissionDesc.containsKey(permissionGroupID)) {
            if (mPermissionDesc.get(permissionGroupID) != null) {
                return if (needSpecial) {
                    addPermissionHtmlWrapper(mPermissionDesc.get(permissionGroupID)!!)
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
            getDefaultPermissionContent(context, getPermissionKey(scene, permissionGroupID), needSpecial)
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

    fun getPermissionScene(
        sceneID: String,
        tempPermissionList: List<String>,
        useDefault: Boolean,
        needSpecial: Boolean,
    ): String {
        tempPermissionList.map {
            getPermissionScene(sceneID, it, useDefault, needSpecial)
        }.filter { it.isNotBlank() }.let {
            return it.joinToString("、", "", "")
        }
    }

    fun getPermissionDesc(
        sceneID: String,
        tempPermissionList: List<String>,
        useDefault: Boolean,
        needSpecial: Boolean,
    ): String {
        tempPermissionList.map {
            getPermissionDesc(sceneID, it, useDefault, needSpecial)
        }.filter { it.isNotBlank() }.let {
            return it.joinToString("、", "", "")
        }
    }

    fun getPermissionContent(
        context: Context,
        sceneID: String,
        tempPermissionGroupList: List<String>,
        useDefault: Boolean,
        needSpecial: Boolean,
    ): String {
        return if (tempPermissionGroupList.size > 1) {
            getDefaultPermissionContent(
                context,
                getPermissionScene(sceneID, tempPermissionGroupList, useDefault, needSpecial),
                getPermissionDesc(sceneID, tempPermissionGroupList, useDefault, needSpecial)
            )
        } else if (tempPermissionGroupList.size > 0) {
            getPermissionContent(context, sceneID, tempPermissionGroupList.get(0), needSpecial)
        } else {
            ""
        }
    }

    fun getDefaultPermissionContent(context: Context, showPermissionGroupID: String, needSpecial: Boolean): String {
        return getDefaultPermissionContent(
            context,
            getPermissionScene(showPermissionGroupID, needSpecial),
            getPermissionDesc(showPermissionGroupID, needSpecial)
        )
    }

    fun getDefaultPermissionContent(context: Context, sceneText: String, permissionDesc: String): String {
        return String.format(
            ThemeResourcesManager.getString(ResR.string.com_bihe0832_permission_default_content)!!,
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
        return ThemeResourcesManager.getString(ResR.string.com_bihe0832_permission_title)!!
    }

    fun getNegativeText(context: Context): String {
        return ThemeResourcesManager.getString(ResR.string.com_bihe0832_permission_negtive)!!
    }

    fun getPositiveText(context: Context): String {
        return ThemeResourcesManager.getString(ResR.string.com_bihe0832_permission_positive)!!
    }

    fun getPermissionGroupDenyTime(permissionGroup: String): Long {
        return Config.readConfig(USER_DENY_KEY + permissionGroup, 0L)
    }

    fun setUserDenyTime(permissionGroup: String) {
        Config.writeConfig(USER_DENY_KEY + permissionGroup, System.currentTimeMillis())
    }


    fun getPermissionSettings(permission: String): String {
        mPermissionSettings.get(permission)?.let {
            return it
        }
        return ""
    }

    fun addPermissionHtmlWrapper(content: String): String {
        var color = "#38ADFF"
        ThemeResourcesManager.getColor(ResR.color.com_bihe0832_permission_color)?.let {
            color = String.format("#%06X", (0xFFFFFF and it))
        }
        return "<font color ='$color'><b>$content</b></font>"
    }


}