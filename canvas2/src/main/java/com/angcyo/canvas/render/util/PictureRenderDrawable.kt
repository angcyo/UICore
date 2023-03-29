package com.angcyo.canvas.render.util

import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.drawable.PictureDrawable

/**
 * 不支持跟随Bounds的变化进行Scale
 * [android.graphics.Picture.beginRecording]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class PictureRenderDrawable(picture: Picture?) : PictureDrawable(picture) {

    init {
        picture?.let { setBounds(0, 0, it.width, it.height) }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
    }
}