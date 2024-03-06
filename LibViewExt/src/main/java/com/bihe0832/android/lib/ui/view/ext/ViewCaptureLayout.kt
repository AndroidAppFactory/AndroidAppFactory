package com.bihe0832.android.lib.ui.view.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bihe0832.android.lib.thread.ThreadManager

/**
 * Summary
 *
 * @author code@bihe0832.com
 * Created on 2024/3/6.
 * Description:
 */
class ViewCaptureLayout : RelativeLayout {

    private var viewHeight = 0
    private var viewWidth = 0
    private var measureHeightMode = View.MeasureSpec.UNSPECIFIED
    private var measureWidthMode = View.MeasureSpec.UNSPECIFIED

    private var viewHolder = RelativeLayout(context)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setViewSize(width: Int, height: Int) {
        when (height) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                measureHeightMode = View.MeasureSpec.AT_MOST
                viewHeight = 0
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                measureHeightMode = View.MeasureSpec.UNSPECIFIED
                viewHeight = 0
            }

            else -> {
                measureHeightMode = View.MeasureSpec.EXACTLY
                viewHeight = height
            }
        }

        when (width) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                measureWidthMode = View.MeasureSpec.AT_MOST
                viewWidth = 0
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                measureWidthMode = View.MeasureSpec.UNSPECIFIED
                viewWidth = 0
            }

            else -> {
                measureWidthMode = View.MeasureSpec.EXACTLY
                viewWidth = width
            }
        }
    }


    override fun removeAllViews() {
        super.removeAllViews()
        viewHolder.removeAllViews()
    }

    /**
     * 这里先不去真正的addView，而是添加到以一个ViewHolder中，等待所有的add完成后 prepare 时统一add
     */
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        viewHolder.addView(child, index, params)
    }

    /**
     * 这里没有使用addView， 使用了轻量的attachViewToParent方法，目前看来带来的副作用也是有的，
     * 某些复杂的布局可能会不生效，这里推荐在再裹一层ViewGroup的方式，直接放复杂布局包裹在内部就好。
     * 这里之所以不用addView是因为这个ShotLayout并不需要添加到ViewTree，所以需要骗过系统，这里是
     * 用了dispatchVisibilityAggregated这个方法来通知系统 我的View和Drawable都是可见的。
     */
    private fun attachViewHolder() {
        super.removeAllViews()
        var params = viewHolder.layoutParams
        if (null == params) {
            params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        attachViewToParent(viewHolder, 0, params)
        requestLayout()
        invalidate()
    }

    private fun prepareAndGenBitmap(): Bitmap {
        attachViewHolder()
        measure(
            MeasureSpec.makeMeasureSpec(viewWidth, measureWidthMode),
            MeasureSpec.makeMeasureSpec(viewHeight, measureHeightMode)
        )
        layout(0, 0, measuredWidth, measuredHeight)
        super.removeAllViews()
        attachViewHolder()
        onDetachedFromWindow()
        onAttachedToWindow()

        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun getViewBitmap(onShotCallback: ((Bitmap) -> Unit)) {
        ThreadManager.getInstance().runOnUIThread {
            try {
                val bitmap = prepareAndGenBitmap()
                onShotCallback.invoke(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun isAttachedToWindow(): Boolean {
        return true
    }

    override fun getWindowVisibility(): Int {
        return View.VISIBLE
    }

    override fun isShown(): Boolean {
        return true
    }
}