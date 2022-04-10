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
import com.angcyo.canvas.core.*
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.core.component.ControlHandler
import com.angcyo.canvas.core.component.XAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.canvas.core.renderer.*
import com.angcyo.canvas.core.renderer.items.BaseItemRenderer
import com.angcyo.canvas.core.renderer.items.IItemRenderer

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
    val rendererBeforeList = mutableSetOf<IRenderer>()

    /**内容绘制之后, 额外的渲染器*/
    val rendererAfterList = mutableSetOf<IRenderer>()

    /**将操作移动到[onSizeChanged]后触发*/
    val pendingList = mutableListOf<Runnable>()

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
    val itemsRendererList = mutableSetOf<IItemRenderer<*>>()

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasViewBox._canvasViewWidth = w
        canvasViewBox._canvasViewHeight = h

        //前测量

        if (xAxis.enable) {
            xAxisRender.onUpdateRendererBounds(this)
        }
        if (yAxis.enable) {
            yAxisRender.onUpdateRendererBounds(this)
        }

        canvasViewBox.updateContentBox()

        //后测量
        controlRenderer.onUpdateRendererBounds(this)

        rendererBeforeList.forEach {
            it.onUpdateRendererBounds(this)
        }

        itemsRendererList.forEach {
            it.onUpdateRendererBounds(this)
        }

        rendererAfterList.forEach {
            it.onUpdateRendererBounds(this)
        }

        pendingList.forEach {
            it.run()
        }
        pendingList.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (xAxis.enable) {
            if (xAxisRender.visible) {
                xAxisRender.render(canvas)
            }
        }

        if (yAxis.enable) {
            if (yAxisRender.visible) {
                yAxisRender.render(canvas)
            }
        }

        //前置,不处理matrix
        rendererBeforeList.forEach {
            if (it.visible) {
                it.render(canvas)
            }
        }

        //内容, 绘制内容时, 自动使用[matrix]
        canvas.withClip(canvasViewBox.contentRect) {
            canvas.withMatrix(canvasViewBox.matrix) {
                itemsRendererList.forEach {
                    if (it.visible) {
                        it.render(canvas)
                    }
                }
            }
        }

        //后置,不处理matrix
        if (controlRenderer.visible) {
            controlRenderer.render(canvas)
        }

        rendererAfterList.forEach {
            if (it.visible) {
                it.render(canvas)
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
    fun onCanvasMatrixUpdate(matrix: Matrix, oldValue: Matrix) {
        if (xAxis.enable && xAxisRender.visible) {
            xAxisRender.onCanvasMatrixUpdate(matrix, oldValue)
        }

        if (yAxis.enable && yAxisRender.visible) {
            yAxisRender.onCanvasMatrixUpdate(matrix, oldValue)
        }

        //前置,不处理matrix
        rendererBeforeList.forEach {
            if (it.visible) {
                it.onCanvasMatrixUpdate(matrix, oldValue)
            }
        }

        //内容, 绘制内容时, 自动使用[matrix]
        itemsRendererList.forEach {
            if (it.visible) {
                it.onCanvasMatrixUpdate(matrix, oldValue)
            }
        }

        //后置,不处理matrix
        if (controlRenderer.visible) {
            controlRenderer.onCanvasMatrixUpdate(matrix, oldValue)
        }

        rendererAfterList.forEach {
            if (it.visible) {
                it.onCanvasMatrixUpdate(matrix, oldValue)
            }
        }
    }

    //</editor-fold desc="关键方法">

    //<editor-fold desc="操作方法">

    /**默认在当前视图中心添加一个绘制元素*/
    fun addCentreItemRenderer(
        item: IItemRenderer<*>,
        width: Float = ViewGroup.LayoutParams.WRAP_CONTENT.toFloat(),
        height: Float = ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()
    ) {
        if (canvasViewBox.isCanvasInit()) {
            itemsRendererList.add(item)
            if (item is BaseItemRenderer) {
                if (item.bounds.isEmpty) {
                    item.onUpdateRendererBounds(this)
                }

                val _width = if (width == ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()) {
                    item.bounds.width()
                } else {
                    width
                }

                val _height = if (height == ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()) {
                    item.bounds.height()
                } else {
                    height
                }

                if (_width > 0 && _height > 0) {
                    //当前可视化的中点坐标

                    val rect = canvasViewBox.getContentMatrixRect(_width, _height)
                    item.bounds.set(rect)
                }
            }
            postInvalidateOnAnimation()
        } else {
            pendingList.add(Runnable { addCentreItemRenderer(item, width, height) })
        }
    }

    /**添加一个绘制元素*/
    fun addItemRenderer(item: IItemRenderer<*>) {
        if (canvasViewBox.isCanvasInit()) {
            itemsRendererList.add(item)
            if (item is BaseItemRenderer) {
                if (item.bounds.isEmpty) {
                    item.onUpdateRendererBounds(this)
                }
            }
            postInvalidateOnAnimation()
        } else {
            pendingList.add(Runnable { addItemRenderer(item) })
        }
    }

    /**移除一个绘制元素*/
    fun removeItemRenderer(item: IItemRenderer<*>) {
        itemsRendererList.remove(item)
        if (controlHandler.selectedItemRender == item) {
            selectedItem(null)
        }
        postInvalidateOnAnimation()
    }

    /**选中item[IItemRenderer]*/
    fun selectedItem(itemRenderer: IItemRenderer<*>?) {
        val oldItemRenderer = controlHandler.selectedItemRender

        controlHandler.selectedItemRender = itemRenderer

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

    /**平移选中的[IItemRenderer]*/
    fun translateItem(
        itemRenderer: IItemRenderer<*>?,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ) {
        itemRenderer?.let {
            it.translateBy(distanceX, distanceY)
            canvasListenerList.forEach {
                it.onItemMatrixChangeAfter(itemRenderer)
            }

            postInvalidateOnAnimation()
        }
    }

    /**缩放选中的[IItemRenderer]*/
    fun scaleItem(itemRenderer: IItemRenderer<*>?, scaleX: Float = 1f, scaleY: Float = 1f) {
        itemRenderer?.let {
            it.scaleBy(scaleX, scaleY)
            canvasListenerList.forEach {
                it.onItemMatrixChangeAfter(itemRenderer)
            }

            postInvalidateOnAnimation()
        }
    }

    /**旋转[IItemRenderer]*/
    fun rotateItem(itemRenderer: IItemRenderer<*>?, degrees: Float) {
        itemRenderer?.let {
            it.rotateBy(degrees)
            canvasListenerList.forEach {
                it.onItemMatrixChangeAfter(itemRenderer)
            }

            postInvalidateOnAnimation()
        }
    }

    //</editor-fold desc="操作方法">

}