package com.bihe0832.android.common.svga

import android.net.http.HttpResponseCache
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.utils.encrypt.SHA256
import com.bihe0832.android.lib.utils.time.DateUtil
import com.opensource.svgaplayer.*
import com.opensource.svgaplayer.utils.log.SVGALogger
import java.io.File
import java.io.InputStream

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/9/15.
 * Description: Description
 *
 */
object SVGAHelper {

    init {
        shareParser().init(ZixieContext.applicationContext!!)
        SVGALogger.setLogEnabled(!ZixieContext.isOfficial())
        SVGASoundManager.init()
        val cacheDir = File(ZixieContext.getZixieFolder(), "temp/svga")
        FileUtils.deleteOldAsync(cacheDir, DateUtil.MILLISECOND_OF_DAY * 60)
        HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
    }

    fun shareParser(): SVGAParser {
        return SVGAParser.Companion.shareParser()
    }

    fun playInputStream(inputStream: InputStream, key: String, callback: SVGAParser.ParseCompletion) {
        shareParser().decodeFromInputStream(inputStream, key, callback)
    }

    fun playAssets(svgaName: String, callback: SVGAParser.ParseCompletion) {
        try {
            playInputStream(ZixieContext.applicationContext!!.assets.open(svgaName), "assets_" + SHA256.getSHA256("file:///assets/$svgaName"), callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playFile(path: String, callback: SVGAParser.ParseCompletion) {
        if (FileUtils.checkFileExist(path)) {
            try {
                playInputStream(File(path).inputStream(), "file_" + FileUtils.getFileSHA256(path), callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {

        }
    }


    fun playURL(url: String, md5: String, callback: SVGAParser.ParseCompletion) {
        try {
            DownloadFile.forceDownload(ZixieContext.applicationContext!!, url, md5, object : SimpleDownloadListener() {
                override fun onComplete(filePath: String, item: DownloadItem) {
                    playFile(filePath, callback)
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {

                }

                override fun onProgress(item: DownloadItem) {

                }

            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class CommonSVGAParserParseCompletion(private val mSVGAImageView: SVGAImageView?) : SVGAParser.ParseCompletion {
        override fun onComplete(videoItem: SVGAVideoEntity) {
            mSVGAImageView?.setVideoItem(videoItem)
            mSVGAImageView?.startAnimation()
        }

        override fun onError() {

        }
    }
}

fun SVGAImageView.playAssets(svgaName: String) {
    SVGAHelper.playAssets(svgaName, SVGAHelper.CommonSVGAParserParseCompletion(this))
}

fun SVGAImageView.playFile(path: String) {
    SVGAHelper.playFile(path, SVGAHelper.CommonSVGAParserParseCompletion(this))
}

fun SVGAImageView.playURL(url: String) {
    playURL(url, "")
}

fun SVGAImageView.playURL(url: String, md5: String) {
    SVGAHelper.playURL(url, md5, SVGAHelper.CommonSVGAParserParseCompletion(this))
}

fun SVGAImageView.playFileWithClick(path: String, keys: List<String>) {
    SVGAHelper.playFile(path, object : SVGAParser.ParseCompletion {
        override fun onComplete(videoItem: SVGAVideoEntity) {
            val dynamicEntity = SVGADynamicEntity()
            dynamicEntity.setClickArea(keys)
            val drawable = SVGADrawable(videoItem, dynamicEntity)
            setImageDrawable(drawable)
            startAnimation()
        }

        override fun onError() {

        }
    })
}

fun SVGAImageView.playAssetsWithClick(svgaName: String, keys: List<String>) {
    SVGAHelper.playAssets(svgaName, object : SVGAParser.ParseCompletion {
        override fun onComplete(videoItem: SVGAVideoEntity) {
            val dynamicEntity = SVGADynamicEntity()
            dynamicEntity.setClickArea(keys)
            val drawable = SVGADrawable(videoItem, dynamicEntity)
            setImageDrawable(drawable)
            startAnimation()
        }

        override fun onError() {

        }
    })
}