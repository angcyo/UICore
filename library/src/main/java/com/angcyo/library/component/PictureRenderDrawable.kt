package com.angcyo.library.component

import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.drawable.PictureDrawable
import android.os.Build

/**
 * 默认不支持跟随Bounds的变化进行Scale
 * [PictureScaleRenderDrawable]
 * [android.graphics.Picture.beginRecording]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class PictureRenderDrawable(picture: Picture?) : PictureDrawable(picture) {

    /**是否要激活scale, 这样会缩放/放大以适应[getBounds]*/
    var enableScale: Boolean = false

    private var scaleX = 1f
    private var scaleY = 1f

    init {
        picture?.let { setBounds(0, 0, it.width, it.height) }
    }

    override fun draw(canvas: Canvas) {
        if (enableScale && scaleX != 1f && scaleY != 1f) {
            //需要缩放
            picture?.let { picture ->
                val rect = bounds
                canvas.apply {
                    save(canvas)
                    canvas.clipRect(rect)
                    canvas.translate(rect.left.toFloat(), rect.top.toFloat())
                    canvas.scale(scaleX, scaleY, 0f, 0f)
                    canvas.drawPicture(picture)
                    canvas.restore()
                }
            }
        } else {
            super.draw(canvas)
        }
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || alpha == 255) {
            canvas.save()
        } else {
            canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), alpha)
        }
    }
}