package com.bihe0832.android.app.router

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterAction.SCHEME
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.lifecycle.AAFActivityLifecycleChangedListener
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.router.RouterMappingManager


/**
 * Created by zixie on 2017/6/27.
 *
 */
object RouterHelper {

    //需要拦截
    private val needCheckInterceptHostList by lazy {
        getNeedCheckInterceptHostList()
    }

    //需要登录
    private val needLoginInterceptHostList by lazy {
        getNeedLoginInterceptHostList()
    }

    //不需要检查，直接跳过的路由
    private val skipListHostList by lazy {
        getSkipListHostList()
    }

    //一些特殊场景，需要直接跳过的URL（完整URL）
    private val tempSkipRouterList = mutableListOf<String>()

    fun initRouter() {
        //应用前后台检测
        ApplicationObserver.addStatusChangeListener(object : ApplicationObserver.APPStatusChangeListener {
            override fun onForeground() {
                ZLog.d("onForeground")
            }

            override fun onBackground() {

            }
        })

        ActivityObserver.setActivityLifecycleChangedListener(object : AAFActivityLifecycleChangedListener() {

            override fun onActivityResumed(activity: Activity) {
                RouterMappingManager.getInstance().getRouterHost(activity::class.java).let {
                    if (!TextUtils.isEmpty(it)) {
                        ZLog.d("Activity ：${activity.javaClass} 对应 Router 为：" + it)
                    }
                }
            }
        })

        //路由拦截初始化
        RouterInterrupt.init(object : RouterInterrupt.RouterProcess {

            override fun needLogin(uri: Uri): Boolean {
                return needLoginInterceptHostList.contains(uri.host)
            }

            override fun needInterrupt(uri: Uri): Boolean {
                return if (skipListHostList.contains(uri.host)) {
                    false
                } else {
                    //需要被拦截
                    needCheckInterceptHostList.contains(uri.host) || !AgreementPrivacy.hasAgreedPrivacy()
                }
            }

            override fun doInterrupt(uri: Uri): Boolean {
                return if (!AgreementPrivacy.hasAgreedPrivacy()) {
                    goSplash(uri)
                    true
                } else {
                    false
                }
            }
        })
    }

    private fun goSplash(uri: Uri?) {
        HashMap<String, String>().apply {
            uri?.let {
                this[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(uri.toString())
            }
        }.let {
            RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_SPLASH, it)
        }
    }

    /**
     * 通过传入实际路径打开路由，同时将路由添加到临时权限调用示例
     *
     * RouterHelper.openAndTempAuthorize(RouterHelper.getFinalURL(RouterConstants.MODULE_NAME_TEST,mutableMapOf(RouterConstants.INTENT_EXTRA_KEY_TEST_ITEM_TAB to 1)))
     *
     */
    fun openFinalURLAndTempAuthorize(url: String) {
        tempSkipRouterList.add(url)
        openFinalURL(url)
    }

    private fun openFinalURL(pathHost: String) {
        RouterAction.openFinalURL(pathHost)
    }

    fun getFinalURL(pathHost: String, para: Map<String, String>?): String {
        return RouterAction.getFinalURL(SCHEME, pathHost, para)
    }

    fun getFinalURL(pathHost: String): String {
        return RouterAction.getFinalURL(pathHost)
    }

    fun openPageByRouter(pathHost: String, para: Map<String, String>?) {
        RouterAction.openPageByRouter(pathHost, para)
    }

    fun openPageByRouter(pathHost: String) {
        RouterAction.openPageByRouter(pathHost)
    }
}
