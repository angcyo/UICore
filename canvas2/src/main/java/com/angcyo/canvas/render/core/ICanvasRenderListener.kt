package com.angcyo.canvas.render.core

import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasElementRenderer

/**
 * [ICanvasRenderView]相关事件通知
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/15
 */
interface ICanvasRenderListener {

    //region---Base---

    /**[CanvasUndoManager]*/
    fun onRenderUndoChange(undoManager: CanvasUndoManager) {}

    //endregion---Base---

    //region---CanvasRenderViewBox---

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderBounds]更新时回调*/
    fun onRenderBoxBoundsUpdate(newBounds: RectF) {}

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.originGravity]更新时回调*/
    fun onRenderBoxOriginGravityUpdate(newGravity: Int) {}

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun onRenderBoxMatrixUpdate(newMatrix: Matrix, finish: Boolean) {}

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun onRenderBoxMatrixChange(fromMatrix: Matrix, toMatrix: Matrix) {}

    //endregion---CanvasRenderViewBox---

    //region---CanvasRenderer---

    /**选中的元素, 改变时回调*/
    fun onSelectorRendererChange(
        selectorComponent: CanvasSelectorComponent,
        from: List<BaseRenderer>,
        to: List<BaseRenderer>
    ) {
    }

    /**[com.angcyo.canvas.render.renderer.BaseRenderer.renderFlags]改变时触发的回调*/
    fun onRendererFlagsChange(
        renderer: BaseRenderer,
        oldFlags: Int,
        newFlags: Int,
        reason: Reason
    ) {
    }

    /**[com.angcyo.canvas.render.renderer.BaseRenderer.renderProperty]改变时的回调*/
    fun onRendererPropertyChange(
        renderer: BaseRenderer,
        fromProperty: CanvasRenderProperty?,
        toProperty: CanvasRenderProperty?,
        reason: Reason
    ) {
    }

    //endregion---CanvasRenderer---

}