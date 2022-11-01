package com.angcyo.canvas

import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.annotation.UiThread
import androidx.core.graphics.withClip
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.core.*
import com.angcyo.canvas.core.component.*
import com.angcyo.canvas.core.renderer.*
import com.angcyo.canvas.data.CanvasProjectBean
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.CanvasProjectItemBean.Companion.MM_UNIT
import com.angcyo.canvas.data.LimitDataInfo
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer.Companion.ROTATE_FLAG_NORMAL
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.canvas.utils.limitMaxWidthHeight
import com.angcyo.http.base.json
import com.angcyo.http.base.jsonArray
import com.angcyo.http.base.toJson
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.PixelValueUnit
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

        /**所有手势*/
        const val TOUCH_FLAG_ALL = 0xFF

        /**支持手势缩放画布, 包括双击放大*/
        const val TOUCH_FLAG_SCALE = 0x01

        /**支持手势移动画布*/
        const val TOUCH_FLAG_TRANSLATE = 0x02

        /**支持手势多选*/
        const val TOUCH_FLAG_MULTI_SELECT = 0x04
    }

    //<editor-fold desc="成员变量">

    /**视图控制*/
    var viewBox = CanvasViewBox(this)

    /**画布的手势, 用来处理画布的双指平移和捏合缩放*/
    var canvasTouchHandler = CanvasTouchHandler(this)

    /**事件回调*/
    val canvasListenerList = mutableSetOf<ICanvasListener>()

    /**内容绘制之前, 额外的渲染器.
     * 不处理[Matrix]*/
    val rendererBeforeList = mutableSetOf<BaseRenderer>()

    /**内容绘制之后, 额外的渲染器
     * 不处理[Matrix]*/
    val rendererAfterList = mutableSetOf<BaseRenderer>()

    /**最后渲染的渲染器*/
    val rendererLastList = mutableSetOf<BaseRenderer>()

    /**将操作移动到[onSizeChanged]后触发*/
    val pendingTaskList = mutableListOf<Runnable>()

    /**撤销/重做管理*/
    var undoManager: CanvasUndoManager = CanvasUndoManager(this)

    /**智能提示组件*/
    var smartAssistant: SmartAssistant = SmartAssistant(this)

    /**支持的手势类型
     *
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_ALL]
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_SCALE]
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_TRANSLATE]
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_MULTI_SELECT]
     *
     */
    var touchFlag: Int = TOUCH_FLAG_ALL

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
    var boundsOperateHandler = BoundsOperateHandler()

    //</editor-fold desc="内部成员">

    //<editor-fold desc="渲染组件">

    /**绘制在顶上的x轴*/
    var xAxisRender = XAxisRenderer(xAxis, this)

    /**绘制在左边的y轴*/
    var yAxisRender = YAxisRenderer(yAxis, this)

    /**核心项目渲染器*/
    val itemsRendererList = mutableListOf<BaseItemRenderer<*>>()

    /**智能提示组件渲染
     * [rendererLastList]*/
    var smartAssistantRenderer: SmartAssistantRenderer =
        SmartAssistantRenderer(smartAssistant, this)

    /**控制器渲染*/
    var controlRenderer = ControlRenderer(controlHandler, this)

    /**限制提示框渲染*/
    var limitRenderer: LimitRenderer = LimitRenderer(this)

    /**预览边框, 雕刻进度渲染
     * [rendererLastList]*/
    var progressRenderer = ProgressRenderer(this)

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
        rendererAfterList.add(selectGroupRenderer)

        //
        rendererLastList.add(smartAssistantRenderer)
        rendererLastList.add(progressRenderer)

        if (view.isInEditMode) {
            getCanvasViewBox().valueUnit = PixelValueUnit()
        }
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

        rendererLastList.forEach {
            it.block()
        }
    }

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
        val point = getCanvasViewBox().mapCoordinateSystemPoint(touchPoint, acquireTempPointF())

        //多选渲染优先
        val selectedRenderer = getSelectedRenderer()
        if (selectedRenderer is SelectGroupRenderer) {
            if (selectedRenderer.isVisible() && selectedRenderer.containsPoint(point)) {
                point.release()
                return selectedRenderer
            }
        }

        //item渲染
        itemsRendererList.reversed().forEach {
            if (it.isVisible() && it.containsPoint(point)) {
                point.release()
                return it
            }
        }
        point.release()
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

    override fun dispatchItemBoundsChanged(
        itemRenderer: IRenderer,
        reason: Reason,
        oldBounds: RectF
    ) {
        super.dispatchItemBoundsChanged(itemRenderer, reason, oldBounds)
        canvasListenerList.forEach {
            it.onItemBoundsChanged(itemRenderer, reason, oldBounds)
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

    override fun dispatchItemRenderUpdate(item: IRenderer) {
        super.dispatchItemRenderUpdate(item)
        canvasListenerList.forEach {
            it.onItemRenderUpdate(item)
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
                changeBoundsAction {
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

    //<editor-fold desc="View相关">

    /**视图的[Bounds]*/
    val viewBounds: RectF = emptyRectF()

    /**坐标系可见的[Bounds]*/
    val visualBounds: RectF = emptyRectF()

    /**入口点*/
    @CanvasEntryPoint
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
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
    override fun onTouchEvent(event: MotionEvent): Boolean {
        canvasTouchManager.onTouchEvent(event)
        return true
    }

    /**入口点*/
    @CanvasEntryPoint
    override fun onDraw(canvas: Canvas) {
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
        val canvasViewBox = getCanvasViewBox()
        //肉眼可见的矩形范围
        val visualRect = canvasViewBox.getVisualRect(visualBounds)
        canvas.withClip(canvasViewBox.contentRect) {
            canvas.withMatrix(canvasViewBox.matrix) {
                itemsRendererList.forEach { renderer ->
                    if (renderer.isVisible()) {
                        val renderRotateBounds = renderer.getRenderRotateBounds()

                        if (renderRotateBounds.isOutOf(visualRect)) {
                            //超过了肉眼可见范围, 不绘制
                        } else {
                            //item的旋转, 在此处理
                            val bounds = renderer.getRenderBounds()
                            canvas.withRotation(
                                renderer.rotate,
                                bounds.centerX(),
                                bounds.centerY()
                            ) {
                                renderer.render(canvas)
                            }
                        }
                    }
                }
            }
        }

        //后置,不处理matrix
        if (controlRenderer.isVisible()) {
            controlRenderer.render(canvas)
        }

        //after
        rendererAfterList.forEach {
            if (it.isVisible()) {
                it.render(canvas)
            }
        }

        //限制框提示渲染
        if (limitRenderer.isVisible()) {
            canvas.withClip(canvasViewBox.contentRect) {
                canvas.withMatrix(canvasViewBox.matrix) {
                    canvas.withTranslation(
                        canvasViewBox.getCoordinateSystemX(),
                        canvasViewBox.getCoordinateSystemY()
                    ) {
                        limitRenderer.render(canvas)
                    }
                }
            }
        }

        //last
        rendererLastList.forEach {
            if (it.isVisible()) {
                it.render(canvas)
            }
        }
    }

    /**禁用对应的手势类型
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_ALL]
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_SCALE]
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_TRANSLATE]
     * [com.angcyo.canvas.CanvasDelegate.TOUCH_FLAG_MULTI_SELECT]
     * */
    fun disableTouchFlag(flag: Int, disable: Boolean = true) {
        touchFlag = if (disable) touchFlag.remove(flag) else touchFlag.add(flag)
    }

    /**判断是否激活了对应的手势标识*/
    fun isEnableTouchFlag(flag: Int) = touchFlag.have(flag)

    /**刷新界面*/
    override fun refresh() {
        view.postInvalidateOnAnimation()
    }

    /**长按事件反馈提示*/
    fun longFeedback() {
        view.longFeedback()
    }

    //</editor-fold desc="View相关">

    //<editor-fold desc="操作方法">

    /** [itemOrigin] 是否使用最左上角的元素当做原点, 否则就是0,0位置为左上角原点 */
    fun getBitmap(itemOrigin: Boolean = true, outWidth: Int = -1, outHeight: Int = -1): Bitmap {
        //val contentWidth = getCanvasViewBox().getContentWidth()
        //val contentHeight = getCanvasViewBox().getContentHeight()

        var left: Float? = if (itemOrigin) null else 0f
        var top: Float? = if (itemOrigin) null else 0f
        var right: Float? = null
        var bottom: Float? = null

        itemsRendererList.forEach {
            if (it.isVisible()) {
                val bounds = it.getRotateBounds().adjustFlipRect(acquireTempRectF())
                left = min(left ?: bounds.left, bounds.left)
                top = min(top ?: bounds.top, bounds.top)
                right = max(right ?: bounds.right, bounds.right)
                bottom = max(bottom ?: bounds.bottom, bounds.bottom)
                bounds.release()
            }
        }

        left = left ?: 0f
        top = top ?: 0f
        right = right ?: 1f
        bottom = bottom ?: 1f

        return getBitmap(
            left!!,
            top!!,
            (right!! - left!!).toInt(),
            (bottom!! - top!!).toInt(),
            outWidth,
            outHeight
        )
    }

    /**获取指定坐标对应的图片*/
    fun getBitmap(rect: RectF, outWidth: Int = -1, outHeight: Int = -1): Bitmap {
        return getBitmap(
            rect.left,
            rect.top,
            rect.width().toInt(),
            rect.height().toInt(),
            outWidth,
            outHeight
        )
    }

    /**获取视图中指定坐标宽度的图片, 获取画布那个区域的图片
     * [left] [top] 左上角的像素坐标
     * [width] [height] 需要获取的像素高度
     *
     * [outWidth] [outHeight] 输出的图片宽高, 用来实现压缩 -1表示不缩放, 有一个-2表示等比缩放
     * */
    @UiThread
    fun getBitmap(
        left: Float,
        top: Float,
        width: Int,
        height: Int,
        outWidth: Int = -1,
        outHeight: Int = -1
    ): Bitmap {
        val canvasViewBox = getCanvasViewBox()

        val oldBoxRect = emptyRectF()
        oldBoxRect.set(canvasViewBox.contentRect)

        //更新坐标系为0,0
        canvasViewBox.contentRect.set(0f, 0f, width.toFloat(), height.toFloat())

        val bitmapWidth = if (outWidth > 0) outWidth else width
        val bitmapHeight = if (outHeight > 0) outHeight else height
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        var scaleMatrix: Matrix? = null
        if (outWidth > 0 && outHeight > 0) {
            //都指定了宽高, 则规定比例缩放
            scaleMatrix = acquireTempMatrix()
            scaleMatrix.setScale(outWidth * 1f / width, outHeight * 1f / height)
        } else if (outWidth > 0) {
            //高度等比
            scaleMatrix = acquireTempMatrix()
            val scale = outWidth * 1f / width
            scaleMatrix.setScale(scale, scale)
        } else if (outHeight > 0) {
            scaleMatrix = acquireTempMatrix()
            val scale = outHeight * 1f / height
            scaleMatrix.setScale(scale, scale)
        }

        val oldRenderRect = emptyRectF()
        canvas.setMatrix(scaleMatrix)
        canvas.withTranslation(-left, -top) {
            itemsRendererList.forEach { renderer ->
                if (renderer.isVisible()) {
                    //item的旋转, 在此处理
                    val bounds = renderer.getBounds()
                    //替换渲染矩形坐标
                    val renderBounds = renderer.getRenderBounds()
                    oldRenderRect.set(renderBounds)
                    renderBounds.set(bounds)
                    canvas.withRotation(
                        renderer.rotate,
                        bounds.centerX(),
                        bounds.centerY()
                    ) {
                        renderer.render(canvas)
                    }
                    //恢复渲染矩形
                    renderBounds.set(oldRenderRect)
                }
            }
        }
        scaleMatrix?.release()

        //恢复
        canvasViewBox.contentRect.set(oldBoxRect)

        return bitmap
    }

    /**获取画布上的元素数据*/
    fun getCanvasDataBean(
        file_name: String? = null,
        outWidth: Int = -1,
        outHeight: Int = -1
    ): CanvasProjectBean {
        val bitmap = getBitmap(true, outWidth, outHeight)
        val width = MM_UNIT.convertPixelToValue(bitmap.width.toDouble())
        val height = MM_UNIT.convertPixelToValue(bitmap.height.toDouble())

        val data = jsonArray {
            itemsRendererList.forEach {
                try {
                    if (it is DataItemRenderer) {
                        it.getRendererRenderItem()?.dataBean?.let { bean ->
                            add(bean.toJson().json())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.toString()
        return CanvasProjectBean(
            width,
            height,
            bitmap.toBase64Data(),
            data,
            file_name,
            nowTime(),
            nowTime()
        )
    }

    /**通过[uuid], 获取对应的[BaseItemRenderer]*/
    fun getRendererItem(uuid: String?): BaseItemRenderer<*>? {
        if (uuid.isNullOrEmpty()) {
            return null
        }
        val selectedRenderer = getSelectedRenderer()
        if (selectedRenderer?.getRendererRenderItem()?.uuid == uuid) {
            return selectedRenderer
        }
        for (item in itemsRendererList) {
            if (item.getRendererRenderItem()?.uuid == uuid) {
                return item
            }
        }
        return null
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
                    if (_width < ShapesHelper.defaultWidth && _height < ShapesHelper.defaultHeight) {
                        //同时小于最小值, 则放大到最小的限度值
                        if (_width > _height) {
                            val scale = ShapesHelper.defaultWidth / _width
                            _width = ShapesHelper.defaultWidth
                            _height *= scale
                        } else {
                            val scale = ShapesHelper.defaultHeight / _height
                            _height = ShapesHelper.defaultHeight
                            _width *= scale
                        }
                    }

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
                    item.changeBoundsAction(
                        Reason(
                            Reason.REASON_CODE,
                            true,
                            Reason.REASON_FLAG_BOUNDS
                        )
                    ) {
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

    /**[selected] 是否要群组选择[list]*/
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

    /**移除所有元素*/
    fun removeAllItemRenderer(strategy: Strategy = Strategy.normal) {
        removeItemRenderer(itemsRendererList.toList(), strategy)
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
        if (list.isEmpty()) {
            return
        }
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

    /**可见/不可见一组item*/
    fun visibleItemRenderer(list: List<BaseItemRenderer<*>>, visible: Boolean, strategy: Strategy) {
        val last = list.lastOrNull()
        list.forEach { item ->
            item.setVisible(visible, if (item == last) strategy else Strategy.preview)
        }

        refresh()

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    visibleItemRenderer(list, !visible, Strategy.undo)
                }

                override fun runRedo() {
                    visibleItemRenderer(list, visible, Strategy.redo)
                }
            })
        }
    }

    /**复制一组item*/
    fun copyItemRenderer(list: List<BaseItemRenderer<*>>, strategy: Strategy) {
        val copyDataList = mutableListOf<CanvasProjectItemBean>()
        list.forEach { item ->
            if (item is DataItemRenderer) {
                item.dataItem?.dataBean?.let {
                    copyDataList.add(it.copyBean())
                }
            }
        }

        if (copyDataList.isEmpty()) {
            return
        }

        GraphicsHelper.renderItemDataBeanList(this, copyDataList, strategy)
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
        canvasListenerList.forEach {
            it.onSelectedItem(itemRenderer, oldItemRenderer)
        }

        if (itemRenderer == null) {
            if (oldItemRenderer != null) {
                canvasListenerList.forEach {
                    it.onClearSelectItem(oldItemRenderer)
                }
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
        anchor: PointF
    ) {
        itemRenderer.apply {
            scaleBy(scaleX, scaleY, anchor)
            refresh()
        }
    }

    fun scaleItemTo(
        itemRenderer: BaseItemRenderer<*>,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        anchor: PointF
    ) {
        itemRenderer.apply {
            scaleTo(scaleX, scaleY, anchor)
            refresh()
        }
    }

    /**旋转[BaseItemRenderer]*/
    fun rotateItemBy(itemRenderer: BaseItemRenderer<*>, degrees: Float, rotateFlag: Int) {
        itemRenderer.apply {
            rotateBy(degrees, rotateFlag)
            refresh()
        }
    }

    /**改变宽高/平移*/
    fun changeItemBounds(
        itemRenderer: BaseItemRenderer<*>,
        width: Float,
        height: Float,
        anchor: PointF
    ) {
        itemRenderer.apply {
            updateBounds(width, height, anchor)
            refresh()
        }
    }

    /**获取选中的渲染器
     * [BaseItemRenderer]
     * [SelectGroupRenderer]*/
    fun getSelectedRenderer(): BaseItemRenderer<*>? {
        return controlHandler.selectedItemRender
    }

    /**数据渲染开始的坐标位置, 用来实现线段数据合并时需要的偏移量*/
    fun getSelectedStartBounds(): RectF {
        val selectedRenderer = getSelectedRenderer()
        return if (selectedRenderer == null) {
            RectF(0f, 0f, 0f, 0f)
        } else {
            RectF(selectedRenderer.getRotateBounds())
        }
    }

    /**获取所有选中的单元素*/
    fun getSelectedRendererList(): List<BaseItemRenderer<*>> {
        val selectedRenderer = getSelectedRenderer()
        val result = mutableListOf<BaseItemRenderer<*>>()
        if (selectedRenderer is SelectGroupRenderer) {
            result.addAll(selectedRenderer.selectItemList)
        } else if (selectedRenderer != null) {
            result.add(selectedRenderer)
        }
        return result
    }

    /**添加一个限制框数据*/
    fun addAndShowLimitBounds(path: Path, block: LimitDataInfo.() -> Unit = {}) {
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        limitRenderer.addLimit {
            add(LimitDataInfo(path, false).apply(block))
        }
        showRectBounds(pathBounds)
    }

    /**显示一个限制框, 并且移动画布到最佳可视位置*/
    fun showAndResetLimitBounds(path: Path, block: LimitDataInfo.() -> Unit = {}) {
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        limitRenderer.resetLimit {
            add(LimitDataInfo(path, true).apply(block))
        }
        showRectBounds(pathBounds)
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
        val canvasViewBox = getCanvasViewBox()
        if (!canvasViewBox.isCanvasInit()) {
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

        //先将坐标系移动到view的中心
        val coordinateTranslateX =
            canvasViewBox.getContentCenterX() - canvasViewBox.getCoordinateSystemX()
        val coordinateTranslateY =
            canvasViewBox.getContentCenterY() - canvasViewBox.getCoordinateSystemY()

        //再计算目标中心需要偏移的距离量
        val translateX = coordinateTranslateX - rect.centerX()
        val translateY = coordinateTranslateY - rect.centerY()

        val matrix = Matrix()
        //平移
        matrix.setTranslate(translateX, translateY)

        val width = rect.width() + margin * 2
        val height = rect.height() + margin * 2

        val contentWidth = canvasViewBox.getContentWidth()
        val contentHeight = canvasViewBox.getContentHeight()

        var scaleX = 1f
        var scaleY = 1f

        if (width > contentWidth || height > contentHeight) {
            if (scale) {
                //自动缩放
                val scaleCenterX = canvasViewBox.getContentCenterX()
                val scaleCenterY = canvasViewBox.getContentCenterY()

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
            val offset = (canvasViewBox.getContentHeight() - rect.height() * scaleY) / 2 - margin
            matrix.postTranslate(0f, -offset)
        }

        //更新
        canvasViewBox.updateTo(matrix, anim, finish)
    }

    /**改变宽高/平移
     * 支持撤销
     * 支持[SelectGroupRenderer]
     * [toBounds] 需要最终设置的矩形*/
    fun addChangeItemBounds(itemRenderer: BaseItemRenderer<*>, toBounds: RectF) {
        if (!itemRenderer.canChangeBounds(toBounds)) {
            return
        }

        val oldBounds = RectF(itemRenderer.getBounds())
        val newBounds = RectF(toBounds)

        val reason = Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_BOUNDS)
        val step: ICanvasStep

        if (itemRenderer is SelectGroupRenderer) {
            val itemList = mutableListOf<BaseItemRenderer<*>>()
            itemList.addAll(itemRenderer.selectItemList)

            val anchor = PointF().apply {
                set(itemRenderer.getBoundsScaleAnchor())
            }
            val rotate = itemRenderer.rotate
            step = object : ICanvasStep {
                override fun runUndo() {
                    boundsOperateHandler.changeBoundsItemList(
                        itemList,
                        newBounds,
                        oldBounds,
                        anchor,
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_ROTATE)
                    )
                    if (getSelectedRenderer() == itemRenderer) {
                        itemRenderer.updateSelectBounds()
                    }
                }

                override fun runRedo() {
                    boundsOperateHandler.changeBoundsItemList(
                        itemList,
                        oldBounds,
                        newBounds,
                        anchor,
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_ROTATE)
                    )
                    if (getSelectedRenderer() == itemRenderer) {
                        itemRenderer.updateSelectBounds()
                    }
                }
            }
            getCanvasUndoManager().addUndoAction(step)
            itemRenderer.changeBoundsAction(reason) {
                set(newBounds)
            }
        } else {
            step = object : ICanvasStep {
                override fun runUndo() {
                    itemRenderer.changeBoundsAction(reason) {
                        set(oldBounds)
                    }
                }

                override fun runRedo() {
                    itemRenderer.changeBoundsAction(reason) {
                        set(newBounds)
                    }
                }
            }
            getCanvasUndoManager().addUndoAction(step)
            step.runRedo()
        }
    }

    /**改变旋转角度
     * 支持撤销
     * 支持[SelectGroupRenderer]
     * [fromRotate] 从什么角度开始 [toRotate] 旋转到的角度
     * [run] 是否要执行一次
     * */
    fun addChangeItemRotate(
        itemRenderer: BaseItemRenderer<*>,
        fromRotate: Float,
        toRotate: Float,
        run: Boolean = true
    ) {
        val step: ICanvasStep
        if (itemRenderer is SelectGroupRenderer) {
            val itemList = mutableListOf<BaseItemRenderer<*>>()
            itemList.addAll(itemRenderer.selectItemList)
            val bounds = RectF(itemRenderer.getBounds())
            step = object : ICanvasStep {
                override fun runUndo() {
                    boundsOperateHandler.rotateItemList(
                        itemList,
                        fromRotate - toRotate,
                        bounds.centerX(),
                        bounds.centerY(),
                        Reason(Reason.REASON_USER, flag = Reason.REASON_FLAG_ROTATE)
                    )
                    if (getSelectedRenderer() == itemRenderer) {
                        itemRenderer.updateSelectBounds()
                    }
                }

                override fun runRedo() {
                    boundsOperateHandler.rotateItemList(
                        itemList,
                        toRotate - fromRotate,
                        bounds.centerX(),
                        bounds.centerY(),
                        Reason(Reason.REASON_USER, flag = Reason.REASON_FLAG_ROTATE)
                    )
                    if (getSelectedRenderer() == itemRenderer) {
                        itemRenderer.updateSelectBounds()
                    }
                }
            }
        } else {
            step = object : ICanvasStep {
                override fun runUndo() {
                    itemRenderer.rotateBy(fromRotate - toRotate, ROTATE_FLAG_NORMAL)
                }

                override fun runRedo() {
                    itemRenderer.rotateBy(toRotate - fromRotate, ROTATE_FLAG_NORMAL)
                }
            }
        }

        getCanvasUndoManager().addUndoAction(step)
        if (run) {
            step.runRedo()
        }
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
        //刷新
        refresh()
    }

    /**排序
     * [toRendererList] 最后的顺序结果*/
    fun arrangeSort(toRendererList: List<BaseItemRenderer<*>>, strategy: Strategy) {
        val newList = toRendererList.toList()
        val oldList = itemsRendererList
        if (oldList.isChange(newList)) {
            //数据改变过
            val step: ICanvasStep = object : ICanvasStep {
                override fun runUndo() {
                    itemsRendererList.resetAll(oldList)
                    dispatchItemSortChanged(oldList)
                }

                override fun runRedo() {
                    itemsRendererList.resetAll(newList)
                    dispatchItemSortChanged(newList)
                }
            }

            step.runRedo()
            if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
                getCanvasUndoManager().addUndoAction(step)
            }
            //刷新
            refresh()
        }
    }

    //</editor-fold desc="操作方法">
}