package com.angcyo.canvas.render.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.angcyo.canvas.render.annotation.RenderFlag
import com.angcyo.canvas.render.core.component.BaseControl
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.CanvasOverlayComponent
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.core.component.LimitMatrixComponent
import com.angcyo.canvas.render.core.component.PointTouchComponent
import com.angcyo.canvas.render.data.LimitInfo
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TouchSelectorInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.library.L
import com.angcyo.library.annotation.Api
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.core.CanvasTouchManager
import com.angcyo.library.canvas.core.CanvasViewBox
import com.angcyo.library.canvas.core.IRendererManager
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.Strategy
import com.angcyo.library.component.SupportUndo
import com.angcyo.library.component.onMain
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.isNotEmpty
import com.angcyo.library.ex.longFeedback
import com.angcyo.library.ex.size
import com.angcyo.library.unit.IRenderUnit
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min

/**
 * 渲染控制代理, 入口核心类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasRenderDelegate(val view: View) : BaseRenderDispatch(), ICanvasRenderView {

    companion object {
        /**[com.angcyo.canvas.render.core.CanvasRenderManager.getAllElementRendererList]*/
        fun getSingleElementRendererListIn(
            rendererList: Iterable<BaseRenderer>?,
            dissolveGroup: Boolean = true,
            includeGroup: Boolean = false,
            includeEmptySize: Boolean = true,
        ): List<BaseRenderer> {
            val result = mutableListOf<BaseRenderer>()
            rendererList ?: return result
            for (renderer in rendererList) {
                if (renderer is CanvasSelectorComponent) {
                    continue
                } else if (renderer is CanvasGroupRenderer) {
                    if (includeGroup) {
                        result.add(renderer)
                    }
                    if (dissolveGroup) {
                        result.addAll(
                            getSingleElementRendererListIn(
                                renderer.rendererList,
                                true,
                                includeGroup
                            )
                        )
                    }
                } else {
                    result.add(renderer)
                }
            }
            return if (includeEmptySize) result else result.filter {
                it.getRendererBounds()?.isNotEmpty() == true
            }
        }
    }

    /**事件监听者列表*/
    private val renderListenerList = CopyOnWriteArrayList<ICanvasRenderListener>()

    /**绘制区域设置, 坐标映射处理*/
    var renderViewBox = CanvasRenderViewBox(this)

    /**手势管理*/
    var touchManager = CanvasTouchManager(this).apply {
        onIgnoreTouchListener = { listener ->
            if (_canvasOverlayComponent == null) {
                defIgnoreTouchListener(listener)
            } else if (listener is CanvasControlManager || listener is CanvasSelectorManager) {
                true
            } else {
                defIgnoreTouchListener(listener)
            }
        }
    }

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
    var renderParams = RenderParams(this, this, this)

    /**异步加载管理*/
    var asyncManager = CanvasAsyncManager(this)

    /**限制组件*/
    var limitMatrixComponent: LimitMatrixComponent = LimitMatrixComponent()

    val pointTouchComponentList = mutableListOf<PointTouchComponent>()

    /**左上角点位事件触发组件*/
    val initialPointComponent = PointTouchComponent(this).apply {
        pointTag = PointTouchComponent.TAG_INITIAL
    }

    /**覆盖层组件, 支持绘制并拦截元素操作手势, 但不拦截画布手势*/
    protected var _canvasOverlayComponent: CanvasOverlayComponent? = null

    init {
        renderListenerList.add(limitMatrixComponent)

        //左上角点位
        pointTouchComponentList.add(initialPointComponent)
    }

    //region---get---

    /**画布主要的限制区域*/
    @Pixel
    @CanvasInsideCoordinate
    val mainLimitBounds: RectF?
        get() {
            val primaryLimitBounds =
                renderManager.limitRenderer.findLimitInfo { it.tag == LimitInfo.TAG_MAIN }?.bounds
            if (primaryLimitBounds != null) {
                return RectF(primaryLimitBounds)
            }
            return null
        }

    /**是否有覆盖组件*/
    val haveOverlayComponent: Boolean
        get() = _canvasOverlayComponent != null

    //endregion---get---

    //region---View视图方法---

    override fun getRawView(): View = view

    override fun getRenderManager(): IRendererManager = renderManager

    override fun computeScroll() {
        touchManager.flingComponent.onComputeScroll()
    }

    override fun onSizeChanged(w: Int, h: Int) {
        val size = 20 * dp
        renderViewBox.updateRenderBounds(RectF(size, size, w.toFloat(), h.toFloat()))
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

        if (selectorManager.isSelectorElement) {
            //有选中元素时, 因为控制按钮会覆盖在画布上, 所以需要先处理控制按钮事件
        } else if (!_isTouchDownInCanvas) {
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
        dispatchCanvasTouchEvent(event)
        return handle
    }

    override fun onAttachedToWindow() {
        asyncManager.startAsync()
    }

    override fun onDetachedFromWindow() {
        asyncManager.releaseAsync()
    }

    /**长按事件反馈提示*/
    fun longFeedback() {
        view.longFeedback()
    }

    override fun getCanvasViewBox(): CanvasViewBox = renderViewBox

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

    override fun dispatchLimitControlMatrix(
        control: BaseControl,
        controlRenderer: BaseRenderer,
        controlMatrix: Matrix,
        controlType: Int
    ) {
        for (listener in renderListenerList) {
            listener.onLimitControlMatrix(control, controlRenderer, controlMatrix, controlType)
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

    override fun dispatchCanvasTouchEvent(event: MotionEvent) {
        for (listener in renderListenerList) {
            listener.onDispatchTouchEvent(event)
        }
    }

    //endregion---ICanvasRenderView---

    //region---CanvasRenderer---

    override fun dispatchElementRendererListChange(
        from: List<BaseRenderer>,
        to: List<BaseRenderer>,
        op: List<BaseRenderer>,
        reason: Reason
    ) {
        selectorManager.updateSelectorRendererOrder(reason)
        for (listener in renderListenerList) {
            listener.onElementRendererListChange(from, to, op, reason)
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
        onMain {
            for (listener in renderListenerList) {
                listener.onRendererFlagsChange(renderer, oldFlags, newFlags, reason)
            }
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
    @SupportUndo
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

    /**[showRectBounds]*/
    fun showRendererBounds(
        renderer: BaseRenderer?,
        zoomIn: Boolean = false /*自动放大*/,
        zoomOut: Boolean = true /*自动缩小*/,
        margin: Float = BaseControlPoint.DEFAULT_CONTROL_POINT_SIZE
    ) {
        renderer?.let {
            if (it.isVisible) {
                it.renderProperty?.getRenderBounds()?.let { bounds ->
                    showRectBounds(bounds, margin, zoomIn, zoomOut)
                }
            }
        }
    }

    /**[showRectBounds]
     * [showRendererBounds]*/
    fun showBounds(
        @CanvasInsideCoordinate
        rect: RectF?,
        zoomIn: Boolean = false /*自动放大*/,
        zoomOut: Boolean = true /*自动缩小*/,
        margin: Float = BaseControlPoint.DEFAULT_CONTROL_POINT_SIZE
    ) {
        rect?.let {
            showRectBounds(it, margin, zoomIn, zoomOut)
        }
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
        margin: Float = BaseControlPoint.DEFAULT_CONTROL_POINT_SIZE,
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

        var scaleX = renderViewBox.renderMatrix.getScaleX()
        var scaleY = renderViewBox.renderMatrix.getScaleY()

        val visibleWidth = renderViewBox.visibleBoundsInside.width()
        val visibleHeight = renderViewBox.visibleBoundsInside.height()

        if (zoomOut) {
            //需要自动缩小
            if (width > visibleWidth || height > visibleHeight) {
                //目标的宽高, 大于画布当前可见的宽高
                scaleX = (contentWidth - margin * 2) / rect.width()
                scaleY = (contentHeight - margin * 2) / rect.height()
            }
        }

        if (zoomIn) {
            //需要自动放大
            if (width < visibleWidth || height < visibleHeight) {
                //目标的宽高, 小于画布当前可见的宽高
                scaleX = (contentWidth - margin * 2) / rect.width()
                scaleY = (contentHeight - margin * 2) / rect.height()
            }
        }

        if (lockScale) {
            scaleX = min(scaleX, scaleY)
            scaleY = scaleX
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

    /**自动移动画布, 已便能够完全显示[renderer]
     * [margin] 需要偏移的最小距离*/
    fun autoTranslateCanvas(renderer: BaseRenderer?, margin: Float = 4f * dp) {
        renderer ?: return
        val renderProperty = renderer.renderProperty ?: return
        val renderBounds = renderProperty.getRenderBounds()
        val visibleBoundsInside = renderViewBox.visibleBoundsInside
        //计算需要移动的距离x
        val translateX = if (renderBounds.left < visibleBoundsInside.left) {
            visibleBoundsInside.left - renderBounds.left + margin
        } else if (renderBounds.right > visibleBoundsInside.right) {
            visibleBoundsInside.right - renderBounds.right - margin
        } else {
            0f
        }

        //计算需要移动的距离y
        val translateY = if (renderBounds.top < visibleBoundsInside.top) {
            visibleBoundsInside.top - renderBounds.top + margin
        } else if (renderBounds.bottom > visibleBoundsInside.bottom) {
            visibleBoundsInside.bottom - renderBounds.bottom - margin
        } else {
            0f
        }

        if (translateX != 0f || translateY != 0f) {
            //需要移动
            renderViewBox.translateBy(translateX, translateY)
        }
    }

    /**创建一个预览图
     * [bounds] 需要预览的范围
     * [overrideSize] 需要等比输出的大小
     * [rendererList] 指定需要渲染的渲染器, 默认所有渲染器
     * */
    fun preview(
        @Pixel bounds: RectF? = null,
        overrideSize: Float? = null,
        ignoreVisible: Boolean = false,
        rendererList: List<BaseRenderer>? = renderManager.elementRendererList
    ): Bitmap? {
        return CanvasGroupRenderer.createRenderBitmap(
            rendererList,
            overrideSize,
            bounds,
            ignoreVisible
        )
    }

    /**是否禁用所有能够编辑元素的手势
     * [disable] 禁用 or 启动*/
    fun disableEditTouchGesture(disable: Boolean) {
        val enable = !disable
        if (selectorManager.isEnableComponent == enable) {
            return
        }
        selectorManager.isEnableComponent = enable
        controlManager.isEnableComponent = enable
        refresh()
    }

    /**移除所有元素渲染器
     * [CanvasRenderManager]*/
    fun removeAllElementRenderer(reason: Reason, strategy: Strategy = Strategy.normal) {
        renderManager.removeAllElementRenderer(reason, strategy)
    }

    /**选中所有元素渲染器
     * [CanvasRenderManager]
     * [CanvasSelectorManager]
     * */
    fun selectAllElementRenderer(reason: Reason) {
        selectorManager.resetSelectorRenderer(renderManager.elementRendererList, reason)
    }

    /**隐藏所有未选中的元素*/
    fun hideAllNoSelectElementRenderer(
        hide: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        val list = mutableListOf<BaseRenderer>()
        for (renderer in renderManager.elementRendererList) {
            if (!selectorManager.isSelected(renderer)) {
                list.add(renderer)
            }
        }
        renderManager.updateRendererVisible(list, !hide, reason, Strategy.normal, delegate)
    }

    /**将[rendererList]中的渲染器, 过滤筛选成对应的元素渲染器
     * 主要解决, [rendererList]有可能只是其中一个[CanvasGroupRenderer]的子元素*/
    fun getElementRendererListOf(rendererList: List<BaseRenderer>?): List<BaseRenderer>? {
        rendererList ?: return null
        val result = mutableListOf<BaseRenderer>()

        val subList = mutableListOf<BaseRenderer>()
        for (renderer in rendererList) {
            if (subList.contains(renderer)) {
                continue
            }
            val groupRenderer = renderManager.findElementGroupRenderer(renderer)
            if (groupRenderer != null) {
                subList.addAll(groupRenderer.rendererList)
            }
            val elementRenderer = groupRenderer ?: renderer
            result.add(elementRenderer)
        }
        return result
    }

    /**获取所有简单的渲染器*/
    fun getAllSingleElementRendererList(
        dissolveGroup: Boolean = true,
        includeGroup: Boolean = false
    ): List<BaseRenderer> {
        return renderManager.getAllElementRendererList(dissolveGroup, includeGroup)
    }

    /**获取简单的选中元素列表*/
    fun getSelectorSingleElementRendererList(
        dissolveGroup: Boolean = false,
        includeGroup: Boolean = false
    ): List<BaseRenderer>? {
        return if (selectorManager.isSelectorElement) {
            getSingleElementRendererListIn(
                selectorManager.selectorComponent.rendererList,
                dissolveGroup,
                includeGroup
            )
        } else {
            null
        }
    }

    /**获取选中的元素, 如果没有进入选中状态, 则返回所有元素
     * [ignoreVisible] 默认只获取可见的元素
     * [allElement] 是否获取所有元素, 不管是否选中
     * [com.angcyo.canvas.render.core.CanvasRenderManager.getAllElementRendererList]*/
    fun getSelectorOrAllElementRendererList(
        dissolveGroup: Boolean = true,
        includeGroup: Boolean = false,
        ignoreVisible: Boolean = false,
        allElement: Boolean = false,
    ): List<BaseRenderer> {
        val list = if (!allElement && selectorManager.isSelectorElement) {
            selectorManager.selectorComponent.rendererList
        } else {
            renderManager.getAllElementRendererList(dissolveGroup, includeGroup)
        }
        val result = getSingleElementRendererListIn(list, dissolveGroup, includeGroup)
        if (ignoreVisible) {
            return result
        }
        return result.filter { it.isVisible }
    }

    /**为所有的简单渲染器触发一个flag更新通知
     * [flag]
     * [reason]
     * */
    @RenderFlag
    fun dispatchAddAllRendererFlag(
        flag: Int,
        reason: Reason,
        predicate: ((BaseRenderer) -> Boolean)? = null
    ) {
        val allRendererList = renderManager.getAllElementRendererList(true, false)
        allRendererList.filter { predicate == null || predicate(it) }.forEach { renderer ->
            renderer.addRenderFlag(flag, reason, this)
        }
    }

    /**为所有的简单渲染器触发一个数据改变的控制通知 */
    @RenderFlag
    fun dispatchAllRendererDataChange(
        reason: Reason = Reason.user,
        predicate: ((BaseRenderer) -> Boolean)? = null
    ) {
        reason.controlType = reason.controlType ?: BaseControlPoint.CONTROL_TYPE_DATA
        dispatchAddAllRendererFlag(BaseRenderer.RENDERER_FLAG_NORMAL, reason, predicate)
    }

    /**打印渲染器的日志*/
    fun logRendererProperty(rendererList: List<BaseRenderer>? = renderManager.elementRendererList): String? {
        rendererList ?: return null
        val list = getSingleElementRendererListIn(rendererList, true, false)
        val log = buildString {
            append("共:${list.size()}↓")
            for (renderer in list) {
                appendLine()
                append(renderer.hashCode())
                append(":")
                append(renderer.renderProperty?.toShortString() ?: "null")
                renderer.renderProperty?.let {
                    append(" ")
                    append(it.getRenderBounds())
                }
            }
        }
        L.i(log)
        return log
    }

    /**指定的渲染器是否被选中*/
    fun isRendererSelector(renderer: BaseRenderer?): Boolean {
        return getSingleElementRendererListIn(
            selectorManager.selectorComponent.rendererList,
            true,
            true
        ).contains(renderer)
    }

    /**附加覆盖层[cancelSelectedElement] 是否取消当前选中的元素*/
    fun attachOverlay(
        overlay: CanvasOverlayComponent?,
        cancelSelectedElement: Boolean = true,
    ) {
        detachOverlay()
        if (overlay != null) {
            if (cancelSelectedElement && selectorManager.isSelectorElement) {
                selectorManager.clearSelectedElement()
            }
            _canvasOverlayComponent = overlay
            overlay.attachToCanvasDelegate(this)
        }
    }

    /** 移除覆盖层[_overlayComponent] */
    fun detachOverlay() {
        _canvasOverlayComponent?.detachFromCanvasDelegate(this)
        _canvasOverlayComponent = null
        refresh()
    }

    /**
     * 隐藏某个tag的绘制
     * */
    @Api
    fun hideLimitInfoByTag(tag: String?, hide: Boolean = true) {
        renderManager.limitRenderer.hideLimitInfoByTag(tag, hide)
    }

    //endregion---操作---

}