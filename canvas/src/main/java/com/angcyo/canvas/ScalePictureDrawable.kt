package com.angcyo.canvas

import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.os.Build

/**
 * 默认的[PictureDrawable] [setBounds]放大缩小之后, 不会进行缩放
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/13
 */
open class ScalePictureDrawable(picture: Picture) : PictureDrawable(picture) {

    /**需要的缩放倍数*/
    var scaleX = 1f
    var scaleY = 1f

    /**缩放控制点*/
    var scalePointX = 0f
    var scalePointY = 0f

    override fun draw(canvas: Canvas) {
        save(canvas)
        canvas.clipRect(bounds)
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(scaleX, scaleY, scalePointX, scalePointY)
        canvas.drawPicture(picture)
        canvas.restore()
    }

    private fun save(canvas: Canvas) {
        if (alpha == 255 || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            canvas.save()
        } else {
            canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), alpha)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        scaleX = width.toFloat() / picture.width.toFloat()
        scaleY = height.toFloat() / picture.height.toFloat()
        super.setBounds(left, top, right, bottom)
    }
}