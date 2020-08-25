package com.bihe0832.android.lib.ui.recycleview.ext;

import com.bihe0832.android.lib.log.ZLog


class ScrollDirectionDetector(private val onDetectScrollListener: OnDetectScrollListener) {

    private var oldTop: Int = 0
    private var oldFirstVisiblePosition: Int = 0

    private var oldScrollDirection: ScrollDirection? = null

    interface OnDetectScrollListener {
        fun onScrollDirectionChanged(scrollDirection: ScrollDirection)
    }

    enum class ScrollDirection {
        UP, DOWN
    }

    fun onDetectedListScroll(itemsPositionGetter: ItemsPositionGetter) {
        ZLog.d(TAG +
            ">> onDetectedListScroll, firstVisiblePosition ${itemsPositionGetter.firstVisiblePosition}, oldFirstVisiblePosition $oldFirstVisiblePosition"
        )
        val firstVisiblePosition = itemsPositionGetter.firstVisiblePosition
        val view = itemsPositionGetter.getChildAt(0)
        val top = view?.top ?: 0
        ZLog.d(TAG +"onDetectedListScroll, view $view, top $top, oldTop $oldTop")

        if (firstVisiblePosition == oldFirstVisiblePosition) {
            if (top >= oldTop) {
                onScrollUp()
            } else if (top < oldTop) {
                onScrollDown()
            }
        } else {
            if (firstVisiblePosition < oldFirstVisiblePosition) {
                onScrollUp()
            } else {
                onScrollDown()
            }
        }

        oldTop = top
        oldFirstVisiblePosition = firstVisiblePosition
        ZLog.d(TAG + "<< onDetectedListScroll")
    }

    private fun onScrollDown() {
        ZLog.d(TAG +"onScroll Down")

        if (oldScrollDirection != ScrollDirection.DOWN) {
            oldScrollDirection = ScrollDirection.DOWN
            onDetectScrollListener.onScrollDirectionChanged(ScrollDirection.DOWN)
        } else {
            ZLog.d(TAG +"onDetectedListScroll, scroll state not changed " + oldScrollDirection!!)
        }
    }

    private fun onScrollUp() {
        ZLog.d(TAG +"onScroll Up")

        if (oldScrollDirection != ScrollDirection.UP) {
            oldScrollDirection = ScrollDirection.UP
            onDetectScrollListener.onScrollDirectionChanged(ScrollDirection.UP)
        } else {
            ZLog.d(TAG + "onDetectedListScroll, scroll state not changed " + oldScrollDirection!!)
        }
    }

    companion object {

        private val TAG = ScrollDirectionDetector::class.java.simpleName
    }
}
