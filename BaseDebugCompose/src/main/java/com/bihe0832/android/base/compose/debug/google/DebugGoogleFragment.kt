package com.bihe0832.android.base.compose.debug.google

import android.content.Intent
import android.view.View
import androidx.compose.runtime.Composable
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeFragment
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.services.google.AAFGoogleOAuth
import com.bihe0832.android.services.google.pay.AAFGooglePay
import com.bihe0832.android.services.google.pay.AAFGooglePayListener
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/22.
 * Description: Description
 *
 */
class DebugGoogleFragment : DebugCommonComposeFragment() {

    val LOG_TAG = this.javaClass.simpleName
    private var mAAFGoogleOAuth: AAFGoogleOAuth? = null
    private var mAAFGooglePay: AAFGooglePay? = null

    override fun initView(view: View) {
        super.initView(view)
        mAAFGoogleOAuth = AAFGoogleOAuth(
            activity!!, "809676995038-g86cvrvt9thn3qam94n6vcd81oqdevhv.apps.googleusercontent.com"
        )
        mAAFGooglePay = AAFGooglePay(activity!!, object : AAFGooglePayListener {

            override fun onPurchasesSuccess(
                billingResult: BillingResult, purchases: List<Purchase>?
            ) {
                ZLog.d("onPurchasesSuccess:$billingResult")
            }

            override fun onPurchasesCancel(
                billingResult: BillingResult, purchases: List<Purchase>?
            ) {
                ZLog.d("onPurchasesCancel:$billingResult")
            }

            override fun onPurchasesFailed(
                billingResult: BillingResult, purchases: List<Purchase>?
            ) {
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
        mAAFGooglePay?.startConnection()

    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                DebugContent {
                    DebugItem("登录") { mAAFGoogleOAuth?.startLogin(100) }
                    DebugItem("查看个人信息") {
                        mAAFGoogleOAuth?.getLastUserInfo()?.let {
                            showUser(it)
                        }
                    }
                    DebugItem("刷新Token") {
                        mAAFGoogleOAuth?.refreshToken {
                            showLastUser()
                        }
                    }
                    DebugItem("解除授权") {
                        mAAFGoogleOAuth?.revokeAccess {
                            showLastUser()
                        }
                    }
                    DebugItem("登出") {
                        mAAFGoogleOAuth?.logout {
                            showLastUser()
                        }
                    }

                    DebugItem("拉起订阅列表并支付") {
                        getSublist()
                    }


                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> {
                try {
                    showUser(mAAFGoogleOAuth?.parseIntent(data))
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun showLastUser() {
        mAAFGoogleOAuth?.getLastUserInfo()?.let {
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
        mAAFGooglePay?.querySubsDetails(mutableListOf("vip_new")) { result, productDetails ->
            for (productDetail in productDetails) {
                if (BillingClient.ProductType.SUBS.equals(productDetail.productType) && productDetail.subscriptionOfferDetails != null) {
                    productDetail?.subscriptionOfferDetails?.forEach { item ->
                        val googleProductPrice =
                            item.pricingPhases.pricingPhaseList.get(0)?.formattedPrice ?: ""
                        val googleCurrencyCode =
                            item?.pricingPhases?.pricingPhaseList?.get(0)?.priceCurrencyCode ?: ""
                        val replacePrice = mAAFGooglePay!!.getFinalProductPrice(
                            googleProductPrice, googleCurrencyCode
                        )
                        ZLog.d("productDetails:" + item.basePlanId + " " + replacePrice)
                    }
                }
            }



            productDetails.firstOrNull()?.let { productDetailInfo ->
                val billingResult = mAAFGooglePay!!.startBuy(
                    activity!!,
                    productDetailInfo,
                    productDetailInfo.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                )
                ZLog.d("billingResult:$billingResult")
                //productDetailsList为可用商 品的集合
            }
        }
    }
}