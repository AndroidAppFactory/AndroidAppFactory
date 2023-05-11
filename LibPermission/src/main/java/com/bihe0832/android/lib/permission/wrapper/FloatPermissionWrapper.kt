package com.bihe0832.android.lib.permission.wrapper

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.os.Binder
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 *
 * @author zixie code@bihe0832.com Created on 12/24/20.
 *
 */

fun checkFloatPermission(context: Context?): Boolean {
    if(context == null){
        return false
    }
    ZLog.d("CheckFloatPermissionUtil sdk_int:" + BuildUtils.SDK_INT)
    if (BuildUtils.SDK_INT < Build.VERSION_CODES.KITKAT) return true
    return if (BuildUtils.SDK_INT < Build.VERSION_CODES.M) {
        try {
            var cls = Class.forName("android.content.Context")
            val declaredField: Field = cls.getDeclaredField("APP_OPS_SERVICE")
            declaredField.setAccessible(true)
            var obj: Any? = declaredField.get(cls) as? String ?: return false
            val str2 = obj as String
            obj = cls.getMethod("getSystemService", String::class.java).invoke(context, str2)
            cls = Class.forName("android.app.AppOpsManager")
            val declaredField2: Field = cls.getDeclaredField("MODE_ALLOWED")
            declaredField2.setAccessible(true)
            val checkOp: Method = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String::class.java)
            val result = checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName()) as Int
            result == declaredField2.getInt(cls)
        } catch (e: java.lang.Exception) {
            false
        }
    } else {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val appOpsMgr = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", Process.myUid(), context.getPackageName())
                Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

        } else {
            Settings.canDrawOverlays(context)
        }
    }
    return false
}

fun openFloatSettings(context: Context?) {
    IntentUtils.startAppSettings(context, PermissionManager.getPermissionSettings(Manifest.permission.SYSTEM_ALERT_WINDOW))
}