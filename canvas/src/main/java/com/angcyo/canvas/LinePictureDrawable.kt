package com.angcyo.canvas

import android.graphics.Canvas
import android.graphics.Picture

/**
 * 用来绘制线段的Drawable
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/02
 */
class LinePictureDrawable(picture: Picture) : ScalePictureDrawable(picture) {

    override fun draw(canvas: Canvas) {
        //super.draw(canvas)
        save(canvas)
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.drawPicture(picture)
        canvas.restore()
    }
}