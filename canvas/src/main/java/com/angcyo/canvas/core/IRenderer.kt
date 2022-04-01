package com.angcyo.canvas.core

import android.graphics.Canvas
import android.graphics.RectF
import com.angcyo.canvas.CanvasView

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
interface IRenderer {

    /**更新需要渲染的区域*/
    fun updateRenderBounds(canvasView: CanvasView) {

    }

    /**获取渲染的边界坐标*/
    fun getRenderBounds(): RectF

    /**渲染方法*/
    fun render(canvas: Canvas)

}