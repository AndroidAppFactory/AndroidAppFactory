package com.bihe0832.android.lib.ui.recycleview.ext;

import android.support.v7.widget.RecyclerView
import android.view.View
import com.bihe0832.android.lib.log.ZLog

data class ItemData(
    val positionInAdapter: Int,
    val viewHolder: RecyclerView.ViewHolder?,
    val view: View?
) {
    private val TAG = "ItemData"

    fun isAvailable(): Boolean {
        val isAvailable = viewHolder != null && view != null
        ZLog.d(TAG +  "isAvailable $isAvailable")
        return isAvailable
    }
}