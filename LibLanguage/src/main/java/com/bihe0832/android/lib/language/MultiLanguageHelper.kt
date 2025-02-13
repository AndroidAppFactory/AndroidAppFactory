package com.bihe0832.android.lib.language

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import androidx.core.os.ConfigurationCompat
import com.bihe0832.android.lib.config.Config
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

    /**
     * 更新当前页面语言资源，返回更新后的Context
     */
    fun modifyContextLanguageConfig(context: Context): Context {
        return modifyContextLanguageConfig(context, getLanguageConfig())
    }

    fun modifyContextLanguageConfig(context: Context, locale: Locale): Context {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N) {
            setAppLanguageApi24(context, locale)
        } else {
            setAppLanguage(context, locale)
        }
        return context
    }

    /**
     * 设置应用语言
     */
    private fun setAppLanguage(context: Context, locale: Locale) {
        val resources = context.resources
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
    private fun setAppLanguageApi24(context: Context, locale: Locale): Context {
        val resource = context.resources
        val configuration = resource.configuration
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
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

    fun getLanguageConfig(): Locale {
        Locale.forLanguageTag(Config.readConfig(KEY_LOCAL_LANGUAGE, "")).let {
            return if (TextUtils.isEmpty(it.language)) {
                getSystemLocale()
            } else {
                it
            }
        }
    }

    fun setLanguageConfig(locale: Locale): Boolean {
        return Config.writeConfig(KEY_LOCAL_LANGUAGE, locale.toLanguageTag())
    }

    fun getRealString(contextAfterConfig: Context, id: Int): String {
        val configuration = Configuration(contextAfterConfig.resources.configuration)
        val localizedContext = contextAfterConfig.createConfigurationContext(configuration)
        return localizedContext.resources.getString(id)
    }

    fun getRealStringWithConfig(context: Context, id: Int): String {
        val contextAfterConfig = modifyContextLanguageConfig(context)
        return getRealString(contextAfterConfig, id)
    }
}