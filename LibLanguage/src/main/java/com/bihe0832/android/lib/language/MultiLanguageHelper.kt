package com.bihe0832.android.lib.language

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import androidx.core.os.ConfigurationCompat
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.util.Locale


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/2/12.
 * Description: Description
 *
 */
object MultiLanguageHelper {

    private const val KEY_LOCAL_LANGUAGE = "app_config_language"
    private const val SP_NAME_LOCAL_LANGUAGE = "language_sp"
    private var lastLocale: Locale? = null

    /**
     * 更新当前页面语言资源，返回更新后的Context
     */
    fun modifyContextLanguageConfig(context: Context): Context {
        return modifyContextLanguageConfig(context, getLanguageConfig(context))
    }

    fun modifyContextLanguageConfig(context: Context, locale: Locale): Context {
        return modifyContextLanguageConfig(context, context.resources, locale)
    }

    fun modifyContextLanguageConfig(
        context: Context, resources: Resources, locale: Locale
    ): Context {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N) {
            return setAppLanguageApi24(context, resources, locale)
        } else {
            setAppLanguage(resources, locale)
        }
        return context
    }


    /**
     * 设置应用语言
     */
    private fun setAppLanguage(resources: Resources, locale: Locale) {
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }
        resources.updateConfiguration(configuration, displayMetrics)
    }

    /**
     * 兼容 7.0 及以上
     */
    private fun setAppLanguageApi24(
        context: Context,
        resources: Resources,
        locale: Locale
    ): Context {
        val configuration = resources.configuration
        configuration.setLocales(LocaleList(locale))
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    /**
     * 获取当前系统语言，如未包含则默认英文
     */
    fun getSystemLocale(): Locale {
        val locale = if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N) {
            ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
        } else {
            Locale.getDefault()
        }
        return locale ?: Locale.ENGLISH
    }

    fun getContextLocale(context: Context): Locale {
        val locale: Locale = if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION") context.resources.configuration.locale
        }
        return locale
    }

    fun getLanguageConfig(context: Context): Locale {
        if (null == lastLocale) {
            val sp = context.getSharedPreferences(SP_NAME_LOCAL_LANGUAGE, MODE_PRIVATE)
            Locale.forLanguageTag(sp.getString(KEY_LOCAL_LANGUAGE, "") ?: "").let {
                lastLocale = if (TextUtils.isEmpty(it.language)) {
                    getSystemLocale()
                } else {
                    it
                }
            }
        }
        return lastLocale!!

    }

    fun setLanguageConfig(context: Context, locale: Locale): Boolean {
        lastLocale = locale
        val editor = context.getSharedPreferences(SP_NAME_LOCAL_LANGUAGE, MODE_PRIVATE).edit()
        editor.putString(KEY_LOCAL_LANGUAGE, locale.toLanguageTag())
        return editor.commit()
    }

    fun getResources(context: Context, locale: Locale): Resources {
        val resources: Resources = context.getResources()
        val configuration = Configuration()
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return resources
    }

    fun getRealResources(context: Context): Resources {
        val resources: Resources = context.applicationContext.getResources()
        val configuration = Configuration()
        configuration.setLocale(getLanguageConfig(context))
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return resources
    }
}