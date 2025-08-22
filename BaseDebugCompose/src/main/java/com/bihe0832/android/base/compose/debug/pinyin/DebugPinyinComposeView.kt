package com.bihe0832.android.base.compose.debug.pinyin


import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.chinese.ChineseHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.pinyin.PinYinWithTone
import com.bihe0832.android.lib.pinyin.PinyinFormat

@Composable
fun DebugPinyinComposeView() {
    DebugContent {
        DebugItem("文字转拼音") { testPinyin() }
        DebugItem("中文简繁体转换") { testTraditional() }
    }
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
                it, "-", PinyinFormat.WITH_TONE_MARK, false
            )
        )
        ZLog.d(
            "$it Convert to Pinyin：" + PinYinWithTone.toPinYin(
                it, " ", PinyinFormat.WITH_TONE_MARK, false
            )
        )
        ZLog.d(
            "$it Convert to Pinyin：" + PinYinWithTone.toPinYin(
                it, " ", PinyinFormat.WITH_TONE_NUMBER, false
            )
        )
        ZLog.d(
            "$it Convert to Pinyin：" + PinYinWithTone.toPinYin(
                it, " ", PinyinFormat.WITHOUT_TONE, false
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