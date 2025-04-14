package com.bihe0832.android.lib.gson

import android.util.Log
import com.bihe0832.android.lib.gson.adapter.*
import com.bihe0832.android.lib.gson.type.ParameterizedTypeImpl
import com.bihe0832.android.lib.log.ZLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-18.
 * Description: Description
 */
object JsonHelper {

    fun getGson(): Gson {
        return getGson(false)
    }

    fun getGson(needPrettyPrinting: Boolean): Gson {
        return getGsonBuilder().apply {
            if (needPrettyPrinting) {
                setPrettyPrinting()
            }
        }.disableHtmlEscaping().create()
    }

    fun getGsonBuilder(): GsonBuilder {
        return GsonBuilder().registerTypeAdapter(Double::class.java, DoubleDefaultAdapter())
            .registerTypeAdapter(Double::class.javaPrimitiveType, DoubleDefaultAdapter())
            .registerTypeAdapter(Boolean::class.java, BooleanTypeAdapter())
            .registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanTypeAdapter())
            .registerTypeAdapter(Float::class.java, FloatDefaultAdapter())
            .registerTypeAdapter(Float::class.javaPrimitiveType, FloatDefaultAdapter())
            .registerTypeAdapter(Int::class.java, IntegerDefaultAdapter())
            .registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDefaultAdapter())
            .registerTypeAdapter(Long::class.java, LongDefaultAdapter())
            .registerTypeAdapter(Long::class.javaPrimitiveType, LongDefaultAdapter())
            .registerTypeAdapter(String::class.java, StringNullAdapter())
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
        return toJson(beanObject, false)
    }

    fun <T> toJson(beanObject: T, needPrettyPrinting: Boolean): String? {
        return toJson(getGson(needPrettyPrinting), beanObject)
    }

    fun <T> toJson(gson: Gson, beanObject: T): String? {
        try {
            return gson.toJson(beanObject)
        } catch (e: Exception) {
            Log.e("JsonHelper", "JsonParserWrapper toJson error:$e")
        }

        return null
    }

    fun toMap(beanObject: Any): Map<*, *>? {
        val json = toJson(beanObject) ?: return null
        return toMap(json)
    }

    fun toMap(data: String): Map<*, *>? {
        return try {
            getGson(false).fromJson(data, object : TypeToken<Map<*, *>>() {}.type)
        } catch (e: Exception) {
            ZLog.e("JsonHelper", "------------------------------------")
            ZLog.e("JsonHelper", "JsonParserWrapper fromJson error:$e")
            ZLog.e("JsonHelper", "JsonParserWrapper json:$data")
            ZLog.e("JsonHelper", "------------------------------------")
            null
        }
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
     */
    fun <T> fromJson(json: String, beanClass: Class<T>): T? {
        return fromJson(getGson(false), json, beanClass)
    }

    /**
     * 将json字符串转为bean类
     *
     * @param <T> bean的类型
     * @param json json的String格式
     * @param responseClass 泛型的外层Class类型
     * @param contentClass 泛型的内层Class类型
     * @return T类型的bean类
     *
     * i.e: BeanClass beanClass = JsonHelper.fromJson(json, BeanClass.class);
     */
    open fun <R, T> fromJson(json: String?, responseClass: Class<R>, contentClass: Class<T>): R? {
        try {
            TypeToken.getParameterized(responseClass, contentClass).type.let {
                return getGson(false).fromJson(json, it)
            }
        } catch (e: Exception) {
            ZLog.e("JsonHelper", "------------------------------------")
            ZLog.e("JsonHelper", "JsonParserWrapper fromJson error:$e")
            ZLog.e("JsonHelper", "JsonParserWrapper json:$json")
            ZLog.e("JsonHelper", "JsonParserWrapper responseClass:$responseClass")
            ZLog.e("JsonHelper", "JsonParserWrapper contentClass:$contentClass")
            ZLog.e("JsonHelper", "------------------------------------")
        }
        return null
    }

    fun <T> fromJson(gson: Gson, json: String, beanClass: Class<T>): T? {
        try {
            return gson.fromJson(json, beanClass)
        } catch (e: Exception) {
            ZLog.e("JsonHelper", "------------------------------------")
            ZLog.e("JsonHelper", "JsonParserWrapper fromJson error:$e")
            ZLog.e("JsonHelper", "JsonParserWrapper json:$json")
            ZLog.e("JsonHelper", "JsonParserWrapper beanClass:$beanClass")
            ZLog.e("JsonHelper", "------------------------------------")
        }

        return null
    }

    /**
     * 将json字符串转为bean类 的 List
     *
     * @param <T> bean的类型
     * @param json json的String格式
     * @param beanClass bean的Class类型
     * @return List<T> bean类的List
     *
     * i.e: List<BeanClass>  beanClass = JsonHelper.fromJsonList(json, BeanClass.class);
     */
    fun <T> fromJsonList(json: String, clazz: Class<T>): List<T>? {
        return fromJsonList(getGson(false), json, clazz)
    }

    fun <T> fromJsonList(gson: Gson, json: String, clazz: Class<T>): List<T>? {
        try {
            val type: Type = ParameterizedTypeImpl(clazz)
            return gson.fromJson(json, type)
        } catch (e: Exception) {
            ZLog.e("JsonHelper", "------------------------------------")
            ZLog.e("JsonHelper", "JsonParserWrapper list fromJson Exception:\n")
            ZLog.e("JsonHelper", "\t $e \n")
            ZLog.e("JsonHelper", "JsonParserWrapper list json:$json")
            ZLog.e("JsonHelper", "JsonParserWrapper list beanClass:$clazz")
            ZLog.e("JsonHelper", "------------------------------------")
            try {
                ZLog.e("JsonHelper", "------------------------------------")
                ZLog.e("JsonHelper", "JsonParserWrapper start parse list fromJsonArray")
                val result = mutableListOf<T>()
                var jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    fromJson(jsonArray.get(i).toString(), clazz).let {
                        if (null == it) {
                            ZLog.e("JsonHelper", "JsonParserWrapper parse list result is null")
                            ZLog.e(
                                "JsonHelper",
                                "JsonParserWrapper parse list result is null:" + jsonArray.get(i)
                                    .toString()
                            )
                            ZLog.e(
                                "JsonHelper",
                                "JsonParserWrapper parse list result is null:$clazz"
                            )
                        } else {
                            result.add(it)
                        }
                    }
                }
                ZLog.e("JsonHelper", "------------------------------------")
                if (result.size != jsonArray.length()) {
                    ZLog.e(
                        "JsonHelper",
                        "JsonParserWrapper parse list :result is ${result.size}, but jsonArray is ${jsonArray.length()}"
                    )
                }
                return result
            } catch (e: java.lang.Exception) {
                ZLog.e("JsonHelper", "------------------------------------")
                ZLog.e(
                    "JsonHelper",
                    "JsonParserWrapper start parse list fromJsonArray Exception:\n"
                )
                ZLog.e("JsonHelper", "\t $e \n")
                ZLog.e("JsonHelper", "------------------------------------")
            }
        }
        return mutableListOf()
    }
}
