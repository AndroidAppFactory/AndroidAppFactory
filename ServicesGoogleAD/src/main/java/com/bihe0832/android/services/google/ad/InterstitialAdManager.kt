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
 * @author hardyshi code@bihe0832.com
 * Created on 2023/7/14.
 * Description: Description
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
        ZLog.d(AAFGoogleAD.TAG, "加载插屏广告 adUnitId$adUnitId")
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
                super.onAdFailedToLoad(loadAdError)
                // 加载失败
                loadCallback?.onAdFailedToLoad(loadAdError)
            }
        })
    }

    private fun showInterstitialAd(activity: Activity, data: InterstitialAd, callback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(AAFGoogleAD.TAG, "展示插屏广告 adUnitId:${data.adUnitId}")
        data.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]展示成功")
                callback?.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]显示")
                callback?.onAdShowedFullScreenContent()
                loadInterstitialAd(activity, data.adUnitId, null)
            }

            override fun onAdClicked() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]被点击")
                callback?.onAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]被关闭")
                callback?.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                ZLog.e(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]展示失败 error:${adError}")
                callback?.onAdFailedToShowFullScreenContent(adError)
                loadInterstitialAd(activity, data.adUnitId, null)
            }
        }
        data.onPaidEventListener = OnPaidEventListener { value ->
            ZLog.d(AAFGoogleAD.TAG, "插屏广告[${data.adUnitId}]广告价值: valuemicros-${value.valueMicros} currency-${value.currencyCode} precision-${value.precisionType}")
            paidEventListener?.onPaidEvent(value)
        }
        data.show(activity)

    }

    fun showInterstitialAd(activity: Activity, adUnitId: String, callback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(AAFGoogleAD.TAG, "展示插屏广告 adUnitId$adUnitId")
        val data = mInterstitialAdList.get(adUnitId)?.remove()
        if (data != null) {
            showInterstitialAd(activity, data, callback, paidEventListener)
        } else {
            loadInterstitialAd(activity, adUnitId, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    showInterstitialAd(activity, p0, callback, paidEventListener)
                }
            })
        }
    }
}