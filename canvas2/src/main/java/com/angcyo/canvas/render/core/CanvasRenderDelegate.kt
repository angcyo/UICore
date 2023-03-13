package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.IStateStack
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.unit.IRenderUnit
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import com.angcyo.library.isMain
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 渲染控制代理, 入口核心类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasRenderDelegate(val view: View) : BaseRenderDispatch(), ICanvasRenderView {

    /**事件监听者列表*/
    private val renderListenerList = CopyOnWriteArrayList<ICanvasRenderListener>()

    /**绘制区域设置, 坐标映射处理*/
    var renderViewBox = CanvasRenderViewBox(this)

    /**手势管理*/
    var touchManager = CanvasTouchManager(this)

    /**坐标尺管理*/
    var axisManager = CanvasAxisManager(this)

    /**渲染管理*/
    var renderManager = CanvasRenderManager(this)

    /**元素选择管理*/
    var selectorManager = CanvasSelectorManager(this)

    /**控制点管理*/
    var controlManager = CanvasControlManager(this)

    /**回退栈控制*/
    var undoManager = CanvasUndoManager(this)

    /**渲染参数*/
    var renderParams = RenderParams(this)

    /**异步加载管理*/
    var asyncManager = CanvasAsyncManager(this)

    //region---View视图方法---

    override fun computeScroll() {
        touchManager.flingComponent.onComputeScroll()
    }

    override fun onSizeChanged(w: Int, h: Int) {
        val size = 20 * dp
        renderViewBox.updateRenderBounds(
            RectF(
                size * 2,
                size * 2,
                w.toFloat() - size,
                h.toFloat() - size
            )
        )
        axisManager.updateAxisBounds(size, 0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        //刻度尺/网格
        dispatchRender(canvas, axisManager, renderParams)
        //渲染器
        dispatchRender(canvas, renderManager, renderParams)
        //选择控制器
        dispatchRender(canvas, selectorManager, renderParams)
        //控制点管理
        dispatchRender(canvas, controlManager, renderParams)
    }

    /**是否在画板区域按下*/
    var _isTouchDownInCanvas = false

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val renderBounds = renderViewBox.renderBounds

        val eventX = event.x
        val eventY = event.y

        //事件的坐标是相对于画板左上角的坐标
        val offsetX = -renderBounds.left
        val offsetY = -renderBounds.top
        event.offsetLocation(offsetX, offsetY)

        val x = event.x
        val y = event.y

        //L.d("${action.actionToString()}:${eventX},${eventY} inside:$x,$y $_isTouchDownInCanvas")

        //init
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            _isTouchDownInCanvas = renderBounds.contains(eventX, eventY)
        }

        if (!_isTouchDownInCanvas) {
            //按下的时候, 没有在画板区域, 则后续不处理事件
            return false
        }

        //view
        if (action == MotionEvent.ACTION_DOWN) {
            view.disableParentInterceptTouchEvent()
        } else if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_CANCEL
        ) {
            view.disableParentInterceptTouchEvent(false)
        }

        //dispatch
        val handle = touchManager.dispatchTouchEventDelegate(event)
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            _isTouchDownInCanvas = false
        }
        return handle
    }

    override fun refresh() {
        if (isMain()) {
            view.invalidate()
        } else {
            view.postInvalidate()
        }
    }

    override fun onAttachedToWindow() {
        asyncManager.startAsync()
    }

    override fun onDetachedFromWindow() {
        asyncManager.releaseAsync()
    }

    //endregion---View视图方法---

    //region---ICanvasRenderView---

    override fun dispatchRenderBoxBoundsUpdate(newBounds: RectF) {
        for (listener in renderListenerList) {
            listener.onRenderBoxBoundsUpdate(newBounds)
        }
    }

    override fun dispatchRenderBoxOriginGravityUpdate(newGravity: Int) {
        for (listener in renderListenerList) {
            listener.onRenderBoxOriginGravityUpdate(newGravity)
        }
    }

    override fun dispatchRenderBoxMatrixUpdate(newMatrix: Matrix, finish: Boolean) {
        for (listener in renderListenerList) {
            listener.onRenderBoxMatrixUpdate(newMatrix, finish)
        }
    }

    override fun dispatchRenderBoxMatrixChange(fromMatrix: Matrix, toMatrix: Matrix) {
        for (listener in renderListenerList) {
            listener.onRenderBoxMatrixChange(fromMatrix, toMatrix)
        }
    }

    override fun dispatchRenderUndoChange() {
        for (listener in renderListenerList) {
            listener.onRenderUndoChange(undoManager)
        }
    }

    override fun dispatchAsyncStateChange(uuid: String, state: Int) {
        for (listener in renderListenerList) {
            listener.onAsyncStateChange(uuid, state)
        }
    }

    override fun dispatchRenderUnitChange(from: IRenderUnit, to: IRenderUnit) {
        for (listener in renderListenerList) {
            listener.onRenderUnitChange(from, to)
        }
    }

    //endregion---ICanvasRenderView---

    //region---CanvasRenderer---

    override fun dispatchRendererListChange(
        from: List<BaseRenderer>,
        to: List<BaseRenderer>,
        op: List<BaseRenderer>
    ) {
        for (listener in renderListenerList) {
            listener.onRendererListChange(from, to, op)
        }
    }

    override fun dispatchSelectorRendererChange(from: List<BaseRenderer>, to: List<BaseRenderer>) {
        for (listener in renderListenerList) {
            listener.onSelectorRendererChange(selectorManager.selectorComponent, from, to)
        }
    }

    override fun dispatchRendererFlagsChange(
        renderer: BaseRenderer,
        oldFlags: Int,
        newFlags: Int,
        reason: Reason
    ) {
        for (listener in renderListenerList) {
            listener.onRendererFlagsChange(renderer, oldFlags, newFlags, reason)
        }
        refresh()
    }

    override fun dispatchRendererPropertyChange(
        renderer: BaseRenderer,
        fromProperty: CanvasRenderProperty?,
        toProperty: CanvasRenderProperty?,
        reason: Reason
    ) {
        for (listener in renderListenerList) {
            listener.onRendererPropertyChange(renderer, fromProperty, toProperty, reason)
        }
        refresh()
    }

    //endregion---CanvasRenderer---

    //region---操作---

    /**添加一个事件监听*/
    fun addCanvasRenderListener(listener: ICanvasRenderListener) {
        renderListenerList.add(listener)
    }

    /**移除一个事件监听*/
    fun removeCanvasRenderListener(listener: ICanvasRenderListener) {
        renderListenerList.remove(listener)
    }

    /**自动添加一个状态到回退栈*/
    fun addStateToStack(
        undoState: IStateStack,
        redoState: IStateStack,
        redoIt: Boolean = false,
        reason: Reason = Reason.user.apply {
            controlType = BaseControlPoint.CONTROL_TYPE_KEEP_GROUP_PROPERTY or
                    BaseControlPoint.CONTROL_TYPE_DATA
        },
        strategy: Strategy = Strategy.normal
    ) {
        undoManager.addToStack(undoState, redoState, redoIt, reason, strategy)
    }

    //endregion---操作---

}