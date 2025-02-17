package com.bihe0832.android.common.language.card

import com.bihe0832.android.common.language.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule
import java.util.Locale

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
class SettingsDataLanguage : CardBaseModule() {
    var title: String = ""
    var locale:Locale? = null

    override fun getResID(): Int {
        return R.layout.card_settings_language
    }

    override fun getViewHolderClass(): Class<out CardBaseHolder> {
        return SettingsHolderLanguage::class.java
    }
}