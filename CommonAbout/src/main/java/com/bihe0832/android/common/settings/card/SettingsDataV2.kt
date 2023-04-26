package com.bihe0832.android.common.settings.card;

import android.view.View
import android.widget.CompoundButton
import com.bihe0832.android.common.about.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule

class SettingsDataV2 : CardBaseModule() {
    var title: String = ""
    var description: String = ""
    var tips: String = ""

    var onClickListener: View.OnClickListener? = null
    var onCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    var isChecked = true
    var mShowDriver = true

    override fun getResID(): Int {
        return R.layout.card_privacy_setting
    }

    override fun getViewHolderClass(): Class<out CardBaseHolder> {
        return SettingsHolderV2::class.java
    }
}