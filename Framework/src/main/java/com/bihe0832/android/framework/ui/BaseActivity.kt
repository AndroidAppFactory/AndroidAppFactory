package com.bihe0832.android.framework.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.color.utils.ColorUtils
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.image.clearImage
import com.bihe0832.android.lib.media.image.loadImage
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import me.yokeyword.fragmentation.SupportActivity

open class BaseActivity : SupportActivity() {

    var mToolbar: Toolbar? = null
    var mTitleView: TextView? = null
    private var mNavigationImageButton: ImageButton? = null
    private val mNeedEnableLayerToGray by lazy {
        (LifecycleHelper.getCurrentTime() / 1000).let {
            return@lazy it in Config.readConfig(
                Constants.CONFIG_KEY_LAYER_START_VALUE,
                0L,
            )..Config.readConfig(Constants.CONFIG_KEY_LAYER_END_VALUE, 0L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resetDensity()) {
            DisplayUtil.resetDensity(
                this,
                ConvertUtils.parseFloat(
                    ThemeResourcesManager.getString(R.string.custom_density),
                    Constants.CUSTOM_DENSITY,
                ),
            )
        }
    }

    open fun resetDensity(): Boolean {
        return true
    }

    open fun getStatusBarColor(): Int {
        return ThemeResourcesManager.getColor(R.color.colorPrimaryDark)!!
    }

    open fun getNavigationBarColor(): Int {
        return ThemeResourcesManager.getColor(R.color.navigationBarColor)!!
    }

    override fun onResume() {
        super.onResume()
        if (getStatusBarColor() == Color.TRANSPARENT) {
            enableActivityImmersive(
                ColorUtils.addAlpha(0f, ThemeResourcesManager.getColor(R.color.colorPrimaryDark)!!),
                getNavigationBarColor(),
            )
        } else {
            enableActivityImmersive(getStatusBarColor(), getNavigationBarColor())
        }

        mToolbar?.setBackgroundColor(ThemeResourcesManager.getColor(R.color.colorPrimary)!!)

        if (mNeedEnableLayerToGray) {
            setLayerToGray()
        }
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

    protected open fun updateTitle(titleString: String?) {
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

    protected open fun updateIcon(image: ImageView?, iconURL: String?, iconRes: Int) {
        if (iconRes > 0) {
            image?.loadImage(iconRes)
        } else if (URLUtils.isHTTPUrl(iconURL)) {
            image?.loadImage(iconURL!!)
        } else if (null == iconURL) {
            image?.clearImage()
        }
    }

    protected open fun updateIcon(iconURL: String?, iconRes: Int, listener: View.OnClickListener?) {
        updateIcon(mNavigationImageButton, iconURL, iconRes)
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
        iconRes: Int,
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
        iconRes: Int,
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
                            mTitleView!!.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT
                        } else {
                            mTitleView!!.setGravity(Gravity.CENTER_VERTICAL or Gravity.LEFT)
                            mTitleView!!.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                        continue
                    }
                    // 拿到导航按钮（也叫返回按钮）
                    if (view is ImageButton) {
                        mNavigationImageButton = view
                        DisplayUtil.dip2px(this, 4f).let { padding ->
                            mNavigationImageButton?.setPadding(padding * 2, padding, padding, padding)
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

    open fun dispatchOnActivityResult(): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (dispatchOnActivityResult()) {
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
}
