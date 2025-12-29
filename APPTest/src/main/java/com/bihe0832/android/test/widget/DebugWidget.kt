package com.bihe0832.android.test.widget

import android.content.Context
import com.bihe0832.android.app.dialog.AAFUniqueDialogManager
import com.bihe0832.android.common.debug.widget.app.AAFDebugWidgetProviderDetail
import com.bihe0832.android.common.debug.widget.app.AAFDebugWidgetProviderSimple
import com.bihe0832.android.common.debug.widget.device.AAFDebugWidgetProviderDevice
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.widget.tools.WidgetTools
import com.bihe0832.android.lib.aaf.res.R as ResR


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/6/15.
 * Description: Description
 *
 */

object DebugWidget {

    private var hasShow = false
    private val CONFIG_KEY_HAS_REFUSE = "DebugWidget"
    fun showAddDebugWidgetTips(context: Context) {
        if (!hasShow && !ZixieContext.isOfficial()
                && !Config.isSwitchEnabled(CONFIG_KEY_HAS_REFUSE, false)
                && !WidgetTools.hasAddWidget(context, AAFDebugWidgetProviderDetail::class.java)
                && !WidgetTools.hasAddWidget(context, AAFDebugWidgetProviderSimple::class.java)
                && !WidgetTools.hasAddWidget(context, AAFDebugWidgetProviderDevice::class.java)) {
            hasShow = true
            AAFUniqueDialogManager.tipsUniqueDialogManager.showUniqueDialog(
                    context,
                    CONFIG_KEY_HAS_REFUSE,
                    "调试优化",
                    "为提高开发调试效率，" + context.resources.getString(ResR.string.app_name) + "已支持在主界面添加调试组件快速查看基本信息，是否立即添加？",
                    "立即添加",
                    "稍后添加",
                    true,
                    object : OnDialogListener {
                        override fun onPositiveClick() {
                            WidgetTools.addWidgetToHome(context, AAFDebugWidgetProviderDetail::class.java)
                        }

                        override fun onNegativeClick() {
                            Config.writeConfig(CONFIG_KEY_HAS_REFUSE, Config.VALUE_SWITCH_ON)
                        }

                        override fun onCancel() {

                        }

                    })
        }
    }


}