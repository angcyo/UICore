package com.angcyo.glide

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
fun ImageView.loadImage(url: String?, reset: Boolean = true, action: DslGlide.() -> Unit = {}) {
    DslGlide().apply {
        targetView = this@loadImage
        if (reset) {
            reset()
        }
        action()
        load(url)
    }
}