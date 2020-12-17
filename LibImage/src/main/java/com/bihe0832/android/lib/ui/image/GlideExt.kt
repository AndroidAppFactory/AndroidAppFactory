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

fun ImageView.loadCircleCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadCircleCropImage(url, true, placeholder, error)
}

fun ImageView.loadCircleCropImage(url: String, needFade: Boolean, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, needFade, placeholder, error, RequestOptions().circleCrop())
}

fun ImageView.loadRoundCropImage(url: String, radius: Int, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadRoundCropImage(url, radius, true, placeholder, error)
}

fun ImageView.loadRoundCropImage(url: String, radius: Int, needFade: Boolean, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, needFade, placeholder, error, RequestOptions.bitmapTransform(RoundedCorners(radius)))
}

fun ImageView.loadCenterCropImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadCenterCropImage(url, true, placeholder, error)
}

fun ImageView.loadCenterCropImage(url: String, needFade: Boolean, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, needFade, placeholder, error, RequestOptions().centerCrop())
}

fun ImageView.loadCenterInsideImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadCenterInsideImage(url, true, placeholder, error)
}

fun ImageView.loadCenterInsideImage(url: String, needFade: Boolean, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, needFade, placeholder, error, RequestOptions().centerInside())
}

fun ImageView.loadFitCenterImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadFitCenterImage(url, true, placeholder, error)
}

fun ImageView.loadFitCenterImage(url: String, needFade: Boolean, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, needFade, placeholder, error, RequestOptions().fitCenter())
}

fun ImageView.loadImage(url: String, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, true, placeholder, error)
}

fun ImageView.loadImage(url: String, needFade: Boolean, placeholder: Int = Color.GRAY, error: Int = placeholder) {
    loadImage(url, needFade, placeholder, error, RequestOptions())
}


fun ImageView.loadImage(url: String, placeholder: Int, error: Int, requestOptions: RequestOptions = RequestOptions()) {
    loadImage(url, true, placeholder, error, requestOptions)
}

fun ImageView.loadImage(url: String, needFade: Boolean, placeholder: Int, error: Int, requestOptions: RequestOptions = RequestOptions()) {
    loadImage(url, placeholder, error, 0, 0, needFade, DiskCacheStrategy.RESOURCE, requestOptions)
}


fun ImageView.loadImage(url: String, placeholder: Int, error: Int, width: Int, height: Int, needFade: Boolean, strategy: DiskCacheStrategy, requestOptions: RequestOptions = RequestOptions()) {
    ZLog.d("Glide", "load image:$url")
    requestOptions
            .downsample(DownsampleStrategy.CENTER_INSIDE)//将图片缩小至目标大小
            .diskCacheStrategy(strategy)//换成变换后的图片
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

fun ImageView.loadCircleCropImage(resId: Int) {
    loadImage(resId, RequestOptions().circleCrop())
}

fun ImageView.loadCenterCropImage(resId: Int) {
    loadImage(resId, RequestOptions().centerCrop())
}

fun ImageView.loadCenterInsideImage(resId: Int) {
    loadImage(resId, RequestOptions().centerInside())
}


fun ImageView.loadImage(resId: Int) {
    loadImage(resId, RequestOptions())
}


fun ImageView.loadImage(resId: Int, requestOptions: RequestOptions = RequestOptions()) {
    requestOptions.dontAnimate()
    try {
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

fun ImageView.clear() {
    try {
        Glide.with(this.context).clear(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}