package com.bihe0832.android.base.debug.svga

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.svga.*
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.opensource.svgaplayer.*
import java.net.MalformedURLException

class DebugSvgaFragment : DebugEnvFragment() {

    private var mSVGAImageView: SVGAImageView? = null
    private var currentStep = 0

    override fun hasHeaderView(): Boolean {
        return true
    }

    override fun initView(view: View) {
        super.initView(view)
        mSVGAImageView = SVGAImageView(view.context).apply {
            setBackgroundColor(Color.BLACK)
            setOnClickListener {
                stepToFrame(currentStep++, false)
            }

            setOnAnimKeyClickListener(object : SVGAClickAreaListener {
                override fun onClick(clickKey: String) {
                    ZixieContext.showDebug(clickKey)
                }
            })
            loops = 1
            fillMode = SVGAImageView.FillMode.Backward
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(context, 300f))
            callback = object : SVGACallback {
                override fun onFinished() {
                    stepToFrame(currentStep, false)
                }

                override fun onPause() {

                }

                override fun onRepeat() {

                }

                override fun onStep(frame: Int, percentage: Double) {
                    if (frame > 0) {
                        currentStep == frame
                    }
                }
            }

        }
        getListHeader().addView(mSVGAImageView)

    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("加载SDCard SVGA", View.OnClickListener { loadAssetsAnimation() }))
            add(getDebugItem("加载Assets SVGA", View.OnClickListener { loadAssetsAnimation() }))
            add(getDebugItem("加载网络 SVGA", View.OnClickListener { loadHttpAnimation() }))
            add(getDebugItem("返回 SVGA 的指定内容的点击", View.OnClickListener { loadDynamicClickAnimation() }))
            add(getDebugItem("替换 SVGA 的指定内容", View.OnClickListener { loadDynamicAnimation() }))
        }
    }


    // 加载Assets SVGA
    private fun loadAssetsAnimation() {
        SVGAHelper.shareParser().setFrameSize(100, 100)
        mSVGAImageView?.playAssets("mp3_to_long.svga")
    }

    // 加载网络svga
    private fun loadHttpAnimation() {
        mSVGAImageView?.playURL("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true")
    }

    //返回MerryChristmas.svga的指定内容的点击
    private fun loadDynamicClickAnimation() {
        mSVGAImageView?.playAssetsWithClick("MerryChristmas.svga", mutableListOf<String>().apply {
            add("img_10")
        })
    }


    //替换kingset.svga的指定内容
    private fun loadDynamicAnimation() {
        try { // new URL needs try catch.
            SVGAHelper.playURL("https://github.com/yyued/SVGA-Samples/blob/master/kingset.svga?raw=true","", object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    val dynamicEntity = SVGADynamicEntity()
                    dynamicEntity.setDynamicImage("https://github.com/PonyCui/resources/blob/master/svga_replace_avatar.png?raw=true", "99") // Here is the KEY implementation.
                    val drawable = SVGADrawable(videoItem, dynamicEntity)
                    mSVGAImageView?.setImageDrawable(drawable)
                    mSVGAImageView?.startAnimation()
                }

                override fun onError() {}
            })
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }
}