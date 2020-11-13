package com.bihe0832.android.test.module.card

import com.bihe0832.android.framework.ui.list.CommonListActivity
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.test.module.card.section.SectionDataContent
import com.bihe0832.android.test.module.card.section.SectionDataContent2
import com.bihe0832.android.test.module.card.section.SectionDataHeader
import com.bihe0832.android.test.module.card.section.SectionDataHeader2

const val ROUTRT_NAME_TEST_SECTION = "testlist"

@Module(ROUTRT_NAME_TEST_SECTION)
class TestListActivity : CommonListActivity() {
    val mDataList = ArrayList<CardBaseModule>()
    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun fetchData() {
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun clearData() {
                mDataList.clear()
            }

            override fun loadMore() {
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun hasMore(): Boolean {
                return true
            }

            override fun canRefresh(): Boolean {
                return true
            }

            override fun getEmptyText(): String {
                return ""
            }
        }
    }

    private fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            for (i in 0..2) {
                add(if (i < 2) {
                    SectionDataHeader("标题1:${System.currentTimeMillis()}")
                } else {
                    SectionDataHeader2("标题2:${System.currentTimeMillis()}")
                })
                for (j in 0..3) {
                    add(if (i < 2) {
                        SectionDataContent("内容1:${System.currentTimeMillis()}")
                    } else {
                        SectionDataContent2("内容2:${System.currentTimeMillis()}")
                    })
                }
            }
        }
    }

    override fun getTitleText(): String {
        return "Section测试"
    }
}