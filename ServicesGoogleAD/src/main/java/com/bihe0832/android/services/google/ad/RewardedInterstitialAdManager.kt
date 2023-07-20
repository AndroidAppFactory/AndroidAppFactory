package com.bihe0832.android.services.google.ad

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.log.ZLog
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/7/17.
 * Description: Description
 *
 */
object RewardedInterstitialAdManager {
    private val mRewardedInterstitialAdList = ConcurrentHashMap<String, LinkedList<RewardedInterstitialAd>>()

    fun addToRewardedInterstitialAdList(adUnitId: String, rewardedAd: RewardedInterstitialAd) {
        var list = mRewardedInterstitialAdList.get(adUnitId)
        if (list == null) {
            list = LinkedList()
            mRewardedInterstitialAdList[adUnitId] = list
        }
        list.add(rewardedAd)
    }

    fun loadRewardedInterstitialAd(context: Context, adUnitId: String, loadCallback: RewardedInterstitialAdLoadCallback?) {
        ZLog.d(AAFGoogleAD.TAG, "开始加载插屏激励广告 adUnitId$adUnitId")
        RewardedInterstitialAd.load(context, adUnitId, AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                super.onAdLoaded(rewardedAd)
                if (loadCallback == null) {
                    addToRewardedInterstitialAdList(adUnitId, rewardedAd)
                } else {
                    loadCallback.onAdLoaded(rewardedAd)
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                ZLog.d(AAFGoogleAD.TAG, "加载插屏激励广告 失败：$loadAdError")
                // 加载失败
                loadCallback?.onAdFailedToLoad(loadAdError)
            }
        })
    }

    private fun showRewardedInterstitialAd(activity: Activity, data: RewardedInterstitialAd, onUserEarnedRewardListener: OnUserEarnedRewardListener?, fullScreenContentCallback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(AAFGoogleAD.TAG, "开始展示插屏激励广告 adUnitId:${data.adUnitId}")
        data.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                ZLog.d(AAFGoogleAD.TAG, "插屏激励广告[${data.adUnitId}]展示成功")
                fullScreenContentCallback?.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                ZLog.d(AAFGoogleAD.TAG, "插屏激励广告[${data.adUnitId}]显示")
                fullScreenContentCallback?.onAdShowedFullScreenContent()
                loadRewardedInterstitialAd(activity.applicationContext, data.adUnitId, null)
            }

            override fun onAdClicked() {
                ZLog.d(AAFGoogleAD.TAG, "插屏激励广告[${data.adUnitId}]被点击")
                fullScreenContentCallback?.onAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                ZLog.d(AAFGoogleAD.TAG, "插屏激励广告[${data.adUnitId}]被关闭")
                fullScreenContentCallback?.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                ZLog.e(AAFGoogleAD.TAG, "插屏激励广告[${data.adUnitId}]展示失败 error:${adError}")
                fullScreenContentCallback?.onAdFailedToShowFullScreenContent(adError)
                loadRewardedInterstitialAd(activity.applicationContext, data.adUnitId, null)
            }
        }
        data.onPaidEventListener = OnPaidEventListener { value ->
            ZLog.d(AAFGoogleAD.TAG, "插屏激励广告[${data.adUnitId}]广告价值: valuemicros-${value.valueMicros} currency-${value.currencyCode} precision-${value.precisionType}")
            paidEventListener?.onPaidEvent(value)
        }
        val options = ServerSideVerificationOptions.Builder().setCustomData("SAMPLE_CUSTOM_DATA_STRING").build()
        data.setServerSideVerificationOptions(options)
        data.show(activity) { rewardItem ->
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            ZLog.d(AAFGoogleAD.TAG, "插屏激励广告[${data.adUnitId}]用户获得激励: rewardType:$rewardType, amount: $rewardAmount ")
            onUserEarnedRewardListener?.onUserEarnedReward(rewardItem)
        }
    }

    fun showRewardedInterstitialAd(activity: Activity, adUnitId: String, onUserEarnedRewardListener: OnUserEarnedRewardListener?, fullScreenContentCallback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(AAFGoogleAD.TAG, "开始展示插屏激励广告 adUnitId$adUnitId")
        val data = mRewardedInterstitialAdList.get(adUnitId)?.remove()
        if (data != null) {
            showRewardedInterstitialAd(activity, data, onUserEarnedRewardListener, fullScreenContentCallback, paidEventListener)
        } else {
            loadRewardedInterstitialAd(activity, adUnitId, object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: RewardedInterstitialAd) {
                    super.onAdLoaded(p0)
                    showRewardedInterstitialAd(activity, p0, onUserEarnedRewardListener, fullScreenContentCallback, paidEventListener)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    fullScreenContentCallback?.onAdFailedToShowFullScreenContent(p0)
                }
            })
        }
    }
}