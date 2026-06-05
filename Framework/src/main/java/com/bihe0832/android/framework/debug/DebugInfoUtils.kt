package  com.bihe0832.android.framework.debug;

import android.content.Context
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.ui.dialog.senddata.SendTextUtils
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.utils.time.DateUtil

/**
 * 调试信息工具类
 *
 * 提供统一的调试信息收集和展示功能，可复用于 View 和 Compose 体系
 *
 * @author zixie code@bihe0832.com
 * Created on 2026/6/5.
 */
object DebugInfoUtils {

    /**
     * 显示调试信息对话框
     *
     * @param context 上下文
     * @param titleResId 标题资源ID，默认为开发者分享标题
     * @param contentResId 内容资源ID，默认为开发者分享内容
     * @param tipsResId 提示资源ID，默认为开发者分享提示
     * @param buttonTextResId 按钮文本资源ID，默认为开发者分享
     */
    fun showDebugInfo(
        context: Context,
        titleResId: Int = R.string.com_bihe0832_share_to_develop_title,
        contentResId: Int = R.string.com_bihe0832_share_to_develop_content,
        tipsResId: Int = R.string.com_bihe0832_share_to_develop_tips,
        buttonTextResId: Int = R.string.com_bihe0832_share_to_develop
    ) {
        val debugInfo = getDebugInfo(context)
        SendTextUtils.sendInfo(
            context,
            context.getString(titleResId),
            context.getString(contentResId),
            debugInfo,
            context.getString(tipsResId),
            context.getString(buttonTextResId),
            true
        )
    }

    /**
     * 发送调试信息（Java兼容版本）
     *
     * @param context 上下文
     * @param result 调试信息内容
     */
    @JvmStatic
    fun sendInfo(context: Context, result: String) {
        SendTextUtils.sendInfo(
            context,
            context.getString(R.string.com_bihe0832_share_to_develop_title),
            context.getString(R.string.com_bihe0832_share_to_develop_content),
            result,
            context.getString(R.string.com_bihe0832_share_to_develop_tips),
            context.getString(R.string.com_bihe0832_share_to_develop),
            true
        )
    }

    /**
     * 获取完整的调试信息
     *
     * @param context 上下文
     * @return 调试信息字符串
     */
    @JvmStatic
    fun getDebugInfo(context: Context): String {
        return getDebugVersionInfo(context, true) + getDebugDeviceInfo(true)
    }

    /**
     * 获取版本调试信息
     *
     * @param context 上下文
     * @param needSpaceLine 是否需要空行分隔
     * @return 版本调试信息字符串
     */
    @JvmStatic
    fun getDebugVersionInfo(context: Context, needSpaceLine: Boolean): String {
        val builder = StringBuilder()
        builder.append("版本信息: \\n")
        builder.append("应用版本: ${ZixieContext.getVersionName()}.${ZixieContext.getVersionCode()}\\n")
        builder.append("版本标识: ${ZixieContext.getVersionTag()}\\n")
        builder.append("安装时间: ${DateUtil.getDateEN(LifecycleHelper.getVersionInstalledTime())}\\n")
        builder.append("channel: ${ZixieContext.channelID}\\n")
        builder.append("签名MD5: ${APKUtils.getSigMd5ByPkgName(context, context.packageName)}\\n")
        builder.append("official: ${ZixieContext.isOfficial()}\\n")
        if (needSpaceLine) {
            builder.append("\\n")
        }
        return builder.toString()
    }

    /**
     * 获取设备调试信息
     *
     * @param needSpaceLine 是否需要空行分隔
     * @return 设备调试信息字符串
     */
    @JvmStatic
    fun getDebugDeviceInfo(needSpaceLine: Boolean): String {
        val builder = StringBuilder()
        builder.append("设备信息: \\n")
        builder.append("厂商&型号: ${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}\\n")
        if (ManufacturerUtil.isHarmonyOs()) {
            builder.append("系统版本: Android ${BuildUtils.RELEASE}, API ${BuildUtils.SDK_INT}, Harmony(${ManufacturerUtil.getHarmonyVersion()})\\n")
        } else {
            builder.append("系统版本: Android ${BuildUtils.RELEASE}, API ${BuildUtils.SDK_INT}\\n")
        }
        builder.append("系统指纹: ${ManufacturerUtil.FINGERPRINT}\\n")
        builder.append("设备标识: ${ZixieContext.deviceId}\\n")
        if (needSpaceLine) {
            builder.append("\\n")
        }
        return builder.toString()
    }
}