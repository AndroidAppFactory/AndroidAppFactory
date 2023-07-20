package com.bihe0832.android.services.google.pay

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/7/14.
 * Description: Description
 */
interface AAFGooglePayListener {
    // 购买成功
    fun onPurchasesSuccess(billingResult: BillingResult, purchases: List<Purchase>?)
    // 用户取消购买
    fun onPurchasesCancel(billingResult: BillingResult, purchases: List<Purchase>?)
    // 购买异常
    fun onPurchasesFailed(billingResult: BillingResult, purchases: List<Purchase>?)
    // 服务断开，并在重试五次后无效
    fun onServiceDisconnected()

    // 获取订单列表成功
    fun onBillingSetupFinished(billingResult: BillingResult)
}