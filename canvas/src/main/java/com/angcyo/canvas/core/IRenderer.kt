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

    //<editor-fold desc="属性操作">

    /**是否可见, 决定是否绘制*/
    var visible: Boolean

    /**获取坐标系中的坐标, 非视图系的坐标*/
    fun getRendererBounds(): RectF

    //</editor-fold desc="属性操作">

    //<editor-fold desc="回调">

    /**当[CanvasView]大小改变时的回调
     * 更新需要渲染的区域, 真实的坐标. 非[Matrix]后的坐标
     * [com.angcyo.canvas.CanvasView.onSizeChanged]*/
    fun onCanvasSizeChanged(canvasView: CanvasView) {
    }

    /**当[CanvasView]中,[CanvasViewBox]的[Matrix]改变时的回调
     * 当视图改变后的回调
     * 可以用来计算数据, 并缓存
     * [com.angcyo.canvas.CanvasView.canvasMatrixUpdate]*/
    fun onCanvasMatrixUpdate(canvasView: CanvasView, matrix: Matrix, oldValue: Matrix) {
    }

    //</editor-fold desc="回调">

    //<editor-fold desc="渲染">

    /**核心渲染方法
     * [com.angcyo.canvas.CanvasView.onDraw]*/
    fun render(canvasView: CanvasView, canvas: Canvas)

    //</editor-fold desc="渲染">


}