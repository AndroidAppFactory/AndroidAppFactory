package com.bihe0832.android.app.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterAction.SCHEME
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.lifecycle.AAFActivityLifecycleChangedListener
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.router.RouterMappingManager
import com.bihe0832.android.lib.router.Routers
import com.bihe0832.android.lib.utils.intent.IntentUtils

/**
 * AAF 路由辅助类
 *
 * 提供应用内页面跳转的统一入口，包括：
 * - 路由初始化和拦截器配置
 * - 页面跳转方法封装
 * - 临时授权跳转支持
 *
 * @author zixie code@bihe0832.com
 * Created on 2017/6/27.
 */
object RouterHelper {

    /** 需要权限检查拦截的路由列表 */
    private val needCheckInterceptHostList by lazy {
        getNeedCheckInterceptHostList()
    }

    /** 需要登录拦截的路由列表 */
    private val needLoginInterceptHostList by lazy {
        getNeedLoginInterceptHostList()
    }

    /** 不需要检查，直接跳过的路由列表 */
    private val skipListHostList by lazy {
        getSkipListHostList()
    }

    /** 临时跳过检查的路由列表（完整 URL） */
    private val tempSkipRouterList = mutableListOf<String>()

    /**
     * 初始化路由系统
     *
     * 配置应用前后台监听、Activity 生命周期监听和路由拦截器
     */
    fun initRouter() {
        // 应用前后台检测
        ApplicationObserver.addStatusChangeListener(object : ApplicationObserver.APPStatusChangeListener {
            override fun onForeground() {
                ZLog.d("onForeground")
            }

            override fun onBackground() {
            }
        })

        ActivityObserver.setActivityLifecycleChangedListener(object : AAFActivityLifecycleChangedListener() {

            override fun onActivityResumed(activity: Activity) {
                if (!ZixieContext.isOfficial()) {
                    RouterMappingManager.getInstance().getRouterHost(activity::class.java).let {
                        if (!TextUtils.isEmpty(it)) {
                            ZLog.d("Router", "Activity ：${activity.javaClass} 对应 Router 为：" + it)
                        }
                    }
                }
            }
        })

        // 路由拦截初始化
        RouterInterrupt.init(object : RouterInterrupt.RouterProcess {

            override fun needLogin(uri: Uri, source: String): Boolean {
                return needLoginInterceptHostList.contains(uri.host)
            }

            override fun needInterrupt(uri: Uri, source: String): Boolean {
                return if (skipListHostList.contains(uri.host)) {
                    false
                } else {
                    // 需要被拦截
                    needCheckInterceptHostList.contains(uri.host) || !AgreementPrivacy.hasAgreedPrivacy()
                }
            }

            override fun doInterrupt(uri: Uri, source: String): Boolean {
                return if (!AgreementPrivacy.hasAgreedPrivacy()) {
                    goSplash(uri)
                    true
                } else {
                    false
                }
            }

            override fun notFound(context: Context, uri: Uri, source: String) {
                super.notFound(context, uri, source)
                ZixieContext.applicationContext?.let {
                    if (Routers.ROUTERS_VALUE_PARSE_SOURCE.equals(source, ignoreCase = true)) {
                        goSplash(null)
                    } else {
                        try {
                            val resolveActivityPackage = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                                setComponent(null)
                                setSelector(null)
                            }.resolveActivity(it.packageManager).packageName
                            if (!resolveActivityPackage.equals(it.packageName, ignoreCase = true)) {
                                IntentUtils.jumpToOtherApp(context, uri.toString())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    /**
     * 跳转到启动页
     *
     * @param uri 原始跳转 URI，启动页完成后会继续跳转
     */
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
     * 打开路由并临时授权
     *
     * 将路由添加到临时跳过列表后打开，用于特殊场景的权限绕过
     *
     * @param url 完整的路由 URL
     */
    fun openFinalURLAndTempAuthorize(url: String) {
        tempSkipRouterList.add(url)
        openFinalURL(url)
    }

    /**
     * 打开完整路由 URL
     *
     * @param pathHost 完整的路由路径
     */
    private fun openFinalURL(pathHost: String) {
        RouterAction.openFinalURL(pathHost)
    }

    /**
     * 构建完整的路由 URL
     *
     * @param pathHost 路由路径
     * @param para 路由参数
     * @return 完整的路由 URL
     */
    fun getFinalURL(pathHost: String, para: Map<String, String>?): String {
        return RouterAction.getFinalURL(SCHEME, pathHost, para)
    }

    /**
     * 构建完整的路由 URL（无参数）
     *
     * @param pathHost 路由路径
     * @return 完整的路由 URL
     */
    fun getFinalURL(pathHost: String): String {
        return RouterAction.getFinalURL(pathHost)
    }

    /**
     * 通过路由打开页面
     *
     * @param pathHost 路由路径
     * @param para 路由参数
     */
    fun openPageByRouter(pathHost: String, para: Map<String, String>?) {
        RouterAction.openPageByRouter(pathHost, para)
    }

    /**
     * 通过路由打开页面（无参数）
     *
     * @param pathHost 路由路径
     */
    fun openPageByRouter(pathHost: String) {
        RouterAction.openPageByRouter(pathHost)
    }
}
