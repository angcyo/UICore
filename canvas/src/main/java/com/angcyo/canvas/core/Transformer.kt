package com.angcyo.canvas.core

import android.graphics.RectF

/**
 * 变压器, 用来将像素坐标转换成绘图可视化坐标
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class Transformer(val canvasViewBox: CanvasViewBox) {

    /**将坐标矩形, 映射成变换后的矩形*/
    fun mapRectF(rect: RectF): RectF {
        return RectF()
    }

}