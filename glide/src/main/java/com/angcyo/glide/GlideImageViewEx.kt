package com.angcyo.glide

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.IdRes
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