package com.bihe0832.android.lib.utils.os

import android.os.Build
import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.TextFactoryCore
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * Created by zixie on 2017/10/31.
 */
object ManufacturerUtil {

    private var mSystemProperties = ConcurrentHashMap<String, String>()

    @Synchronized
    fun getValueByKey(key: String, getDefaultValue: () -> String): String {
        if (!mSystemProperties.containsKey(key)) {
            ZLog.d("ManufacturerUtil", "ManufacturerUtil read system key $key")
            mSystemProperties[key] = android.os.SystemProperties.get(key, getDefaultValue())
        }
        return mSystemProperties[key] ?: ""
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    val BRAND: String by lazy {
        getValueByKey("ro.product.brand") { Build.BRAND }
    }

    /**
     * 获取手机型号
     *
     * @return 型号
     */
    val MODEL: String by lazy {
        getValueByKey("ro.product.model") { Build.MODEL }
    }

    /**
     * 设备唯一识别码（FINGERPRINT）
     */
    val FINGERPRINT: String by lazy {
        getValueByKey("ro.build.fingerprint") { Build.FINGERPRINT }
    }

    /**
     * 硬件制造商（MANUFACTURER）
     */
    val MANUFACTURER: String by lazy {
        getValueByKey("ro.product.manufacturer") { Build.MANUFACTURER }
    }

    /**
     * 产品名称（PRODUCT）：即手机厂商

     */
    val PRODUCT: String by lazy {
        getValueByKey("ro.product.name") { Build.PRODUCT }
    }

    /**
     * 设备名 （DEVICE)
     */
    val DEVICE: String by lazy {
        getValueByKey("ro.product.device") { Build.DEVICE }
    }

    /**
     * 显示屏参数（DISPLAY)
     */
    val DISPLAY: String by lazy {
        getValueByKey("ro.build.display.id") { Build.DISPLAY }
    }

    fun isCurrentLanguageSimpleChinese(): Boolean {
        return Locale.getDefault().language.trim { it <= ' ' }.toLowerCase() == "zh"
    }

    val isHuawei: Boolean by lazy {
        if (TextFactoryCore.trimSpace(MANUFACTURER.toLowerCase()).contains("huawei")) {
            true
        } else {
            TextFactoryCore.trimSpace(FINGERPRINT.toLowerCase()).contains("huawei")
        }
    }

    val emuiVersion: String by lazy {
        try {
            val emuiVersion = getValueByKey("ro.build.version.emui") { "" }
            if (!TextUtils.isEmpty(emuiVersion)) {
                return@lazy emuiVersion.substring(emuiVersion.indexOf("_") + 1)
            }
        } catch (var1: Exception) {
            var1.printStackTrace()
        }
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N) "5.0" else "4.0"

    }

    val isXiaomi: Boolean by lazy {
        val manufacturerModel = "$MANUFACTURER-$DEVICE"
        (TextFactoryCore.trimSpace(manufacturerModel.toLowerCase()).contains("xiaomi")
                || TextFactoryCore.trimSpace(manufacturerModel.toLowerCase()).contains("redmi"))
    }

    val isMiRom: Boolean by lazy {
        if (!TextUtils.isEmpty(miuiVersion)) {
            true
        } else {
            TextFactoryCore.trimSpace(MANUFACTURER).toLowerCase().contains("xiaomi")
        }
    }

    val miuiVersion: String by lazy {
        val version = getValueByKey("ro.miui.ui.version.name") { "" }
        if (!TextUtils.isEmpty(version)) {
            try {
                return@lazy version.substring(1)
            } catch (var2: Exception) {
                ZLog.e("get miui version code error, version : $version")
            }
        }
        return@lazy version
    }

    val miuiVersionCode: Long by lazy {
        ConvertUtils.parseLong(getValueByKey("ro.miui.ui.version.code") { "" }, -1)
    }

    val isOppo: Boolean by lazy {
        try {
            val manufacturer = MANUFACTURER
            if (!TextUtils.isEmpty(manufacturer) && manufacturer.toLowerCase().contains("oppo")) {
                return@lazy true
            }
            val fingerprint = FINGERPRINT
            if (!TextUtils.isEmpty(fingerprint) && fingerprint.toLowerCase().contains("oppo")) {
                return@lazy true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@lazy false
    }

    val oppoRomVersion: String by lazy {
        val romVersion = getValueByKey("ro.build.version.opporom") { "" }
        try {
            return@lazy romVersion.substring(1)
        } catch (var2: Exception) {
            var2.printStackTrace()
            ZLog.e("getOppoRomVersion version code error, version : $romVersion")
            return@lazy romVersion
        }
    }

    val isVivo: Boolean =
        TextFactoryCore.trimSpace("$MANUFACTURER-$DEVICE".toLowerCase()).contains("vivo")

    val isSmartisan: Boolean = TextFactoryCore.trimSpace(BRAND.toLowerCase()).contains("smartisan")

    val isMeizu: Boolean = TextFactoryCore.trimSpace(BRAND.toLowerCase()).contains("meizu")

    val vivoRomVersion: String by lazy {
        getValueByKey("ro.vivo.android.os.version") { "" }
    }

    val isSumsung: Boolean =
        TextFactoryCore.trimSpace(MANUFACTURER.toLowerCase()).contains("samsung")

    val sumsungRomVersion: String by lazy {
        val version = getValueByKey("ro.build.display.id") { "" }
        try {
            if (version != null) {
                return@lazy version.substring(0, version.indexOf("."))
            }
        } catch (var2: Exception) {
            var2.printStackTrace()
        }
        return@lazy version
    }

    /**
     * 是否为鸿蒙系统
     *
     * @return true为鸿蒙系统
     */
    fun isHarmonyOs(): Boolean {


        val version = getValueByKey("com.huawei.system.BuildEx") {
            try {
                val buildExClass = Class.forName("com.huawei.system.BuildEx")
                val osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass)
                return@getValueByKey osBrand.toString()
            }catch (e:java.lang.Exception){
                e.printStackTrace()
                return@getValueByKey ""
            }
        }

        return "Harmony".equals(version, ignoreCase = true)
    }

    /**
     * 获取鸿蒙系统版本号
     *
     * @return 版本号
     */
    fun getHarmonyVersion(): String {
        return getValueByKey("hw_sc.build.platform.version") { BuildUtils.DISPLAY }
    }
}