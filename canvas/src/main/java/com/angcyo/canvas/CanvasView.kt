package com.angcyo.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.withClip
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import com.angcyo.canvas.core.*
import com.angcyo.canvas.core.component.*
import com.angcyo.canvas.core.renderer.*
import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/29
 */
class CanvasView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet), ICanvasView {

    //<editor-fold desc="成员变量">

    /**视图控制*/
    val canvasViewBox = CanvasViewBox(this)

    /**手势控制*/
    val canvasTouchHandler = CanvasTouchHandler(this)

    /**事件回调*/
    val canvasListenerList = mutableSetOf<ICanvasListener>()

    /**内容绘制之前, 额外的渲染器*/
    val rendererBeforeList = mutableSetOf<BaseRenderer>()

    /**内容绘制之后, 额外的渲染器*/
    val rendererAfterList = mutableSetOf<BaseRenderer>()

    /**将操作移动到[onSizeChanged]后触发*/
    val pendingTaskList = mutableListOf<Runnable>()

    /**撤销/重做管理*/
    val undoManager: CanvasUndoManager = CanvasUndoManager(this)

    //</editor-fold desc="成员变量">

    //<editor-fold desc="横纵坐标轴">

    val xAxis = XAxis()

    /**绘制在顶上的x轴*/
    val xAxisRender = XAxisRenderer(xAxis, canvasViewBox)

    val yAxis = YAxis()

    /**绘制在左边的y轴*/
    val yAxisRender = YAxisRenderer(yAxis, canvasViewBox)

    //</editor-fold desc="横纵坐标轴">

    //<editor-fold desc="渲染组件">

    /**核心项目渲染器*/
    val itemsRendererList = mutableSetOf<BaseItemRenderer<*>>()

    //</editor-fold desc="渲染组件">

    //<editor-fold desc="内部成员">

    val controlHandler = ControlHandler()

    /**控制器渲染*/
    val controlRenderer = ControlRenderer(controlHandler, canvasViewBox)

    //</editor-fold desc="内部成员">

    //<editor-fold desc="关键方法">

    init {
        rendererAfterList.add(MonitorRenderer(canvasViewBox))
        rendererAfterList.add(CenterRenderer(canvasViewBox))
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
        if (controlRenderer._visible) {
            controlRenderer.block()
        }

        rendererAfterList.forEach {
            it.block()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasViewBox._canvasViewWidth = w
        canvasViewBox._canvasViewHeight = h

        //前测量
        eachAxisRender { axis ->
            if (axis.enable) {
                onCanvasSizeChanged(this@CanvasView)
            }
        }

        canvasViewBox.updateContentBox()

        //需要等待[canvasViewBox]测量后
        eachAllRenderer {
            onCanvasSizeChanged(this@CanvasView)
        }

        //任务
        pendingTaskList.forEach {
            it.run()
        }
        pendingTaskList.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        eachAxisRender { axis ->
            if (axis.enable && _visible) {
                render(this@CanvasView, canvas)
            }
        }

        //前置,不处理matrix
        rendererBeforeList.forEach {
            if (it._visible) {
                it.render(this@CanvasView, canvas)
            }
        }

        //内容, 绘制内容时, 自动使用[matrix]
        canvas.withClip(canvasViewBox.contentRect) {
            canvas.withMatrix(canvasViewBox.matrix) {
                itemsRendererList.forEach {
                    if (it._visible) {
                        //item的旋转, 在此处理
                        val bounds = it.getRendererBounds()
                        canvas.withRotation(
                            it.rotate,
                            bounds.centerX(),
                            bounds.centerY()
                        ) {
                            it.render(this@CanvasView, canvas)
                        }
                    }
                }
            }
        }

        //后置,不处理matrix
        if (controlRenderer._visible) {
            controlRenderer.render(this@CanvasView, canvas)
        }

        rendererAfterList.forEach {
            if (it._visible) {
                it.render(this@CanvasView, canvas)
            }
        }
    }

    /**手指是否按下*/
    var isTouchHold: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        isTouchHold = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> true
            else -> false
        }
        canvasListenerList.forEach {
            it.onCanvasTouchEvent(event)
        }
        if (controlHandler.enable) {
            if (controlHandler.onTouch(this, event)) {
                return true
            }
        }
        if (canvasTouchHandler.enable) {
            return canvasTouchHandler.onTouch(this, event)
        }
        return super.onTouchEvent(event)
    }

    /**当[Matrix]更新后触发
     * [com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    fun canvasMatrixUpdate(matrix: Matrix, oldValue: Matrix) {
        eachAxisRender { axis ->
            if (axis.enable && _visible) {
                onCanvasMatrixUpdate(this@CanvasView, matrix, oldValue)
            }
        }
        eachAllRenderer {
            if (_visible) {
                onCanvasMatrixUpdate(this@CanvasView, matrix, oldValue)
            }
        }
    }

    //</editor-fold desc="关键方法">

    //<editor-fold desc="操作方法">

    /**默认在当前视图中心添加一个绘制元素*/
    fun addCentreItemRenderer(
        item: BaseItemRenderer<*>,
        width: Float = ViewGroup.LayoutParams.WRAP_CONTENT.toFloat(),
        height: Float = ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()
    ) {
        if (canvasViewBox.isCanvasInit()) {
            itemsRendererList.add(item)
            if (item is BaseItemRenderer) {
                if (item._bounds.isEmpty) {
                    item.onCanvasSizeChanged(this)
                }

                val _width = if (width == ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()) {
                    item._bounds.width()
                } else {
                    width
                }

                val _height = if (height == ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()) {
                    item._bounds.height()
                } else {
                    height
                }

                if (_width > 0 && _height > 0) {
                    //当前可视化的中点坐标

                    val rect = canvasViewBox.getContentMatrixRect(_width, _height)
                    item.changeBounds {
                        set(rect)
                    }
                }
            }
            postInvalidateOnAnimation()
        } else {
            pendingTaskList.add(Runnable { addCentreItemRenderer(item, width, height) })
        }
    }

    /**添加一个绘制元素*/
    fun addItemRenderer(item: BaseItemRenderer<*>) {
        if (canvasViewBox.isCanvasInit()) {
            itemsRendererList.add(item)
            if (item is BaseItemRenderer) {
                if (item._bounds.isEmpty) {
                    item.onCanvasSizeChanged(this)
                }
            }
            postInvalidateOnAnimation()
        } else {
            pendingTaskList.add(Runnable { addItemRenderer(item) })
        }
    }

    /**移除一个绘制元素*/
    fun removeItemRenderer(item: BaseItemRenderer<*>) {
        itemsRendererList.remove(item)
        if (controlHandler.selectedItemRender == item) {
            selectedItem(null)
        }
        postInvalidateOnAnimation()
    }

    /**选中item[BaseItemRenderer]*/
    fun selectedItem(itemRenderer: BaseItemRenderer<*>?) {
        val oldItemRenderer = controlHandler.selectedItemRender

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

        postInvalidateOnAnimation()
    }

    /**平移选中的[BaseItemRenderer]*/
    fun translateItem(
        itemRenderer: BaseItemRenderer<*>?,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ) {
        itemRenderer?.let {
            it.translateBy(distanceX, distanceY)
            postInvalidateOnAnimation()
        }
    }

    /**缩放选中的[BaseItemRenderer]*/
    fun scaleItem(itemRenderer: BaseItemRenderer<*>?, scaleX: Float = 1f, scaleY: Float = 1f) {
        itemRenderer?.let {
            it.scaleBy(scaleX, scaleY)
            postInvalidateOnAnimation()
        }
    }

    fun scaleItemWithCenter(
        itemRenderer: BaseItemRenderer<*>?,
        scaleX: Float = 1f,
        scaleY: Float = 1f
    ) {
        itemRenderer?.let {
            it.scaleBy(scaleX, scaleY, true)
            postInvalidateOnAnimation()
        }
    }

    /**旋转[BaseItemRenderer]*/
    fun rotateItem(itemRenderer: BaseItemRenderer<*>?, degrees: Float) {
        itemRenderer?.let {
            it.rotateBy(degrees)
            postInvalidateOnAnimation()
        }
    }

    //</editor-fold desc="操作方法">

}