package com.angcyo.canvas

import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.core.*
import com.angcyo.canvas.core.component.*
import com.angcyo.canvas.core.renderer.*
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils._tempPoint
import com.angcyo.canvas.utils._tempRectF
import com.angcyo.canvas.utils.limitMaxWidthHeight
import com.angcyo.library.ex.*
import kotlin.math.max
import kotlin.math.min

/**
 *
 * [ICanvasView]Canvas的代理实现类, 用来脱离[View]的依赖
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/09
 */
class CanvasDelegate(val view: View) : ICanvasView {

    companion object {
        /**前进, 图层上移*/
        const val ARRANGE_FORWARD: Int = 1

        /**后退, 图层下移*/
        const val ARRANGE_BACKWARD: Int = 2

        /**置顶*/
        const val ARRANGE_FRONT: Int = 3

        /**置底*/
        const val ARRANGE_BACK: Int = 4
    }

    //<editor-fold desc="成员变量">

    /**视图控制*/
    var viewBox = CanvasViewBox(this)

    /**画布的手势, 用来处理画布的双指平移和捏合缩放*/
    var canvasTouchHandler = CanvasTouchHandler(this)

    /**事件回调*/
    val canvasListenerList = mutableSetOf<ICanvasListener>()

    /**内容绘制之前, 额外的渲染器*/
    val rendererBeforeList = mutableSetOf<BaseRenderer>()

    /**内容绘制之后, 额外的渲染器*/
    val rendererAfterList = mutableSetOf<BaseRenderer>()

    /**将操作移动到[onSizeChanged]后触发*/
    val pendingTaskList = mutableListOf<Runnable>()

    /**撤销/重做管理*/
    var undoManager: CanvasUndoManager = CanvasUndoManager(this)

    /**智能提示组件*/
    var smartAssistant: SmartAssistant = SmartAssistant(this)

    /**手指是否按下*/
    val isTouchHold: Boolean
        get() = canvasTouchManager.isTouchHold

    /**渲染项的数量*/
    val itemRendererCount: Int
        get() = itemsRendererList.size()

    /**可见渲染项的数量*/
    val itemRendererVisibleCount: Long
        get() = itemsRendererList.sumOf {
            if (it.isVisible()) {
                1L
            } else {
                0L
            }
        }

    //</editor-fold desc="成员变量">

    //<editor-fold desc="内部成员">

    /**x轴坐标计算*/
    var xAxis = XAxis()

    /**y轴坐标计算*/
    var yAxis = YAxis()

    /**控制点[ControlPoint]的控制和[selectedItemRender]的平移控制*/
    var controlHandler = ControlHandler(this)

    /**手势处理*/
    var canvasTouchManager = CanvasTouchManager(this)

    /**[BaseItemRenderer]操作处理*/
    var operateHandler = OperateHandler()

    //</editor-fold desc="内部成员">

    //<editor-fold desc="渲染组件">

    /**绘制在顶上的x轴*/
    var xAxisRender = XAxisRenderer(xAxis, this)

    /**绘制在左边的y轴*/
    var yAxisRender = YAxisRenderer(yAxis, this)

    /**核心项目渲染器*/
    val itemsRendererList = mutableListOf<BaseItemRenderer<*>>()

    /**智能提示组件渲染
     * [rendererAfterList]*/
    var smartAssistantRenderer: SmartAssistantRenderer =
        SmartAssistantRenderer(smartAssistant, this)

    /**控制器渲染*/
    var controlRenderer = ControlRenderer(controlHandler, this)

    /**限制提示框渲染*/
    var limitRenderer: LimitRenderer = LimitRenderer(this)

    /**多选提示框
     * [rendererAfterList]*/
    var selectGroupRenderer: SelectGroupRenderer = SelectGroupRenderer(this)

    //</editor-fold desc="渲染组件">

    //<editor-fold desc="关键方法">

    init {
        rendererAfterList.add(MonitorRenderer(this))
        if (BuildConfig.DEBUG) {
            rendererAfterList.add(CenterRenderer(this))
        }
        rendererAfterList.add(smartAssistantRenderer)
        rendererAfterList.add(selectGroupRenderer)
    }

    /**枚举[BaseAxisRenderer]*/
    fun eachAxisRender(block: BaseAxisRenderer.(axis: BaseAxis) -> Unit) {
        xAxisRender.block(xAxis)
        yAxisRender.block(yAxis)
    }

    /**枚举所有[IRenderer]*/
    fun eachAllRenderer(block: BaseRenderer.() -> Unit) {
        //前置
        rendererBeforeList.forEach {
            it.block()
        }

        //内容
        itemsRendererList.forEach {
            it.block()
        }

        //后置
        if (controlRenderer.isVisible()) {
            controlRenderer.block()
        }

        rendererAfterList.forEach {
            it.block()
        }
    }

    //<editor-fold desc="View相关">

    val viewBounds: RectF = emptyRectF()

    /**入口点*/
    @CanvasEntryPoint
    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewBounds.set(0f, 0f, w.toFloat(), h.toFloat())

        //前测量
        eachAxisRender { axis ->
            if (axis.enable) {
                onCanvasSizeChanged(this@CanvasDelegate)
            }
        }

        //canvasViewBox
        val l = if (yAxisRender.axis.enable) {
            yAxisRender.getRenderBounds().right
        } else {
            0f
        }
        val t = if (xAxisRender.axis.enable) {
            xAxisRender.getRenderBounds().bottom
        } else {
            0f
        }
        if (getCanvasViewBox().getCoordinateSystemY() == -1f && getCanvasViewBox().getCoordinateSystemY() == -1f) {
            getCanvasViewBox().updateCoordinateSystemOriginPoint(l, t)
        }
        getCanvasViewBox().updateContentBox(l, t, w.toFloat(), h.toFloat())

        //需要等待[canvasViewBox]测量后
        eachAllRenderer {
            onCanvasSizeChanged(this@CanvasDelegate)
        }

        //任务
        pendingTaskList.forEach {
            it.run()
        }
        pendingTaskList.clear()
    }

    /**入口点*/
    @CanvasEntryPoint
    fun onDraw(canvas: Canvas) {
        eachAxisRender { axis ->
            if (axis.enable && isVisible()) {
                render(canvas)
            }
        }

        //前置,不处理matrix
        rendererBeforeList.forEach {
            if (it.isVisible()) {
                it.render(canvas)
            }
        }

        //内容, 绘制内容时, 自动使用[matrix]
        canvas.withClip(getCanvasViewBox().contentRect) {
            canvas.withMatrix(getCanvasViewBox().matrix) {
                itemsRendererList.forEach {
                    if (it.isVisible()) {
                        //item的旋转, 在此处理
                        val bounds = it.getRenderBounds()
                        canvas.withRotation(
                            it.rotate,
                            bounds.centerX(),
                            bounds.centerY()
                        ) {
                            it.render(canvas)
                        }
                    }
                }
            }
        }

        //后置,不处理matrix
        if (controlRenderer.isVisible()) {
            controlRenderer.render(canvas)
        }

        rendererAfterList.forEach {
            if (it.isVisible()) {
                it.render(canvas)
            }
        }

        //限制框提示渲染
        if (limitRenderer.isVisible()) {
            canvas.withClip(getCanvasViewBox().contentRect) {
                canvas.withMatrix(getCanvasViewBox().matrix) {
                    canvas.withTranslation(
                        getCanvasViewBox().getCoordinateSystemX(),
                        getCanvasViewBox().getCoordinateSystemY()
                    ) {
                        limitRenderer.render(canvas)
                    }
                }
            }
        }
    }

    /**入口点*/
    @CanvasEntryPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        return canvasTouchManager.onTouchEvent(event)
    }

    /**刷新界面*/
    override fun refresh() {
        view.postInvalidateOnAnimation()
    }

    /**长按事件反馈提示*/
    fun longFeedback() {
        view.longFeedback()
    }

    //</editor-fold desc="View相关">

    override fun getCanvasViewBox(): CanvasViewBox = viewBox

    /**移动坐标原点到View中心*/
    fun moveOriginToCenter() {
        val centerX = getCanvasViewBox().getContentCenterX()
        val centerY = getCanvasViewBox().getContentCenterY()
        getCanvasViewBox().updateCoordinateSystemOriginPoint(centerX, centerY)
    }

    /**移动坐标原点到View左上角*/
    fun moveOriginToLT() {
        val left = getCanvasViewBox().getContentLeft()
        val top = getCanvasViewBox().getContentTop()
        getCanvasViewBox().updateCoordinateSystemOriginPoint(left, top)
    }

    override fun addCanvasListener(listener: ICanvasListener) {
        canvasListenerList.add(listener)
    }

    override fun removeCanvasListener(listener: ICanvasListener) {
        canvasListenerList.remove(listener)
    }

    override fun dispatchCanvasBoxMatrixChangeBefore(matrix: Matrix, newValue: Matrix) {
        canvasListenerList.forEach {
            it.onCanvasBoxMatrixChangeBefore(matrix, newValue)
        }
    }

    override fun findItemRenderer(touchPoint: PointF): BaseItemRenderer<*>? {
        val point = getCanvasViewBox().mapCoordinateSystemPoint(touchPoint, _tempPoint)

        //多选渲染优先
        val selectedRenderer = getSelectedRenderer()
        if (selectedRenderer is SelectGroupRenderer) {
            if (selectedRenderer.isVisible() && selectedRenderer.containsPoint(point)) {
                return selectedRenderer
            }
        }

        itemsRendererList.reversed().forEach {
            if (it.isVisible() && it.containsPoint(point)) {
                return it
            }
        }
        return null
    }

    /**当[Matrix]更新后触发
     * [com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    override fun dispatchCanvasBoxMatrixChanged(matrix: Matrix, oldValue: Matrix) {
        eachAxisRender { axis ->
            if (axis.enable) {
                onCanvasBoxMatrixUpdate(this@CanvasDelegate, matrix, oldValue)
            }
        }
        eachAllRenderer {
            onCanvasBoxMatrixUpdate(this@CanvasDelegate, matrix, oldValue)
        }
        canvasListenerList.forEach {
            it.onCanvasBoxMatrixChanged(matrix, oldValue)
        }
    }

    override fun dispatchItemBoundsChanged(item: IRenderer, reason: Reason, oldBounds: RectF) {
        super.dispatchItemBoundsChanged(item, reason, oldBounds)
        canvasListenerList.forEach {
            it.onItemBoundsChanged(item, reason, oldBounds)
        }
    }

    override fun dispatchItemVisibleChanged(item: IRenderer, visible: Boolean) {
        super.dispatchItemVisibleChanged(item, visible)
        canvasListenerList.forEach {
            it.onItemVisibleChanged(item, visible)
        }
        if (!visible) {
            //不可见
            val selectedRenderer = getSelectedRenderer()
            if (selectedRenderer == item) {
                selectedItem(null)
            }
        }
    }

    override fun dispatchItemLockScaleRatioChanged(item: BaseItemRenderer<*>) {
        super.dispatchItemLockScaleRatioChanged(item)
        canvasListenerList.forEach {
            it.onItemLockScaleRatioChanged(item)
        }
    }

    override fun dispatchCoordinateSystemOriginChanged(point: PointF) {
        super.dispatchCoordinateSystemOriginChanged(point)
        eachAxisRender {
            onCoordinateSystemOriginChanged(point)
        }
        eachAllRenderer {
            if (this is IItemRenderer<*>) {
                changeBounds {
                    //notify changed
                }
            }
        }
    }

    override fun dispatchCoordinateSystemUnitChanged(valueUnit: IValueUnit) {
        super.dispatchCoordinateSystemUnitChanged(valueUnit)
        eachAxisRender {
            onCoordinateSystemUnitChanged(valueUnit)
        }
        /*eachAllRenderer {
            if (this is IItemRenderer<*>) {
                changeBounds {
                    //notify changed
                }
            }
        }*/
    }

    override fun dispatchCanvasUndoChanged() {
        super.dispatchCanvasUndoChanged()
        canvasListenerList.forEach {
            it.onCanvasUndoChanged(getCanvasUndoManager())
        }
    }

    override fun dispatchItemSortChanged(itemList: List<BaseItemRenderer<*>>) {
        super.dispatchItemSortChanged(itemList)
        canvasListenerList.forEach {
            it.onItemSortChanged(itemList)
        }
    }

    override fun getCanvasUndoManager(): CanvasUndoManager = undoManager

    //</editor-fold desc="关键方法">

    //<editor-fold desc="操作方法">

    fun getBitmap(): Bitmap {
        //val contentWidth = getCanvasViewBox().getContentWidth()
        //val contentHeight = getCanvasViewBox().getContentHeight()

        var left = 0f
        var top = 0f
        var right = 0f
        var bottom = 0f

        itemsRendererList.forEach {
            val bounds = it.getRotateBounds().adjustFlipRect(_tempRectF)
            left = min(left, bounds.left)
            top = min(top, bounds.top)
            right = max(right, bounds.right)
            bottom = max(bottom, bounds.bottom)
        }

        return getBitmap(left, top, right - left, bottom - top)
    }

    /**获取视图中指定坐标宽度的图片
     * [left] [top] 左上角的像素坐标
     * [width] [height] 需要获取的像素高度*/
    fun getBitmap(left: Float, top: Float, width: Float, height: Float): Bitmap {
        val oldBoxRect = emptyRectF()
        oldBoxRect.set(getCanvasViewBox().contentRect)

        //更新坐标系为0,0
        getCanvasViewBox().updateContentBox(0f, 0f, width, height)

        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val oldRenderRect = emptyRectF()
        canvas.withTranslation(-left, -top) {
            itemsRendererList.forEach {
                if (it.isVisible()) {
                    //item的旋转, 在此处理
                    val bounds = it.getBounds()
                    //替换渲染矩形坐标
                    val renderBounds = it.getRenderBounds()
                    oldRenderRect.set(renderBounds)
                    renderBounds.set(bounds)
                    canvas.withRotation(
                        it.rotate,
                        bounds.centerX(),
                        bounds.centerY()
                    ) {
                        it.render(canvas)
                    }
                    //恢复渲染矩形
                    renderBounds.set(oldRenderRect)
                }
            }
        }

        //恢复
        getCanvasViewBox().updateContentBox(oldBoxRect)

        return bitmap
    }

    /**默认在当前视图中心添加一个绘制元素*/
    fun addCentreItemRenderer(item: BaseItemRenderer<*>, strategy: Strategy) {
        if (getCanvasViewBox().isCanvasInit()) {
            val bounds = item.getBounds()
            if (item is BaseItemRenderer) {
                if (bounds.isNoSize()) {
                    item.onCanvasSizeChanged(this)
                }
                var _width = bounds.width()
                var _height = bounds.height()

                if (_width > 0 && _height > 0) {
                    //当前可视化的中点坐标

                    val visualRect = getCanvasViewBox().getVisualRect()
                    val maxWidth = visualRect.width() * 3 / 4
                    val maxHeight = visualRect.height() * 3 / 4

                    limitMaxWidthHeight(_width, _height, maxWidth, maxHeight).apply {
                        _width = this[0]
                        _height = this[1]
                    }

                    //更新坐标
                    val rect = getCanvasViewBox().getCoordinateSystemCenterRect(_width, _height)
                    item.changeBounds {
                        set(rect)
                    }
                }
            }
            addItemRenderer(item, strategy)
        } else {
            pendingTaskList.add(Runnable { addCentreItemRenderer(item, strategy) })
        }
    }

    /**添加一个绘制元素*/
    fun addItemRenderer(item: BaseItemRenderer<*>, strategy: Strategy) {
        val itemList = mutableListOf<BaseItemRenderer<*>>()
        if (item is SelectGroupRenderer) {
            itemList.addAll(item.selectItemList)
            item.onAddRenderer()
        } else {
            itemList.add(item)
        }
        addItemRenderer(itemList, strategy)
    }

    fun addItemRenderer(list: List<BaseItemRenderer<*>>, strategy: Strategy) {
        if (getCanvasViewBox().isCanvasInit()) {
            list.forEach { item ->
                if (!itemsRendererList.contains(item)) {
                    if (itemsRendererList.add(item)) {
                        item.onAddRenderer()

                        canvasListenerList.forEach {
                            it.onItemRendererAdd(item)
                        }
                    }
                    if (item is BaseItemRenderer) {
                        if (item._renderBounds.isNoSize()) {
                            item.onCanvasSizeChanged(this)
                        }
                    }
                }
            }
            refresh()

            if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
                getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                    override fun runUndo() {
                        removeItemRenderer(list, Strategy.undo)
                    }

                    override fun runRedo() {
                        addItemRenderer(list, Strategy.redo)
                    }
                })
            }
        } else {
            pendingTaskList.add(Runnable { addItemRenderer(list, strategy) })
        }
    }

    /**移除一个绘制元素*/
    fun removeItemRenderer(item: BaseItemRenderer<*>, strategy: Strategy) {
        val itemList = mutableListOf<BaseItemRenderer<*>>()
        if (item is SelectGroupRenderer) {
            itemList.addAll(item.selectItemList)
            item.onRemoveRenderer()
        } else {
            itemList.add(item)
        }
        removeItemRenderer(itemList, strategy)
    }

    fun removeItemRenderer(list: List<BaseItemRenderer<*>>, strategy: Strategy) {
        itemsRendererList.removeAll(list)
        list.forEach { item ->
            item.onRemoveRenderer()
            canvasListenerList.forEach {
                it.onItemRendererRemove(item)
            }
        }

        if (list.contains(controlHandler.selectedItemRender)) {
            selectedItem(null)
        }
        refresh()

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    addItemRenderer(list, Strategy.undo)
                }

                override fun runRedo() {
                    removeItemRenderer(list, Strategy.redo)
                }
            })
        }
    }

    /**选中item[BaseItemRenderer]*/
    fun selectedItem(itemRenderer: BaseItemRenderer<*>?) {
        if (itemRenderer != null && !itemRenderer.isVisible()) {
            //选中一个不可见的项
            return
        }

        val oldItemRenderer = controlHandler.selectedItemRender

        if (oldItemRenderer != null && oldItemRenderer != itemRenderer) {
            oldItemRenderer.onCancelSelected(itemRenderer)
        }

        controlHandler.selectedItemRender = itemRenderer
        controlHandler.setLockScaleRatio(itemRenderer?.isLockScaleRatio ?: true)

        //通知
        if (itemRenderer == null) {
            if (oldItemRenderer != null) {
                canvasListenerList.forEach {
                    it.onClearSelectItem(oldItemRenderer)
                }
            }
        } else {
            canvasListenerList.forEach {
                it.onSelectedItem(itemRenderer, oldItemRenderer)
            }
        }

        refresh()
    }

    /**分发双击item[BaseItemRenderer]*/
    fun dispatchDoubleTapItem(itemRenderer: BaseItemRenderer<*>) {
        canvasListenerList.forEach {
            it.onDoubleTapItem(itemRenderer)
        }
    }


    /**平移选中的[BaseItemRenderer], 不支持撤销*/
    fun translateItemBy(
        itemRenderer: BaseItemRenderer<*>,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ) {
        itemRenderer.apply {
            translateBy(distanceX, distanceY)
            refresh()
        }
    }

    /**缩放选中的[BaseItemRenderer]*/
    fun scaleItemBy(
        itemRenderer: BaseItemRenderer<*>,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        adjustType: Int = ADJUST_TYPE_LT
    ) {
        itemRenderer.apply {
            scaleBy(scaleX, scaleY, adjustType)
            refresh()
        }
    }

    fun scaleItemTo(
        itemRenderer: BaseItemRenderer<*>,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        adjustType: Int = ADJUST_TYPE_LT
    ) {
        itemRenderer.apply {
            scaleTo(scaleX, scaleY, adjustType)
            refresh()
        }
    }

    /**旋转[BaseItemRenderer]*/
    fun rotateItemBy(itemRenderer: BaseItemRenderer<*>, degrees: Float) {
        itemRenderer.apply {
            rotateBy(degrees)
            refresh()
        }
    }

    /**改变宽高/平移*/
    fun changeItemBounds(
        itemRenderer: BaseItemRenderer<*>,
        width: Float,
        height: Float,
        adjustType: Int = ADJUST_TYPE_LT
    ) {
        itemRenderer.apply {
            updateBounds(width, height, adjustType)
            refresh()
        }
    }

    /**获取选中的渲染器
     * [BaseItemRenderer]
     * [SelectGroupRenderer]*/
    fun getSelectedRenderer(): BaseItemRenderer<*>? {
        return controlHandler.selectedItemRender
    }

    /**显示一个限制框, 并且移动画布到最佳可视位置*/
    fun showAndLimitBounds(path: Path) {
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        limitRenderer.updateLimit {
            addPath(path)
        }
        showRectBounds(pathBounds)
    }

    /**将画板移动到可以完全显示出[rect]
     * [rect] 坐标系中的矩形坐标
     * [scale] 是否要缩放, 以适应过大的矩形
     * [lockScale] 锁定缩放的比例
     * [margin] 边缘额外显示的距离*/
    fun showRectBounds(
        rect: RectF,
        margin: Float = 40f * dp,
        scale: Boolean = true,
        lockScale: Boolean = true,
        anim: Boolean = true
    ) {
        //中心需要偏移的距离量
        val translateX =
            getCanvasViewBox().getContentCenterX() - rect.centerX() - getCanvasViewBox().getCoordinateSystemX()
        val translateY =
            getCanvasViewBox().getContentCenterY() - rect.centerY() - getCanvasViewBox().getCoordinateSystemY()

        val matrix = Matrix()
        matrix.setTranslate(translateX, translateY)

        val width = rect.width() + margin * 2
        val height = rect.height() + margin * 2

        val contentWidth = getCanvasViewBox().getContentWidth()
        val contentHeight = getCanvasViewBox().getContentHeight()

        if (width > contentWidth || height > contentHeight) {
            if (scale) {
                //自动缩放
                val scaleCenterX = getCanvasViewBox().getContentCenterX()
                val scaleCenterY = getCanvasViewBox().getContentCenterY()

                val scaleX = (contentWidth - margin * 2) / rect.width()
                val scaleY = (contentHeight - margin * 2) / rect.height()

                if (lockScale) {
                    val targetScale = min(scaleX, scaleY)

                    matrix.postScale(
                        targetScale,
                        targetScale,
                        scaleCenterX,
                        scaleCenterY
                    )
                } else {
                    matrix.postScale(
                        scaleX,
                        scaleY,
                        scaleCenterX,
                        scaleCenterY
                    )
                }

            }
        } else {
            //不处理自动放大的情况
        }

        //更新
        getCanvasViewBox().updateTo(matrix, anim)
    }

    /**改变宽高/平移
     * 支持撤销
     * 支持[SelectGroupRenderer]
     * [bounds] 需要最终设置的矩形*/
    fun addChangeItemBounds(itemRenderer: BaseItemRenderer<*>, bounds: RectF) {
        val item = itemRenderer
        val originBounds = RectF(item.getBounds())
        val newBounds = RectF(bounds)

        val step: ICanvasStep
        if (item is SelectGroupRenderer) {
            val itemList = mutableListOf<BaseItemRenderer<*>>()
            itemList.addAll(item.selectItemList)
            step = object : ICanvasStep {
                override fun runUndo() {
                    operateHandler.changeBoundsItemList(itemList, newBounds, originBounds)
                    if (getSelectedRenderer() == item) {
                        item.updateSelectBounds()
                    }
                }

                override fun runRedo() {
                    operateHandler.changeBoundsItemList(itemList, originBounds, newBounds)
                    if (getSelectedRenderer() == item) {
                        item.updateSelectBounds()
                    }
                }
            }
        } else {
            step = object : ICanvasStep {
                override fun runUndo() {
                    item.changeBounds {
                        set(originBounds)
                    }
                }

                override fun runRedo() {
                    item.changeBounds {
                        set(newBounds)
                    }
                }
            }
        }
        getCanvasUndoManager().addUndoAction(step)
        step.runRedo()
    }

    /**改变旋转角度
     * 支持撤销
     * 支持[SelectGroupRenderer]
     * [rotate] 需要旋转到的角度*/
    fun addChangeItemRotate(itemRenderer: BaseItemRenderer<*>, rotate: Float) {
        val item = itemRenderer
        val originRotate = itemRenderer.rotate
        val newRotate = rotate
        val bounds = RectF(item.getBounds())

        val step: ICanvasStep
        if (item is SelectGroupRenderer) {
            val itemList = mutableListOf<BaseItemRenderer<*>>()
            itemList.addAll(item.selectItemList)

            step = object : ICanvasStep {
                override fun runUndo() {
                    operateHandler.rotateItemList(
                        itemList, originRotate - newRotate,
                        bounds.centerX(),
                        bounds.centerY()
                    )
                    if (getSelectedRenderer() == item) {
                        item.updateSelectBounds()
                    }
                }

                override fun runRedo() {
                    operateHandler.rotateItemList(
                        itemList, newRotate - originRotate,
                        bounds.centerX(),
                        bounds.centerY()
                    )
                    if (getSelectedRenderer() == item) {
                        item.updateSelectBounds()
                    }
                }
            }
        } else {
            step = object : ICanvasStep {
                override fun runUndo() {
                    item.rotateBy(originRotate - newRotate)
                }

                override fun runRedo() {
                    item.rotateBy(newRotate - originRotate)
                }
            }
        }
        getCanvasUndoManager().addUndoAction(step)
        step.runRedo()
    }

    /**检查[renderer]是否可以执行指定的排序操作*/
    fun canArrange(renderer: BaseItemRenderer<*>, type: Int): Boolean {
        val list = mutableListOf<BaseItemRenderer<*>>()

        if (renderer is SelectGroupRenderer) {
            list.addAll(renderer.selectItemList)
        } else {
            list.add(renderer)
        }
        val first = list.firstOrNull() ?: return false
        val firstIndex = itemsRendererList.indexOf(first)
        if (firstIndex == -1) {
            return false
        }

        val last = list.lastOrNull() ?: return false
        val lastIndex = itemsRendererList.indexOf(last)
        if (lastIndex == -1) {
            return false
        }

        return when (type) {
            //后退, 图层下移
            ARRANGE_BACKWARD, ARRANGE_BACK -> firstIndex != 0
            //前进, 图层上移
            else -> lastIndex != itemsRendererList.lastIndex
        }
    }

    /**安排排序*/
    fun arrange(renderer: BaseItemRenderer<*>, type: Int, strategy: Strategy) {
        val list = mutableListOf<BaseItemRenderer<*>>()

        if (renderer is SelectGroupRenderer) {
            list.addAll(renderer.selectItemList)
        } else {
            list.add(renderer)
        }
        val first = list.firstOrNull() ?: return
        val firstIndex = itemsRendererList.indexOf(first)
        if (firstIndex == -1) {
            return
        }

        val last = list.lastOrNull() ?: return
        val lastIndex = itemsRendererList.indexOf(last)
        if (lastIndex == -1) {
            return
        }

        val toIndex = when (type) {
            //前进, 图层上移
            ARRANGE_BACKWARD -> firstIndex - 1
            ARRANGE_FORWARD -> lastIndex + 1
            ARRANGE_BACK -> 0
            else -> itemsRendererList.lastIndex
        }

        arrangeSort(list, toIndex, strategy)
    }

    /**排序, 将[rendererList], 放到指定的位置[to]
     * [SelectGroupRenderer]
     * */
    fun arrangeSort(rendererList: List<BaseItemRenderer<*>>, to: Int, strategy: Strategy) {
        val first = rendererList.firstOrNull() ?: return
        val last = rendererList.lastOrNull() ?: return

        val firstIndex = itemsRendererList.indexOf(first)
        val lastIndex = itemsRendererList.indexOf(last)

        if (firstIndex == -1 || lastIndex == -1) {
            return
        }

        itemsRendererList.getOrNull(to) ?: return
        val oldRendererList = itemsRendererList.toList()

        itemsRendererList.removeAll(rendererList)
        if (to <= firstIndex) {
            //往前移动
            itemsRendererList.addAll(to, rendererList)
        } else {
            //往后移
            itemsRendererList.addAll(to - (rendererList.size - 1), rendererList)
        }
        val newRendererList = itemsRendererList.toList()

        //撤销回退
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            val step: ICanvasStep = object : ICanvasStep {
                override fun runUndo() {
                    itemsRendererList.resetAll(oldRendererList)
                    dispatchItemSortChanged(oldRendererList)
                }

                override fun runRedo() {
                    itemsRendererList.resetAll(newRendererList)
                    dispatchItemSortChanged(newRendererList)
                }
            }

            getCanvasUndoManager().addUndoAction(step)
            step.runRedo()
        } else {
            dispatchItemSortChanged(newRendererList)
        }
    }

    //</editor-fold desc="操作方法">
}