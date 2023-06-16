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
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.services.google.GoogleOAuth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException


class DebugGoogleFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    private var mGoogleOAuth: GoogleOAuth? = null
    override fun initView(view: View) {
        super.initView(view)
        mGoogleOAuth = GoogleOAuth(activity!!, "809676995038-g86cvrvt9thn3qam94n6vcd81oqdevhv.apps.googleusercontent.com")
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
}