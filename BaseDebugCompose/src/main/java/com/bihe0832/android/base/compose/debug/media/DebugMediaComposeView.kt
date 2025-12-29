package com.bihe0832.android.base.compose.debug.media

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.text.Layout
import android.text.TextUtils
import androidx.compose.runtime.Composable
import com.bihe0832.android.base.compose.debug.audio.DebugAudioComposeView
import com.bihe0832.android.base.compose.debug.audio.DebugAudioWaveView
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.common.compose.debug.item.DebugComposeFragmentItem
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.media.MediaTools
import com.bihe0832.android.common.video.FFmpegTools
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.file.AAFFileWrapper.getTempImagePath
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.TextToImageUtils
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil
import com.bihe0832.android.lib.utils.time.DateUtil


private val LOG_TAG = "DebugComposeCacheView "
private val audio = "audio.mp3"
private val audioPath = AAFFileWrapper.getMediaTempFolder() + audio
private var videoPath: String? = null
private var imagePath: String? = null
private var lastPath: String = ""


@Composable
fun DebugMediaComposeView() {
    DebugContent {
        DebugComposeItem("音频播放", "DebugComposeAudioView") { DebugAudioComposeView() }
        DebugComposeItem("音频波形", "DebugAudioWaveView") { DebugAudioWaveView() }
        DebugComposeFragmentItem("拍照、相册、裁剪调试", DebugPhotosFragment::class.java)
        DebugItem("文字转图片1（有图标，有标题）") { textToImage(it) }
        DebugItem("文字转图片2（无图标，无标题）") { textToImage2(it) }
        DebugItem("音频转视频1") { audioToVideo(it, 1) }
        DebugItem("音频转视频2") { audioToVideo(it, 2) }
        DebugItem("音频图片转视频1") { audioImageToVideo(it, 1) }
        DebugItem("音频图片转视频2") { audioImageToVideo(it, 2) }
        DebugItem("图片无损存图库") { saveImage(it) }
        DebugItem("视频图片存图库") { save(it) }
        DebugItem("下载图片并添加到相册") { testDownImage(it) }
        DebugItem("下载视频并添加到相册") { testDownVideo(it) }
        DebugItem("从相册删除最后一次的照片") { testDelete(it) }
    }
}

private fun testDownVideo(context: Context) {
    DownloadFile.download(
        context,
        "https://vfx.mtime.cn/Video/2018/11/09/mp4/181109123910577905.mp4",
        object : SimpleDownloadListener() {
            override fun onProgress(item: DownloadItem) {
            }

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
            }

            override fun onComplete(filePath: String, item: DownloadItem): String {
                videoPath = filePath
                save(context)
                return filePath
            }
        },
    )
}

private fun testDownImage(context: Context) {
    DownloadFile.download(
        context,
        "https://cdn.bihe0832.com/images/cv_v.png",
        object : SimpleDownloadListener() {
            override fun onProgress(item: DownloadItem) {
            }

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
            }

            override fun onComplete(filePath: String, item: DownloadItem): String {
                imagePath = filePath
                save(context)
                return filePath
            }
        },
    )
}

private fun saveImage(context: Context) {
    imagePath = AAFFileWrapper.getTempImagePath()
    FileUtils.copyAssetsFileToPath(context, "cv_v.jpg", imagePath!!)
    save(context)
}

private fun save(context: Context) {
    if (!TextUtils.isEmpty(videoPath)) {
        lastPath = Media.addToPhotos(context, videoPath, "zixie", true)
    }

    if (!TextUtils.isEmpty(imagePath)) {
        lastPath = Media.addToPhotos(context, imagePath, "zixie", true)
    }
}

private fun audioToVideo(context: Context, type: Int) {
    AAFFileWrapper.clear()
    FileUtils.copyAssetsFileToPath(context, audio, audioPath)
    if (type == 1) {
        MediaTools.convertAudioWithTextToVideo(
            context,
            audioPath,
            "这是一个测试" + DateUtil.getCurrentDateEN(),
            object : AAFDataCallback<String>() {
                override fun onSuccess(result: String?) {
                    ZixieContext.showToast("转换成功，已经添加到相册")
                    if (!TextUtils.isEmpty(result)) {
                        videoPath = result
                        save(context)
                    }
                }
            },
        )
    } else {

        val width = 720
        val height = 1280
        val imageData = MediaTools.createImageFromText(
            context, "这是一个测试" + DateUtil.getCurrentDateEN(), width, height
        )
        val imagePath = BitmapUtil.saveBitmapWithPath(
            imageData, getTempImagePath(".jpg")
        )
        MediaTools.convertAudioWithImageToVideo(imagePath,
            audioPath,
            object : AAFDataCallback<String>() {
                override fun onSuccess(result: String?) {
                    ZixieContext.showToast("转换成功，已经添加到相册")
                    if (!TextUtils.isEmpty(result)) {
                        videoPath = result
                        save(context)
                    }
                }
            })
    }
}

private fun audioImageToVideo(context: Context, type: Int) {

    val width = 720
    val height = 720

    AAFFileWrapper.clear()
    textToImage(context)
    val IMAGE = "cv_v.jpg"
    val cv_v = AAFFileWrapper.getMediaTempFolder() + IMAGE
    FileUtils.copyAssetsFileToPath(context, IMAGE, cv_v)
    FileUtils.copyAssetsFileToPath(context, audio, audioPath)
    val imagePath1 = BitmapUtil.transImageFileToRequiredSize(
        cv_v,
        AAFFileWrapper.getMediaTempFolder() + System.currentTimeMillis() + ".jpg",
        width,
        height,
        Color.YELLOW
    )
    val imagePath2 = BitmapUtil.transImageFileToRequiredSize(
        imagePath ?: "",
        AAFFileWrapper.getMediaTempFolder() + System.currentTimeMillis() + ".jpg",
        width,
        height,
        Color.YELLOW
    )
    try {

        val textNum = 100L * 1000
        if (type == 1) {
            FFmpegTools.convertAudioWithImageToVideo(
                width,
                height,
                audioPath,
                textNum / 50,
                mutableListOf<String>().apply {
                    add(imagePath1)
                    add(imagePath2)
                    add(imagePath1)
                },
                object : AAFDataCallback<String>() {
                    override fun onSuccess(result: String?) {
                        lastPath = Media.addToPhotos(context, result)
                    }
                },
            )
        } else {
            FFmpegTools.convertAudioWithImageToVideo(
                width,
                height,
                audioPath,
                textNum / 50,
                mutableListOf<String>().apply {
                    add(cv_v)
                    add(imagePath ?: "")
                    add(cv_v)
                },
                object : AAFDataCallback<String>() {
                    override fun onSuccess(result: String?) {
                        lastPath = Media.addToPhotos(context, result)
                    }
                },
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun textToImage(context: Context) {
    TextToImageUtils.createImageFromText(
        context,
        720,
        280,
        "#000000",
        16,
        BitmapUtil.getLocalBitmap(ZixieContext.applicationContext, ResR.mipmap.icon, 1),
        18,
        DateUtil.getCurrentDateEN(),
        "#FFFFFF",
        10,
        1f,
        Layout.Alignment.ALIGN_NORMAL,
        10,
        "这是一段非常长的文字这是一段非常长的文字这是一段非常长的文字",
        "#FF00FF",
        10,
        1.25f,
        3,
        Layout.Alignment.ALIGN_NORMAL,
        12,
    )?.let {
        imagePath = BitmapUtil.saveBitmap(ZixieContext.applicationContext, it)
        Media.addToPhotos(ZixieContext.applicationContext, imagePath)
        Media.addToPhotos(
            ZixieContext.applicationContext, BitmapUtil.saveBitmap(
                ZixieContext.applicationContext, BitmapUtil.resizeAndCenterBitmap(
                    it, 300, 500, Color.TRANSPARENT, Matrix.ScaleToFit.CENTER
                )
            )
        )
        Media.addToPhotos(
            ZixieContext.applicationContext, BitmapUtil.saveBitmap(
                ZixieContext.applicationContext, BitmapUtil.resizeAndCenterBitmap(
                    it, 1300, 500, Color.TRANSPARENT, Matrix.ScaleToFit.FILL
                )
            )
        )
        Media.addToPhotos(
            ZixieContext.applicationContext, BitmapUtil.saveBitmap(
                ZixieContext.applicationContext, BitmapUtil.resizeAndCenterBitmap(
                    it, 2300, 500, Color.TRANSPARENT, Matrix.ScaleToFit.START
                )
            )
        )
        Media.addToPhotos(
            ZixieContext.applicationContext, BitmapUtil.saveBitmap(
                ZixieContext.applicationContext, BitmapUtil.resizeAndCenterBitmap(
                    it, 300, 1500, Color.TRANSPARENT, Matrix.ScaleToFit.END
                )
            )
        )
        Media.addToPhotos(
            ZixieContext.applicationContext, BitmapUtil.saveBitmap(
                ZixieContext.applicationContext, BitmapUtil.resizeAndCenterBitmap(
                    it, 300, 2500, Color.TRANSPARENT, Matrix.ScaleToFit.CENTER
                )
            )
        )
        Media.addToPhotos(
            ZixieContext.applicationContext, BitmapUtil.saveBitmap(
                ZixieContext.applicationContext, BitmapUtil.resizeAndCenterBitmap(
                    it, 1300, 1500, Color.TRANSPARENT, Matrix.ScaleToFit.CENTER
                )
            )
        )
        Media.addToPhotos(
            ZixieContext.applicationContext, BitmapUtil.saveBitmap(
                ZixieContext.applicationContext, BitmapUtil.resizeAndCenterBitmap(
                    it, 1300, 2500, Color.TRANSPARENT, Matrix.ScaleToFit.CENTER
                )
            )
        )
        save(context)
    }
}

private fun textToImage2(context: Context) {
    TextToImageUtils.createImageFromText(
        context,
        720,
        1280,
        "#000000",
        16,
        null,
        0,
        "",
        "#FFFFFF",
        10,
        1f,
        Layout.Alignment.ALIGN_NORMAL,
        0,
        "这是一段非常长的文字这是一段非常长的文字这是一段非常长的文字",
        "#FF00FF",
        10,
        1.25f,
        3,
        Layout.Alignment.ALIGN_NORMAL,
        12,
    )?.let {
        imagePath = BitmapUtil.saveBitmap(ZixieContext.applicationContext, it)
        save(context)
    }
}

private fun testDelete(context: Context) {
    Media.removeFromPhotos(context, lastPath)

}