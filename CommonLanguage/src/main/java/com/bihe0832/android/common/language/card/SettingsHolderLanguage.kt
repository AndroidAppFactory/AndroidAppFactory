package com.bihe0832.android.common.language.card

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.common.language.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.language.MultiLanguageHelper

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
class SettingsHolderLanguage(view: View, context: Context) : CardBaseHolder(view, context) {
    private var mStatus: ImageView? = null
    private var settingTitle: TextView? = null
    override fun initView() {
        mStatus = getView(R.id.selected_status)
        settingTitle = getView<TextView>(R.id.title_text)
    }

    override fun initData(item: CardBaseModule?) {
        (item as? SettingsDataLanguage)?.let { settingData ->
            settingTitle?.setText(settingData.title)
            val configLocale = MultiLanguageHelper.getLanguageConfig(context)
            val isCurrent = if (settingData.useLanguage) {
                configLocale.language.equals(settingData.locale?.language)
            } else {
                configLocale.equals(settingData.locale)
            }
            if (isCurrent) {
                mStatus?.visibility = View.VISIBLE
            } else {
                mStatus?.visibility = View.GONE
            }
        }
    }
}