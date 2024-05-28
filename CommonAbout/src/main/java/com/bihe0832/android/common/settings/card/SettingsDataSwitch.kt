package com.bihe0832.android.common.settings.card;

import android.view.View
import android.widget.CompoundButton
import com.bihe0832.android.common.about.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule

open class SettingsDataSwitch : CardBaseModule() {
    var title: String = ""
    var mItemIconURL = ""
    var mItemIconRes = -1
    var mItemIconResColorFilter: Int? = null
    var mAutoGenerateColorFilter = true

    var description: String = ""
    var tips: String = ""

    var onClickListener: View.OnClickListener? = null
    var onCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    var isChecked = true
    var mShowDriver = true

    override fun getResID(): Int {
        return R.layout.card_setting_switch
    }

    override fun getViewHolderClass(): Class<out CardBaseHolder> {
        return SettingsHolderSwitch::class.java
    }
}