package com.angcyo.canvas.items

import android.graphics.Paint
import android.widget.LinearLayout

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */

@Deprecated("废弃")
class LineItem : BaseItem() {

    /**线的长度, 像素*/
    var length: Float = 100f

    /**线的方向, 水平垂直*/
    var orientation: Int = LinearLayout.VERTICAL

    /**是否是虚线*/
    var dash: Boolean = false

    init {
        paint.style = Paint.Style.STROKE
        itemName = "Line"
    }
}