package com.angcyo.library.canvas.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
interface ICanvasView {

    //region---View视图方法---

    /**返回宿主视图*/
    fun getRawView(): View

    /**[OverScroller]滚动支持*/
    fun computeScroll() {}

    /**绘制窗口大小改变*/
    fun onSizeChanged(w: Int, h: Int) {
        getCanvasViewBox().updateRenderBounds(RectF(0f, 0f, w.toFloat(), h.toFloat()))
    }

    /**绘制核心入口方法*/
    fun onDraw(canvas: Canvas)

    /**手势核心入口方法*/
    fun dispatchTouchEvent(event: MotionEvent): Boolean = false

    /**重绘刷新
     * [just] 是否立即刷新*/
    fun refresh(just: Boolean = false) {
        if (just) {
            getRawView().invalidate()
        } else {
            getRawView().postInvalidate()
        }
        //getView().postInvalidateDelayed(LibHawkKeys.minInvalidateDelay) //2023-5-15
    }

    fun onAttachedToWindow() {}

    fun onDetachedFromWindow() {}

    //endregion---View视图方法---

    //region---CanvasRenderViewBox---

    /**[Matrix]*/
    fun getCanvasViewBox(): CanvasViewBox

    /**当[com.angcyo.library.canvas.core.CanvasViewBox.renderBounds]更新时回调*/
    fun dispatchRenderBoxBoundsUpdate(newBounds: RectF) {}

    /**当[com.angcyo.library.canvas.core.CanvasViewBox.originGravity]更新时回调*/
    fun dispatchRenderBoxOriginGravityUpdate(newGravity: Int) {}

    /**当[com.angcyo.library.canvas.core.CanvasViewBox.renderMatrix]更新时回调*/
    fun dispatchRenderBoxMatrixUpdate(newMatrix: Matrix, reason: Reason, finish: Boolean) {}

    /**当[com.angcyo.library.canvas.core.CanvasViewBox.renderMatrix]更新时回调*/
    fun dispatchRenderBoxMatrixChange(fromMatrix: Matrix, toMatrix: Matrix, reason: Reason) {}

    //endregion---CanvasRenderViewBox---

    //region---manager---

    /**获取渲染管理器*/
    fun getRenderManager(): IRendererManager

    //endregion---manager---

}