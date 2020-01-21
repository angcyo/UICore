package com.angcyo.glide

import android.app.Activity
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.IdRes
import com.angcyo.library.L
import com.angcyo.widget.DslViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

fun DslViewHolder.giv(@IdRes id: Int): GlideImageView? {
    return v(id)
}

fun ImageView.load(url: String?, configRequest: RequestBuilder<Drawable>.() -> Unit = {}) {
    if (url.isNullOrBlank()) {
        L.w("url is null or blank")
    } else {
        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

        val view = this
        Glide.with(this).apply {
            load(url).apply {
                when (scaleType) {
                    ImageView.ScaleType.CENTER_CROP -> centerCrop()
                    ImageView.ScaleType.CENTER_INSIDE -> centerInside()
                    ImageView.ScaleType.CENTER -> centerInside()
                    ImageView.ScaleType.FIT_CENTER -> fitCenter()
                    else -> {
                    }
                }
                if (view.measuredWidth > 0 && view.measuredHeight > 0) {
                    override(view.measuredWidth, view.measuredHeight)
                }
                configRequest()
                into(view)
            }
        }
    }
}