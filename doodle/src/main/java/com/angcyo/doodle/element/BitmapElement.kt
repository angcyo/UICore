package com.angcyo.doodle.element

import android.graphics.Canvas
import com.angcyo.doodle.data.BitmapData
import com.angcyo.doodle.layer.BaseLayer

/**
 * 用来绘制图片的元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class BitmapElement(val bitmapData: BitmapData) : BaseElement() {
    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        bitmapData.bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, paint)
        }
    }
}