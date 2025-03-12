package com.bihe0832.android.lib.theme

import android.app.Application
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.text.TextUtils
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.theme.core.ActivityLifecycleForTheme
import java.util.Observable


object ThemeManager : Observable() {

    const val TAG = "ThemeManager"
    private val CONFIG_KEY_THEME_ENABLED = "com.bihe0832.android.lib.theme.enabled"
    private val CONFIG_KEY_CURRENT_THEME = "com.bihe0832.android.lib.theme.path"
    private lateinit var mApplication: Application
    private var isDebug: Boolean = false

    fun init(application: Application, isDebug: Boolean) {
        this.mApplication = application
        this.isDebug = isDebug
        ThemeResourcesManager.init(application)
        if (isEnabled()) {
            application.registerActivityLifecycleCallbacks(ActivityLifecycleForTheme())
            if (hasTheme()) {
                applyTheme(Config.readConfig(CONFIG_KEY_CURRENT_THEME, ""))
            }
        }
    }

    fun hasTheme(): Boolean {
        return !TextUtils.isEmpty(Config.readConfig(CONFIG_KEY_CURRENT_THEME, ""))
    }

    fun isEnabled(): Boolean {
        return Config.isSwitchEnabled(CONFIG_KEY_THEME_ENABLED, false)
    }

    fun isDebug(): Boolean {
        return isDebug
    }

    fun applyTheme(path: String?) {
        if (isEnabled()) {
            if (TextUtils.isEmpty(path)) {
                ThemeResourcesManager.reset()
                changeThemPath("")
            } else {
                try {
                    val assetManager = AssetManager::class.java.newInstance()
                    val method =
                        assetManager.javaClass.getMethod("addAssetPath", String::class.java)
                    method.isAccessible = true
                    method.invoke(assetManager, path)
                    val context = MultiLanguageHelper.modifyContextLanguageConfig(
                        mApplication, MultiLanguageHelper.getLanguageConfig(mApplication)
                    )
                    val resources = context.resources
                    val skinRes =
                        Resources(assetManager, resources.displayMetrics, resources.configuration)
                    //获取外部Apk(皮肤包) 包名
                    val packageName = mApplication.packageManager.getPackageArchiveInfo(
                        path!!,
                        PackageManager.GET_ACTIVITIES
                    )?.packageName
                    if (!TextUtils.isEmpty(packageName)) {
                        ThemeResourcesManager.apply(skinRes, packageName)
                        changeThemPath(path)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            setChanged()
            notifyObservers()
        }
    }

    private fun changeThemPath(path: String?) {
        Config.writeConfig(CONFIG_KEY_CURRENT_THEME, path ?: "")
    }
}