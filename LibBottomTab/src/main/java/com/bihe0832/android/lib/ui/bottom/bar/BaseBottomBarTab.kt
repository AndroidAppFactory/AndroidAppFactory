package com.bihe0832.android.lib.ui.bottom.bar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.bihe0832.android.lib.ui.custom.view.background.changeStatusWithUnreadMsg
import com.bihe0832.android.lib.ui.custom.view.background.setUnreadMsg
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 * height:56
 */
abstract class BaseBottomBarTab(context: Context, attrs: AttributeSet?, defStyleAttr: Int, icon: Int, title: CharSequence?) : FrameLayout(context, attrs, defStyleAttr) {

    protected abstract fun getIconView(): ImageView?

    protected abstract fun getTitleView(): TextView?

    protected abstract fun getTipsView(): com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground?

    protected abstract fun getLayoutID(): Int

    protected abstract fun initView(context: Context)

    private var mTipsNum = -1
    private var mTabPosition = -1

    constructor(context: Context, @DrawableRes icon: Int, title: CharSequence?) : this(context, null, 0, icon, title)

    init {
        inflate(context, getLayoutID(), this)
        initView(context)
        initViewEvent(icon, title)
    }

    protected open fun initViewEvent(icon: Int, title: CharSequence?) {
        getIconView()?.setImageResource(icon)
        getTitleView()?.text = title
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
    }

    open fun setTabPosition(position: Int) {
        mTabPosition = position
    }

    open fun getTabPosition(): Int {
        return mTabPosition
    }

    open fun setUnreadMsgNum(num: Int) {
        mTipsNum = num
        getTipsView()?.setUnreadMsg(num, context.resources.getDimension(ResR.dimen.com_bihe0832_tab_red_dot_size).toInt())
    }

    open fun getUnreadMsgNum(): Int {
        return mTipsNum
    }

    fun getTabView(): View {
        return this
    }

    open fun showUnreadMsg(num: Int) {
        mTipsNum = num
        showUnreadMsg()
    }

    open fun showUnreadMsg() {
        getTipsView()?.changeStatusWithUnreadMsg(getUnreadMsgNum(), context.resources.getDimension(ResR.dimen.com_bihe0832_tab_red_dot_size).toInt())
    }

    open fun hideUnreadMsg() {
        getTipsView()?.visibility = View.GONE
    }
}