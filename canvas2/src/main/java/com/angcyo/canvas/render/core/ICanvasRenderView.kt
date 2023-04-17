package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.OverScroller
import androidx.annotation.WorkerThread
import com.angcyo.canvas.render.core.component.BaseControl
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.PointTouchComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TouchSelectorInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.library.unit.IRenderUnit

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

    /**分发渲染单位发生改变
     * [CanvasUndoManager]*/
    fun dispatchRenderUnitChange(from: IRenderUnit, to: IRenderUnit)

    /**当操作控制矩阵需要应用到属性时触发, 可以通过修改[controlMatrix]达到修改的目的
     *
     * [control] 当前的控制对象
     * [controlRenderer] 当前操作的对象
     * [controlMatrix] 当前操作的矩阵
     *
     * [com.angcyo.canvas.render.core.component.BaseControl.applyTranslate]
     * [com.angcyo.canvas.render.core.component.BaseControl.applyRotate]
     * [com.angcyo.canvas.render.core.component.BaseControl.applyScale]
     *
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_TRANSLATE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_WIDTH]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_HEIGHT]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_SCALE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_ROTATE]
     * */
    fun dispatchApplyControlMatrix(
        control: BaseControl,
        controlRenderer: BaseRenderer,
        controlMatrix: Matrix,
        controlType: Int
    )

    /** 当需要将[matrix]应用到[renderer]时触发, 可以在此回调用进行限制操作
     * [controlType] 控制类型
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_TRANSLATE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_WIDTH]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_HEIGHT]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_SCALE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_ROTATE]
     * */
    fun dispatchApplyMatrix(
        renderer: BaseRenderer,
        matrix: Matrix,
        controlType: Int
    )

    /**点击选择时, 底部有多个元素被选中的回调
     * [com.angcyo.canvas.render.data.TouchSelectorInfo.touchRendererList]*/
    fun dispatchSelectorRendererList(
        selectorManager: CanvasSelectorManager,
        selectorInfo: TouchSelectorInfo
    )

    /**双击选中某个渲染器[renderer]
     * [com.angcyo.canvas.render.data.TouchSelectorInfo.touchRendererList]*/
    fun dispatchDoubleTapItem(selectorManager: CanvasSelectorManager, renderer: BaseRenderer)

    /**
     * 当有触发了控制点时
     *
     * [controlPoint] 对应的控制点
     * [end] 是否控制结束
     *
     * [RotateControlPoint]
     * [ScaleControlPoint]
     * [TranslateRendererControl]
     * */
    fun dispatchControlHappen(controlPoint: BaseControl, end: Boolean)

    /**当要渲染[renderer]时, 触发
     * [endDraw] false:表示在draw之前, true:表示在draw之后*/
    fun dispatchRenderDrawable(renderer: BaseRenderer, params: RenderParams, endDraw: Boolean)

    /**范围点击事件
     * [type] 事件类型
     * [PointTouchComponent.TOUCH_TYPE_CLICK]
     * [PointTouchComponent.TOUCH_TYPE_LONG_PRESS]
     * */
    fun dispatchPointTouchEvent(component: PointTouchComponent, type: Int)

    /**画布内手势事件监听*/
    fun dispatchCanvasTouchEvent(event: MotionEvent)

    //endregion---Base---

    //region---CanvasRenderViewBox---

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderBounds]更新时回调*/
    fun dispatchRenderBoxBoundsUpdate(newBounds: RectF)

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.originGravity]更新时回调*/
    fun dispatchRenderBoxOriginGravityUpdate(newGravity: Int)

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun dispatchRenderBoxMatrixUpdate(newMatrix: Matrix, reason: Reason, finish: Boolean)

    /**当[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]更新时回调*/
    fun dispatchRenderBoxMatrixChange(fromMatrix: Matrix, toMatrix: Matrix, reason: Reason)

    //endregion---CanvasRenderViewBox---

    //region---CanvasRenderer---

    /**派发有元素添加/删除
     * [com.angcyo.canvas.render.core.CanvasRenderManager.addElementRenderer]
     * [from] 原有的集合
     * [to] 改变后的集合
     * [op] 操作的集合, 比如删除的元素集合/添加的元素集合
     * [reason] 操作的原因
     * */
    fun dispatchElementRendererListChange(
        from: List<BaseRenderer>,
        to: List<BaseRenderer>,
        op: List<BaseRenderer>,
        reason: Reason
    )

    /**选中的元素, 改变时回调*/
    fun dispatchSelectorRendererChange(from: List<BaseRenderer>, to: List<BaseRenderer>)

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

    /**当有渲染器需要保存状态时, 触发*/
    fun dispatchRendererSaveState(renderer: BaseRenderer, stateStack: IStateStack)

    /**当有渲染器需要恢复状态时, 触发*/
    fun dispatchRendererRestoreState(renderer: BaseRenderer, stateStack: IStateStack)

    /**元素分组/拆组变化时回调
     * [groupRenderer] 群组渲染器
     * [subRendererList] 组合的子元素或者拆组的子元素
     * [groupType]
     * [com.angcyo.canvas.render.renderer.CanvasGroupRenderer.GROUP_TYPE_GROUP] 群组操作
     * [com.angcyo.canvas.render.renderer.CanvasGroupRenderer.GROUP_TYPE_DISSOLVE] 解组操作
     * */
    fun dispatchRendererGroupChange(
        groupRenderer: CanvasGroupRenderer,
        subRendererList: List<BaseRenderer>,
        groupType: Int
    )

    //endregion---CanvasRenderer---

}