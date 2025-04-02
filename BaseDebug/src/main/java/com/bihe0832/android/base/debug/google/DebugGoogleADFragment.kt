/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.google


import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.services.google.ad.AAFGoogleAD
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem


class DebugGoogleADFragment : DebugEnvFragment() {

    private var mADContainerView: LinearLayout? = null

    override fun getLayoutID(): Int {
        return R.layout.fragment_debug_google_ad
    }

    private var initialLayoutComplete = false
    override fun initView(view: View) {
        super.initView(view)
        mADContainerView = view.findViewById(R.id.ad_banner)
        val adView = AdView(view.context)
        mADContainerView?.addView(adView)
        mADContainerView?.getViewTreeObserver()?.addOnGlobalLayoutListener(OnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadBanner(adView, AAFGoogleAD.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context!!, mADContainerView!!.width.toFloat()))
            }
        })
    }

    override fun initData() {
        super.initData()
        preloadAD()
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("预加载广告", View.OnClickListener {
                preloadAD()
            }))

            add(getDebugItem("展示插屏广告", View.OnClickListener {
                showInterstitialAd()

            }))
            add(getDebugItem("激励视频广告", View.OnClickListener {
                showRewardedAd()
            }))

            add(getDebugItem("插屏激励视频广告", View.OnClickListener {
                showRewardedInterstitialAd()
            }))
        }
    }

    private fun preloadAD() {
        AAFGoogleAD.loadInterstitialAd(context!!, ThemeResourcesManager.getString(R.string.admob_interstitial_unitid)!!)
        AAFGoogleAD.loadRewardedAd(context!!, ThemeResourcesManager.getString(R.string.admob_rewardedvideo_unitid)!!)
        AAFGoogleAD.loadRewardedInterstitialAd(context!!, ThemeResourcesManager.getString(R.string.admob_rewardedvideo_interstitial_unitid)!!)
    }

    private fun showInterstitialAd() {
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

    private fun showRewardedAd() {
        AAFGoogleAD.showRewardedAd(activity!!, ThemeResourcesManager.getString(R.string.admob_rewardedvideo_unitid)!!, object : OnUserEarnedRewardListener {
            override fun onUserEarnedReward(rewardItem: RewardItem) {
                ZixieContext.showToast("用户获得激励: rewardType:${rewardItem.type}, amount: ${rewardItem.amount} ")
            }

        }, object : FullScreenContentCallback() {
            override fun onAdImpression() {
                super.onAdImpression()
                ZixieContext.showToast("激励广告展示成功")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                ZixieContext.showToast("激励广告被点击")

            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                ZixieContext.showToast("激励广告被关闭")
            }
        }, null)
    }

    private fun showRewardedInterstitialAd() {
        AAFGoogleAD.showRewardedInterstitialAd(activity!!, ThemeResourcesManager.getString(R.string.admob_rewardedvideo_interstitial_unitid)!!, object : OnUserEarnedRewardListener {
            override fun onUserEarnedReward(rewardItem: RewardItem) {
                ZixieContext.showToast("用户获得激励: rewardType:${rewardItem.type}, amount: ${rewardItem.amount} ")
            }
        }, object : FullScreenContentCallback() {
            override fun onAdImpression() {
                super.onAdImpression()
                ZixieContext.showToast("激励广告展示成功")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                ZixieContext.showToast("激励广告被点击")

            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                ZixieContext.showToast("激励广告被关闭")
            }
        }, null)
    }

    private fun loadBanner(adView: AdView, size: AdSize) {
        AAFGoogleAD.addBanner(adView, ThemeResourcesManager.getString(R.string.admob_banner_unitid)!!, size, object : AdListener() {
            override fun onAdClicked() {
                ZixieContext.showToast("Banner广告[${adView.adUnitId}]被点击")
            }

            override fun onAdClosed() {
                ZixieContext.showToast("Banner广告[${adView.adUnitId}]被关闭")
            }


            override fun onAdImpression() {
                ZixieContext.showToast("Banner广告[${adView.adUnitId}]展示成功")
            }

            override fun onAdLoaded() {
                ZLog.d(AAFGoogleAD.TAG, "Banner广告[${adView.adUnitId}]加载成功")
            }

            override fun onAdOpened() {
                ZixieContext.showToast("Banner广告[${adView.adUnitId}]展示成功")
            }
        })
    }
}
