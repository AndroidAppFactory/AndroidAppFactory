package com.bihe0832.android.common.message.list.card

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.common.message.R
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.custom.view.ViewWithBackground
import com.bihe0832.android.lib.utils.time.DateUtil
import java.text.SimpleDateFormat

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
class MessageItemHolder(itemView: View?, context: Context?) : CardBaseHolder(itemView, context) {
    override fun initView() {}
    override fun initData(item: CardBaseModule) {
        val data = item as MessageItemData
        addOnClickListener(R.id.message_delete)
        addOnClickListener(R.id.message_content)
        data.mMessageInfoItem?.let { messageItem ->
            getView<ViewWithBackground>(R.id.message_is_new).visibility = if (!messageItem.hasRead()) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            getView<TextView>(R.id.message_title).apply {
                text = TextFactoryUtils.getSpannedTextByHtml(messageItem.title)
            }

            getView<TextView>(R.id.message_time).apply {
                SimpleDateFormat("yyyyMMddHHmm").parse(messageItem.createDate).time.let {
                    text = TextFactoryUtils.getSpannedTextByHtml(DateUtil.getDateEN(it, "yyyy-MM-dd HH:mm"))
                }
            }
        }

    }
}