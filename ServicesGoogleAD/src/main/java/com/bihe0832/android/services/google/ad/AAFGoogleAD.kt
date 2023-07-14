package com.bihe0832.android.services.google.ad

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/6/16.
 * Description: Description
 *
 */
object AAFGoogleAD {

    const val TAG = "AAFGoogleAD"

    private val mAdapterStatusMap = ConcurrentHashMap<String, AdapterStatus>()

    fun initModule(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            initializationStatus.adapterStatusMap.entries.filter {
                // 判断适配器初始化的状态
                // 准备就绪 AdapterStatus.State.READY
                // 没准备好 AdapterStatus.State.NOT_READY
                it.value.initializationState == AdapterStatus.State.READY
            }.forEach {
                mAdapterStatusMap[it.key] = it.value
            }
        }
    }

    fun canLoadAds(): Boolean {
        // 有任意一种适配器初始化成功就可以开始加载广告
        return !mAdapterStatusMap.isNullOrEmpty()
    }

    // 插屏广告加载
    fun loadInterstitialAd(context: Context, adUnitId: String, loadCallback: InterstitialAdLoadCallback? = null): Boolean {
        return if (canLoadAds()) {
            InterstitialAdManager.loadInterstitialAd(context, adUnitId, loadCallback)
            true
        } else {
            false
        }
    }

    fun showInterstitialAd(activity: Activity, adUnitId: String, callback: FullScreenContentCallback? = null, paidEventListener: OnPaidEventListener? = null): Boolean {
        return if (canLoadAds()) {
            InterstitialAdManager.showInterstitialAd(activity, adUnitId, callback, paidEventListener)
            true
        } else {
            false
        }

    }

}