package com.bihe0832.android.lib.gson

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-18.
 * Description: Description
 */
object JsonHelper {

    /**
     * 增加后台返回""和"null"的处理
     * 1.int=>0
     * 2.double=>0.00
     * 3.long=>0L
     *
     * @return
     */
    fun getGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(Double::class.java, DoubleDefaultAdapter())
                .registerTypeAdapter(Double::class.javaPrimitiveType, DoubleDefaultAdapter())
                .registerTypeAdapter(Boolean::class.java, DoubleDefaultAdapter())
                .registerTypeAdapter(Boolean::class.javaPrimitiveType, DoubleDefaultAdapter())
                .registerTypeAdapter(Float::class.java, FloatDefaultAdapter())
                .registerTypeAdapter(Float::class.javaPrimitiveType, FloatDefaultAdapter())
                .registerTypeAdapter(Int::class.java, IntegerDefaultAdapter())
                .registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDefaultAdapter())
                .registerTypeAdapter(Long::class.java, LongDefaultAdapter())
                .registerTypeAdapter(Long::class.javaPrimitiveType, LongDefaultAdapter())
                .registerTypeAdapter(String::class.java, StringNullAdapter())
                .create()
    }

    /**
     * 将bean类转为json字符串
     *
     * @param <T> bean的类型
     * @param beanObject bean类
     * @return json的String格式
     *
     * i.e: String json = JsonHelper.toJson(beanObject);
    </T> */
    fun <T> toJson(beanObject: T): String? {
        try {
            return getGson().toJson(beanObject)
        } catch (e: Exception) {
            Log.e("JsonHelper","JsonParserWrapper toJson error:$e")
        }

        return null
    }

    /**
     * 将json字符串转为bean类
     *
     * @param <T> bean的类型
     * @param json json的String格式
     * @param beanClass bean的Class类型
     * @return T类型的bean类
     *
     * i.e: BeanClass beanClass = JsonHelper.fromJson(json, BeanClass.class);
    </T> */
    fun <T> fromJson(json: String, beanClass: Class<T>): T? {
        try {
            return getGson().fromJson(json, beanClass)
        } catch (e: Exception) {
            Log.e("JsonHelper","JsonParserWrapper fromJson error:$e")
        }

        return null
    }
}
