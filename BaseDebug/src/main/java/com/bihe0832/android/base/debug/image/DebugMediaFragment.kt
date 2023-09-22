package com.bihe0832.android.base.debug.image

import android.text.Layout
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.photos.DebugPhotosFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.media.MediaTools
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.BitmapUtil
import com.bihe0832.android.lib.media.image.TextToImageUtils
import com.bihe0832.android.lib.utils.time.DateUtil

class DebugMediaFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    val audio = "audio.mp3"
    val audioPath = AAFFileWrapper.getMediaTempFolder() + audio
    var videoPath: String? = null
    var imagePath: String? = null
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("图片操作调试", DebugImageFragment::class.java))
            add(getDebugFragmentItemData("拍照及相册调试", DebugPhotosFragment::class.java))
            add(DebugItemData("文字转图片1（有图标，有标题）", View.OnClickListener { textToImage() }))
            add(DebugItemData("文字转图片2（无图标，无标题）", View.OnClickListener { textToImage2() }))
            add(DebugItemData("音频转视频", View.OnClickListener { audioToVideo() }))
            add(DebugItemData("图片无损存图库", View.OnClickListener { saveImage() }))
            add(DebugItemData("视频图片存图库", View.OnClickListener { save() }))
            add(DebugItemData("下载图片并添加到相册", View.OnClickListener { testDownImage() }))
            add(DebugItemData("下载视频并添加到相册", View.OnClickListener { testDownVideo() }))
        }
    }

    private fun testDownVideo() {
        DownloadFile.download(
            context!!,
            "https://vfx.mtime.cn/Video/2018/11/09/mp4/181109123910577905.mp4",
            object :
                SimpleDownloadListener() {
                override fun onProgress(item: DownloadItem) {
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    videoPath = filePath
                    save()
                    return filePath
                }
            },
        )
    }

    private fun testDownImage() {
        DownloadFile.download(
            context!!,
            "https://cdn.bihe0832.com/images/cv_v.png",
            object :
                SimpleDownloadListener() {
                override fun onProgress(item: DownloadItem) {
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    imagePath = filePath
                    save()
                    return filePath
                }
            },
        )
    }

    private fun saveImage() {
        imagePath = AAFFileWrapper.getTempImagePath()
        FileUtils.copyAssetsFileToPath(context, "cv_v.jpg", imagePath!!)
        save()
    }

    private fun save() {
        if (!TextUtils.isEmpty(videoPath)) {
            Media.addVideoToPhotos(context, videoPath)
        }

        if (!TextUtils.isEmpty(imagePath)) {
            Media.addPicToPhotos(context, imagePath)
        }
    }

    private fun audioToVideo() {
        FileUtils.copyAssetsFileToPath(context, audio, audioPath)
        MediaTools.convertAudioWithTextToVideo(
            context,
            audioPath,
            "这是一个测试" + DateUtil.getCurrentDateEN(),
            object : AAFDataCallback<String>() {
                override fun onSuccess(result: String?) {
                    ZixieContext.showToast("转换成功，已经添加到相册")
                    if (!TextUtils.isEmpty(result)) {
                        videoPath = result
                        save()
                    }
                }
            },
        )
    }

    private fun textToImage() {
        TextToImageUtils.createImageFromText(
            context,
            720,
            1280,
            "#000000",
            16,
            BitmapUtil.getLocalBitmap(ZixieContext.applicationContext, R.mipmap.icon, 1),
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
            save()
        }
    }

    private fun textToImage2() {
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
            save()
        }
    }
}
