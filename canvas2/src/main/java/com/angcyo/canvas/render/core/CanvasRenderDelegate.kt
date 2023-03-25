package com.angcyo.canvas.render.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.core.component.*
import com.angcyo.canvas.render.data.LimitInfo
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TouchSelectorInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.unit.IRenderUnit
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import com.angcyo.library.isMain
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min

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
    var renderParams = RenderParams(this, this)

    /**异步加载管理*/
    var asyncManager = CanvasAsyncManager(this)

    /**限制组件*/
    var limitMatrixComponent: LimitMatrixComponent = LimitMatrixComponent()

    val pointTouchComponentList = mutableListOf<PointTouchComponent>()

    /**左上角点位事件触发组件*/
    val initialPointComponent = PointTouchComponent(this).apply {
        pointTag = PointTouchComponent.TAG_INITIAL
    }

    init {
        renderListenerList.add(limitMatrixComponent)

        //左上角点位
        pointTouchComponentList.add(initialPointComponent)
    }

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

        //initialPointComponent
        val right = axisManager.xAxisBounds.height()
        val bottom = axisManager.yAxisBounds.width()
        initialPointComponent.pointRect.set(0f, 0f, right, bottom)
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
        //提前处理事件
        for (pointTouch in pointTouchComponentList) {
            pointTouch.dispatchTouchEvent(event)
        }

        //inner
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

    override fun dispatchRenderBoxMatrixUpdate(newMatrix: Matrix, reason: Reason, finish: Boolean) {
        for (listener in renderListenerList) {
            listener.onRenderBoxMatrixUpdate(newMatrix, reason, finish)
        }
    }

    override fun dispatchRenderBoxMatrixChange(
        fromMatrix: Matrix,
        toMatrix: Matrix,
        reason: Reason
    ) {
        for (listener in renderListenerList) {
            listener.onRenderBoxMatrixChange(fromMatrix, toMatrix, reason)
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

    override fun dispatchApplyControlMatrix(
        control: BaseControl,
        controlRenderer: BaseRenderer,
        controlMatrix: Matrix,
        controlType: Int
    ) {
        for (listener in renderListenerList) {
            listener.onApplyControlMatrix(control, controlRenderer, controlMatrix, controlType)
        }
    }

    override fun dispatchApplyMatrix(renderer: BaseRenderer, matrix: Matrix, controlType: Int) {
        for (listener in renderListenerList) {
            listener.onApplyMatrix(this, renderer, matrix, controlType)
        }
    }

    override fun dispatchSelectorRendererList(
        selectorManager: CanvasSelectorManager,
        selectorInfo: TouchSelectorInfo
    ) {
        for (listener in renderListenerList) {
            listener.onSelectorRendererList(selectorManager, selectorInfo)
        }
    }

    override fun dispatchDoubleTapItem(
        selectorManager: CanvasSelectorManager,
        renderer: BaseRenderer
    ) {
        for (listener in renderListenerList) {
            listener.onDoubleTapItem(selectorManager, renderer)
        }
    }

    override fun dispatchControlHappen(controlPoint: BaseControl, end: Boolean) {
        for (listener in renderListenerList) {
            listener.onControlHappen(controlPoint, end)
        }
    }

    override fun dispatchRenderDrawable(
        renderer: BaseRenderer,
        params: RenderParams,
        endDraw: Boolean
    ) {
        for (listener in renderListenerList) {
            listener.onRenderDrawable(renderer, params, endDraw)
        }
    }

    override fun dispatchRendererGroupChange(
        groupRenderer: CanvasGroupRenderer,
        subRendererList: List<BaseRenderer>,
        groupType: Int
    ) {
        for (listener in renderListenerList) {
            listener.onRendererGroupChange(groupRenderer, subRendererList, groupType)
        }
    }

    override fun dispatchPointTouchEvent(component: PointTouchComponent, type: Int) {
        for (listener in renderListenerList) {
            listener.onPointTouchEvent(component, type)
        }
    }

    //endregion---ICanvasRenderView---

    //region---CanvasRenderer---

    override fun dispatchElementRendererListChange(
        from: List<BaseRenderer>,
        to: List<BaseRenderer>,
        op: List<BaseRenderer>
    ) {
        for (listener in renderListenerList) {
            listener.onElementRendererListChange(from, to, op)
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

    override fun dispatchRendererSaveState(renderer: BaseRenderer, stateStack: IStateStack) {
        for (listener in renderListenerList) {
            listener.onRendererSaveState(renderer, stateStack)
        }
    }

    override fun dispatchRendererRestoreState(renderer: BaseRenderer, stateStack: IStateStack) {
        for (listener in renderListenerList) {
            listener.onRendererRestoreState(renderer, stateStack)
        }
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
        renderer: BaseRenderer,
        undoState: IStateStack,
        redoState: IStateStack,
        redoIt: Boolean = false,
        reason: Reason = Reason.user.apply {
            controlType = BaseControlPoint.CONTROL_TYPE_DATA
        },
        strategy: Strategy = Strategy.normal
    ) {
        undoManager.addToStack(renderer, undoState, redoState, redoIt, reason, strategy)
    }

    /**[com.angcyo.canvas.render.core.CanvasRenderManager.limitRenderer]*/
    fun resetLimitRender(list: List<LimitInfo>) {
        renderManager.limitRenderer.resetLimit {
            addAll(list)
        }
    }

    /**[com.angcyo.canvas.render.core.CanvasRenderManager.limitRenderer]*/
    fun clearLimitRender() {
        renderManager.limitRenderer.clear()
    }

    /**将画板移动到可以完全显示出[rect]
     * [rect] 坐标系中的矩形坐标
     * [zoomIn]  当矩形很小的时候, 是否要放大.
     * [zoomOut] 当矩形很大的时候, 是否要缩小.
     * [lockScale] 锁定缩放的比例, 等比
     * [margin] 边缘额外显示的距离
     * [offsetRectTop] 自动偏移到[rect]的顶部
     * [offsetX] 额外偏移的x
     * [offsetY] 额外偏移的y
     * */
    fun showRectBounds(
        @CanvasInsideCoordinate
        rect: RectF,
        margin: Float = 4f * dp,
        zoomIn: Boolean = true /*自动放大*/,
        zoomOut: Boolean = true /*自动缩小*/,
        lockScale: Boolean = true,
        anim: Boolean = true,
        offsetRectTop: Boolean = false,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        finish: (isCancel: Boolean) -> Unit = {}
    ) {
        val renderViewBox = renderViewBox
        if (!renderViewBox.isCanvasInit) {
            view.post {
                showRectBounds(
                    rect,
                    margin,
                    zoomIn,
                    zoomOut,
                    lockScale,
                    anim,
                    offsetRectTop,
                    offsetX,
                    offsetY,
                    finish
                )
            }
            return
        }

        val contentWidth = renderViewBox.renderBounds.width()
        val contentHeight = renderViewBox.renderBounds.height()
        val centerX = contentWidth / 2
        val centerY = contentHeight / 2
        val originPoint = renderViewBox.getOriginPoint()

        //先将坐标系移动到view的中心
        val coordinateTranslateX = centerX - originPoint.x
        val coordinateTranslateY = centerY - originPoint.y

        //再计算目标中心需要偏移的距离量
        val translateX = coordinateTranslateX - rect.centerX()
        val translateY = coordinateTranslateY - rect.centerY()

        val matrix = Matrix()
        //平移
        matrix.setTranslate(translateX, translateY)

        val width = rect.width() + margin * 2
        val height = rect.height() + margin * 2

        var scaleX = 1f
        var scaleY = 1f

        if (((width > contentWidth || height > contentHeight) && zoomOut) ||
            (width < contentWidth || height < contentHeight) && zoomIn
        ) {
            scaleX = (contentWidth - margin * 2) / rect.width()
            scaleY = (contentHeight - margin * 2) / rect.height()

            if (lockScale) {
                scaleX = min(scaleX, scaleY)
                scaleY = scaleX
            }
        }

        //自动缩小
        //自动放大
        matrix.postScale(
            scaleX,
            scaleY,
            centerX,
            centerY
        )

        //偏移量的平移
        matrix.postTranslate(offsetX, offsetY)

        if (offsetRectTop) {
            val offset = (renderViewBox.renderBounds.height() - rect.height() * scaleY) / 2 - margin
            matrix.postTranslate(0f, -offset)
        }

        //更新
        renderViewBox.changeRenderMatrix(matrix, anim, Reason.user.apply {
            controlType =
                BaseControlPoint.CONTROL_TYPE_SCALE or BaseControlPoint.CONTROL_TYPE_TRANSLATE
        }, finish)
    }

    /**创建一个预览图
     * [bounds] 需要预览的范围
     * [overrideSize] 需要等比输出的大小
     * [rendererList] 指定需要渲染的渲染器, 默认所有渲染器
     * */
    fun preview(
        @Pixel bounds: RectF? = null,
        overrideSize: Float? = null,
        rendererList: List<BaseRenderer>? = renderManager.elementRendererList
    ): Bitmap? {
        return CanvasGroupRenderer.createRenderBitmap(rendererList, overrideSize, bounds)
    }

    /**是否禁用所有能够编辑元素的手势
     * [disable] 禁用 or 启动*/
    fun disableEditTouchGesture(disable: Boolean) {
        controlManager.isEnableComponent = !disable
        refresh()
    }

    /**移除所有元素渲染器
     * [CanvasRenderManager]*/
    fun removeAllElementRenderer(strategy: Strategy = Strategy.normal) {
        renderManager.removeAllElementRenderer(strategy)
    }

    //endregion---操作---

}