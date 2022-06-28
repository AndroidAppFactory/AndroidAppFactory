package com.bihe0832.android.lib.ui.touchregion;

import android.view.View
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 *
 */
fun View.expandTouchRegionWithpx(px: Int) {
    TouchRegion(this).expandViewTouchRegion(this, px)
}

fun View.expandTouchRegionWithdp(dp: Float) {
    TouchRegion(this).expandViewTouchRegion(this, DisplayUtil.dip2px(context, dp))
}

fun View.expandTouchRegionWithdp(left: Float, top: Float, right: Float, bottom: Float) {
    TouchRegion(this).expandViewTouchRegion(
            this,
            DisplayUtil.dip2px(context, left),
            DisplayUtil.dip2px(context, top),
            DisplayUtil.dip2px(context, right),
            DisplayUtil.dip2px(context, bottom))
}

fun View.expandTouchRegionWithpx(left: Int, top: Int, right: Int, bottom: Int) {
    TouchRegion(this).expandViewTouchRegion(this, left, top, right, bottom)
}