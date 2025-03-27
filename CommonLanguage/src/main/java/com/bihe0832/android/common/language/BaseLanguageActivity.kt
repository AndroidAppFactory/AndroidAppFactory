package com.bihe0832.android.common.language

import com.bihe0832.android.common.language.card.SettingsDataLanguage
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieCoreInit
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.language.MultiLanguageHelper
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17. Description: Description
 */

abstract class BaseLanguageActivity : CommonListActivity() {

    abstract fun getLanguageList(): MutableList<SettingsDataLanguage>

    val mDataList = ArrayList<CardBaseModule>()

    override fun supportMultiLanguage(): Boolean {
        return true
    }

    override fun getTitleText(): String {
        return resources.getString(R.string.settings_language_title)
    }

    override fun initView() {
        super.initView()
        mAdapter.apply {
            setOnItemClickListener { baseQuickAdapter, view, i ->
                setLocale((baseQuickAdapter.data[i] as? SettingsDataLanguage))
            }
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun canRefresh(): Boolean {
                return false
            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun initData() {
                mDataList.clear()
                mDataList.addAll(getLanguageList())
                postValue(mDataList)
            }

            override fun loadMore() {

            }

            override fun refresh() {
                initData()
            }
        }
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SettingsDataLanguage::class.java, true))
        }
    }

    open fun setLocale(settingData: SettingsDataLanguage?) {
        if (null != settingData?.locale) {
            updateApplicationLocale(settingData.locale!!)
            onLocaleChanged(getLastLocale(), settingData.locale!!)
        } else {
            ZixieContext.showToast(this@BaseLanguageActivity.resources.getString(R.string.toast_settings_language_tips))
        }
    }

    fun updateApplicationLocale(locale: Locale) {
        ZixieCoreInit.updateApplicationLocale(this, locale)
        MultiLanguageHelper.modifyContextLanguageConfig(resources, locale)
    }

    override fun onLocaleChanged(lastLocale: Locale, newLocale: Locale) {
        super.onLocaleChanged(lastLocale, newLocale)
        iniToolBar()
        mDataLiveData.refresh()
    }
}
