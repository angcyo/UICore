package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.OverScroller
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasElementRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
interface ICanvasRenderView {

    //region---View视图方法---

    /**[OverScroller]滚动支持*/
    fun computeScroll()

    /**绘制窗口大小改变*/
    fun onSizeChanged(w: Int, h: Int)

    /**绘制核心入口方法*/
    fun onDraw(canvas: Canvas)

    /**手势核心入口方法*/
    fun dispatchTouchEvent(event: MotionEvent): Boolean

    /**重绘刷新*/
    fun refresh()

    //endregion---View视图方法---

    //region---Base---

    /**分发回退/恢复栈发生改变
     * [CanvasUndoManager]*/
    fun dispatchRenderUndoChange() {}

    //endregion---Base---

    //region---CanvasRenderViewBox---

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderBounds]更新时回调*/
    fun dispatchRenderBoxBoundsUpdate(newBounds: RectF)

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.originGravity]更新时回调*/
    fun dispatchRenderBoxOriginGravityUpdate(newGravity: Int)

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun dispatchRenderBoxMatrixUpdate(newMatrix: Matrix, finish: Boolean)

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun dispatchRenderBoxMatrixChange(fromMatrix: Matrix, toMatrix: Matrix)

    //endregion---CanvasRenderViewBox---

    //region---CanvasRenderer---

    /**选中的元素, 改变时回调*/
    fun dispatchSelectorRendererChange(
        selectorComponent: CanvasSelectorComponent,
        from: List<BaseRenderer>,
        to: List<BaseRenderer>
    )

    /**[com.angcyo.canvas.render.renderer.BaseRenderer.renderFlags]改变时触发的回调*/
    fun dispatchRendererFlagsChange(
        renderer: BaseRenderer,
        oldFlags: Int,
        newFlags: Int,
        reason: Reason
    )

    /**[com.angcyo.canvas.render.renderer.BaseRenderer.renderProperty]改变时触发*/
    fun dispatchRendererPropertyChange(
        renderer: BaseRenderer,
        fromProperty: CanvasRenderProperty?,
        toProperty: CanvasRenderProperty?,
        reason: Reason
    )

    //endregion---CanvasRenderer---

}