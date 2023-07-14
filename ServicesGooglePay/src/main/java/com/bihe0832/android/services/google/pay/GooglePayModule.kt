package com.bihe0832.android.services.google.pay

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.bihe0832.android.lib.thread.ThreadManager

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/7/14.
 * Description: Description
 */
open class GooglePayModule(private val mContext: Context, private val purchasesUpdatedListener: AAFGooglePayListener) : BillingClientStateListener {

    private val reteyTime = 0

    private fun getBillClient(): BillingClient {
        BillingClient.newBuilder(mContext).apply {
            setListener { billingResult, purchases ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        purchasesUpdatedListener.onPurchasesSuccess(billingResult, purchases)
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        purchasesUpdatedListener.onPurchasesCancel(billingResult, purchases)
                    }
                    else -> {
                        purchasesUpdatedListener.onPurchasesFailed(billingResult, purchases)
                    }
                }
            } //支持待处理的交易
            enablePendingPurchases()
        }.build().let {
            return it
        }
    }

    private val mGoogleBillingClient by lazy {
        getBillClient()
    }

    override fun onBillingServiceDisconnected() {
        if (reteyTime > 5) {
            purchasesUpdatedListener.onServiceDisconnected()
        } else {
            ThreadManager.getInstance().start({ mGoogleBillingClient.startConnection(this) }, 1000L)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        purchasesUpdatedListener.onBillingSetupFinished(billingResult)
    }

    fun startConnection() {
        mGoogleBillingClient.startConnection(this)
    }


    fun getGoogleBillingClient(): BillingClient {
        return mGoogleBillingClient
    }

    fun getFinalProductPrice(googleProductPrice: String, googleCurrencyCode: String): String? {
        getCurrencyLocalCode(googleCurrencyCode).let {
            return replaceCurrencySymbol(googleProductPrice, googleCurrencyCode, it)
        }
    }

    fun querySubsDetails(productID: List<String>, productDetailsResponseListener: ProductDetailsResponseListener) {
        //查询订阅类型的商品
        //设置查询参数方式有所更改，productId为产品ID(从谷歌后台获取)
        val subscriptionProductInfo: ArrayList<QueryProductDetailsParams.Product> = ArrayList<QueryProductDetailsParams.Product>().apply {
            productID.forEach {
                add(QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build())
            }
        }
        mGoogleBillingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(subscriptionProductInfo).build(), object : ProductDetailsResponseListener {
            override fun onProductDetailsResponse(result: BillingResult, productDetails: MutableList<ProductDetails>) {
                productDetailsResponseListener.onProductDetailsResponse(result, productDetails)
            }

        })
    }

    fun startBuy(activity: Activity, productDetailInfo: ProductDetails, offerToken: String): BillingResult {
        val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetailInfo)
                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                .setOfferToken(offerToken).build())

        val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()

        // Launch the billing flow
        return getGoogleBillingClient().launchBillingFlow(activity, billingFlowParams)
    }
}