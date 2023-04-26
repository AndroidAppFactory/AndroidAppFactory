package com.bihe0832.android.common.settings.card;

import android.content.Context
import android.graphics.Color
import com.bihe0832.android.common.about.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule

/**
 * Created by lwtorlu on 2022/8/2.
 */
class PlaceholderData() : CardBaseModule() {

    var heightOfDP: Float = 0f
    var color: Int = Color.TRANSPARENT

    constructor(context: Context, heightOfDP: Float, color: Int) : this() {
        this.heightOfDP = heightOfDP
        this.color = context.resources.getColor(color)
    }

    override fun getResID(): Int {
        return R.layout.card_placehoder
    }

    override fun getViewHolderClass(): Class<out CardBaseHolder> {
        return PlaceholderHolder::class.java
    }
}