package com.bihe0832.android.common.message.list.card

import com.bihe0832.android.common.message.R
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
class MessageItemData() : CardBaseModule() {
    var mMessageInfoItem: MessageInfoItem? = null
    var showDirvier = true
    override fun getResID(): Int {
        return R.layout.message_item_layout
    }

    override fun getViewHolderClass(): Class<out CardBaseHolder?> {
        return MessageItemHolder::class.java
    }

    constructor(item: MessageInfoItem?) : this() {
        mMessageInfoItem = item
    }

}