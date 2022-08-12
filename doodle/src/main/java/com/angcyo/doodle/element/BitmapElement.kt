package com.angcyo.doodle.element

import android.graphics.Canvas
import androidx.core.graphics.withSave
import com.angcyo.doodle.data.BitmapElementData
import com.angcyo.doodle.layer.BaseLayer

/**
 * 用来绘制图片的元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class BitmapElement(val bitmapData: BitmapElementData) : BaseElement(bitmapData) {
    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        val bounds = bitmapData.bounds
        bitmapData.bitmap?.let {
            canvas.withSave {
                translate(bounds.left, bounds.top)
                scale(bounds.width() / it.width, bounds.height() / it.height)
                drawBitmap(it, 0f, 0f, paint)
            }
        }
    }
}