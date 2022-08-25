/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.file


import android.view.View
import com.bihe0832.android.app.file.AAFFileUtils
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.config.OnConfigChangedListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.action.FileAction
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.zip.ZipUtils
import java.io.File


class DebugFileFragment : DebugEnvFragment() {
    val LOG_TAG = "DebugFileFragment"

    val configListener = object : OnConfigChangedListener {
        override fun onValueChanged(key: String?, value: String?) {
            ZLog.d(LOG_TAG, "onNewValue config key: $key value: $value")
        }

        override fun onValueAgain(key: String?, value: String?) {
            ZLog.d(LOG_TAG, "onValueSetted config key: $key value: $value")
        }

    }

    override fun initView(view: View) {
        super.initView(view)
        Config.addOnConfigChangedListener(configListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Config.removeOnConfigChangedListener(configListener)
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {

            add(DebugItemData("文件及文件夹操作", View.OnClickListener { testFolder() }))

            add(DebugItemData("文本查看器", View.OnClickListener { testEdit() }))
            add(DebugItemData("Assets 操作", View.OnClickListener { testAssets() }))

            add(DebugItemData("文件MD5", View.OnClickListener {
                testMD5()
            }))

            add(DebugItemData("文件选择", View.OnClickListener {
//                activity?.showPhotoChooser()
            }))

            add(DebugItemData("ZIP测试", View.OnClickListener { testZIP() }))
            add(DebugItemData("配置 Config 管理测试", View.OnClickListener { testConfig() }))
        }
    }

    private fun testFolder() {
        mutableListOf<String>(
                "https://voice-file-1300342614.cos.ap-shanghai.myqcloud.com/voice%2FA6FCC3060878E3B121899003F5B42CD5%2Bf82ca891eb9845c3a743f6e64cf95ffe.mp3?q-sign-algorithm=sha1\\u0026q-ak=AKIDCLEqBF2YnmUv5zcy3rOzKODk0zh9KErD\\u0026q-sign-time=1660720054%3B1660727254\\u0026q-key-time=1660720054%3B1660727254\\u0026q-header-list=host\\u0026q-url-param-list=\\u0026q-signature=b49a8fafe797c3c618acb23d93dc74488643bc27",
                "/sdcard/10053761_com.tencent.hjzqgame_h759087_1.0.1306_lcbw83.apk",
                "10053761_com.tencent.hjzqgame_h759087_1.0.1306_lcbw83.apk",
                "/storage/emulated/0/Android/data/com.tencent.cmocmna.video/files/mocmna/temp/log//server_20220818.txt",
                "file://cdn.bihe0832.com/app/update/get_apk.json",
                "https://webcdn.m.qq.com/webapp_myapp/index.html#/"

        ).forEach {
            ZLog.d(LOG_TAG, "===============start==================")
            ZLog.d(LOG_TAG, "source :$it")
            ZLog.d(LOG_TAG, "${FileUtils.getFileName(it)}")
            ZLog.d(LOG_TAG, "${FileUtils.getExtensionName(it)}")
            ZLog.d(LOG_TAG, "${FileUtils.getFileNameWithoutEx(it)}")
            ZLog.d(LOG_TAG, "===============start==================")
        }

        val logPath = AAFLoggerFile.getLogPathByModuleName(AAFLoggerFile.MODULE_UPDATE)
        FileUtils.copyFile(File(logPath), File(AAFFileUtils.getFileTempFolder() + FileUtils.getFileName(logPath))).let {
            ZLog.d(LOG_TAG, "===============$it==================")
        }

        FileUtils.copyDirectory(File(logPath).parentFile, File(AAFFileUtils.getFileTempFolder())).let {
            ZLog.d(LOG_TAG, "===============$it==================")
        }
    }

    private fun testMD5() {
        File("/sdcard/screen.png").let {
            ZLog.d(LOG_TAG, MD5.getFileMD5(it))
            ZLog.d(LOG_TAG, MD5.getFileMD5(it, 0, it.length()))
        }



        ThreadManager.getInstance().start {
            File("/sdcard/10053761_com.tencent.hjzqgame_h759087_1.0.1306_lcbw83.apk").let {
                ZLog.d(LOG_TAG, "===============start==================")
                var start = System.currentTimeMillis() / 1000
                for (i in 0..5) {
                    ZLog.d(LOG_TAG, MD5.getFileMD5(it))
                }

                ZLog.d(LOG_TAG, "total time : " + (System.currentTimeMillis() / 1000 - start))
                ZLog.d(LOG_TAG, "===============end==================")
                ZLog.d(LOG_TAG, "===============start==================")
                start = System.currentTimeMillis() / 1000
                for (i in 0..5) {
                    ZLog.d(LOG_TAG, MD5.getFileMD5(it, 0, it.length()))
                }
                ZLog.d(LOG_TAG, "total time : " + (System.currentTimeMillis() / 1000 - start))
                ZLog.d(LOG_TAG, "===============end==================")

            }
        }
    }

    private fun testZIP() {

        var startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip",
                "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927"
        )
        var duration = System.currentTimeMillis() - startTime
        ZLog.d(
                LOG_TAG,
                "ZipCompressor unzip com.herogame.gplay.lastdayrulessurvival_20200927.zip cost:$duration"
        )

        startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/com.garena.game.kgtw.zip",
                "/sdcard/Download/com.garena.game.kgtw"
        )
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip com.garena.game.kgtw.zip cost:$duration")

        startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/com.supercell.brawlstars.zip",
                "/sdcard/Download/com.supercell.brawlstars"
        )
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip com.supercell.brawlstars.zip cost:$duration")

        startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/jp.co.sumzap.pj0007.zip",
                "/sdcard/Download/jp.co.sumzap.pj0007"
        )
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip jp.co.sumzap.pj0007.zip cost:$duration")
    }

    private fun testConfig() {
        try {
//            var startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.readConfig("test$i", "")
//            }
//            var duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")
//
//            startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.writeConfig("test$i", i.toString())
//            }
//            duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig write 1000 cost:$duration")
//
//            startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.readConfig("test$i", "")
//            }
//            duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
//            Config.writeConfig("A","testconfig")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
//            Config.writeConfig("A","testconfig")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
            var key = "aaa"
            ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
            Config.writeConfig(key, true)
            ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
            Config.writeConfig(key, false)
            ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
            Config.writeConfig(key, Config.isSwitchEnabled(key, false))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun testEdit() {
        RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_EDITOR)
    }

    private fun testAssets() {
        File(ZixieContext.getLogFolder()).listFiles().forEach { file ->
            ZLog.d(LOG_TAG, file.absolutePath)
        }

        ZLog.d(LOG_TAG, ZixieContext.getLogFolder())
        var path = ZixieContext.getLogFolder() + "config.default"
        FileAction.copyAssetsFileToPath(context, "config.default", path).let {
            ZLog.d(LOG_TAG, "result:$it")
            ZLog.d(LOG_TAG, " ${FileUtils.getAssetFileContent(context!!, "config.default")}")
            ZLog.d(LOG_TAG, " ${FileUtils.getFileContent(path)}")
            ZLog.d(LOG_TAG, " $it")
            ZLog.d(LOG_TAG, " " + FileUtils.checkFileExist(path))
            FileUtils.deleteFile(path)
        }

        FileAction.copyAssetsFolderToFolder(context, "", ZixieContext.getLogFolder()).let {
            ZLog.d(LOG_TAG, "result:$it")
            File(ZixieContext.getLogFolder()).listFiles().forEach { file ->
                ZLog.d(LOG_TAG, file.absolutePath)
            }
        }
    }
}