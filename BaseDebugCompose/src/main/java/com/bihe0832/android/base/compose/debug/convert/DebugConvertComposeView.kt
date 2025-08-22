package com.bihe0832.android.base.compose.debug.convert

import android.content.Context
import androidx.compose.runtime.Composable
import com.bihe0832.android.app.api.AAFResponse
import com.bihe0832.android.base.compose.debug.json.JsonTest
import com.bihe0832.android.base.compose.debug.pinyin.DebugPinyinComposeView
import com.bihe0832.android.base.compose.debug.tree.TreeNode
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.MathUtils
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.utils.time.TimeUtil
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Arrays
import java.util.Locale

private const val LOG_TAG = "DebugComposeConvertView"

@Composable
fun DebugConvertComposeView() {
    val taskIDTree = TreeNode("5")

    DebugContent {
        DebugItem("JsonHelper") { testJson() }
        DebugItem("Boolean 转化") { testConvertBoolean() }
        DebugItem("Float 转化") { testConvertFloat() }
        DebugItem("Int Long 与 Byte 数组转化") { testToByte() }

        DebugItem("树结构") { testTree(taskIDTree) }

        DebugItem("数据百分比转化") { testPercent() }
        DebugItem("时间数据格式化") { context -> testFormat(context) }
        DebugItem("版本号比较") { testVersion() }
        DebugComposeItem(
            "中文简繁体、文字转拼音", "DebugComposePinyinView"
        ) { DebugPinyinComposeView() }

        DebugItem("坐标映射") { testPoint() }
    }
}

private fun testToByte() {
    mutableListOf(
        "1",
        "-1",
        "4294967298",
        "100",
        "-100",
        "2147483647",
        "3147483647",
        "3456788147483647",
        "899167231",
        "8589934590",
        "8589934591"
    ).forEach {
        testToByte(it)
    }
}

private fun testToByte(data: String) {
    ZLog.d(LOG_TAG, "==========================================")
    ZLog.d(LOG_TAG, "testToByte data: $data")
    ConvertUtils.parseInt(data).let { intValue ->
        ZLog.d(LOG_TAG, "data parseInt: ${intValue}")
        ZLog.d(
            LOG_TAG,
            "data parseInt intToBytes: ${Arrays.toString(ConvertUtils.intToBytes(intValue))}"
        )
        intValue.toLong().let {
            ZLog.d(LOG_TAG, "parseInt kotlin toLong: $it")
            ZLog.d(
                LOG_TAG,
                "parseInt kotlin toLong longToBytes: ${Arrays.toString(ConvertUtils.longToBytes(it))}"
            )
        }

        ConvertUtils.getUnsignedInt(intValue).let { longValue ->
            ZLog.d(LOG_TAG, "parseInt getUnsignedInt(long): ${longValue}")
            ZLog.d(
                LOG_TAG, "parseInt getUnsignedInt(long) longToBytes: ${
                    Arrays.toString(
                        ConvertUtils.longToBytes(
                            longValue
                        )
                    )
                }"
            )
            try {
                longValue.toInt().let {
                    ZLog.d(LOG_TAG, "parseInt getUnsignedInt(long) kotlin toInt: $it")
                    ZLog.d(
                        LOG_TAG, "parseInt getUnsignedInt(long) kotlin toInt intToBytes: ${
                            Arrays.toString(
                                ConvertUtils.intToBytes(
                                    it
                                )
                            )
                        }"
                    )
                }

                longValue.toUInt().let {
                    ZLog.d(LOG_TAG, "parseInt getUnsignedInt(long) kotlin toUInt: $it")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ConvertUtils.longToIntWithLossOfPrecision(longValue).let {
                ZLog.d(LOG_TAG, "parseInt getUnsignedInt(long) longToIntWithLossOfPrecision: ${it}")
                ZLog.d(
                    LOG_TAG,
                    "parseInt getUnsignedInt(long) longToIntWithLossOfPrecision intToBytes: ${
                        Arrays.toString(
                            ConvertUtils.intToBytes(it)
                        )
                    }"
                )
            }
        }
    }

    ZLog.d(LOG_TAG, "-------------------------------------------")

    ConvertUtils.parseLong(data, -1).let { longValue ->
        ZLog.d(LOG_TAG, "data parseLong: $longValue")
        ZLog.d(
            LOG_TAG,
            "data parseLong longToBytes: ${Arrays.toString(ConvertUtils.longToBytes(longValue))}"
        )
        try {
            longValue.toInt().let {
                ZLog.d(LOG_TAG, "parseLong kotlin toInt: $it")
                ZLog.d(
                    LOG_TAG,
                    "parseLong kotlin toInt intToBytes: ${Arrays.toString(ConvertUtils.intToBytes(it))}"
                )
            }
            longValue.toUInt().let {
                ZLog.d(LOG_TAG, "parseLong kotlin toUInt: $it")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ConvertUtils.longToIntWithLossOfPrecision(longValue).let { intValue ->
            ZLog.d(LOG_TAG, "parseLong longToIntWithLossOfPrecision: $intValue")
            ZLog.d(
                LOG_TAG, "parseLong longToIntWithLossOfPrecision intToBytes: ${
                    Arrays.toString(
                        ConvertUtils.intToBytes(intValue)
                    )
                }"
            )
            try {
                intValue.toLong().let {
                    ZLog.d(LOG_TAG, "parseLong longToIntWithLossOfPrecision kotlin toLong: $it")
                    ZLog.d(
                        LOG_TAG,
                        "parseLong longToIntWithLossOfPrecision kotlin toLong longToBytes: ${
                            Arrays.toString(
                                ConvertUtils.longToBytes(it)
                            )
                        }"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ConvertUtils.getUnsignedInt(intValue).let {
                ZLog.d(
                    LOG_TAG, "parseLong longToIntWithLossOfPrecision getUnsignedInt(long): ${it}"
                )
                ZLog.d(
                    LOG_TAG,
                    "parseLong longToIntWithLossOfPrecision getUnsignedInt(long) longToBytes: ${
                        Arrays.toString(
                            ConvertUtils.longToBytes(it)
                        )
                    }"
                )
            }
        }
        ZLog.d(LOG_TAG, "==========================================")
    }

}

private fun testJson() {
    for (i in 0..100) {
        var start = System.currentTimeMillis()
        ZLog.d(LOG_TAG, "JsonHelper: start $start")
        JsonHelper.getGson().fromJson("{\"key\": 1222}", JsonTest::class.java)
        var end = System.currentTimeMillis()
        ZLog.d(LOG_TAG, "JsonHelper: end $end; duration : ${end - start}")
    }

    var list =
        "[" + "{\"key\": \"value1\",\"value1\": [1222,2222],\"value\":true}," + "{\"key\": 2,\"value1\": [1222,2222],\"value2\":1}," + "{\"key\": 3,\"value1\": [1222,2222],\"value2\":\"true\"}," + "{\"key\": 4,\"value1\": [1222,2222],\"value2\":\"1\"}," + "{\"key\": 5,\"value1\": [1222,2222],\"value2\":\"0\"}," + "{\"key\": 6,\"value1\": [1222,2222],\"value2\":false}," + "{\"key\": 7,\"value1\": [1222,2222],\"value2\":0}," + "{\"key\": 8,\"value1\": [1222,2222],\"value2\":\"false\"}" + "]"

//        list.let {
//            ZLog.d(LOG_TAG, "result:" + fromJsonList<JsonTest>(it, JsonTest::class.java))
//            ZLog.d(LOG_TAG, "result:" + fromJsonList<JsonTest>(JsonHelper.getGson(), it, JsonTest::class.java))
//            ZLog.d(
//                LOG_TAG,
//                "result:" + fromJsonList<JsonTest>(
//                    JsonHelper.getGsonBuilder().apply {
//                        registerTypeAdapter(Int::class.java, IntegerDebugAdapter())
//                        registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDebugAdapter())
//                    }.create(),
//                    it,
//                    JsonTest::class.java,
//                ),
//            )
//        }

    val enerics =
        "{\"content\":{\"key\":\"value1\",\"value1\":[1222,2222],\"value2\":true},\"err_code\":0,\"message\":\"\"}"
    ZLog.d(
        LOG_TAG,
        "result:" + JsonHelper.fromJson(enerics, AAFResponse::class.java, JsonTest::class.java)
    )

    JsonTest().apply {
        key = 1212
        setData3("chat&user")
    }.let {
        ZLog.d(LOG_TAG, "result data:" + JsonHelper.toJson(it))
//            ZLog.d(LOG_TAG, "result JsonTest map:" + JsonTest.getData(it))
        ZLog.d(LOG_TAG, "result JsonHelper map:" + JsonHelper.toMap(it))
    }
    ZLog.d(
        LOG_TAG,
        "String result JsonHelper map:" + JsonHelper.toMap("{\"key\": \"value1\",\"value1\": \"value1\",\"value\":true}")
    )

}

fun testConvertBoolean() {
    mutableListOf("1", "-1", "-1", "0", "233", "true", "tRUe", "false", "False").forEach { data ->
        ZLog.d(LOG_TAG, data + " result is:" + ConvertUtils.parseBoolean(data, false))
        ZLog.d(LOG_TAG, data + " result is:" + ConvertUtils.parseBoolean(data, true))
    }
}

private fun testFormat(context: Context) {
    mutableListOf(
        1645771904111, 1345775904112, 1625775904313, 1645775304114, 1645772904115, 1645772404116
    ).forEach { data ->
        ZLog.d(LOG_TAG, "DateEN $data trans result is:" + DateUtil.getDateEN(data))
        ZLog.d(LOG_TAG, "DateEN $data start is:" + DateUtil.getDayStartTimestamp(data))
        ZLog.d(
            LOG_TAG,
            "DateEN $data start is:" + DateUtil.getDateEN(DateUtil.getDayStartTimestamp(data))
        )
        ZLog.d(
            LOG_TAG,
            "DateEN $data trans result is:" + DateUtil.getCurrentWeekCN(data, "yyyy-MM-dd E HH:mm")
        )
        ZLog.d(
            LOG_TAG,
            "DateCompare $data trans result is:" + DateUtil.getDateCompareResult(data.toLong())
        )
        ZLog.d(
            LOG_TAG,
            "DateCompare $data trans result is:" + DateUtil.getDateCompareResult1(data.toLong())
        )
        ZLog.d(LOG_TAG, "DateCompare $data trans result is:" + DateUtil.getDateCompareResult2(data))
    }

    mutableListOf(1, 37, 67, 2434, 24064, 2403564).forEach {
        ZLog.d(
            LOG_TAG,
            "formatSecondsTo00 Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong())
        )
        ZLog.d(
            LOG_TAG, "formatSecondsTo00 Value $it trans to :" + TimeUtil.formatSecondsTo00(
                it.toLong(), false, false, false
            )
        )
        ZLog.d(
            LOG_TAG, "formatSecondsTo00 Value $it trans to :" + TimeUtil.formatSecondsTo00(
                it.toLong(), false, false, true
            )
        )
        ZLog.d(
            LOG_TAG, "formatSecondsTo00 Value $it trans to :" + TimeUtil.formatSecondsTo00(
                it.toLong(), true, true, false
            )
        )
        ZLog.d(
            LOG_TAG, "formatSecondsTo00 Value $it trans to :" + TimeUtil.formatSecondsTo00(
                it.toLong(), true, true, true
            )
        )
    }
    mutableListOf(1, 37, 67, 2434, 3600, 3602, 24064, 86400, 86440, 2403564).forEach {
        ZLog.d(
            LOG_TAG,
            "formatSecondsTo00 Value $it trans to :" + TimeUtil.formatSecondsToDurationDesc(
                context, it.toLong()
            )
        )
    }

    mutableListOf(2.6, 2.699, 2.722, 2.70, 2.73888888).forEach {
        ZLog.d(LOG_TAG, "BigDecimal $it to ${BigDecimal(it).setScale(2, RoundingMode.DOWN)}")
    }

    mutableListOf("2022-02-25 00:00:00", "2022-02-25 14:51:44", "2012-08-24 00:00:00").forEach {
        ZLog.d(
            LOG_TAG, "Day start $it to ${DateUtil.getDayStartTimestamp(it, "yyyy-MM-dd HH:mm:ss")}"
        )
    }

    mutableListOf("2022-02-25 00:00", "2022-02-25 51:44", "2012-08-24 00:00").forEach {
        ZLog.d(LOG_TAG, "Day start $it to ${DateUtil.getDayStartTimestamp(it, "yyyy-MM-dd mm:ss")}")
    }
}

private fun testConvertFloat() {
    ZLog.d(LOG_TAG, "3 " + "3".toFloat() + " " + ConvertUtils.parseFloat("3", 0f))
    ZLog.d(LOG_TAG, "3 " + "3".toDouble() + " " + ConvertUtils.parseDouble("3", 0.0))
    ZLog.d(LOG_TAG, "3.6 " + "3.6".toFloat() + " " + ConvertUtils.parseFloat("3.6", 0f))
    ZLog.d(LOG_TAG, "0.6 " + "0.6".toFloat() + " " + ConvertUtils.parseFloat("0.6.1", 0f))
    ZLog.d(LOG_TAG, "0.61 " + "0.61".toFloat() + " " + ConvertUtils.parseFloat("0.61", 0f))
    ZLog.d(LOG_TAG, "3.6 " + "3.6".toDouble() + " " + ConvertUtils.parseDouble("3.6", 0.0))
    ZLog.d(LOG_TAG, "0.6 " + "0.6".toDouble() + " " + ConvertUtils.parseDouble("0.6.1", 0.0))
    ZLog.d(LOG_TAG, "0.61 " + "0.61".toDouble() + " " + ConvertUtils.parseDouble("0.61", 0.0))
}

fun testPercent() {
    mutableListOf(0, 1, 2, 3, 4, 5).forEach { data ->
//            testPercentItem(data, MathUtils.getFormatPercent(1, 1, data))
//            testPercentItem(data, MathUtils.getFormatPercent(3, 1, data))
//            testPercentItem(data, MathUtils.getFormatPercent(3, 0, data))
//            testPercentItem(data, MathUtils.getFormatPercent(0, 3, data))
//            testPercentItem(data, MathUtils.getFormatPercent(1L, 3L, data))
        testPercentItem(
            data, MathUtils.getFormatPercent(212121212121212121, 32121212121212121, data)
        )
        testPercentItem(data, MathUtils.getFormatPercent(4344341.43434, 4344343.43434, data))
        testPercentItem(
            data, MathUtils.getFormatPercent(43443434343434340, 43443434343434342, data)
        )
    }
    mutableListOf(0.0f, 1.0f, 0.02f, 0.003f, 0.0004f, 0.00005f, 0.00006f, 1.0f).forEach { data ->
        ZLog.d(LOG_TAG, "testPercent 0: $data " + MathUtils.getFormatPercentDesc(data, 0))
        ZLog.d(LOG_TAG, "testPercent 1: $data " + MathUtils.getFormatPercentDesc(data, 1))
        ZLog.d(LOG_TAG, "testPercent 2: $data " + MathUtils.getFormatPercentDesc(data, 2))
        ZLog.d(LOG_TAG, "testPercent 3: $data " + MathUtils.getFormatPercentDesc(data, 3))
        ZLog.d(LOG_TAG, "testPercent 4: $data " + MathUtils.getFormatPercentDesc(data, 4))
        ZLog.d(LOG_TAG, "testPercent 5: $data " + MathUtils.getFormatPercentDesc(data, 5))
        ZLog.d(LOG_TAG, "testPercent 6: $data " + MathUtils.getFormatPercentDesc(data, 6))
    }
}

private fun testPercentItem(scale: Int, data: Float) {
    data.let {
        ZLog.d(
            LOG_TAG,
            "testPercent ($scale):" + data.toString() + " " + MathUtils.getFormatPercentDesc(
                data, scale
            ),
        )
    }
}

private fun testVersion() {
    val v1 = "1.0.1"
    val v2 = "1.0.02"
    val v2_1 = "1.0.002.1"
    val v2_2 = "1.0.2.02"
    val v3 = "1.0.3"

    ZLog.d(
        LOG_TAG,
        "$v1 VS $v1 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v1, v1
        )
    )
    ZLog.d(
        LOG_TAG,
        "$v1 VS $v2 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v1, v2
        )
    )
    ZLog.d(
        LOG_TAG,
        "$v2 VS $v1 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v2, v1
        )
    )
    ZLog.d(
        LOG_TAG,
        "$v2_1 VS $v1 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v2_1, v1
        )
    )
    ZLog.d(
        LOG_TAG,
        "$v2_1 VS $v2 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v2_1, v2
        )
    )
    ZLog.d(
        LOG_TAG,
        "$v2_2 VS $v2_1 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v2_2, v2_1
        )
    )
    ZLog.d(
        LOG_TAG,
        "$v3 VS $v2 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v3, v2
        )
    )
    ZLog.d(
        LOG_TAG,
        "$v3 VS $v2_2 结果（版本一致 0，oldVersion 更高 1， newVersion 更高 2，无法比较 -1）:" + APKUtils.compareVersion(
            v3, v2_2
        )
    )
}

fun testTree(taskIDTree: TreeNode<String>) {

    taskIDTree.addChild("54")?.apply {
        addChild("42")
        addChild("41")
        addChild("40")
    }
    taskIDTree.addChild("53")?.apply {
        addChild("32")
        addChild("30")
    }
    taskIDTree.addChild("52")?.apply {
        addChild("21")
        addChild("20")
    }
    taskIDTree.addChild("51")?.apply {
        addChild("10")
    }

    taskIDTree.forEach {
        ZLog.e("node is :$it")
    }
}

fun testPoint() {
    mutableListOf(0, 10, 50, 90, 100).forEach {
        transform(it, 0, 100f, 100f, 100f, 100f, true)
        transform(it, 0, 100f, 100f, 100f, 1000f, true)
        transform(it, 0, 100f, 100f, 1000f, 100f, true)
        transform(it, 0, 100f, 100f, 1000f, 1000f, true)

        transform(0, it, 100f, 100f, 100f, 100f, true)
        transform(0, it, 100f, 100f, 100f, 1000f, true)
        transform(0, it, 100f, 100f, 1000f, 100f, true)
        transform(0, it, 100f, 100f, 1000f, 1000f, true)

        transform(it, it, 100f, 100f, 100f, 100f, true)
        transform(it, it, 100f, 100f, 100f, 1000f, true)
        transform(it, it, 100f, 100f, 1000f, 100f, true)
        transform(it, it, 100f, 100f, 1000f, 1000f, true)
    }
}

private fun transform(
    x: Int,
    y: Int,
    srcWidth: Float,
    srcHeight: Float,
    destWidth: Float,
    destHeight: Float,
    isFit: Boolean,
) {
//        val point = PointUtils.transform(x, y, srcWidth, srcHeight, destWidth, destHeight, isFit)
    ZLog.d(
        LOG_TAG, String.format(
            Locale.getDefault(),
            "transform ( %d, %d ): %f,%f | %f,%f",
            x,
            y,
            srcWidth,
            srcHeight,
            destWidth,
            destHeight
        )
    )
//        ZLog.d(
//            LOG_TAG,
//            String.format(
//                Locale.getDefault(), "transform ( %d, %d ) : ( %d, %d )", x, y, point.x, point.y
//            )
//        )
}
