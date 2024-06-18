package com.bihe0832.android.lib.ui.dialog.impl

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.R


open class BottomDialog : CommonDialog {
    constructor(context: Context?) : super(context, R.style.BaseDialog)

    constructor(context: Context?, themeResId: Int) : super(context, themeResId)

    override fun initView() {
        super.initView()
        showAnimation()
        getWindow()?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun initEvent() {
        super.initEvent()
        getRootView()?.apply {
            setOnClickListener {
                if (shouldCanceled) {
                    dismiss()
                    getOnClickBottomListener()?.onCancel()
                }
            }
        }
    }
    open fun getRootView(): View? {
        return getContentView().parent as View
    }

    open fun getContentView(): View {
        return findViewById(R.id.dialog_content_layout)
    }

    override fun getLayoutID(): Int {
        return R.layout.com_bihe0832_common_bottom_dialog_layout
    }

    private fun showAnimation() {
        getContentView().viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    getContentView().viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val showAnimator = ObjectAnimator.ofFloat(
                        getContentView(),
                        "translationY",
                        getContentView().height.toFloat(),
                        0f
                    )
                    val alphaAnimator = ObjectAnimator.ofFloat(
                        getContentView(),
                        "alpha",
                        0f,
                        0.5f,
                        1f
                    )
                    showAnimator.duration = 300
                    alphaAnimator.duration = 300
                    val animatorSet = AnimatorSet()
                    animatorSet.play(showAnimator).with(alphaAnimator)
                    animatorSet.start()
                }
            })
    }

    private fun hideAnimation() {
        val showAnimator = ObjectAnimator.ofFloat(
            getContentView(),
            "translationY",
            0f,
            getContentView().height.toFloat()
        )
        val alphaAnimator = ObjectAnimator.ofFloat(
            getContentView(),
            "alpha",
            1f,
            0.5f,
            0f
        )
        showAnimator.duration = 300
        alphaAnimator.duration = 300
        val animatorSet = AnimatorSet()
        animatorSet.play(showAnimator).with(alphaAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                realDismiss()
            }
        })
        animatorSet.start()
    }

    override fun dismiss() {
        hideAnimation()
    }

    private fun realDismiss() {
        super.dismiss()
    }

}