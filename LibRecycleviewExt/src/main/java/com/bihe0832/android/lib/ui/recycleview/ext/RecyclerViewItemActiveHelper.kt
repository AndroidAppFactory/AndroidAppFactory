package com.bihe0832.android.lib.ui.recycleview.ext;

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import com.bihe0832.android.lib.log.ZLog

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/9/17.
 * Description: Description
 *
 */
class RecyclerViewItemActiveHelper(
        private val recyclerView: RecyclerView,
        private val activeCallback: ActiveCallback
) : RecyclerView.OnScrollListener(), ScrollDirectionDetector.OnDetectScrollListener {
    private val TAG = RecyclerViewItemActiveHelper::class.java.simpleName

    private val INACTIVE_LIST_ITEM_VISIBILITY_PERCENTS = 70

    private var scrollDirection: ScrollDirectionDetector.ScrollDirection =
            ScrollDirectionDetector.ScrollDirection.UP

    private val scrollDirectionDetector by lazy { ScrollDirectionDetector(this) }

    private val dimenRect by lazy { Rect() }

    private val itemsPositionGetter by lazy {
        if(recyclerView.layoutManager is SafeLinearLayoutManager){
            RecyclerViewItemPositionGetterForLinearLayoutManager(
                    recyclerView,
                    recyclerView.layoutManager as SafeLinearLayoutManager
            )
        }else if(recyclerView.layoutManager is SafeGridLayoutManager){
            RecyclerViewItemPositionGetterForGridLayoutManager(
                    recyclerView,
                    recyclerView.layoutManager as SafeGridLayoutManager
            )
        }else{
            null
        }

    }

    private var currentItem = ItemData(0, null, null)
        set(value) {
            ZLog.d(TAG + "setCurrentItem, newCurrentItem $value")
            field = value.copy()
        }

    fun onScrollStateIdle(forceNotify: Boolean = false) {
        itemsPositionGetter?.let {
            calculateFirstVisibleItem(it,forceNotify)
        }
    }

    private fun calculateFirstVisibleItem(itemsPositionGetter: ItemsPositionGetter, forceNotify: Boolean = false) {
        try {
            val firstVisibleItem = getFirstVisibleItem(itemsPositionGetter)
            ZLog.d(TAG + "FindFirstVisibleItem:$firstVisibleItem")
            if (firstVisibleItem?.isAvailable() == true) {
                if (currentItem != firstVisibleItem || forceNotify) {
                    ZLog.d(TAG + "Item changed")
                    try {
                        activeCallback.onDeactive(recyclerView, currentItem.positionInAdapter)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        activeCallback.onActive(recyclerView, firstVisibleItem.positionInAdapter)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    currentItem = firstVisibleItem
                } else {
                    ZLog.d(TAG + "Item not changed")
                }
            }
        } catch (e: Exception) {
            ZLog.d(TAG + "Error in calculateFirstVisibleItem:$e")
        }
    }

    private fun getFirstVisibleItem(itemsPositionGetter: ItemsPositionGetter): ItemData? {
        val firstVisiblePosition = itemsPositionGetter.firstVisiblePosition
        val lastVisiblePosition = itemsPositionGetter.lastVisiblePosition
        ZLog.d(TAG +
                "getFirstVisibleItem, firstVisiblePosition $firstVisiblePosition, lastVisiblePosition $lastVisiblePosition"
        )
        return if (firstVisiblePosition == lastVisiblePosition) {
            val vh = itemsPositionGetter.findViewHolderForAdapterPosition(firstVisiblePosition)
            ItemData(firstVisiblePosition, vh, vh?.itemView)
        } else {
            (firstVisiblePosition..lastVisiblePosition).mapNotNull {
                val vh = itemsPositionGetter.findViewHolderForAdapterPosition(it)
                ItemData(it, vh, vh?.itemView)
            }.maxBy {
                it.view?.getLocalVisibleRect(dimenRect)
                dimenRect.bottom - dimenRect.top // 取最大面积
            }
        }
    }

    fun onScrolled(itemsPositionGetter: RecyclerViewItemPositionGetterForLinearLayoutManager?, scrollState: Int) {
//        itemsPositionGetter?.let {
//            ZLog.d(TAG + ">> onScroll")
//            ZLog.d(TAG +
//                "onScroll, firstVisibleItem " + itemsPositionGetter.firstVisiblePosition + ", visibleItemCount " + (itemsPositionGetter.lastVisiblePosition - itemsPositionGetter.firstVisiblePosition
//                        + 1) + ", scrollState " + scrollState
//            )
//            scrollDirectionDetector.onDetectedListScroll(itemsPositionGetter)
//            when (scrollState) {
//                RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
//                    ZLog.d(TAG +
//                        ">> onScroll, scrollDirection $scrollDirection"
//                    )
//
//                    val listItemData = currentItem
//                    ZLog.d(TAG + "onScroll, listItemData $listItemData")
//
//                    if (listItemData.isAvailable()) {
//                        calculateActiveItem(itemsPositionGetter, listItemData)
//                    }
//
//                    ZLog.d(TAG +
//                        "<< onScroll, scrollDirection $scrollDirection"
//                    )
//                }
//                else -> ZLog.d(TAG +"onScroll, SCROLL_STATE_IDLE. ignoring")
//            }
//        }
    }


    private fun calculateActiveItem(
            itemsPositionGetter: RecyclerViewItemPositionGetterForLinearLayoutManager,
            listItemData: ItemData
    ) {

        /** 2.  */
        val neighbourItemData = when (scrollDirection) {
            ScrollDirectionDetector.ScrollDirection.UP -> findPreviousItem(
                    itemsPositionGetter,
                    listItemData
            )
            ScrollDirectionDetector.ScrollDirection.DOWN -> findNextItem(
                    itemsPositionGetter,
                    listItemData
            )
        }

//        /** 3.  */
//        if (neighbourItemData.isAvailable) {
//
//            // neighbour item become active (current)
//            /** 4.  */
//            currentItem = neighbourItemData
//            activeCallback.onActive(recyclerView, currentItem.index)
//        }
    }

    private fun findPreviousItem(
            itemsPositionGetter: ItemsPositionGetter,
            currentIem: ItemData
    ): ItemData? {
        val previousItemIndex = currentIem.positionInAdapter - 1
        ZLog.d(TAG + "findPreviousItem, previousItemIndex $previousItemIndex")

        if (previousItemIndex >= 0) {
            val previousViewHolder =
                    itemsPositionGetter.findViewHolderForAdapterPosition(previousItemIndex)
            if (previousViewHolder != null) {
                val previousItem =
                        ItemData(previousItemIndex, previousViewHolder, previousViewHolder.itemView)
                ZLog.d(TAG + "findPreviousItem: $previousItem")
                return previousItem
            } else {
                ZLog.d(TAG + "findPreviousItem, current view is no longer attached to listView")
            }
        }
        return null
    }

    private fun findNextItem(
            itemsPositionGetter: ItemsPositionGetter,
            currentIem: ItemData
    ): ItemData? {
        val nextItemIndex = currentIem.positionInAdapter - 1
        ZLog.d(TAG + "findNextItem, nextItemIndex $nextItemIndex")

        if (nextItemIndex >= 0) {
            val nextViewHolder = itemsPositionGetter.findViewHolderForAdapterPosition(nextItemIndex)
            if (nextViewHolder != null) {
                val nextItem = ItemData(nextItemIndex, nextViewHolder, nextViewHolder.itemView)
                ZLog.d(TAG + "findNextItem: $nextItem")
                return nextItem
            } else {
                ZLog.d(TAG + "findNextItem, current view is no longer attached to listView")
            }
        }
        return null
    }

    override fun onScrollDirectionChanged(scrollDirection: ScrollDirectionDetector.ScrollDirection) {
        ZLog.d(TAG + "onScrollDirectionChanged, scrollDirection $scrollDirection")
        this.scrollDirection = scrollDirection
    }

    private var scrollState: Int = 0

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        scrollState = newState
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onScrollStateIdle()
        }

    }

//    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//        onScrolled(itemsPositionGetter, scrollState)
//    }



    abstract class ActiveCallback {
        open fun onActive(recyclerView: RecyclerView, position: Int) {}
        open fun onDeactive(recyclerView: RecyclerView, position: Int) {}
    }
}
