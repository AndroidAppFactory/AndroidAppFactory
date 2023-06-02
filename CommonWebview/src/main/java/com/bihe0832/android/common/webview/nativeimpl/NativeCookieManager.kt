package com.bihe0832.android.common.webview.nativeimpl

import android.net.Uri
import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 * @author zixie code@bihe0832.com Created on 8/12/21.
 */
object NativeCookieManager {


    fun init(mWebView: WebView) {
        if (null == mWebView) {
            return
        }
        CookieSyncManager.createInstance(mWebView.context)
        getCookieManager().setAcceptCookie(true)
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getCookieManager().setAcceptThirdPartyCookies(mWebView, true)
        }
        syncCookie()
    }

    fun getCookieManager(): CookieManager {
        return CookieManager.getInstance()
    }

    fun syncCookie() {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().flush()
        } else {
            CookieSyncManager.getInstance().sync()
        }
    }

    fun removeCookiesForDomain(url: String) {
        val uri = Uri.parse(url)
        if (uri.host == null) {
            return
        }
        var domain = uri.host?.toLowerCase() ?: ""

        /* http://code.google.com/p/android/issues/detail?id=19294 */
        if (BuildUtils.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            /* Trim leading '.'s */
            if (domain.startsWith(".")) domain = domain.substring(1)
        }
        val cookiesForDomain = getCookieManager().getCookie(domain)
        if (cookiesForDomain != null) {
            val cookies = cookiesForDomain.split(";".toRegex()).toTypedArray()
            for (cookieTuple in cookies) {
                val cookieParts = cookieTuple.split("=".toRegex()).toTypedArray()
                getCookieManager().setCookie(url, getCookieString(cookieParts[0], "", domain) + "; Expires=Thu, 12 Aug 2021 21:13:52 GMT")
            }
            syncCookie()
        }
    }

    fun logCookieForDomain(url: String) {
        ZLog.d("Cookie", "========= Cookies for url:$url =========")
        val uri = Uri.parse(url)
        if (uri.host == null) {
            return
        }

        val cookiesForDomain = getCookieManager().getCookie(uri.host?.toLowerCase())
        if (cookiesForDomain != null) {
            val cookies = cookiesForDomain.split(";".toRegex()).toTypedArray()
            for (cookieTuple in cookies) {
                ZLog.d("Cookie", cookieTuple)
            }
        }
        ZLog.d("Cookie", "========= Cookies for url:$url =========")
    }

    fun getCookieString(name: String, value: String, domain: String?): String {
        return getCookieString(name, value, "/", domain)
    }

    fun getCookieString(name: String, value: String, path: String, domain: String?): String {
        var v = "$name=$value"
        if (domain != null) {
            v += "; path=$path"
            v += "; domain=$domain"
        }
        return v
    }

    fun setCookie(url: String, name: String, value: String, path: String, domain: String?, time: Long) {
        getCookieManager().setCookie(url, getCookieString(name, value, path, domain) + "; MAX-Age=" + time)
    }

    fun setCookie(url: String, name: String, value: String, path: String, time: Long) {
        val uri = Uri.parse(url)
        if (uri.host == null) {
            return
        }
        setCookie(url, name, value, path, uri.host?.toLowerCase(), time)
    }

    fun setCookie(url: String, name: String, value: String, time: Long) {

        setCookie(url, name, value, "/", time)
    }

    fun setCookie(url: String, name: String, value: String) {
        setCookie(url, name, value, 60 * 60 * 24)
    }
}