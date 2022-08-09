package com.bihe0832.android.lib.ui.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver


open class BottomDialog : CommonDialog {
    constructor(context: Context?) : super(context, R.style.BottomInAndOutStyle)

    constructor(context: Context?, themeResId: Int) : super(context, themeResId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow()!!.getDecorView().setSystemUiVisibility(View.INVISIBLE)
        showAnimation()
        getRootView()?.setOnClickListener {
            if (shouldCanceled) {
                dismiss()
                getOnClickBottomListener().onCancel()
            }
        }
    }

    open fun getRootView(): View? {
        return getContentView().parent as View
    }

    open fun getContentView(): View {
        return findViewById(R.id.dialog_layout)
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
            override fun onAnimationEnd(animation: Animator?) {
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