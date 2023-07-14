package com.bihe0832.android.services.google.pay

import android.icu.util.Currency
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/7/14.
 * Description: Description
 *
 */

//用map存储，便于匹配
private val currencyArrayMap: ConcurrentHashMap<String, String> by lazy {
    ConcurrentHashMap<String, String>().apply {
        for (availableCurrency in Currency.getAvailableCurrencies()) {
            //currentcyCode为key,货币符号为value
            //对于没有特定符号的货币，symbol与currencyCode相同。
            put(availableCurrency.getCurrencyCode(), availableCurrency.getSymbol())
        }
    }
}

fun replaceCurrencySymbol(priceStr: String, currencyCode: String, currencyLocalSymbol: String?): String? {
    var priceStr = priceStr
    if (priceStr.startsWith("$")) {
        if (currencyLocalSymbol != null) {
            if (currencyLocalSymbol == currencyCode) {
                //没有货币符号的情况，把货币码拼接到前面
                priceStr = currencyLocalSymbol + priceStr
            } else {
                if (!priceStr.startsWith(currencyLocalSymbol)) {
                    priceStr = priceStr.replace("$", currencyLocalSymbol)
                }
            }
        }
    }
    return priceStr
}

fun getCurrencyLocalCode(googleCurrencyCode: String): String {
    return if (!currencyArrayMap.contains(googleCurrencyCode) || currencyArrayMap.get(googleCurrencyCode) == null) {
        googleCurrencyCode
    } else {
        currencyArrayMap.get(googleCurrencyCode) ?: ""
    }
}





