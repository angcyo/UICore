package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.OverScroller
import androidx.annotation.WorkerThread
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.renderer.BaseRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */

@WorkerThread
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

    fun onAttachedToWindow()

    fun onDetachedFromWindow()

    //endregion---View视图方法---

    //region---Base---

    /**分发回退/恢复栈发生改变
     * [CanvasUndoManager]*/
    fun dispatchRenderUndoChange()

    /**分发异步状态发生改变
     * [CanvasAsyncManager]*/
    fun dispatchAsyncStateChange(uuid: String, state: Int)

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

    /**派发有元素添加/删除
     * [com.angcyo.canvas.render.core.CanvasRenderManager.addRenderer]
     * [from] 原有的集合
     * [to] 改变后的集合
     * [op] 操作的集合, 比如删除的元素集合/添加的元素集合
     * */
    fun dispatchRendererListChange(
        from: List<BaseRenderer>,
        to: List<BaseRenderer>,
        op: List<BaseRenderer>
    )

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