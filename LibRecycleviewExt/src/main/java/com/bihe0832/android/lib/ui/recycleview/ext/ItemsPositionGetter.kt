package com.bihe0832.android.lib.ui.recycleview.ext;

import android.support.v7.widget.RecyclerView
import android.view.View

interface ItemsPositionGetter {

    val childCount: Int

    val lastVisiblePosition: Int

    val firstVisiblePosition: Int

    fun getChildAt(position: Int): View?

    fun indexOfChild(view: View): Int

    fun findViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder?
}