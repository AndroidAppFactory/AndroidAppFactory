package com.bihe0832.android.base.debug.image

import android.text.Layout
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.media.MediaTools
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.BitmapUtil
import com.bihe0832.android.lib.media.image.TextToImageUtils
import com.bihe0832.android.lib.utils.time.DateUtil

class DebugMediaFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    val audio = "audio.mp3"
    val audioPath = AAFFileWrapper.getMediaTempFolder() + audio

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("图片操作调试", DebugImageFragment::class.java))
            add(DebugItemData("文字转图片1（有图标，有标题）", View.OnClickListener { textToImage() }))
            add(DebugItemData("文字转图片2（无图标，无标题）", View.OnClickListener { textToImage2() }))
            add(DebugItemData("音频转视频", View.OnClickListener { audioToVideo() }))
            add(DebugItemData("图片无损存图库", View.OnClickListener { saveImage() }))
        }
    }

    private fun saveImage() {
        val path = AAFFileWrapper.getTempImagePath()
        FileUtils.copyAssetsFileToPath(context, "MVIMG_20230921_211547.jpg", path)
        Media.addPicToPhotos(context, path)
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
                        Media.addVideoToPhotos(context, result)
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
            Media.addPicToPhotos(context, BitmapUtil.saveBitmap(ZixieContext.applicationContext, it))
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
            Media.addPicToPhotos(context, BitmapUtil.saveBitmap(ZixieContext.applicationContext, it))
        }
    }
}
