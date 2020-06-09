package com.angcyo.glide

import android.graphics.Color
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.IdRes
import com.angcyo.drawable.base.dslGradientDrawable
import com.angcyo.drawable.text.dslTextDrawable
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.getColor
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

fun DslViewHolder.giv(@IdRes id: Int): GlideImageView? {
    return v(id)
}

/**使用[Glide]加载图片*/
fun ImageView.loadImage(uri: Uri?, action: DslGlide.() -> Unit = {}) {
    DslGlide().apply {
        targetView = this@loadImage
        if (placeholderDrawable == null) {
            this@loadImage.drawable?.also {
                placeholderDrawable = it
            }
        }
        action()
        load(uri)
    }
}

/**core 头像设置*/
fun GlideImageView.loadAvatar(
    url: String?,
    fullName: CharSequence,
    textColor: Int = Color.WHITE,
    fontSizeDp: Float = 14 * dp,
    solidColor: Int = getColor(R.color.colorPrimaryDark)
) {
    val textDrawable = dslTextDrawable(fullName) {
        textSize = fontSizeDp
        this.textColor = textColor
        drawableWidth = -1
        drawableHeight = -1
        textBgDrawable = dslGradientDrawable {
            gradientSolidColor = solidColor
        }
    }
    if (url.isNullOrEmpty()) {
        setImageDrawable(textDrawable)
    } else {
        load(url) {
            placeholderDrawable = textDrawable
            errorDrawable = textDrawable
        }
    }
}