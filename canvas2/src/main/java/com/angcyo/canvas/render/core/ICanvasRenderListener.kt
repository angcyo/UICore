package com.angcyo.canvas.render.core

import android.graphics.Matrix
import android.graphics.RectF
import androidx.annotation.WorkerThread
import com.angcyo.canvas.render.core.component.BaseControl
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TouchSelectorInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.unit.IRenderUnit

/**
 * [ICanvasRenderView]相关事件通知
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/15
 */

@WorkerThread
interface ICanvasRenderListener {

    //region---Base---

    /**[CanvasUndoManager]*/
    fun onRenderUndoChange(undoManager: CanvasUndoManager) {}

    /**[CanvasAsyncManager]*/
    fun onAsyncStateChange(uuid: String, state: Int) {}

    /**[CanvasAxisManager]*/
    fun onRenderUnitChange(from: IRenderUnit, to: IRenderUnit) {}

    /**[CanvasControlManager]*/
    fun onControlHappen(controlPoint: BaseControl, end: Boolean) {}

    /**[CanvasRenderDelegate]*/
    fun onRenderDrawable(renderer: BaseRenderer, params: RenderParams, endDraw: Boolean) {}

    //endregion---Base---

    //region---CanvasRenderViewBox---

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderBounds]更新时回调*/
    fun onRenderBoxBoundsUpdate(newBounds: RectF) {}

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.originGravity]更新时回调*/
    fun onRenderBoxOriginGravityUpdate(newGravity: Int) {}

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun onRenderBoxMatrixUpdate(newMatrix: Matrix, reason: Reason, finish: Boolean) {}

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun onRenderBoxMatrixChange(fromMatrix: Matrix, toMatrix: Matrix, reason: Reason) {}

    //endregion---CanvasRenderViewBox---

    //region---CanvasRenderer---

    /**当有元素添加/删除时*/
    fun onElementRendererListChange(
        from: List<BaseRenderer>,
        to: List<BaseRenderer>,
        op: List<BaseRenderer>
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

    /**[com.angcyo.canvas.render.core.ICanvasRenderView.dispatchApplyControlMatrix]*/
    fun onApplyControlMatrix(
        control: BaseControl,
        controlRenderer: BaseRenderer,
        controlMatrix: Matrix,
        controlType: Int
    ) {
    }

    /**[com.angcyo.canvas.render.core.ICanvasRenderView.dispatchApplyMatrix]*/
    fun onApplyMatrix(
        delegate: CanvasRenderDelegate,
        renderer: BaseRenderer,
        matrix: Matrix,
        controlType: Int
    ) {
    }

    /**选中的元素, 改变时回调*/
    fun onSelectorRendererChange(
        selectorComponent: CanvasSelectorComponent,
        from: List<BaseRenderer>,
        to: List<BaseRenderer>
    ) {
    }

    /**点击选择时, 底部有多个元素被选中的回调*/
    fun onSelectorRendererList(
        selectorManager: CanvasSelectorManager,
        selectorInfo: TouchSelectorInfo
    ) {
    }

    /**双击选中某个渲染器[renderer] */
    fun onDoubleTapItem(selectorManager: CanvasSelectorManager, renderer: BaseRenderer) {
    }


    /**[com.angcyo.canvas.render.core.ICanvasRenderView.dispatchRendererSaveState]*/
    fun onRendererSaveState(renderer: BaseRenderer, stateStack: IStateStack) {}

    /**[com.angcyo.canvas.render.core.ICanvasRenderView.dispatchRendererRestoreState]*/
    fun onRendererRestoreState(renderer: BaseRenderer, stateStack: IStateStack) {}

    //endregion---CanvasRenderer---

}