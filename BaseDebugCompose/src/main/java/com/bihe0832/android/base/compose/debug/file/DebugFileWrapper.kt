package com.bihe0832.android.base.compose.debug.file

import android.content.Context
import android.util.Base64
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.file.AAFFileWrapper.getTempFolder
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.FileUtils.copyAssetsFolderToFolder
import com.bihe0832.android.lib.file.FileUtils.getFolderPathWithSeparator
import com.bihe0832.android.lib.file.action.FileAction
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.sqlite.BaseDBHelper
import com.bihe0832.android.lib.sqlite.impl.CommonDBManager
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.MathUtils
import com.bihe0832.android.lib.utils.encrypt.compression.CompressionUtils
import com.bihe0832.android.lib.utils.encrypt.compression.GzipUtils
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.part.DataSegment
import com.bihe0832.android.lib.utils.encrypt.part.DataSegmentTools
import com.bihe0832.android.lib.zip.ZipUtils
import java.io.File





/**
 * 获取文件夹大小
 *
 * @param file File实例
 * @return long
 */
internal fun getFolderSize(file: File): Long {
    var size: Long = 0
    try {
        val fileList = file.listFiles()
        if (fileList != null) {
            for (i in fileList.indices) {
                size = if (fileList[i].isDirectory) size + getFolderSize(fileList[i])
                else size + fileList[i].length()
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return size
}


internal fun testReadAndWrite() {
    val filePath = AAFFileWrapper.getFileTempFolder() + "test.panel"
    ZLog.d(LOG_TAG, "===============start==================")
    ZLog.d(LOG_TAG, "filePath: $filePath")
    "1234567890".let {
        ZLog.d(LOG_TAG, "write : $it")
        FileUtils.writeToFile(filePath, it, false)
    }
    ZLog.d(LOG_TAG, "read : ${FileUtils.getFileContent(filePath)}")
    "1234567890".let {
        ZLog.d(LOG_TAG, "write : $it")
        FileUtils.writeToFile(filePath, it, true)
    }
    ZLog.d(LOG_TAG, "read : ${FileUtils.getFileContent(filePath)}")
    "ABCDEFG".let {
        ZLog.d(LOG_TAG, "write : $it")
        FileUtils.writeToFile(filePath, it, true)
    }
    ZLog.d(LOG_TAG, "read : ${FileUtils.getFileContent(filePath)}")
    "ABCDEFG".let {
        ZLog.d(LOG_TAG, "write : $it")
        FileUtils.writeToFile(filePath, it, false)
    }
    ZLog.d(LOG_TAG, "read : ${FileUtils.getFileContent(filePath)}")
    "1234567890".let {
        ZLog.d(LOG_TAG, "write : $it")
        FileUtils.writeDataToFile(filePath, 0, it.toByteArray(), false)
    }
    ZLog.d(LOG_TAG, "read : ${FileUtils.getFileContent(filePath)}")
    "HIJKLMN".let {
        ZLog.d(LOG_TAG, "write : $it")
        FileUtils.writeDataToFile(filePath, 0, it.toByteArray(), false)
    }
    ZLog.d(LOG_TAG, "read : ${FileUtils.getFileContent(filePath)}")
    "OPQ".let {
        ZLog.d(LOG_TAG, "write : $it")
        FileUtils.writeDataToFile(filePath, 0, it.toByteArray(), true)
    }
    ZLog.d(LOG_TAG, "read : ${FileUtils.getFileContent(filePath)}")
}

internal fun testFolder() {
    mutableListOf<String>(
        "https://voice-file-1300342614.aaa.com/voice%2FA6FCC3060878E3B121899003F5B42CD5%2Bf82ca891eb9845c3a743f6e64cf95ffe.mp3?q-sign-algorithm=sha126q-ak=AKIDCLEqBF2YnmUvwqwqzh9KErD026q-sign-time=1660720w054%3B166107272540026",
        "/sdcard/10053761_com.tencent.hjzqgame_h759087_1.0.1306_lcbw83.apk",
        "10053761_com.tencent.hjzqgame_h759087_1.0.1306_lcbw83.apk",
        "/storage/emulated/0/Android/data/com.tencent.cmocmna.video/files/mocmna/temp/log//server_20220818.txt",
        "file://cdn.bihe0832.com/app/update/get_apk.json",
        "https://webcdn.m.qq.com/webapp_myapp/index.html#/",

        ).forEach {
        ZLog.d(LOG_TAG, "===============start==================")
        ZLog.d(LOG_TAG, "source :$it")
        ZLog.d(LOG_TAG, "${FileUtils.getFileName(it)}")
        ZLog.d(LOG_TAG, "${FileUtils.getExtensionName(it)}")
        ZLog.d(LOG_TAG, "${FileUtils.getFileNameWithoutEx(it)}")
        ZLog.d(LOG_TAG, "===============start==================")
    }

    val logPath = AAFLoggerFile.getLogPathByModuleName(AAFLoggerFile.MODULE_UPDATE)
    FileUtils.copyFile(
        File(logPath), File(AAFFileWrapper.getFileTempFolder() + FileUtils.getFileName(logPath))
    ).let {
        ZLog.d(LOG_TAG, "===============$it==================")
    }

    FileUtils.copyDirectory(File(logPath).parentFile, File(AAFFileWrapper.getFileTempFolder()))
        .let {
            ZLog.d(LOG_TAG, "===============$it==================")
        }
}

internal fun testMD5() {
    File("/sdcard/screen.png").let {
        ZLog.d(LOG_TAG, MD5.getFileMD5(it))
        ZLog.d(LOG_TAG, MD5.getFilePartMD5(it, 0, it.length()))
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
                ZLog.d(LOG_TAG, MD5.getFilePartMD5(it, 0, it.length()))
            }
            ZLog.d(LOG_TAG, "total time : " + (System.currentTimeMillis() / 1000 - start))
            ZLog.d(LOG_TAG, "===============end==================")
        }
    }
}

internal fun testZIP() {
    var startTime = System.currentTimeMillis()
    ZipUtils.unCompress(
        "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip",
        "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927",
    )
    var duration = System.currentTimeMillis() - startTime
    ZLog.d(
        LOG_TAG,
        "ZipCompressor unzip com.herogame.gplay.lastdayrulessurvival_20200927.zip cost:$duration",
    )

    startTime = System.currentTimeMillis()
    ZipUtils.unCompress(
        "/sdcard/Download/com.garena.game.kgtw.zip",
        "/sdcard/Download/com.garena.game.kgtw",
    )
    duration = System.currentTimeMillis() - startTime
    ZLog.d(LOG_TAG, "ZipCompressor unzip com.garena.game.kgtw.zip cost:$duration")

    startTime = System.currentTimeMillis()
    ZipUtils.unCompress(
        "/sdcard/Download/com.supercell.brawlstars.zip",
        "/sdcard/Download/com.supercell.brawlstars",
    )
    duration = System.currentTimeMillis() - startTime
    ZLog.d(LOG_TAG, "ZipCompressor unzip com.supercell.brawlstars.zip cost:$duration")

    startTime = System.currentTimeMillis()
    ZipUtils.unCompress(
        "/sdcard/Download/jp.co.sumzap.pj0007.zip",
        "/sdcard/Download/jp.co.sumzap.pj0007",
    )
    duration = System.currentTimeMillis() - startTime
    ZLog.d(LOG_TAG, "ZipCompressor unzip jp.co.sumzap.pj0007.zip cost:$duration")
}


internal fun testEdit() {
    RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_EDITOR)
}

internal fun testAssets(context: Context) {
    File(ZixieContext.getLogFolder()).listFiles().forEach { file ->
        ZLog.d(LOG_TAG, file.absolutePath)
    }

    ZLog.d(LOG_TAG, ZixieContext.getLogFolder())
    var path = ZixieContext.getLogFolder() + "config.default"
    FileAction.copyAssetsFileToPath(context, "config.default", path).let {
        ZLog.d(LOG_TAG, "result:$it")
        ZLog.d(LOG_TAG,
            " ${FileUtils.getAssetFileContent(context, "config.default")}"
        )
        ZLog.d(LOG_TAG, " ${FileUtils.getFileContent(path)}")
        ZLog.d(LOG_TAG, " $it")
        ZLog.d(LOG_TAG, " " + FileUtils.checkFileExist(path))
        FileUtils.deleteFile(path)
    }
//
//        FileAction.copyAssetsFolderToFolder(context, "", ZixieContext.getLogFolder()).let {
//            ZLog.d(LOG_TAG, "result:$it")
//            File(ZixieContext.getLogFolder()).listFiles().forEach { file ->
//                ZLog.d(LOG_TAG, file.absolutePath)
//            }
//        }

    val tempFolder = getFolderPathWithSeparator(
        getTempFolder("book") + "dsfsdfs",
    )
    copyAssetsFolderToFolder(
        context,
        "imageList" + File.separator + "111" + File.separator,
        tempFolder,
    ).let {
        ZLog.d(LOG_TAG, "result:$it")
        File(tempFolder).listFiles().forEach { file ->
            ZLog.d(LOG_TAG, file.absolutePath)
        }
    }
}

internal fun testDB() {
    System.currentTimeMillis().let {
        CommonDBManager.saveData("sss" + it, "Fsdfsd")
        CommonDBManager.getData("sss" + it).let {
            ZLog.d(BaseDBHelper.TAG, it.toString())
        }
        CommonDBManager.getAll().forEach {
            ZLog.d(BaseDBHelper.TAG, it.toString())
        }
    }
}

internal fun testSegment(dataKey: String, content: ByteArray, size: Int): ByteArray? {
    ZLog.d(LOG_TAG, "=============================")
    ZLog.d(LOG_TAG, "byteArray 数据长度:${content.size}")
    val sorce = DataSegmentTools.splitToDataSegmentList(dataKey, content, size)
    val signatureValue = MD5.getMd5(content)
    val totalLength = content.size
    ZLog.d(LOG_TAG, "DataSegment 源数据 MD5:${signatureValue}")
    ZLog.d(LOG_TAG, "DataSegment 源数据 数据长度:${totalLength}")
    sorce.let {
        ZLog.d(LOG_TAG, "DataSegment 源数据 分片数量:${it.size}")
        ZLog.d(LOG_TAG, "DataSegment 源数据 首片长度:${it.firstOrNull()?.content?.size}")
        ZLog.d(
            LOG_TAG, "DataSegment 源数据 首片数据:${
                String(
                    Base64.encode(
                        it.firstOrNull()?.content, Base64.NO_WRAP
                    )
                )
            }"
        )
    }
    val data = mutableListOf<DataSegment>().apply {
        addAll(sorce.shuffled())
        add(sorce.random())
        add(sorce.random())
    }
    ZLog.d(LOG_TAG, "-----------------------------------------------------")
    data.let {
        ZLog.d(LOG_TAG, "DataSegment 乱序后 分片数量:${it.size}")
        ZLog.d(LOG_TAG, "DataSegment 乱序后 首片长度:${it.firstOrNull()?.content?.size}")
        ZLog.d(
            LOG_TAG, "DataSegment 乱序后 首片数据:${
                String(
                    Base64.encode(
                        it.firstOrNull()?.content, Base64.NO_WRAP
                    )
                )
            }"
        )

    }
    ZLog.d(LOG_TAG, "=============================")
    return DataSegmentTools.mergeDataSegment(
        dataKey, data, totalLength, MD5.MESSAGE_DIGEST_TYPE_MD5, signatureValue
    )
}

internal fun testSegment() {
    val builder = StringBuilder()
    for (i in 0..50) {
        builder.append('a' + (TextFactoryUtils.getRandomString(26)))
    }
    val text = builder.toString()
    ZLog.d(LOG_TAG, "testSegment 原始数据： $text")
    testSegment("key1", text.toByteArray(), 40)?.let {
        ZLog.d(LOG_TAG, "testSegment 再次合并后数据： " + String(it))
    }
    val compres = CompressionUtils.compress(text.toByteArray())
    ZLog.d(
        LOG_TAG,
        "testSegment CompressionUtils compres 前后： " + compres.size + " : " + text.toByteArray().size
    )
    testSegment("key2", compres, 40)?.let {
        ZLog.d(
            LOG_TAG, "testSegment CompressionUtils 再次合并后解压数据： " + String(
                CompressionUtils.uncompress(
                    it
                )
            )
        )
    }

    val gizpData = GzipUtils.compress(text)
    ZLog.d(
        LOG_TAG,
        "testSegment GzipUtils compres 前后： " + gizpData.size + " : " + text.toByteArray().size
    )
    testSegment("key3", gizpData, 40)?.let {
        ZLog.d(
            LOG_TAG,
            "testSegment GzipUtils 再次合并后解压数据： " + GzipUtils.uncompressToString(it)
        )
    }
}

internal fun testZLib() {
    val builder = StringBuilder()
    for (i in 0..50) {
        builder.append('a' + (TextFactoryUtils.getRandomString(26)))
    }
    val text = builder.toString()

    val compres = CompressionUtils.compress(text.toByteArray())
    ZLog.d("testZlib", "compres 前后： " + compres.size + " : " + text.toByteArray().size)

    val b = Base64.encode(CompressionUtils.compress(text.toByteArray()), Base64.DEFAULT)
    val uncompressResult = String(CompressionUtils.uncompress(Base64.decode(b, Base64.DEFAULT)))

    val res = String(CompressionUtils.uncompress(compres))
    ZLog.d("testZlib", "压缩再解压一致性确认：")
    ZLog.d("testZlib", "text：\n$text\n\n")
    ZLog.d("testZlib", "result：\n$res\n\n")
}

internal fun share() {
    var sharedFile = AAFFileWrapper.getShareFolder() + "shared_file.txt"
    ZLog.d("AAF", "File :$sharedFile")
    ZLog.d("AAF", "File Content:" + FileUtils.getFileContent(sharedFile))
    FileUtils.writeToFile(sharedFile, "fsdfs", true)
    ZLog.d("AAF", "File Content:" + FileUtils.getFileContent(sharedFile))
    sharedFile = AAFFileWrapper.getShareFolder() + "shared_file1.txt"
    ZLog.d("AAF", "File :$sharedFile")
    ZLog.d("AAF", "File Content:" + FileUtils.getFileContent(sharedFile))
    FileUtils.writeToFile(sharedFile, "fsdfs", true)
    ZLog.d("AAF", "File Content:" + FileUtils.getFileContent(sharedFile))
}

internal fun testFileLength() {
    mutableListOf<Int>().apply {
        add((1024 * 1204 * 3.514f).toInt())
        for (i in 0..5) {
            add(MathUtils.getRandNumByLimit(0, FileUtils.SPACE_KB.toInt()))
            add(
                MathUtils.getRandNumByLimit(
                    FileUtils.SPACE_KB.toInt(), FileUtils.SPACE_MB.toInt()
                )
            )
            add(
                MathUtils.getRandNumByLimit(
                    FileUtils.SPACE_MB.toInt(), FileUtils.SPACE_GB.toInt()
                )
            )
        }
    }.forEach {
        ZLog.d(
            "AAF", "File length:$it and format to : ${FileUtils.getFileLength(it.toLong(), 0)}"
        )
        ZLog.d(
            "AAF", "File length:$it and format to : ${FileUtils.getFileLength(it.toLong(), 1)}"
        )
        ZLog.d(
            "AAF", "File length:$it and format to : ${FileUtils.getFileLength(it.toLong(), 2)}"
        )
        ZLog.d(
            "AAF", "File length:$it and format to : ${FileUtils.getFileLength(it.toLong(), 3)}"
        )

    }
}

internal fun createFile() {
    val filePath = AAFFileWrapper.getFileTempFolder() + "Temp.file"
    FileUtils.deleteFile(filePath)
    var fileLength = 100L
    ZLog.d("AAF", "File create:${FileUtils.createFile(filePath, fileLength)}")
    fileLength = 200L
    ZLog.d("AAF", "File create:${FileUtils.createFile(filePath, fileLength)}")
}

internal fun modifyFile(replace: Boolean) {
    val file = File(AAFFileWrapper.getFileTempFolder() + "Temp.file")
    FileUtils.deleteFile(file.absolutePath)
    var fileLength = 100L
    var datas = "zixie".encodeToByteArray()

    ZLog.d("AAF", "File create:${FileUtils.createFile(file.absolutePath, fileLength)}")
    ZLog.d(
        "AAF", "File writeDataToFile:${
            FileUtils.writeDataToFile(
                file.absolutePath, 50L, datas, replace
            )
        }"
    )
    readFile(file)
    ZLog.d(
        "AAF", "File writeDataToFile:${
            FileUtils.writeDataToFile(
                file.absolutePath, 98L, datas, replace
            )
        }"
    )
    readFile(file)
    ZLog.d(
        "AAF", "File writeDataToFile:${
            FileUtils.writeDataToFile(
                file.absolutePath, 200L, datas, replace
            )
        }"
    )
    readFile(file)
}

internal fun readFile(file: File) {
    ZLog.d(
        "AAF",
        "File readDataFromFile:${String(FileUtils.readDataFromFile(file.absolutePath, 50L, 5))}"
    )
    ZLog.d(
        "AAF",
        "File readDataFromFile:${String(FileUtils.readDataFromFile(file.absolutePath, 98L, 5))}"
    )
    ZLog.d(
        "AAF", "File readDataFromFile:${
            String(
                FileUtils.readDataFromFile(
                    file.absolutePath, 200L, 5
                )
            )
        }"
    )
    ZLog.d("AAF", "File readDataFromFile:${file.length()}")

}

