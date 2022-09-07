package com.angcyo.canvas.core.component

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.contains
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasTouch
import com.angcyo.canvas.core.component.control.DeleteControlPoint
import com.angcyo.canvas.core.component.control.LockControlPoint
import com.angcyo.canvas.core.component.control.RotateControlPoint
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.core.renderer.SelectGroupRenderer
import com.angcyo.canvas.data.ControlTouchInfo
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.gesture.DoubleGestureDetector2
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

    //---

    /**按下时的一些信息*/
    var touchDownInfo: ControlTouchInfo? = null

    /**当前手势移动的坐标*/
    val movePoint = PointF()
    val moveSystemPointPoint = PointF()

    val touchPoint = PointF()
    val touchSystemPointPoint = PointF()

    /**
     * 是否触发了双击在同一个[BaseItemRenderer]中
     * */
    var isDoubleTouch: Boolean = false

    //是否移动过
    var isTranslated = false

    /**双击检测*/
    val doubleGestureDetector = DoubleGestureDetector2() {
        touchDownInfo?.let {
            val itemRenderer = canvasDelegate.findItemRenderer(it.touchPoint)
            if (itemRenderer != null) {
                isDoubleTouch = true
                canvasDelegate.dispatchDoubleTapItem(itemRenderer)
            }
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

        val canvasViewBox = canvasDelegate.getCanvasViewBox()

        //第1个手指的id
        val touchPointerId = event.getPointerId(0)
        val x = event.x
        val y = event.y
        updateTouchPoint(x, y, canvasViewBox)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTranslated = false
                touchDownInfo?.release()

                //事件的一些手势信息
                val touchInfo = ControlTouchInfo()
                touchDownInfo = touchInfo
                touchInfo.controlPoint = touchControlPoint
                touchInfo.itemRenderer = selectedItemRender
                touchInfo.itemRenderer?.let {
                    touchInfo.itemBounds.set(it.getBounds())
                }
                touchInfo.touchPointerId = touchPointerId
                touchInfo.updateTouchPoint(x, y, canvasViewBox)

                //
                updateMovePoint(x, y, canvasViewBox)
                val touchPoint = touchInfo.touchPoint

                if (selectedItemRender != null) {
                    //已经有选中, 则查找控制点
                    val controlPoint = findItemControlPoint(touchPoint)
                    touchControlPoint = controlPoint
                    holdControlPoint = controlPoint
                    touchInfo.controlPoint = controlPoint

                    //notify
                    if (controlPoint != null) {
                        selectedItemRender.onControlStart(controlPoint)
                    }
                }

                if (touchControlPoint == null) {
                    //未点在控制点上, 则检查是否点在[BaseItemRenderer]中
                    val itemRenderer = canvasDelegate.findItemRenderer(touchPoint)
                    //selectedItemRender = itemRenderer
                    touchInfo.itemRenderer = itemRenderer
                    touchInfo.itemRenderer?.let {
                        touchInfo.itemBounds.set(it.getBounds())
                    }
                    canvasDelegate.selectedItem(itemRenderer)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                touchControlPoint = null
                //touchPointerId = -1
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchDownInfo?.touchPointerId == touchPointerId) {
                    //按下的手指和移动的手指同一个时, 才处理

                    //L.d("\ntouch:${_touchPoint}\nmove:${_movePoint}")
                    if (touchControlPoint == null) {
                        //没有在控制点上按压时, 才处理item的移动
                        if (selectedItemRender != null && event.pointerCount < 2 /*单手操作*/) {

                            val dx = touchPoint.x - movePoint.x
                            val dy = touchPoint.y - movePoint.y

                            if (dx.absoluteValue >= translateThreshold ||
                                dy.absoluteValue >= translateThreshold
                            ) {
                                //触发了移动
                                //移动的时候不绘制控制点
                                handle = true
                                isTranslated = true
                                canvasDelegate.controlRenderer.drawControlPoint = false

                                val translateX = touchSystemPointPoint.x - moveSystemPointPoint.x
                                val translateY = touchSystemPointPoint.y - moveSystemPointPoint.y
                                canvasDelegate.smartAssistant.smartTranslateItemBy(
                                    selectedItemRender,
                                    translateX,
                                    translateY
                                ).apply {
                                    if (this[0] || this[1]) {
                                        //如果被消耗了, 才更新坐标
                                        //move point
                                        updateMovePoint(x, y, canvasViewBox)
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
                    val touchItemBounds = touchDownInfo?.itemBounds
                    if (touchItemBounds != null && !touchItemBounds.isNoSize() && isTranslated) {

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
                                        originBounds,
                                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                                    )
                                    if (canvasDelegate.getSelectedRenderer() == item) {
                                        item.updateSelectBounds()
                                    }
                                } else {
                                    item.changeBoundsAction {
                                        set(originBounds)
                                    }
                                }
                            }

                            override fun runRedo() {
                                if (item is SelectGroupRenderer) {
                                    canvasDelegate.boundsOperateHandler.changeBoundsItemList(
                                        itemList,
                                        originBounds,
                                        newBounds,
                                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                                    )
                                    if (canvasDelegate.getSelectedRenderer() == item) {
                                        item.updateSelectBounds()
                                    }
                                } else {
                                    item.changeBoundsAction {
                                        set(newBounds)
                                    }
                                }
                            }
                        })
                    }
                }
                isDoubleTouch = false
                touchControlPoint = null
                //touchPointerId = -1
            }
        }

        //控制点
        selectedItemRender?.let {
            //控制点的事件转发
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

    fun updateMovePoint(x: Float, y: Float, canvasViewBox: CanvasViewBox) {
        movePoint.set(x, y)
        canvasViewBox.viewPointToCoordinateSystemPoint(movePoint, moveSystemPointPoint)
    }

    fun updateTouchPoint(x: Float, y: Float, canvasViewBox: CanvasViewBox) {
        touchPoint.set(x, y)
        canvasViewBox.viewPointToCoordinateSystemPoint(touchPoint, touchSystemPointPoint)
    }

    //临时变量
    val _controlPointOffsetRect = RectF()

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
        val point = acquireTempPointF()
        val matrix = acquireTempMatrix()
        point.set(x, y)
        //旋转后的点坐标
        matrix.reset()
        matrix.postRotate(
            itemRenderer.rotate,
            _controlPointOffsetRect.centerX(),
            _controlPointOffsetRect.centerY()
        )
        matrix.mapPoint(point, point)

        controlPoint.bounds.set(
            point.x - controlPointSize / 2,
            point.y - controlPointSize / 2,
            point.x + controlPointSize / 2,
            point.y + controlPointSize / 2
        )

        point.release()
        matrix.release()
    }
}