package com.bihe0832.android.framework.request

import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.encypt.MD5

/**
 *
 * 文件下载及校验
 */

class ZixieRequestFile {

    private val TAG = "[MnaRequestFile] -> "
    private val CONFIG_SUFFIX_VALUE = "_value"

    // 云控字段
    private var configValue: String = ""

    // 云控字段的默认值
    private var defaultValue: String = ""

    // 下载文件是否检查MD5
    private var forceCheckMD5: Boolean = true

    // 下载文件是否保存本地
    private var saveDataToLocal: Boolean = true

    interface ResponseHandler {
        companion object {
            const val SUCCESS = 0
            const val ERROR_CONFIG = -1
            const val ERROR_CDN_MD5 = -2
            const val ERROR_NETWORK = -3
            const val ERROR_DATA_EMPTY = -4

            //云端重新拉取
            const val TYPE_NEW = 1

            //使用本地缓存
            const val TYPE_NEW_LOCAL = 2

            //云端重新拉取的默认值
            const val TYPE_DEFAULT = 3

            //使用本地缓存的默认值
            const val TYPE_DEFAULT_LOCAL = 4
        }

        fun onSuccess(type: Int, response: String)
        fun onFailed(errorCode: Int, msg: String)
    }

    fun get(configValue: String, defaultValue: String, callback: ResponseHandler) {
        get(configValue, defaultValue, true, callback)
    }

    fun get(configValue: String, defaultValue: String, forceCheckMD5: Boolean, callback: ResponseHandler) {
        get(configValue, defaultValue, forceCheckMD5, true, callback)
    }

    fun get(configValue: String, defaultValue: String, forceCheckMD5: Boolean, saveToLocal: Boolean, callback: ResponseHandler) {
        this.configValue = configValue
        this.defaultValue = defaultValue
        this.forceCheckMD5 = forceCheckMD5
        this.saveDataToLocal = saveToLocal
        execute(callback)
    }

    private fun execute(callback: ResponseHandler) {
        var configValueArray = configValue.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val configUrl = ConvertUtils.getSafeValueFromArray(configValueArray, 0, "")
        val configMd5 = ConvertUtils.getSafeValueFromArray(configValueArray, 1, "")

        if (configUrl.isNullOrEmpty()) {
            // 云端URL错误，尝试拉取默认数据
            ZLog.e(TAG, "cloud config error, url or md5 is empty")
            loadDefaultValue(callback)
        } else {
            // MD5 无效且校验MD5，拉取默认数据
            if (configMd5.isNullOrEmpty() && forceCheckMD5) {
                loadDefaultValue(callback)
            } else {
                loadAndSaveData(configUrl, configMd5, false, object : ResponseHandler {
                    override fun onSuccess(type: Int, response: String) {
                        // 云端数据拉取成功
                        callback.onSuccess(type, response)
                    }

                    override fun onFailed(errorCode: Int, msg: String) {
                        // 云端数据拉取失败，尝试拉取默认数据
                        loadDefaultValue(callback)
                    }
                })
            }
        }
    }

    private fun loadDefaultValue(callback: ResponseHandler) {
        var defaultValueArray = defaultValue.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val defaultUrl = ConvertUtils.getSafeValueFromArray(defaultValueArray, 0, "")
        val defaultMd5 = ConvertUtils.getSafeValueFromArray(defaultValueArray, 1, "")

        if (defaultUrl.isNullOrEmpty()) {
            // 所有的URL 配置都错误
            ZLog.e(TAG, "default config error, url is empty")
            callback.onFailed(ResponseHandler.ERROR_CONFIG, "default config error, url is empty: $configValue; $defaultValue")
        } else {
            // MD5 无效且校验MD5，拉取默认数据
            if (defaultMd5.isNullOrEmpty() && forceCheckMD5) {
                ZLog.e(TAG, "default config error, md5 is empty")
                callback.onFailed(ResponseHandler.ERROR_CONFIG, "default config error, md5 is empty: $configValue; $defaultValue")
            } else {
                loadAndSaveData(defaultUrl, defaultMd5, true, object : ResponseHandler {
                    override fun onSuccess(type: Int, response: String) {
                        // 默认数据拉取成功
                        callback.onSuccess(type, response)
                    }

                    override fun onFailed(errorCode: Int, msg: String) {
                        // 默认数据拉取失败，彻底失败
                        callback.onFailed(errorCode, msg)
                    }
                })
            }
        }
    }

    private fun loadAndSaveData(url: String, fileMD5: String, isDefault: Boolean, callback: ResponseHandler) {
        val localData = if (saveDataToLocal) {
            Config.readConfig(url + CONFIG_SUFFIX_VALUE, "")
        } else {
            ""
        }
        if (localData.isNotEmpty()) {
            if (!forceCheckMD5) {
                callback.onSuccess(getType(isDefault, true), localData)
            } else {
                if (MD5.getMd5(localData) == fileMD5) {
                    ZLog.d(TAG, " use local data")
                    callback.onSuccess(getType(isDefault, true), localData)
                } else {
                    fetchDataAndSave(url, fileMD5, object : ResponseHandler {
                        override fun onSuccess(type: Int, response: String) {
                            callback.onSuccess(getType(isDefault, false), localData)
                        }

                        override fun onFailed(errorCode: Int, msg: String) {
                            callback.onFailed(errorCode, msg)
                        }

                    })
                }
            }
        } else {
            fetchDataAndSave(url, fileMD5, object : ResponseHandler {
                override fun onSuccess(type: Int, response: String) {
                    callback.onSuccess(getType(isDefault, false), localData)
                }

                override fun onFailed(errorCode: Int, msg: String) {
                    callback.onFailed(errorCode, msg)
                }
            })
        }
    }

    private fun getType(isDefault: Boolean, isLocal: Boolean): Int {
        return if (isDefault) {
            if (isLocal) {
                ResponseHandler.TYPE_DEFAULT_LOCAL
            } else {
                ResponseHandler.TYPE_DEFAULT
            }
        } else {
            if (isLocal) {
                ResponseHandler.TYPE_NEW_LOCAL
            } else {
                ResponseHandler.TYPE_NEW
            }
        }
    }

    private fun fetchDataAndSave(url: String, configMD5: String, callback: ResponseHandler) {
        ZixieRequestHttp.get(url, HttpResponseHandler { statusCode, response ->
            if (statusCode != 200) {
                ZLog.d(TAG, "data error")
                callback.onFailed(ResponseHandler.ERROR_NETWORK, "data error, $statusCode")
            } else if (response.isNullOrEmpty()) {
                ZLog.d(TAG, "data error")
                callback.onFailed(ResponseHandler.ERROR_DATA_EMPTY, "data error, $statusCode")
            } else {
                if (forceCheckMD5) {
                    // 校验下载内容的md5
                    val dataMd5 = MD5.getMd5(response)
                    if (dataMd5 != configMD5) {
                        // 文件md5与配置的md5值不相同
                        ZLog.d(TAG, "cdn file md5 error")
                        callback.onFailed(ResponseHandler.ERROR_CDN_MD5, "cdn file md5 is error")
                    } else {
                        ZLog.d("$TAG update cdn")
                        if (saveDataToLocal) {
                            Config.writeConfig(url + CONFIG_SUFFIX_VALUE, response)
                        }
                        callback.onSuccess(ResponseHandler.TYPE_NEW, response)
                    }
                } else {
                    if (saveDataToLocal) {
                        Config.writeConfig(url + CONFIG_SUFFIX_VALUE, response)
                    }
                    callback.onSuccess(ResponseHandler.TYPE_NEW, response)
                }
            }
        })
    }
}