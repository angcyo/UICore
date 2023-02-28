package com.angcyo.canvas.render.util

import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class PictureRenderDrawable(picture: Picture?) : PictureDrawable(picture) {
    override fun draw(canvas: Canvas) {
        //super.draw(canvas)
        picture?.let { canvas.drawPicture(it) }
    }
}