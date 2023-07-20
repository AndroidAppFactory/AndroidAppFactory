package com.bihe0832.android.services.google.ad

import android.content.Context
import com.bihe0832.android.lib.log.ZLog
import com.google.android.gms.ads.*

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/7/17.
 * Description: Description
 *
 */
object BannerADManager {

    fun getCurrentOrientationAnchoredAdaptiveBannerAdSize(context: Context, parentSize: Float): AdSize {
        var adWidthPixels = parentSize
        val density = context.resources.displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context!!, adWidth)
    }

    fun addBanner(adView: AdView, adUnitId: String, size: AdSize, listener: AdListener?) {
        ZLog.d(AAFGoogleAD.TAG, "开始添加Banner广告 adUnitId$adUnitId")
        adView.apply {
            setAdUnitId(adUnitId)
            setAdSize(size)
            adListener = object : AdListener() {
                override fun onAdClicked() {
                    ZLog.d(AAFGoogleAD.TAG, "Banner广告[${adView.adUnitId}]被点击 onAdClicked")
                    listener?.onAdClicked()
                }

                override fun onAdClosed() {
                    ZLog.d(AAFGoogleAD.TAG, "Banner广告[${adView.adUnitId}]被关闭 onAdClosed")
                    listener?.onAdClosed()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    ZLog.d(AAFGoogleAD.TAG, "Banner广告[${adView.adUnitId}]展示失败 onAdFailedToLoad error:${adError}")
                    listener?.onAdFailedToLoad(adError)
                }

                override fun onAdImpression() {
                    ZLog.d(AAFGoogleAD.TAG, "Banner广告[${adView.adUnitId}] onAdImpression 广告位添加并展示成功")
                    listener?.onAdImpression()
                }

                override fun onAdLoaded() {
                    ZLog.d(AAFGoogleAD.TAG, "Banner广告[${adView.adUnitId}]onAdLoaded 加载成功")
                    listener?.onAdLoaded()
                }

                override fun onAdOpened() {
                    ZLog.d(AAFGoogleAD.TAG, "Banner广告[${adView.adUnitId}]onAdOpened 展示成功")
                    listener?.onAdOpened()
                }
            }
        }
        adView.loadAd(AdRequest.Builder().build())
    }
}