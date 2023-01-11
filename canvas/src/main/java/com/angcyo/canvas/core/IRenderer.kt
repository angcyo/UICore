package com.angcyo.canvas.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.CanvasView

/**
 * 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
interface IRenderer {

    //<editor-fold desc="属性操作">

    /**是否可见*/
    fun isVisible(renderParams: RenderParams? = null): Boolean

    /**是否锁定了, 锁定后,不能进行操作*/
    fun isLock(): Boolean

    /**获取图层描述的名字*/
    fun getName(): CharSequence?

    /**距离坐标系原点的像素坐标
     *
     * [com.angcyo.canvas.items.renderer.IItemRenderer.getRotateBounds]
     * */
    fun getBounds(): RectF

    /**获取坐标系中的可绘制坐标, 非视图系的坐标
     * 相对于坐标系原点的像素坐标
     * [com.angcyo.canvas.items.renderer.BaseItemRenderer.renderItemBoundsChanged]
     *
     * [com.angcyo.canvas.items.renderer.IItemRenderer.getRotateBounds]
     * */
    fun getRenderBounds(): RectF

    /**获取视图系中的坐标
     * 相对于视图左上角的像素坐标
     * [com.angcyo.canvas.items.renderer.BaseItemRenderer.renderItemBoundsChanged]
     *
     * [com.angcyo.canvas.items.renderer.IItemRenderer.getVisualRotateBounds]
     * */
    fun getVisualBounds(): RectF

    //</editor-fold desc="属性操作">

    //<editor-fold desc="回调">

    /**当[CanvasView]大小改变时的回调
     * 更新需要渲染的区域, 真实的坐标. 非[Matrix]后的坐标
     * [com.angcyo.canvas.CanvasView.onSizeChanged]*/
    fun onCanvasSizeChanged(canvasView: CanvasDelegate)

    /**当[CanvasView]中,[CanvasViewBox]的[Matrix]改变时的回调
     * 当视图改变后的回调
     * 可以用来计算数据, 并缓存
     * [com.angcyo.canvas.CanvasView.canvasMatrixUpdate]*/
    fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix,
        isEnd: Boolean
    )

    //</editor-fold desc="回调">

    //<editor-fold desc="渲染">

    /**核心渲染方法
     * [com.angcyo.canvas.CanvasView.onDraw]*/
    fun render(canvas: Canvas, renderParams: RenderParams)

    /**获取用于预览的[Drawable]*/
    fun preview(renderParams: RenderParams): Drawable? {
        return null
    }

    //</editor-fold desc="渲染">
}