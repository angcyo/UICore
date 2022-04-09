package com.angcyo.canvas.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.canvas.CanvasView

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
interface IRenderer {

    /**是否可见, 决定是否绘制*/
    var visible: Boolean

    /**更新需要渲染的区域, 真实的坐标. 非[Matrix]后的坐标
     * [com.angcyo.canvas.CanvasView.onSizeChanged]*/
    fun onUpdateRendererBounds(canvasView: CanvasView) {
    }

    /**获取坐标系中的坐标*/
    fun getRendererBounds(): RectF

    /**
     * 当视图改变后的回调
     * 可以用来计算数据, 并缓存
     * [com.angcyo.canvas.CanvasView.onCanvasMatrixUpdate]*/
    fun onCanvasMatrixUpdate(matrix: Matrix, oldValue: Matrix) {

    }

    /**核心渲染方法
     * [com.angcyo.canvas.CanvasView.onDraw]*/
    fun render(canvas: Canvas)

}