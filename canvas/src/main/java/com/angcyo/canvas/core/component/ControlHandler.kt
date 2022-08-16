package com.angcyo.canvas.core.component

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.contains
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasTouch
import com.angcyo.canvas.core.component.control.DeleteControlPoint
import com.angcyo.canvas.core.component.control.LockControlPoint
import com.angcyo.canvas.core.component.control.RotateControlPoint
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.core.renderer.SelectGroupRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.gesture.DoubleGestureDetector2
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue

/**
 * 控制渲染的数据组件, 用来实现拖拽元素, 操作控制点等
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlHandler(val canvasDelegate: CanvasDelegate) : BaseComponent(), ICanvasTouch {

    /**当前选中的[IItemRenderer]*/
    var selectedItemRender: BaseItemRenderer<*>? = null

    /**绘制宽高时的偏移量*/
    var sizeOffset = 4 * dp

    /**当前按下的控制点*/
    var touchControlPoint: ControlPoint? = null

    //<editor-fold desc="控制点">

    /**所有的控制点*/
    val controlPointList = mutableListOf<ControlPoint>()

    /**控制点的大小, 背景圆的直径*/
    var controlPointSize = 22 * dp

    /**图标padding的大小*/
    var controlPointPadding: Int = 4 * dpi

    /**相对于目标点的偏移距离*/
    var controlPointOffset = 2 * dp

    /**手指移动多少距离后, 才算作移动了*/
    var translateThreshold = 3

    //缓存
    val _controlPointOffsetRect = emptyRectF()

    //按下的坐标
    val _touchPoint = PointF()
    val _moveStartPoint = PointF()
    val _movePoint = PointF()
    var touchPointerId: Int = -1

    //是否双击在同一个[BaseItemRenderer]中
    var isDoubleTouch: Boolean = false

    /**按下时, 记录bounds 用于恢复*/
    val touchItemBounds = emptyRectF()

    //通过bounds的计算, 来实现平移距离的计算
    val moveItemBounds = emptyRectF()

    //是否移动过
    var isTranslated = false

    /**双击检测*/
    val doubleGestureDetector = DoubleGestureDetector2() {
        val itemRenderer = canvasDelegate.findItemRenderer(_touchPoint)
        if (itemRenderer != null) {
            isDoubleTouch = true
            canvasDelegate.dispatchDoubleTapItem(itemRenderer)
        }
    }

    //</editor-fold desc="控制点">

    /**手势处理
     * [com.angcyo.canvas.CanvasView.onTouchEvent]*/
    override fun onCanvasTouchEvent(canvasDelegate: CanvasDelegate, event: MotionEvent): Boolean {
        doubleGestureDetector.onTouchEvent(event)

        var handle = isDoubleTouch
        var holdControlPoint = touchControlPoint

        val selectedItemRender = selectedItemRender
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchItemBounds.setEmpty()
                isTranslated = false

                touchPointerId = event.getPointerId(0)
                _touchPoint.set(event.x, event.y)
                _moveStartPoint.set(event.x, event.y)
                val touchPoint = _touchPoint

                if (selectedItemRender != null) {
                    //已经有选中, 则查找控制点
                    val controlPoint = findItemControlPoint(touchPoint)
                    touchControlPoint = controlPoint
                    holdControlPoint = controlPoint

                    //notify
                    if (controlPoint != null) {
                        selectedItemRender.onControlStart(controlPoint)
                    }
                }

                if (touchControlPoint == null) {
                    //未点在控制点上, 则检查是否点在[BaseItemRenderer]中
                    val itemRenderer = canvasDelegate.findItemRenderer(touchPoint)
                    if (itemRenderer != null) {
                        touchItemBounds.set(itemRenderer.getBounds())
                    }
                    canvasDelegate.selectedItem(itemRenderer)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                touchControlPoint = null
                touchPointerId = -1
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)

                if (touchPointerId == event.getPointerId(0)) {
                    //L.d("\ntouch:${_touchPoint}\nmove:${_movePoint}")
                    if (touchControlPoint == null) {
                        //没有在控制点上按压时, 才处理本体的移动
                        if (selectedItemRender != null) {
                            //canvasView.canvasViewBox.matrix.invert(_tempMatrix)
                            //canvasView.canvasViewBox.matrix.mapPoint(_movePointList[0])
                            //val p1 = _tempMatrix.mapPoint(_movePointList[0]) //_movePointList[0]
                            //canvasView.canvasViewBox.matrix.mapPoint(_touchPointList[0])
                            //val p2 = _tempMatrix.mapPoint(_touchPointList[0])//_touchPointList[0]

                            val p1 = canvasDelegate.getCanvasViewBox()
                                .mapCoordinateSystemPoint(_movePoint)
                            val p1x = p1.x
                            val p1y = p1.y

                            val p2 = canvasDelegate.getCanvasViewBox()
                                .mapCoordinateSystemPoint(_moveStartPoint)
                            val p2x = p2.x
                            val p2y = p2.y

                            val dx1 = p1x - p2x
                            val dy1 = p1y - p2y

                            if (dx1.absoluteValue > translateThreshold || dy1.absoluteValue > translateThreshold) {
                                handle = true
                                isTranslated = true
                                //移动的时候不绘制控制点
                                canvasDelegate.controlRenderer.drawControlPoint = false
                                canvasDelegate.smartAssistant.smartTranslateItemBy(
                                    selectedItemRender,
                                    dx1,
                                    dy1
                                ).apply {
                                    if (this[0]) {
                                        _moveStartPoint.x = _movePoint.x
                                    }
                                    if (this[1]) {
                                        _moveStartPoint.y = _movePoint.y
                                    }
                                }
                            }
                        }
                    } else {
                        handle = true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //移动的时候不绘制控制点
                canvasDelegate.controlRenderer.drawControlPoint = true
                //notify
                if (holdControlPoint != null) {
                    //控制点操作结束回调
                    selectedItemRender?.let {
                        it.onControlFinish(holdControlPoint)
                    }
                }
                //平移的撤销
                selectedItemRender?.let {
                    if (!touchItemBounds.isNoSize() && isTranslated) {

                        val itemList = mutableListOf<BaseItemRenderer<*>>()
                        if (it is SelectGroupRenderer) {
                            itemList.addAll(it.selectItemList)
                        }

                        canvasDelegate.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                            val item = it
                            val originBounds = RectF(touchItemBounds)
                            val newBounds = RectF(item.getBounds())

                            override fun runUndo() {
                                if (item is SelectGroupRenderer) {
                                    canvasDelegate.boundsOperateHandler.changeBoundsItemList(
                                        itemList,
                                        newBounds,
                                        originBounds
                                    )
                                    if (canvasDelegate.getSelectedRenderer() == item) {
                                        item.updateSelectBounds()
                                    }
                                } else {
                                    item.changeBounds {
                                        set(originBounds)
                                    }
                                }
                            }

                            override fun runRedo() {
                                if (item is SelectGroupRenderer) {
                                    canvasDelegate.boundsOperateHandler.changeBoundsItemList(
                                        itemList,
                                        originBounds,
                                        newBounds
                                    )
                                    if (canvasDelegate.getSelectedRenderer() == item) {
                                        item.updateSelectBounds()
                                    }
                                } else {
                                    item.changeBounds {
                                        set(newBounds)
                                    }
                                }
                            }
                        })
                    }
                }
                isDoubleTouch = false
                touchControlPoint = null
                touchPointerId = -1
            }
        }

        //控制点
        selectedItemRender?.let {
            holdControlPoint?.onTouch(canvasDelegate, it, event)
        }

        //result
        val result = selectedItemRender != null || holdControlPoint != null
        return result && handle
    }

    /**通过坐标, 找到控制点
     * [touchPoint] 视图坐标点*/
    fun findItemControlPoint(touchPoint: PointF): ControlPoint? {
        //val point = canvasViewBox.mapCoordinateSystemPoint(touchPoint, _tempPoint)
        controlPointList.forEach {
            if (it.enable && it.bounds.contains(touchPoint)) {
                return it
            }
        }
        return null
    }

    /**计算4个控制点的矩形位置坐标
     * [itemRect] 目标元素坐标系的矩形坐标*/
    fun calcControlPointLocation(canvasViewBox: CanvasViewBox, itemRenderer: BaseItemRenderer<*>) {
        //将[bounds]转换成视图坐标
        val visualBounds = itemRenderer.getVisualBounds()
        _controlPointOffsetRect.set(visualBounds)

        //在原目标位置, 进行矩形的放大
        val inset = controlPointOffset + controlPointSize / 2
        _controlPointOffsetRect.inset(-inset, -inset)

        val closeControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_DELETE }
            ?: createControlPoint(ControlPoint.POINT_TYPE_DELETE)
        val rotateControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_ROTATE }
            ?: createControlPoint(ControlPoint.POINT_TYPE_ROTATE)
        val scaleControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_SCALE }
            ?: createControlPoint(ControlPoint.POINT_TYPE_SCALE)
        val lockControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_LOCK }
            ?: createControlPoint(ControlPoint.POINT_TYPE_LOCK)

        //矩形是否翻转了
        val bounds = itemRenderer.getBounds()
        val isFlipHorizontal = bounds.isFlipHorizontal
        val isFlipVertical = bounds.isFlipVertical

        val left =
            if (isFlipHorizontal) _controlPointOffsetRect.right else _controlPointOffsetRect.left
        val right =
            if (isFlipHorizontal) _controlPointOffsetRect.left else _controlPointOffsetRect.right

        val top =
            if (isFlipVertical) _controlPointOffsetRect.bottom else _controlPointOffsetRect.top
        val bottom =
            if (isFlipVertical) _controlPointOffsetRect.top else _controlPointOffsetRect.bottom

        updateControlPoint(
            closeControl,
            canvasViewBox,
            itemRenderer,
            left,
            top
        )
        updateControlPoint(
            rotateControl,
            canvasViewBox,
            itemRenderer,
            right,
            top,
        )
        updateControlPoint(
            scaleControl,
            canvasViewBox,
            itemRenderer,
            right,
            bottom,
        )
        updateControlPoint(
            lockControl,
            canvasViewBox,
            itemRenderer,
            left,
            bottom,
        )

        closeControl.enable = itemRenderer.isSupportControlPoint(ControlPoint.POINT_TYPE_DELETE)
        rotateControl.enable = itemRenderer.isSupportControlPoint(ControlPoint.POINT_TYPE_ROTATE)
        scaleControl.enable = itemRenderer.isSupportControlPoint(ControlPoint.POINT_TYPE_SCALE)
        lockControl.enable = itemRenderer.isSupportControlPoint(ControlPoint.POINT_TYPE_LOCK)

        controlPointList.clear()
        controlPointList.add(closeControl)
        controlPointList.add(rotateControl)
        controlPointList.add(scaleControl)
        controlPointList.add(lockControl)
    }

    fun <T : ControlPoint> findControlPoint(type: Int): T? {
        return controlPointList.find { it.type == type } as? T
    }

    /**是否锁定了宽高缩放比例*/
    fun isLockScaleRatio(): Boolean {
        return findControlPoint<LockControlPoint>(ControlPoint.POINT_TYPE_LOCK)?.isLockScaleRatio
            ?: true
    }

    fun setLockScaleRatio(lock: Boolean = true) {
        findControlPoint<LockControlPoint>(ControlPoint.POINT_TYPE_LOCK)?.isLockScaleRatio = lock
        findControlPoint<ScaleControlPoint>(ControlPoint.POINT_TYPE_SCALE)?.isLockScaleRatio = lock
        selectedItemRender?.updateLockScaleRatio(lock)
    }

    /**创建一个控制点*/
    fun createControlPoint(type: Int): ControlPoint {
        return when (type) {
            ControlPoint.POINT_TYPE_DELETE -> DeleteControlPoint()
            ControlPoint.POINT_TYPE_ROTATE -> RotateControlPoint()
            ControlPoint.POINT_TYPE_SCALE -> ScaleControlPoint()
            ControlPoint.POINT_TYPE_LOCK -> LockControlPoint()
            else -> ControlPoint()
        }.apply {
            this.type = type
        }
    }

    /**更新控制点的位置*/
    fun updateControlPoint(
        controlPoint: ControlPoint,
        canvasViewBox: CanvasViewBox,
        itemRenderer: BaseItemRenderer<*>,
        x: Float,
        y: Float
    ) {
        _tempPoint.set(x, y)
        //旋转后的点坐标
        _tempMatrix.reset()
        _tempMatrix.postRotate(
            itemRenderer.rotate,
            _controlPointOffsetRect.centerX(),
            _controlPointOffsetRect.centerY()
        )
        _tempMatrix.mapPoint(_tempPoint, _tempPoint)
        val point = _tempPoint

        controlPoint.bounds.set(
            point.x - controlPointSize / 2,
            point.y - controlPointSize / 2,
            point.x + controlPointSize / 2,
            point.y + controlPointSize / 2
        )
    }
}