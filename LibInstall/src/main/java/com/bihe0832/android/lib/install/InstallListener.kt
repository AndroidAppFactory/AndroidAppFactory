package com.bihe0832.android.lib.install

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-09.
 * Description: Description
 *
 */
interface InstallListener {
    fun onUnCompress()
    fun onInstallPrepare()
    fun onInstallStart()
    fun onInstallFailed(errorCode: Int)
    fun onInstallSuccess()
    fun onInstallTimeOut()
}
