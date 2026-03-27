package com.bihe0832.android.common.about.compose.wrapper

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.openFeedback
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.framework.router.shareAPP
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.lib.superapp.QQHelper
import com.bihe0832.android.lib.superapp.WechatOfficialAccount
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.R as FrameworkR
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.model.res.R as ModelResR

/**
 * Compose 版设置项工厂，与 View 体系中的 SettingsItem 对应。
 *
 * 提供各种常用设置项的 @Composable 工厂方法，其他页面可直接调用，
 * 无需重复编写参数配置。
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/26.
 */
object SettingsItemFactory {

    /**
     * 版本列表（默认标题）
     *
     * @param listener 点击回调
     */
    @Composable
    fun VersionList(
        listener: ((context: Context) -> Unit)? = null,
    ) {
        VersionList(
            ZixieContext.applicationContext!!.getString(ModelResR.string.settings_version_title),
            listener = listener,
        )
    }

    /**
     * 版本列表（自定义标题）
     *
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun VersionList(
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_menu,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                ThemeResourcesManager.getString(FrameworkR.string.version_url).let { url ->
                    if (url.isNullOrEmpty()) {
                        ZixieContext.showWaiting()
                    } else {
                        openZixieWeb(url)
                    }
                }
                listener?.invoke(it)
            }
        )
    }

    /**
     * 检查更新（自定义标题）
     *
     * @param title 标题文本
     * @param cloud 云端更新数据，用于判断是否显示红点
     * @param listener 点击回调
     */
    @Composable
    fun Update(
        title: String,
        cloud: UpdateDataFromCloud?,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_update,
            mHeaderTextBold = true,
            mShowDriver = true,
            mShowGo = true,
            mItemNewNum = if (cloud?.canShowNew() == true) 0 else -1,
            mHeaderListener = listener,
        )
    }

    /**
     * 检查更新（默认标题）
     *
     * @param cloud 云端更新数据
     * @param listener 点击回调
     */
    @Composable
    fun Update(
        cloud: UpdateDataFromCloud?,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        Update(
            title = ThemeResourcesManager.getString(ModelResR.string.settings_update_title) ?: "",
            cloud = cloud,
            listener = listener,
        )
    }

    /**
     * 关于应用标题文本
     */
    fun getAboutTitle(): String {
        return ThemeResourcesManager.getString(ModelResR.string.settings_about_title) +
                ThemeResourcesManager.getString(ModelResR.string.about_app)
    }

    /**
     * 关于应用（带更新红点）
     *
     * @param cloud 云端更新数据
     * @param listener 点击回调
     */
    @Composable
    fun AboutAPP(
        cloud: UpdateDataFromCloud?,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = getAboutTitle(),
            mItemIconRes = ResR.drawable.icon_android,
            mHeaderTextBold = true,
            mShowDriver = true,
            mShowGo = true,
            mItemNewNum = if (cloud?.canShowNew() == true) 0 else -1,
            mHeaderListener = listener,
        )
    }

    /**
     * 消息中心
     *
     * @param msgNum 未读消息数，-1 不显示，0 显示红点，>0 显示数字
     * @param listener 点击回调
     */
    @Composable
    fun Message(
        msgNum: Int,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = ThemeResourcesManager.getString(ModelResR.string.settings_message_title) ?: "",
            mItemIconRes = ResR.drawable.icon_message,
            mShowDriver = true,
            mShowGo = true,
            mItemNewNum = msgNum,
            mHeaderListener = listener,
        )
    }

    /**
     * 意见反馈 - URL 方式（默认标题）
     *
     * @param listener 点击回调
     */
    @Composable
    fun FeedbackURL(
        listener: ((context: Context) -> Unit)? = null,
    ) {
        FeedbackURL(
            ThemeResourcesManager.getString(ModelResR.string.feedback) ?: "",
            listener = listener,
        )
    }

    /**
     * 意见反馈 - URL 方式（自定义标题）
     *
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun FeedbackURL(
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = FrameworkR.drawable.icon_feedback,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                openFeedback()
                listener?.invoke(it)
            }
        )
    }

    /**
     * 意见反馈 - 邮件方式（默认标题）
     *
     * @param activity 当前 Activity，用于发送邮件
     * @param listener 点击回调
     */
    @Composable
    fun FeedbackMail(
        activity: Activity?,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        FeedbackMail(
            activity,
            ThemeResourcesManager.getString(ModelResR.string.feedback) ?: "",
            listener = listener,
        )
    }

    /**
     * 意见反馈 - 邮件方式（自定义标题）
     *
     * @param activity 当前 Activity
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun FeedbackMail(
        activity: Activity?,
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        val mail = ThemeResourcesManager.getString(FrameworkR.string.feedback_mail) ?: ""
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_message,
            mShowDriver = true,
            mShowGo = true,
            mTipsText = "<u>$mail</u>",
            mHeaderTipsListener = {
                IntentUtils.sendMail(
                    activity,
                    mail,
                    String.format(
                        ThemeResourcesManager.getString(ModelResR.string.feedback_mail_title)!!,
                        APKUtils.getAppName(activity),
                    ),
                    "",
                ).let { res ->
                    if (!res) {
                        openFeedback()
                    }
                }
                listener?.invoke(it)
            }
        )
    }

    /**
     * 客服 QQ（默认标题）
     *
     * @param activity 当前 Activity
     * @param listener 点击回调
     */
    @Composable
    fun QQService(
        activity: Activity?,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        QQService(activity, "客服QQ", listener = listener)
    }

    /**
     * 客服 QQ（自定义标题）
     *
     * @param activity 当前 Activity
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun QQService(
        activity: Activity?,
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        val feedbackQQnumber = ThemeResourcesManager.getString(FrameworkR.string.feedback_qq) ?: ""
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_qq,
            mShowDriver = true,
            mShowGo = true,
            mTipsText = "<u>$feedbackQQnumber</u>",
            mHeaderTipsListener = {
                val res = QQHelper.openQQChat(activity, feedbackQQnumber)
                if (!res) {
                    ZixieContext.showToastJustAPPFront(
                        ThemeResourcesManager.getString(ModelResR.string.contact_QQ_join_failed)!!
                    )
                }
                listener?.invoke(it)
            }
        )
    }

    /**
     * 调试入口（默认标题）
     *
     * @param listener 点击回调
     */
    @Composable
    fun Debug(
        listener: ((context: Context) -> Unit)? = null,
    ) {
        Debug("调试", listener = listener)
    }

    /**
     * 调试入口（自定义标题）
     *
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun Debug(
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_android,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_DEBUG)
                listener?.invoke(it)
            }
        )
    }

    /**
     * 微信公众号（默认标题和提示）
     *
     * @param activity 当前 Activity
     * @param listener 点击回调
     */
    @Composable
    fun Wechat(
        activity: Activity?,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        Wechat(
            activity,
            activity?.getString(ModelResR.string.wechat_official_account) ?: "",
            activity?.getString(ModelResR.string.wechat_sub_content_tips) ?: "",
            listener = listener,
        )
    }

    /**
     * 微信公众号（自定义标题和提示）
     *
     * @param activity 当前 Activity
     * @param title 标题文本
     * @param tipsText 提示文本
     * @param listener 点击回调
     */
    @Composable
    fun Wechat(
        activity: Activity?,
        title: String,
        tipsText: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_wechat,
            mShowDriver = true,
            mShowGo = true,
            mTipsText = "<u>$tipsText</u>",
            mHeaderTipsListener = {
                activity?.let {
                    WechatOfficialAccount.showSubscribe(
                        activity,
                        WechatOfficialAccount.WechatOfficialAccountData().apply {
                            this.mAccountID =
                                ThemeResourcesManager.getString(ModelResR.string.wechat_id)
                            this.mAccountTitle =
                                ThemeResourcesManager.getString(ModelResR.string.wechat_name)
                            this.mSubContent =
                                ThemeResourcesManager.getString(ModelResR.string.wechat_sub_content)
                        },
                    )
                }
                listener?.invoke(it)
            }
        )
    }

    /**
     * 关于开发者（默认标题）
     *
     * @param listener 点击回调
     */
    @Composable
    fun Zixie(
        listener: ((context: Context) -> Unit)? = null,
    ) {
        Zixie("关于开发者", listener = listener)
    }

    /**
     * 关于开发者（自定义标题）
     *
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun Zixie(
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_author,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                openZixieWeb("file:///android_asset/web/author.html")
                listener?.invoke(it)
            }
        )
    }

    /**
     * 分享应用（默认标题）
     *
     * @param canSendAPK 是否支持发送 APK 文件
     * @param listener 点击回调
     */
    @Composable
    fun ShareAPP(
        canSendAPK: Boolean,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        ShareAPP(
            canSendAPK,
            ZixieContext.applicationContext!!.getString(ModelResR.string.com_bihe0832_share_title),
            listener = listener,
        )
    }

    /**
     * 分享应用（自定义标题）
     *
     * @param canSendAPK 是否支持发送 APK 文件
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun ShareAPP(
        canSendAPK: Boolean,
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_share,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                shareAPP(canSendAPK)
                listener?.invoke(it)
            }
        )
    }

    /**
     * 清除缓存
     *
     * @param activity 当前 Activity
     */
    fun clearCache(activity: Activity) {
        DialogUtils.showConfirmDialog(
            activity,
            ThemeResourcesManager.getString(ModelResR.string.settings_clear_tips) ?: "",
            true,
            object : OnDialogListener {
                override fun onPositiveClick() {
                    val loadingDialog = LoadingDialog(activity)
                    loadingDialog.setCanCanceled(false)
                    loadingDialog.setIsFullScreen(true)
                    loadingDialog.setHtmlTitle(
                        ThemeResourcesManager.getString(ModelResR.string.settings_clear_loading) ?: ""
                    )
                    loadingDialog.show()
                    ThreadManager.getInstance().start {
                        AAFFileWrapper.clear()
                        ThreadManager.getInstance().runOnUIThread { ZixieContext.exitAPP() }
                    }
                }

                override fun onNegativeClick() {
                }

                override fun onCancel() {
                    onNegativeClick()
                }
            },
        )
    }

    /**
     * 清除缓存（默认标题）
     *
     * @param activity 当前 Activity
     * @param listener 点击回调
     */
    @Composable
    fun ClearCache(
        activity: Activity,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        ClearCache(
            activity,
            activity.getString(ModelResR.string.settings_clear_title),
            listener = listener,
        )
    }

    /**
     * 清除缓存（自定义标题）
     *
     * @param activity 当前 Activity
     * @param title 标题文本
     * @param listener 点击回调
     */
    @Composable
    fun ClearCache(
        activity: Activity,
        title: String,
        listener: ((context: Context) -> Unit)? = null,
    ) {
        SettingsItemGo(
            mItemText = title,
            mItemIconRes = ResR.drawable.icon_delete_fill,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                clearCache(activity)
                listener?.invoke(it)
            }
        )
    }
}

// ==================== 预览函数 ====================

/**
 * 预览：常用设置项组合
 */
@Preview(name = "Factory - 常用设置项", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemFactoryCommon() {
    Column {
        SettingsItemFactory.Update(cloud = null)
        SettingsItemFactory.VersionList()
        SettingsItemFactory.FeedbackURL()
    }
}

/**
 * 预览：社交与分享设置项
 */
@Preview(name = "Factory - 社交与分享", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemFactorySocial() {
    Column {
        SettingsItemFactory.QQService(activity = null)
        SettingsItemFactory.Wechat(activity = null)
        SettingsItemFactory.ShareAPP(canSendAPK = false)
    }
}

/**
 * 预览：其他设置项
 */
@Preview(name = "Factory - 其他设置项", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemFactoryOther() {
    Column {
        SettingsItemFactory.Zixie()
        SettingsItemFactory.Debug()
        SettingsItemFactory.Message(msgNum = 5, listener = null)
    }
}
