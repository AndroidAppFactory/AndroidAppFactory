package com.bihe0832.android.common.settings.card;

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Switch
import android.widget.TextView
import com.bihe0832.android.common.about.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.text.TextFactoryUtils

class SettingsHolderV2(view: View, context: Context) : CardBaseHolder(view, context) {

    private var settingTitle: TextView? = null
    private var settingDesc: TextView? = null
    private var clickEntrance: TextView? = null
    private var switchStatus: Switch? = null
    private var divider: View? = null

    override fun initView() {
        settingTitle = getView<TextView>(R.id.setting_title)
        settingDesc = getView<TextView>(R.id.setting_desc)
        clickEntrance = getView<TextView>(R.id.settings_tips)
        switchStatus = getView<Switch>(R.id.switch_status)
        divider = getView<View>(R.id.settings_driver)
    }

    override fun initData(item: CardBaseModule?) {
        (item as? SettingsDataV2)?.let { settingData ->
            settingData.onClickListener?.let {
                itemView.setOnClickListener(it)
            }

            settingTitle?.text = TextFactoryUtils.getSpannedTextByHtml(settingData.title)

            if (TextUtils.isEmpty(settingData.description)) {
                settingDesc?.visibility = View.GONE
            } else {
                settingDesc?.apply {
                    text = TextFactoryUtils.getSpannedTextByHtml(settingData.description)
                    visibility = View.VISIBLE
                }
            }

            clickEntrance?.apply {
                if (settingData.onClickListener == null) {
                    visibility = View.GONE
                } else {
                    if (!TextUtils.isEmpty(settingData.tips)) {
                        text = TextFactoryUtils.getSpannedTextByHtml(settingData.tips)
                    }
                    visibility = View.VISIBLE
                }
            }

            getView<View>(R.id.settings_go).visibility = if (settingData.onClickListener == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
            switchStatus?.apply {
                if (settingData.onCheckedChangeListener == null) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    isChecked = settingData.isChecked
                    setOnCheckedChangeListener(settingData.onCheckedChangeListener)
                }
            }

            getView<View>(R.id.settings_driver).visibility = if (settingData.mShowDriver) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}