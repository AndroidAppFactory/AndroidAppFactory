package com.bihe0832.android.common.settings.card;

import android.content.Context
import android.view.View
import com.bihe0832.android.common.about.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 * Created by lwtorlu on 2022/8/2.
 */
class PlaceholderHolder(view: View, context: Context) : CardBaseHolder(view, context) {

    private var blockView: View? = null

    override fun initView() {
        blockView = getView(R.id.card_placehoder)
    }

    override fun initData(module: CardBaseModule) {
        if (module !is PlaceholderData) {
            return
        }
        blockView?.apply {
            layoutParams?.also {
                it.height = DisplayUtil.dip2px(context, DisplayUtil.dip2px(context, module.heightOfDP).toFloat())
            }?.let {
                layoutParams = it
            }
            setBackgroundColor(module.color)
        }

    }
}