package com.bihe0832.android.lib.ui.recycleview.ext;

import android.support.v7.widget.RecyclerView
import android.view.View
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.ui.recycleview.ext.ItemsPositionGetter
import com.bihe0832.android.lib.ui.recycleview.ext.SafeLinearLayoutManager
import java.lang.Exception

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/9/17.
 * Description: Description
 *
 */

class RecyclerViewItemPositionGetterForGridLayoutManager(
        private val recyclerView: RecyclerView,
        private val layoutManager: SafeGridLayoutManager

) : ItemsPositionGetter {
    private val TAG = RecyclerViewItemPositionGetterForGridLayoutManager::class.java.simpleName


    override val childCount: Int
        get() {
            val childCount = recyclerView.childCount

            ZLog.d(TAG + "getChildCount, recyclerView $childCount")
            ZLog.d(TAG +"getChildCount, layoutManager ${layoutManager.childCount}")

            return childCount
        }

    override val lastVisiblePosition: Int
        get() = layoutManager.findLastVisibleItemPosition()

    override val firstVisiblePosition: Int
        get() = layoutManager.findFirstVisibleItemPosition()

    override fun getChildAt(position: Int): View? {
        ZLog.d(TAG + "getChildAt, recyclerView.getChildCount ${recyclerView.childCount}")
        ZLog.d(TAG + "getChildAt, layoutManager.getChildCount ${layoutManager.childCount}")

        return try {
            val view = recyclerView.getChildAt(position)
            ZLog.d(TAG + "recyclerView getChildAt, position $position, view $view")
            ZLog.d(TAG +
                    "layoutManager getChildAt, position $position, view ${layoutManager.getChildAt(position)}"
            )
            view
        }catch (e : Exception){
            e.printStackTrace()
            null
        }
    }

    override fun indexOfChild(view: View): Int = recyclerView.indexOfChild(view)

    override fun findViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder? {
        return try {
            recyclerView.findViewHolderForAdapterPosition(position)
        }catch (e : Exception){
            e.printStackTrace()
            null
        }
    }
}
