package com.angcyo.canvas.render.core

import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.angcyo.canvas.render.core.component.BaseControl
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.LimitMatrixComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TouchSelectorInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.unit.IRenderUnit
import com.angcyo.canvas.render.util.createOverrideBitmapCanvas
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import com.angcyo.library.isMain
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
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
    var renderParams = RenderParams(this)

    /**异步加载管理*/
    var asyncManager = CanvasAsyncManager(this)

    /**限制组件*/
    var limitMatrixComponent: LimitMatrixComponent = LimitMatrixComponent()

    init {
        renderListenerList.add(limitMatrixComponent)
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

    /**将画板移动到可以完全显示出[rect]
     * [rect] 坐标系中的矩形坐标
     * [scale] 是否要缩放, 以适应过大的矩形
     * [lockScale] 锁定缩放的比例
     * [margin] 边缘额外显示的距离
     * [offsetRectTop] 自动偏移到[rect]的顶部
     * [offsetX] 额外偏移的x
     * [offsetY] 额外偏移的y
     * */
    fun showRectBounds(
        rect: RectF,
        margin: Float = 4f * dp,
        scale: Boolean = true,
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
                    scale,
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

        val centerPoint = Point()
        renderViewBox.getRenderCenterInside()
        /*
        //先将坐标系移动到view的中心
        val coordinateTranslateX =
            renderViewBox.getContentCenterX() - renderViewBox.getCoordinateSystemX()
        val coordinateTranslateY =
            renderViewBox.getContentCenterY() - renderViewBox.getCoordinateSystemY()

        //再计算目标中心需要偏移的距离量
        val translateX = coordinateTranslateX - rect.centerX()
        val translateY = coordinateTranslateY - rect.centerY()

        val matrix = Matrix()
        //平移
        matrix.setTranslate(translateX, translateY)

        val width = rect.width() + margin * 2
        val height = rect.height() + margin * 2

        val contentWidth = renderViewBox.getContentWidth()
        val contentHeight = renderViewBox.getContentHeight()

        var scaleX = 1f
        var scaleY = 1f

        if (width > contentWidth || height > contentHeight) {
            if (scale) {
                //自动缩放
                val scaleCenterX = renderViewBox.getContentCenterX()
                val scaleCenterY = renderViewBox.getContentCenterY()

                scaleX = (contentWidth - margin * 2) / rect.width()
                scaleY = (contentHeight - margin * 2) / rect.height()

                if (lockScale) {
                    val targetScale = min(scaleX, scaleY)
                    scaleX = targetScale
                    scaleY = targetScale
                }
                matrix.postScale(
                    scaleX,
                    scaleY,
                    scaleCenterX,
                    scaleCenterY
                )
            }
        } else {
            //不处理自动放大的情况, 只处理平移
        }

        //偏移量的平移
        matrix.postTranslate(offsetX, offsetY)

        if (offsetRectTop) {
            val offset = (renderViewBox.renderBounds.height() - rect.height() * scaleY) / 2 - margin
            matrix.postTranslate(0f, -offset)
        }

        //更新
        renderViewBox.changeRenderMatrix(matrix, anim, finish)*/
    }

    /**创建一个预览图
     * [bounds] 需要预览的范围
     * [overrideSize] 需要等比输出的大小
     * [rendererList] 指定需要渲染的渲染器, 默认所有渲染器
     * */
    fun preview(
        @Pixel bounds: RectF? = null,
        overrideSize: Float? = null,
        rendererList: List<BaseRenderer> = renderManager.elementRendererList
    ): Bitmap {
        val rect = RectF(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
        if (bounds == null) {
            for (renderer in rendererList) {
                renderer.renderProperty?.getRenderBounds()?.let {
                    rect.set(
                        min(it.left, rect.left),
                        min(it.top, rect.top),
                        max(it.right, rect.right),
                        max(it.bottom, rect.bottom)
                    )
                }
            }
        } else {
            rect.set(bounds)
        }
        return createOverrideBitmapCanvas(rect.width(), rect.height(), overrideSize) {
            translate(-rect.left, -rect.top)
            for (renderer in rendererList) {
                renderer.renderOnInside(this, RenderParams())
            }
        }
    }

    //endregion---操作---

}