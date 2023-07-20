package com.bihe0832.android.services.google.ad

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.log.ZLog
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/7/14.
 * Description:
 * 1. InterstitialAd 是一种全屏广告，可以在应用程序的自然停顿点或用户自愿触发的情况下展示，例如应用程序的主菜单、游戏关卡之间、文章之间等。
 * 2. InterstitialAd 广告不会提供奖励，仅仅是为了向用户展示广告内容。
 *
 */
object InterstitialAdManager {

    private val mInterstitialAdList = ConcurrentHashMap<String, LinkedList<InterstitialAd>>()

    fun addToInterstitialAdList(adUnitId: String, interstitialAd: InterstitialAd) {
        var list = mInterstitialAdList.get(adUnitId)
        if (list == null) {
            list = LinkedList()
            mInterstitialAdList[adUnitId] = list
        }
        list.add(interstitialAd)
    }

    fun loadInterstitialAd(context: Context, adUnitId: String, loadCallback: InterstitialAdLoadCallback?) {
        ZLog.d(AAFGoogleAD.TAG, "开始加载插屏广告 adUnitId$adUnitId")
        InterstitialAd.load(context, adUnitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                if (loadCallback == null) {
                    addToInterstitialAdList(adUnitId, interstitialAd)
                } else {
                    loadCallback.onAdLoaded(interstitialAd)
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                ZLog.d(AAFGoogleAD.TAG, "加载激励广告 失败：$loadAdError")
                // 加载失败
                loadCallback?.onAdFailedToLoad(loadAdError)
            }
        })
    }

    private fun showInterstitialAd(activity: Activity, data: InterstitialAd, fullScreenContentCallback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(AAFGoogleAD.TAG, "开始展示插屏广告 adUnitId:${data.adUnitId}")
        data.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]展示成功")
                fullScreenContentCallback?.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]显示")
                fullScreenContentCallback?.onAdShowedFullScreenContent()
                loadInterstitialAd(activity.applicationContext, data.adUnitId, null)
            }

            override fun onAdClicked() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]被点击")
                fullScreenContentCallback?.onAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]被关闭")
                fullScreenContentCallback?.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                ZLog.e(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]展示失败 error:${adError}")
                fullScreenContentCallback?.onAdFailedToShowFullScreenContent(adError)
                loadInterstitialAd(activity.applicationContext, data.adUnitId, null)
            }
        }
        data.onPaidEventListener = OnPaidEventListener { value ->
            ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]广告价值: valuemicros-${value.valueMicros} currency-${value.currencyCode} precision-${value.precisionType}")
            paidEventListener?.onPaidEvent(value)
        }
        data.show(activity)

    }

    fun showInterstitialAd(activity: Activity, adUnitId: String, fullScreenContentCallback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(AAFGoogleAD.TAG, "开始展示插屏广告 adUnitId$adUnitId")
        val data = mInterstitialAdList.get(adUnitId)?.remove()
        if (data != null) {
            showInterstitialAd(activity, data, fullScreenContentCallback, paidEventListener)
        } else {
            loadInterstitialAd(activity, adUnitId, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    showInterstitialAd(activity, p0, fullScreenContentCallback, paidEventListener)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    fullScreenContentCallback?.onAdFailedToShowFullScreenContent(p0)
                }
            })
        }
    }
}