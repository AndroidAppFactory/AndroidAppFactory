package com.bihe0832.android.framework.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.common.ColorTools
import com.bihe0832.android.lib.ui.image.clear
import com.bihe0832.android.lib.ui.image.loadImage
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import me.yokeyword.fragmentation.SupportActivity

open class BaseActivity : SupportActivity() {

    var mToolbar: Toolbar? = null
    var mTitleView: TextView? = null
    var mNavigationImageButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resetDensity()) {
            DisplayUtil.resetDensity(
                    this,
                    ConvertUtils.parseFloat(
                            resources.getString(R.string.custom_density),
                            Constants.CUSTOM_DENSITY
                    )
            )
        }
        if (getStatusBarColor() == Color.TRANSPARENT) {
            enableActivityImmersive(
                    ColorTools.getColorWithAlpha(
                            0f,
                            ContextCompat.getColor(this, R.color.primary)
                    ), getNavigationBarColor()
            )
        } else {
            enableActivityImmersive(getStatusBarColor(), getNavigationBarColor())
        }
    }


    open fun resetDensity(): Boolean {
        return true
    }

    open fun getStatusBarColor(): Int {
        return ContextCompat.getColor(this, R.color.colorPrimary)
    }

    open fun getNavigationBarColor(): Int {
        return ContextCompat.getColor(this, R.color.navigationBarColor)
    }

    override fun onPause() {
        super.onPause()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isAdded) {
                fragment.userVisibleHint = false
            }
        }
    }

    protected open fun exitAuto(): Boolean {
        return true
    }

    protected fun updateTitle(titleString: String?) {
        try {
            titleString?.let {
                mToolbar?.title = TextFactoryUtils.getSpannedTextByHtml(titleString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun updateIcon(iconURL: String?, needBack: Boolean) {
        updateIcon(iconURL, -1, needBack)
    }

    protected fun updateIcon(iconRes: Int, needBack: Boolean) {
        updateIcon("", iconRes, needBack)
    }

    protected fun updateIcon(iconURL: String?, iconRes: Int, needBack: Boolean) {
        updateIcon(iconURL, iconRes) {
            if (needBack) {
                onBackPressedSupport()
            }
        }
    }

    protected fun updateIcon(iconURL: String?, iconRes: Int, listener: View.OnClickListener?) {
        if (iconRes > 0) {
            mNavigationImageButton?.loadImage(iconRes)
        } else if (URLUtils.isHTTPUrl(iconURL)) {
            mNavigationImageButton?.loadImage(iconURL!!)
        } else if (null == iconURL) {
            mNavigationImageButton?.clear()
        }
        mToolbar?.setNavigationOnClickListener(listener)
    }

    protected fun initToolbar(resID: Int, needBack: Boolean) {
        initToolbar(resID, null, needBack)
    }

    protected fun initToolbar(resID: Int, titleString: String?, needBack: Boolean) {
        initToolbar(resID, titleString, true, needBack, 0)
    }

    protected fun initToolbar(
            resID: Int,
            titleString: String?,
            needTitleCenter: Boolean,
            needBack: Boolean,
            iconRes: Int
    ) {
        initToolbar(resID, titleString, needTitleCenter, {
            if (needBack) {
                onBackPressedSupport()
            }
        }, iconRes)
    }


    protected fun initToolbar(
            resID: Int,
            titleString: String?,
            needTitleCenter: Boolean,
            nevagationListener: View.OnClickListener,
            iconRes: Int
    ) {
        try {
            if (null == mToolbar) {
                mToolbar = findViewById(resID)
            }
            mToolbar?.apply {
                updateTitle(titleString)
            }?.let {
                for (i in 0 until it.childCount) {
                    val view: View = it.getChildAt(i)
                    //  找到标题的View
                    if (view is TextView) {
                        mTitleView = view
                        mTitleView!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                        if (needTitleCenter) {
                            mTitleView!!.setGravity(Gravity.CENTER)
                            mTitleView!!.getLayoutParams().width =
                                    ViewGroup.LayoutParams.MATCH_PARENT
                        }
                        continue
                    }
                    // 拿到导航按钮（也叫返回按钮）
                    if (view is ImageButton) {
                        mNavigationImageButton = view
                        DisplayUtil.dip2px(this, 4f).let { padding ->
                            mNavigationImageButton?.setPadding(
                                    padding * 2,
                                    padding,
                                    padding,
                                    padding
                            )
                        }

                        //  布局发生改变的时候拿到导航的宽度
                        //  因为标题设置居中的时候会偏向右边
                        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                            if (needTitleCenter) {
                                //  把标题的右边也进行一个padding 导航的宽度，让标题显得在整个屏幕中间
                                mTitleView?.setPadding(0, 0, v.getWidth(), 0)
                            }
                        }
                        continue
                    }

                    if (mTitleView != null && mNavigationImageButton != null) {
                        break
                    }
                }
                updateIcon("", iconRes, nevagationListener)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 页面返回时的特殊处理，例如检查是否是唯一页面，如果是需要跳转到main等，一般点标题栏返回键，建议调用此方法
     */
    override fun onBackPressedSupport() {
        onBackPressedSupportAction(exitAuto())
    }

    /**
     * onBackPressedSupport final以后，外部完成返回键的一些额外处理工作
     */
    open fun onBack() {
        finish()
    }

    override fun finish() {
        intent = null
        super.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ZLog.d("onActivityResult： $this, $requestCode, $resultCode, ${data?.data}")
        try {
            for (fragment in supportFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}