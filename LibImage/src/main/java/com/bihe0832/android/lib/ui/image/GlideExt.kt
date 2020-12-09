package com.bihe0832.android.lib.ui.image

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


fun ImageView.loadCircleCropImage(resId: Int) {
    loadImage(resId, RequestOptions().circleCrop())
}

fun ImageView.loadCircleCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, placeholder, error, RequestOptions().circleCrop())
}

fun ImageView.loadRoundCropImage(url: String, radius: Int, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, placeholder, error, RequestOptions.bitmapTransform(RoundedCorners(radius)))
}

fun ImageView.loadCenterCropImage(resId: Int) {
    loadImage(resId, RequestOptions().centerCrop())
}

fun ImageView.loadCenterCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, placeholder, error, RequestOptions().centerCrop())
}

fun ImageView.loadCenterInsideImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, placeholder, error, RequestOptions().centerInside())
}

fun ImageView.loadFitCenterImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, placeholder, error, RequestOptions().fitCenter())
}

fun ImageView.loadImage(resId: Int) {
    loadImage(resId, RequestOptions())
}

fun ImageView.loadImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, placeholder, error, RequestOptions())
}

fun ImageView.loadImage(url: String, placeholder: Int, error: Int, requestOptions: RequestOptions = RequestOptions()) {
    loadImage(url, placeholder, error, 0, 0, requestOptions)
}

fun ImageView.loadImage(url: String, placeholder: Int, error: Int, width: Int, height: Int, requestOptions: RequestOptions = RequestOptions()) {
    ZLog.d("Glide", "load image:$url")
    requestOptions
            .downsample(DownsampleStrategy.CENTER_INSIDE)//将图片缩小至目标大小
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)//换成变换后的图片
            .placeholder(placeholder)
            .error(error)
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
        }.load(url)
                .thumbnail(0.2f)
                .transition(withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                .apply(requestOptions)
                .into(this)
    } catch (e: Exception) {
        e.printStackTrace()
        loadImage(error)
    }
}

fun ImageView.loadImage(resId: Int, requestOptions: RequestOptions = RequestOptions()) {
    requestOptions.centerCrop()
    try {
        Glide.with(this.context).load(resId).thumbnail(0.2f)
                .transition(withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                .apply(requestOptions)
                .into(this)
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            setImageResource(resId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun ImageView.clear() {
    try {
        Glide.with(this.context).clear(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}