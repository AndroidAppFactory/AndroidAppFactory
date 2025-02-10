/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.pinyin

import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.chinese.ChineseHelper
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.pinyin.PinYinWithTone
import com.bihe0832.android.lib.pinyin.PinyinFormat

class DebugPinYinFragment : DebugEnvFragment() {
    val TAG = this.javaClass.simpleName


    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("文字转拼音", View.OnClickListener { testPinyin() }))
            add(getDebugItem("中文简繁体转换", View.OnClickListener { testTraditional() }))
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        ChineseHelper.init(context!!)
        PinYinWithTone.init(context!!)
    }


    private fun testPinyin() {

        mutableListOf(
            "阿珂没有闪现",
            "妲己没闪现",
            "大司命没有技能",
            "齦齬齪齲齷龍龜",
            "成都",
            "朝阳区",
            "甄嬛传坦",
            "乌拉特前旗"
        ).forEach {
            ZLog.d("===============")
            ZLog.d(
                "$it Convert to Pinyin：" + PinYinWithTone.toPinYin(
                    it, "-", PinyinFormat.WITH_TONE_MARK,false
                )
            )
            ZLog.d(
                "$it Convert to Pinyin：" + PinYinWithTone.toPinYin(
                    it, " ", PinyinFormat.WITH_TONE_MARK,false
                )
            )
            ZLog.d(
                "$it Convert to Pinyin：" + PinYinWithTone.toPinYin(
                    it, " ", PinyinFormat.WITH_TONE_NUMBER,false
                )
            )
            ZLog.d(
                "$it Convert to Pinyin：" + PinYinWithTone.toPinYin(
                    it, " ", PinyinFormat.WITHOUT_TONE,false
                )
            )
            ZLog.d("===============")
        }
    }


    fun testTraditional() {
        "齦齬齪齲齷龍龜".let {
            ZLog.d(it + " Convert to Chinest：" + ChineseHelper.containsChinese(it))
            ZLog.d(it + " Convert to Chinest：" + ChineseHelper.convertToSimplifiedChinese(it))
            ZLog.d(
                it + " Convert to Chinest：" + ChineseHelper.convertToTraditionalChinese(
                    ChineseHelper.convertToSimplifiedChinese(it)
                )
            )
        }
    }
}


