/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.google


import android.content.Intent
import android.view.View
import com.android.billingclient.api.*
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.services.google.GoogleOAuth
import com.bihe0832.android.services.google.pay.AAFGooglePayListener
import com.bihe0832.android.services.google.pay.GooglePayModule
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import java.util.*


class DebugGoogleFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    private var mGoogleOAuth: GoogleOAuth? = null
    private var mGooglePayModule: GooglePayModule? = null

    override fun initView(view: View) {
        super.initView(view)
        mGoogleOAuth = GoogleOAuth(activity!!, "809676995038-g86cvrvt9thn3qam94n6vcd81oqdevhv.apps.googleusercontent.com")
        mGooglePayModule = GooglePayModule(activity!!, object : AAFGooglePayListener {

            override fun onPurchasesSuccess(billingResult: BillingResult, purchases: List<Purchase>?) {
                ZLog.d("onPurchasesSuccess:$billingResult")
            }

            override fun onPurchasesCancel(billingResult: BillingResult, purchases: List<Purchase>?) {
                ZLog.d("onPurchasesCancel:$billingResult")
            }

            override fun onPurchasesFailed(billingResult: BillingResult, purchases: List<Purchase>?) {
                ZLog.d("onPurchasesFailed:$billingResult")
            }

            override fun onServiceDisconnected() {
                ZLog.d("onServiceDisconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                ZLog.d("onBillingSetupFinished:$billingResult")
                if (billingResult.getResponseCode() === BillingClient.BillingResponseCode.OK) {
                    //连接成功，可以进行查询商品等操作
                }
            }

        })
        mGooglePayModule?.startConnection()

    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("登录", View.OnClickListener { mGoogleOAuth?.startLogin(100) }))
            add(DebugItemData("查看个人信息", View.OnClickListener {
                mGoogleOAuth?.getLastUserInfo()?.let {
                    showUser(it)
                }
            }))
            add(DebugItemData("刷新Token", View.OnClickListener {
                mGoogleOAuth?.refreshToken {
                    showLastUser()
                }
            }))
            add(DebugItemData("解除授权", View.OnClickListener {
                mGoogleOAuth?.revokeAccess {
                    showLastUser()
                }
            }))
            add(DebugItemData("登出", View.OnClickListener {
                mGoogleOAuth?.logout {
                    showLastUser()
                }
            }))

            add(DebugItemData("拉起订阅列表并支付", View.OnClickListener {
                getSublist()
            }))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> {
                try {
                    showUser(mGoogleOAuth?.parseIntent(data))
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showLastUser() {
        mGoogleOAuth?.getLastUserInfo()?.let {
            showUser(it)
        }
    }

    private fun showUser(account: GoogleSignInAccount?) {
        account?.let {
            ZLog.d("Google", "---------------------")
            ZLog.d("Google", it.displayName ?: "")
            ZLog.d("Google", it.id ?: "")
            ZLog.d("Google", it.email)
            ZLog.d("Google", it.familyName ?: "")
            ZLog.d("Google", it.givenName ?: "")
            ZLog.d("Google", it.idToken ?: "")
            ZLog.d("Google", it.isExpired.toString())
            ZLog.d("Google", it.serverAuthCode ?: "")
            ZLog.d("Google", "---------------------")
            it.displayName?.let { name ->
                ZixieContext.showToast(name)
            }
        }
    }


    private fun getSublist() {
        mGooglePayModule?.querySubsDetails(mutableListOf("vip_new")) { result, productDetails ->
            for (productDetail in productDetails) {
                if (BillingClient.ProductType.SUBS.equals(productDetail.productType) && productDetail.subscriptionOfferDetails != null) {
                    productDetail?.subscriptionOfferDetails?.forEach { item ->
                        val googleProductPrice = item.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                                ?: ""
                        val googleCurrencyCode = item?.pricingPhases?.pricingPhaseList?.get(0)?.priceCurrencyCode
                                ?: ""
                        val replacePrice = mGooglePayModule!!.getFinalProductPrice(googleProductPrice, googleCurrencyCode)
                        ZLog.d("productDetails:" + item.basePlanId + " " + replacePrice)
                    }
                }
            }



            productDetails.firstOrNull()?.let { productDetailInfo ->
                val billingResult = mGooglePayModule!!.startBuy(activity!!, productDetailInfo, productDetailInfo.subscriptionOfferDetails?.get(0)?.offerToken
                        ?: "")
                ZLog.d("billingResult:$billingResult")
                //productDetailsList为可用商 品的集合
            }
        }
    }
}