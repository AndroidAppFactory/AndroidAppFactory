package com.bihe0832.android.services.google.ad

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.services.google.ad.AAFGoogleAD.TAG
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/7/17.
 * Description:
 *  1. 该广告类型可以在应用程序的自然停顿点或用户自愿触发的情况下展示，并要求用户观看指定长度的广告视频才能获得奖励。
 *  2. 该广告类型适用于应用程序的多个场景，例如应用程序的主菜单、奖励中心等。
 *
 */
object RewardedAdManager {
    private val mRewardedAdList = ConcurrentHashMap<String, LinkedList<RewardedAd>>()

    fun addTorewardedAdList(adUnitId: String, rewardedAd: RewardedAd) {
        var list = mRewardedAdList.get(adUnitId)
        if (list == null) {
            list = LinkedList()
            mRewardedAdList[adUnitId] = list
        }
        list.add(rewardedAd)
    }

    fun loadRewardedAd(context: Context, adUnitId: String, loadCallback: RewardedAdLoadCallback?) {
        ZLog.d(TAG, "开始加载激励广告 adUnitId$adUnitId")
        RewardedAd.load(context, adUnitId, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                super.onAdLoaded(rewardedAd)
                if (loadCallback == null) {
                    addTorewardedAdList(adUnitId, rewardedAd)
                } else {
                    loadCallback.onAdLoaded(rewardedAd)
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                ZLog.d(TAG, "加载激励广告 失败：$loadAdError")
                // 加载失败
                loadCallback?.onAdFailedToLoad(loadAdError)
            }
        })
    }

    private fun showRewardedAd(activity: Activity, data: RewardedAd, onUserEarnedRewardListener: OnUserEarnedRewardListener?, fullScreenContentCallback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(TAG, "开始展示激励广告 adUnitId:${data.adUnitId}")
        data.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                ZLog.d(TAG, "激励广告[${data.adUnitId}]展示成功")
                fullScreenContentCallback?.onAdImpression()
            }

            override fun onAdShowedFullScreenContent() {
                ZLog.d(TAG, "激励广告[${data.adUnitId}]显示")
                fullScreenContentCallback?.onAdShowedFullScreenContent()
                loadRewardedAd(activity.applicationContext, data.adUnitId, null)
            }

            override fun onAdClicked() {
                ZLog.d(TAG, "激励广告[${data.adUnitId}]被点击")
                fullScreenContentCallback?.onAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                ZLog.d(TAG, "激励广告[${data.adUnitId}]被关闭")
                fullScreenContentCallback?.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                ZLog.e(TAG, "激励广告[${data.adUnitId}]展示失败 error:${adError}")
                fullScreenContentCallback?.onAdFailedToShowFullScreenContent(adError)
                loadRewardedAd(activity.applicationContext, data.adUnitId, null)
            }
        }
        data.onPaidEventListener = OnPaidEventListener { value ->
            ZLog.d(TAG, "激励广告[${data.adUnitId}]广告价值: valuemicros-${value.valueMicros} currency-${value.currencyCode} precision-${value.precisionType}")
            paidEventListener?.onPaidEvent(value)
        }
        val options = ServerSideVerificationOptions.Builder().setCustomData("SAMPLE_CUSTOM_DATA_STRING").build()
        data.setServerSideVerificationOptions(options)
        data.show(activity) { rewardItem ->
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            ZLog.d(TAG, "激励广告[${data.adUnitId}]用户获得激励: rewardType:$rewardType, amount: $rewardAmount ")
            onUserEarnedRewardListener?.onUserEarnedReward(rewardItem)
        }
    }

    fun showRewardedAd(activity: Activity, adUnitId: String, onUserEarnedRewardListener: OnUserEarnedRewardListener?, fullScreenContentCallback: FullScreenContentCallback?, paidEventListener: OnPaidEventListener?) {
        ZLog.d(TAG, "开始展示激励广告 adUnitId$adUnitId")
        val data = mRewardedAdList.get(adUnitId)?.remove()
        if (data != null) {
            showRewardedAd(activity, data, onUserEarnedRewardListener, fullScreenContentCallback, paidEventListener)
        } else {
            loadRewardedAd(activity, adUnitId, object : RewardedAdLoadCallback() {
                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    showRewardedAd(activity, p0, onUserEarnedRewardListener, fullScreenContentCallback, paidEventListener)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    fullScreenContentCallback?.onAdFailedToShowFullScreenContent(p0)
                }
            })
        }
    }
}