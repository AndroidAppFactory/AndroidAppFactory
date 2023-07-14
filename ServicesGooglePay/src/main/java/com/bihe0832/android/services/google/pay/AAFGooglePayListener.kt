package com.bihe0832.android.services.google.pay

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/7/14.
 * Description: Description
 */
interface AAFGooglePayListener {
    fun onPurchasesSuccess(billingResult: BillingResult, purchases: List<Purchase>?)
    fun onPurchasesCancel(billingResult: BillingResult, purchases: List<Purchase>?)
    fun onPurchasesFailed(billingResult: BillingResult, purchases: List<Purchase>?)
    fun onServiceDisconnected()
    fun onBillingSetupFinished(billingResult: BillingResult)
}