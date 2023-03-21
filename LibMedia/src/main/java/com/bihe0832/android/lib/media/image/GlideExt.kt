package com.bihe0832.android.lib.media.image

import android.graphics.Color
import android.widget.ImageView
import com.bihe0832.android.lib.log.ZLog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory

/**
 * 如果需要同时多中心效果叠加，需要配合 MultiTransformation 使用  loadImage 接口
 */
fun ImageView.loadCircleCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadCircleCropImage(url, placeholder, error, true)
}

/**
 * circleCrop():圆形裁剪
 */
fun ImageView.loadCircleCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder, needFade: Boolean) {
    loadImage(url, 0, 0, placeholder, error, needFade, RequestOptions().optionalCircleCrop())
}

fun ImageView.loadRoundCropImage(url: String, radius: Int, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadRoundCropImage(url, radius, placeholder, error, true)
}

/**
 * RoundedCorners 圆角
 */
fun ImageView.loadRoundCropImage(url: String, radius: Int, placeholder: Int = Color.GRAY, error: Int = placeholder, needFade: Boolean) {
    loadImage(url, 0, 0, placeholder, error, needFade, RequestOptions.bitmapTransform(RoundedCorners(radius)))
}

fun ImageView.loadCenterCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadCenterCropImage(url, placeholder, error, true)
}

/**
 * centerCrop()：以填满整个控件为目标,等比缩放,超过控件时将被 裁剪 ( 宽高都要填满 ,所以只要图片宽高比与控件宽高比不同时,一定会被剪裁)
 */
fun ImageView.loadCenterCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder, needFade: Boolean) {
    loadImage(url, 0, 0, placeholder, error, needFade, RequestOptions().optionalCenterCrop())
}

fun ImageView.loadCenterInsideImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadCenterInsideImage(url, placeholder, error, true)
}

/**
 * centerInside()：以完整显示图片为目标, 不剪裁 ,当显示不下的时候将缩放,能够显示的情况下不缩放
 */
fun ImageView.loadCenterInsideImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder, needFade: Boolean) {
    loadImage(url, 0, 0, placeholder, error, needFade, RequestOptions().optionalCenterInside())
}

fun ImageView.loadFitCenterImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadFitCenterImage(url, placeholder, error, true)
}

/**
 * fitCenter()：将图片按照原始的长宽比充满全屏
 */
fun ImageView.loadFitCenterImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder, needFade: Boolean) {
    loadImage(url, 0, 0, placeholder, error, needFade, RequestOptions().optionalFitCenter())
}

fun ImageView.loadImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, 0, 0, placeholder, error, true)
}

fun ImageView.loadImage(url: String, width: Int, height: Int, placeholder: Int = Color.GRAY, error: Int = placeholder, needFade: Boolean) {
    loadImage(url, width, height, placeholder, error, needFade, RequestOptions())
}

fun ImageView.loadCircleCropImage(resId: Int) {
    loadImage(resId, 0, 0, RequestOptions().optionalCircleCrop())
}

fun ImageView.loadCenterCropImage(resId: Int) {
    loadImage(resId, 0, 0, RequestOptions().optionalCenterCrop())
}

fun ImageView.loadCenterInsideImage(resId: Int) {
    loadImage(resId, 0, 0, RequestOptions().optionalCenterInside())
}

fun ImageView.loadImage(resId: Int) {
    loadImage(resId, 0, 0, RequestOptions())
}

fun ImageView.loadRoundCropImage(resId: Int, radius: Int) {
    loadImage(resId, 0, 0, RequestOptions.bitmapTransform(RoundedCorners(radius)))
}

fun ImageView.loadImage(url: String, width: Int, height: Int, placeholder: Int, error: Int, needFade: Boolean, requestOptions: RequestOptions = RequestOptions()) {
    loadImage(url, placeholder, error, width, height, needFade, DiskCacheStrategy.NONE, false, requestOptions)
}

fun ImageView.loadImage(url: String, placeholder: Int, error: Int, width: Int, height: Int, needFade: Boolean, strategy: DiskCacheStrategy, skipMemoryCache: Boolean, requestOptions: RequestOptions = RequestOptions()) {
    ZLog.d("Glide", "load image:$url")
    requestOptions.apply {
        downsample(DownsampleStrategy.CENTER_INSIDE)//将图片缩小至目标大小
        diskCacheStrategy(strategy)//换成变换后的图片
        skipMemoryCache(skipMemoryCache)
        placeholder(placeholder)
        error(error)
    }

    if (width > 0) {
        if (height > 0) {
            requestOptions.override(width, height)
        } else {
            requestOptions.override(width)
        }
    }
    try {
        Glide.with(this.context).apply {
            if (url.endsWith("gif")) {
                asGif()
            }
        }.load(url).apply {
            if (needFade) {
                transition(withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                thumbnail(0.3f)
            } else {
                requestOptions.dontAnimate()
            }
        }.apply(requestOptions).into(this)
    } catch (e: Exception) {
        e.printStackTrace()
        loadImage(error)
    }
}

fun ImageView.loadImage(resId: Int, width: Int, height: Int, requestOptions: RequestOptions = RequestOptions()) {
    requestOptions.dontAnimate()
    try {
        if (width > 0) {
            if (height > 0) {
                requestOptions.override(width, height)
            } else {
                requestOptions.override(width)
            }
        }
        Glide.with(this.context).load(resId).apply(requestOptions).into(this)
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            setImageResource(resId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun ImageView.clearImage() {
    try {
        Glide.with(this.context).clear(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}