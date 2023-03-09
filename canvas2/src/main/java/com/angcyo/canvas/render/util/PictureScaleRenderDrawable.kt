package com.angcyo.canvas.render.util

import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.os.Build

/**
 * 支持缩放
 * [PictureRenderDrawable]
 * [com.pixplicity.sharp.SharpDrawable]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/08
 */
class PictureScaleRenderDrawable(picture: Picture?) : PictureDrawable(picture) {

    private var scaleX = 1f
    private var scaleY = 1f

    override fun draw(canvas: Canvas) {
        //super.draw(canvas)
        picture?.let { picture ->
            val clipRect = bounds
            canvas.apply {
                save(canvas)
                canvas.clipRect(clipRect)
                canvas.scale(scaleX, scaleY, 0f, 0f)
                canvas.drawPicture(picture)
                canvas.restore()
            }
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val picture = picture
        if (picture != null) {
            val width = right - left
            val height = bottom - top
            scaleX = width.toFloat() / picture.width.toFloat()
            scaleY = height.toFloat() / picture.height.toFloat()
        } else {
            scaleX = 1f
            scaleY = 1f
        }
        super.setBounds(left, top, right, bottom)
    }

    private fun save(canvas: Canvas) {
        if (alpha == 255 || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            canvas.save()
        } else {
            canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), alpha)
        }
    }
}