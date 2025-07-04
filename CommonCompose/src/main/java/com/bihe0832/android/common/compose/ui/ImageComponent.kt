package com.bihe0832.android.common.compose.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bihe0832.android.framework.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition


@Composable
fun loadPicture(url: String, placeholder: Painter? = null): Painter {
    var state by remember { mutableStateOf(placeholder) }
    val context = LocalContext.current
    val result = object : SimpleTarget<Bitmap>() {
        override fun onLoadCleared(p: Drawable?) {
            state = placeholder
        }

        override fun onResourceReady(
            resource: Bitmap,
            transition: Transition<in Bitmap>?,
        ) {
            state = BitmapPainter(resource.asImageBitmap())
        }
    }
    if (state == placeholder) {
        try {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .into(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return state ?: painterResource(R.color.transparent)
}