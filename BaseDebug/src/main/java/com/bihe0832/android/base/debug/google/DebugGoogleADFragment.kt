/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.google


import android.view.View
import android.widget.LinearLayout
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.services.google.ad.AAFGoogleAD
import com.google.android.gms.ads.FullScreenContentCallback


class DebugGoogleADFragment : DebugEnvFragment() {

    private var mADView: LinearLayout? = null

    override fun getLayoutID(): Int {
        return R.layout.fragment_debug_google_ad
    }

    override fun initView(view: View) {
        super.initView(view)
        mADView = view.findViewById(R.id.ad_banner)
    }

    override fun initData() {
        super.initData()
        preloadAD()
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("预加载广告", View.OnClickListener {
                preloadAD()
            }))

            add(DebugItemData("展示插屏广告", View.OnClickListener {
                showInterstitialAd()

            }))
            add(DebugItemData("激励视频广告", View.OnClickListener {

            }))
            add(DebugItemData("Banner广告", View.OnClickListener {

            }))

        }
    }

    fun preloadAD() {
        AAFGoogleAD.loadInterstitialAd(context!!, ThemeResourcesManager.getString(R.string.admob_interstitial_unitid)!!)
    }

    fun showInterstitialAd() {
        AAFGoogleAD.showInterstitialAd(activity!!, ThemeResourcesManager.getString(R.string.admob_interstitial_unitid)!!, object : FullScreenContentCallback() {
            override fun onAdImpression() {
                super.onAdImpression()
                ZixieContext.showToast("插屏广告展示成功")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                ZixieContext.showToast("插屏广告被点击")

            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                ZixieContext.showToast("插屏广告被关闭")
            }
        })
    }
}