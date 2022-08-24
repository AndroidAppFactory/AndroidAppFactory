package com.bihe0832.android.lib.ui.recycleview.ext;


import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bihe0832.android.lib.log.ZLog
import java.lang.Exception


class RecyclerViewItemPositionGetter(
    private val recyclerView: RecyclerView,
    private val layoutManager: SafeLinearLayoutManager

) : ItemsPositionGetter {
    private val TAG = RecyclerViewItemPositionGetter::class.java.simpleName


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
