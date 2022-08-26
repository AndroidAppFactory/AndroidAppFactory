/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.convert


import android.util.Base64
import android.view.View
import com.bihe0832.android.base.debug.json.IntegerDebugAdapter
import com.bihe0832.android.base.debug.json.JsonTest
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.gson.JsonHelper.fromJsonList
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.MathUtils
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.encrypt.ZlibUtil
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.utils.time.TimeUtil
import java.math.BigDecimal
import java.math.RoundingMode


class DebugConvertFragment : DebugEnvFragment() {
    val LOG_TAG = "DebugTempFragment"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("JsonHelper", View.OnClickListener { testJson() }))
            add(DebugItemData("Boolean 转化", View.OnClickListener { testConvertBoolean() }))
            add(DebugItemData("Float 转化", View.OnClickListener { testConvertFloat() }))

            add(DebugItemData("数据百分比转化", View.OnClickListener { testPercent() }))
            add(DebugItemData("数据格式化", View.OnClickListener { testFormat() }))
            add(DebugItemData("版本号比较", View.OnClickListener { testVersion() }))


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

        var list = "[" +
                "{\"key\": \"value1\",\"value1\": [1222,2222],\"value\":true}," +
                "{\"key\": 2,\"value1\": [1222,2222],\"value2\":1}," +
                "{\"key\": 3,\"value1\": [1222,2222],\"value2\":\"true\"}," +
                "{\"key\": 4,\"value1\": [1222,2222],\"value2\":\"1\"}," +
                "{\"key\": 5,\"value1\": [1222,2222],\"value2\":\"0\"}," +
                "{\"key\": 6,\"value1\": [1222,2222],\"value2\":false}," +
                "{\"key\": 7,\"value1\": [1222,2222],\"value2\":0}," +
                "{\"key\": 8,\"value1\": [1222,2222],\"value2\":\"false\"}" +
                "]"

        list.let {
            ZLog.d(LOG_TAG, "result:" + fromJsonList<JsonTest>(it, JsonTest::class.java))
            ZLog.d(LOG_TAG, "result:" + fromJsonList<JsonTest>(JsonHelper.getGson(), it, JsonTest::class.java))
            ZLog.d(LOG_TAG, "result:" + fromJsonList<JsonTest>(JsonHelper.getGsonBuilder().apply {
                registerTypeAdapter(Int::class.java, IntegerDebugAdapter())
                registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDebugAdapter())
            }.create(), it, JsonTest::class.java))
        }


        JsonTest().apply {
            key = 1212
        }.let {
            ZLog.d(LOG_TAG, "result:" + JsonHelper.toJson(it))
        }

    }

    fun testConvertBoolean() {
        mutableListOf(
                "1",
                "-1",
                "-1",
                "0",
                "233",
                "true",
                "tRUe",
                "false",
                "False"
        ).forEach { data ->
            ZLog.d("testConvert", data + " result is:" + ConvertUtils.parseBoolean(data, false))
            ZLog.d("testConvert", data + " result is:" + ConvertUtils.parseBoolean(data, true))
        }
    }

    private fun testFormat() {

        mutableListOf(
                1645771904111,
                1345775904112,
                1625775904313,
                1645775304114,
                1645772904115,
                1645772404116
        ).forEach { data ->
            ZLog.d(
                    "testDateUtil",
                    "$data trans result is:" + DateUtil.getDateCompareResult(data.toLong())
            )
            ZLog.d(
                    "testDateUtil",
                    "$data trans result is:" + DateUtil.getDateCompareResult1(data.toLong())
            )
            ZLog.d("testDateUtil", "$data trans result is:" + DateUtil.getDateCompareResult2(data))
        }

        mutableListOf(1, 37, 67, 2434, 24064, 2403564).forEach {
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong())
            )
            ZLog.d(
                    "formatSecondsTo00", "Value $it trans to :" + TimeUtil.formatSecondsTo00(
                    it.toLong(),
                    false,
                    false,
                    false
            )
            )
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong(), false, false, true)
            )
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong(), true, true, false)
            )
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong(), true, true, true)
            )
        }
        mutableListOf(1, 37, 67, 2434, 3600, 3602, 24064, 86400, 86440, 2403564).forEach {
            ZLog.d(
                    "formatSecondsToDurationDesc",
                    "Value $it trans to :" + TimeUtil.formatSecondsToDurationDesc(context, it.toLong())
            )
        }

        mutableListOf(2.6, 2.699, 2.722, 2.70, 2.73888888).forEach {
            ZLog.d("BigDecimal", "$it to ${BigDecimal(it).setScale(2, RoundingMode.DOWN)}")
        }

    }

    private fun testConvertFloat() {
        ZLog.d("3 " + "3".toFloat() + " " + ConvertUtils.parseFloat("3", 0f))
        ZLog.d("3 " + "3".toDouble() + " " + ConvertUtils.parseDouble("3", 0.0))
        ZLog.d("3.6 " + "3.6".toFloat() + " " + ConvertUtils.parseFloat("3.6", 0f))
        ZLog.d("0.6 " + "0.6".toFloat() + " " + ConvertUtils.parseFloat("0.6.1", 0f))
        ZLog.d("0.61 " + "0.61".toFloat() + " " + ConvertUtils.parseFloat("0.61", 0f))
        ZLog.d("3.6 " + "3.6".toDouble() + " " + ConvertUtils.parseDouble("3.6", 0.0))
        ZLog.d("0.6 " + "0.6".toDouble() + " " + ConvertUtils.parseDouble("0.6.1", 0.0))
        ZLog.d("0.61 " + "0.61".toDouble() + " " + ConvertUtils.parseDouble("0.61", 0.0))


    }

    private fun testZlib() {
        val builder = StringBuilder()
        for (i in 0..50) {
            builder.append('a' + (TextFactoryUtils.getRandomString(26)))
        }
        val text = builder.toString()

        val compres = ZlibUtil.compress(text.toByteArray())
        ZLog.d("testZlib", "compres 前后： " + compres.size + " : " + text.toByteArray().size)

        val b = Base64.encode(ZlibUtil.compress(text.toByteArray()), Base64.DEFAULT)
        val uncompressResult = String(ZlibUtil.uncompress(Base64.decode(b, Base64.DEFAULT)))


        val res = String(ZlibUtil.uncompress(compres))
        ZLog.d("testZlib", "压缩再解压一致性确认：")
        ZLog.d("testZlib", "text：\n$text\n\n")
        ZLog.d("testZlib", "result：\n$res\n\n")

    }


    fun testPercent() {
        testPercentItem(MathUtils.getFormatPercent(1, 1, 4))
        testPercentItem(MathUtils.getFormatPercent(3, 1, 4))
        testPercentItem(MathUtils.getFormatPercent(3, 0, 4))
        testPercentItem(MathUtils.getFormatPercent(0, 3, 4))
        testPercentItem(MathUtils.getFormatPercent(1L, 3L, 4))
        testPercentItem(MathUtils.getFormatPercent(212121212121212121, 32121212121212121, 4))
        testPercentItem(MathUtils.getFormatPercent(4344343.43434, 4344343.43434, 4))
        testPercentItem(MathUtils.getFormatPercent(43443434343434343.43434, 434434343434343.43434, 4))
    }

    private fun testPercentItem(data: Float) {
        data.let {
            ZLog.d("testPercent", data.toString() + " " + MathUtils.getFormatPercentDesc(data))
        }
    }


    private fun testVersion() {
        val v1 = "1.0.1"
        val v2 = "1.0.02"
        val v2_1 = "1.0.002.1"
        val v2_2 = "1.0.2.02"
        val v3 = "1.0.3"

        ZLog.d("testVerion", "v1 VS v1:" + APKUtils.compareVersion(v1, v1))
        ZLog.d("testVerion", "v1 VS v2:" + APKUtils.compareVersion(v1, v2))
        ZLog.d("testVerion", "v2 VS v1:" + APKUtils.compareVersion(v2, v1))
        ZLog.d("testVerion", "v2_1 VS v1:" + APKUtils.compareVersion(v2_1, v1))
        ZLog.d("testVerion", "v2_1 VS v2:" + APKUtils.compareVersion(v2_1, v2))
        ZLog.d("testVerion", "v2_2 VS v2_1:" + APKUtils.compareVersion(v2_2, v2_1))
        ZLog.d("testVerion", "v3 VS v2:" + APKUtils.compareVersion(v3, v2))
        ZLog.d("testVerion", "v3 VS v2_2:" + APKUtils.compareVersion(v3, v2_2))

    }


}