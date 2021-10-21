package com.bihe0832.android.lib.device

import android.os.Build
import android.os.Build.VERSION
import android.os.SystemProperties
import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.utils.ConvertUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by hardyshi on 2017/10/31.
 */
object ManufacturerUtil {

    private var mSystemProperties = ConcurrentHashMap<String, String>()

    fun getValueByKey(key: String, getDefaultValue: () -> String): String {
        if (!mSystemProperties.contains(key)) {
            mSystemProperties[key] = SystemProperties.get(key, getDefaultValue())
        }
        return mSystemProperties[key] ?: ""
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    val deviceBrand: String
        get() {
            return getValueByKey("ro.product.brand") { Build.BRAND }
        }

    /**
     * 获取手机型号
     *
     * @return 型号
     */
    val deviceModel: String
        get() {
            return getValueByKey("ro.product.model") { Build.MODEL }
        }

    val fingerPrint: String
        get() {
            return getValueByKey("ro.build.fingerprint") { Build.FINGERPRINT }
        }

    val manufacturer: String
        get() {
            return getValueByKey("ro.product.manufacturer") { Build.MANUFACTURER }
        }

    val commonRomVersion: String
        get() = getValueByKey("ro.build.display.id") { "" }

    fun isCurrentLanguageSimpleChinese(): Boolean {
        return Locale.getDefault().language.trim { it <= ' ' }.toLowerCase() == "zh"
    }

    val isHuawei: Boolean
        get() {
            if (TextFactoryUtils.trimSpace(manufacturer.toLowerCase()).contains("huawei")) {
                return true
            }
            return TextFactoryUtils.trimSpace(fingerPrint.toLowerCase()).contains("huawei")
        }

    val emuiVersion: String
        get() {
            try {
                val emuiVersion = getValueByKey("ro.build.version.emui") { "" }
                if (!TextUtils.isEmpty(emuiVersion)) {
                    return emuiVersion.substring(emuiVersion.indexOf("_") + 1)
                }
            } catch (var1: Exception) {
                var1.printStackTrace()
            }
            return if (VERSION.SDK_INT >= 24) "5.0" else "4.0"
        }

    val isXiaomi: Boolean
        get() {
            val manufacturerModel = "$manufacturer-$deviceModel"
            return (TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("xiaomi")
                    || TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("redmi"))
        }

    val isMiRom: Boolean
        get() {
            val miUiVersionName = getValueByKey("ro.miui.ui.version.name") { "" }
            return if (!TextUtils.isEmpty(miUiVersionName)) {
                true
            } else {
                TextFactoryUtils.trimSpace(manufacturer).toLowerCase().contains("xiaomi")
            }
        }

    val miuiVersion: String
        get() {
            val version = getValueByKey("ro.miui.ui.version.name") { "" }
            if (!TextUtils.isEmpty(version)) {
                try {
                    return version.substring(1)
                } catch (var2: Exception) {
                    ZLog.e("get miui version code error, version : $version")
                }
            }
            return version
        }

    val miuiVersionCode: Long
        get() {
            return ConvertUtils.parseLong(getValueByKey("ro.miui.ui.version.code") { "" }, -1)
        }

    val isOppo: Boolean
        get() {
            try {
                val manufacturer = manufacturer
                if (!TextUtils.isEmpty(manufacturer) && manufacturer.toLowerCase().contains("oppo")) {
                    return true
                }
                val fingerprint = fingerPrint
                if (!TextUtils.isEmpty(fingerprint) && fingerprint.toLowerCase().contains("oppo")) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

    val oppoRomVersion: String
        get() {
            val romVersion = getValueByKey("ro.build.version.opporom") { "" }
            return try {
                romVersion.substring(1)
            } catch (var2: Exception) {
                var2.printStackTrace()
                ZLog.e("getOppoRomVersion version code error, version : $romVersion")
                romVersion
            }
        }

    val isVivo: Boolean
        get() {
            return TextFactoryUtils.trimSpace("$manufacturer-$deviceModel".toLowerCase()).contains("vivo")
        }

    val isSmartisan: Boolean
        get() = TextFactoryUtils.trimSpace(deviceBrand.toLowerCase()).contains("smartisan")

    val isMeizu: Boolean
        get() = TextFactoryUtils.trimSpace(deviceBrand.toLowerCase()).contains("meizu")

    val vivoRomVersion: String
        get() = getValueByKey("ro.vivo.android.os.version") { "" }

    val isSumsung: Boolean
        get() = TextFactoryUtils.trimSpace(manufacturer.toLowerCase()).contains("samsung")

    val sumsungRomVersion: String
        get() {
            val version = getValueByKey("ro.build.display.id") { "" }
            try {
                if (version != null) {
                    return version.substring(0, version.indexOf("."))
                }
            } catch (var2: Exception) {
                var2.printStackTrace()
            }
            return version
        }
}