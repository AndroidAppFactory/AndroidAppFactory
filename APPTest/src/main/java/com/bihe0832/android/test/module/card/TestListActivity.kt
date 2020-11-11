package com.bihe0832.android.test.module.card

import com.bihe0832.android.framework.ui.list.CommonListActivity
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.test.module.card.section.SectionDataContent
import com.bihe0832.android.test.module.card.section.SectionDataContent2
import com.bihe0832.android.test.module.card.section.SectionDataHeader
import com.bihe0832.android.test.module.card.section.SectionDataHeader2
const val ROUTRT_NAME_TEST_SECTION= "testlist"
@Module(ROUTRT_NAME_TEST_SECTION)
class TestListActivity : CommonListActivity() {
    val mDataList by lazy {

        ArrayList<CardBaseModule>().apply {
            for (i in 0..5) {
                add(if (i < 2) {
                    SectionDataHeader("标题1:$i")
                } else {
                    SectionDataHeader2("标题2:$i")
                })
                for (j in 0..14) {
                    add(if (i < 2) {
                        SectionDataContent("内容1:$j")
                    } else {
                        SectionDataContent2("内容2:$j")
                    })
                }
            }
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun fetchData() {
                postValue(mDataList)
            }

            override fun clearData() {
                mDataList.clear()
            }

            override fun loadMore() {

            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun canRefresh(): Boolean {
                return false
            }

            override fun getEmptyText(): String {
                return ""
            }
        }
    }

    override fun getCardList(): List<Class<out CardBaseModule?>> {
        return mutableListOf(
                SectionDataHeader::class.java,
                SectionDataHeader2::class.java,
                SectionDataContent::class.java,
                SectionDataContent2::class.java

        )
    }

    override fun getTitleText(): String {
        return "Section测试"
    }
}