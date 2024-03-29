package com.angcyo.library.component

import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.PictureDrawable
import android.os.Build

/**
 * 默认的[PictureDrawable] [setBounds]放大缩小之后, 不会进行缩放
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/13
 */
open class ScalePictureDrawable(picture: Picture) : PictureDrawable(picture) {

    /**需要的缩放倍数*/
    var scalePictureX = 1f
    var scalePictureY = 1f

    /**缩放控制点*/
    var scalePointX = 0f
    var scalePointY = 0f

    var scaleRenderRect = RectF()

    /**是否激活Clip*/
    var enableClip = false

    /**返回是否需要绘制*/
    fun checkDrawThreshold(): Boolean {
        return true
        /*if (scalePictureX.absoluteValue >= 0.1f && scalePictureY.absoluteValue >= 0.1f) {
            return true
        }
        return false*/
    }

    override fun draw(canvas: Canvas) {
        if (checkDrawThreshold()) {
            save(canvas)
            if (enableClip) {
                canvas.clipRect(bounds)
            }
            //canvas.clipRect(scaleRenderRect)
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            canvas.scale(scalePictureX, scalePictureY, scalePointX, scalePointY)
            canvas.drawPicture(picture)
            canvas.restore()
            //L.e("scalePictureX:$scalePictureX scalePictureY:$scalePictureY ${scaleRenderRect.width() / picture.width} ${scaleRenderRect.height() / picture.height} $bounds")
        }
    }

    protected fun save(canvas: Canvas) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && alpha == 255) ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
        ) {
            canvas.save()
        } else {
            canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), alpha)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        scalePictureX = width.toFloat() / picture.width.toFloat()
        scalePictureY = height.toFloat() / picture.height.toFloat()
        super.setBounds(left, top, right, bottom)
        scaleRenderRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
    }
}