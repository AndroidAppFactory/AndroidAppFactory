package com.bihe0832.android.app.about

import android.app.Activity
import android.view.View
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.common.settings.card.SettingsDataGo
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule

/**
 * AAF 关于页面内容 Fragment
 *
 * 展示版本更新、版本列表、反馈入口等设置项
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
open class AboutFragment : com.bihe0832.android.common.about.AboutFragment() {

    /**
     * 获取关于页面的数据列表
     *
     * @param processLast 是否处理最后一项的分隔线
     * @return 设置项数据列表
     */
    override fun getDataList(processLast: Boolean): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(
                SettingsItem.getUpdate(
                    UpdateInfoLiveData.value,
                    View.OnClickListener {
                        activity?.let {
                            UpdateManager.checkUpdateAndShowDialog(
                                it,
                                checkUpdateByUser = true,
                                showIfNeedUpdate = true,
                            )
                        }
                    },
                ),
            )
            add(SettingsItem.getVersionList())
            add(getFeedbackItem(activity))
        }.apply {
            processLastItemDriver(processLast)
        }
    }
}

/**
 * 获取反馈入口设置项
 *
 * @param activity 当前 Activity
 * @return 反馈设置项数据
 */
fun getFeedbackItem(activity: Activity?): SettingsDataGo {
    return SettingsItem.getFeedbackURL()
}
