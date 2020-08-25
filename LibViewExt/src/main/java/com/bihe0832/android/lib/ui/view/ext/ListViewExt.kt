package com.bihe0832.android.lib.ui.view.ext;

import android.widget.ListView

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 *
 */

/**
 * 根据listview的item个数得到其全部显示时的高度
 */
fun ListView.getListViewHeightBaseOnItems(): Int {
    val listAdapter = this.adapter
    var totalHeight = 0
    if (listAdapter == null) {
        return totalHeight
    }
    for (i in 0 until listAdapter.count) {
        val listItem = listAdapter.getView(i, null, this)
        listItem.measure(0, 0)
        totalHeight += listItem.measuredHeight
    }
    totalHeight += this.dividerHeight * (listAdapter.count - 1)
    return totalHeight
}