package com.angcyo.canvas.core.component

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.contains
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.control.CloseControlPoint
import com.angcyo.canvas.core.component.control.LockControlPoint
import com.angcyo.canvas.core.component.control.RotateControlPoint
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.library.L
import com.angcyo.library.ex.*

/**
 * 控制渲染的数据组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlHandler : BaseComponent() {

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

    //缓存
    val _controlPointOffsetRect = RectF()

    //按下的坐标
    val _touchPoint = PointF()
    val _movePoint = PointF()
    var touchPointerId: Int = -1

    //</editor-fold desc="控制点">

    /**手势处理
     * [com.angcyo.canvas.CanvasView.onTouchEvent]*/
    fun onTouch(view: CanvasView, event: MotionEvent): Boolean {
        var handle = false
        var holdControlPoint = touchControlPoint

        val selectedItemRender = selectedItemRender
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchPointerId = event.getPointerId(0)
                _touchPoint.set(event.x, event.y)
                val touchPoint = _touchPoint

                if (selectedItemRender != null) {
                    //已经有选中, 则查找控制点
                    val controlPoint = findItemControlPoint(view.canvasViewBox, touchPoint)
                    touchControlPoint = controlPoint
                    holdControlPoint = controlPoint

                    //notify
                    if (controlPoint != null) {
                        selectedItemRender.onControlStart(controlPoint)
                    }
                }

                if (touchControlPoint == null) {
                    val itemRenderer = view.findItemRenderer(touchPoint)
                    view.selectedItem(itemRenderer)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                touchControlPoint = null
                touchPointerId = -1
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchPointerId == event.getPointerId(0)) {
                    _movePoint.set(event.x, event.y)
                    if (touchControlPoint == null) {
                        //没有在控制点上按压时, 才处理本体的移动
                        if (selectedItemRender != null) {
                            //canvasView.canvasViewBox.matrix.invert(_tempMatrix)
                            //canvasView.canvasViewBox.matrix.mapPoint(_movePointList[0])
                            //val p1 = _tempMatrix.mapPoint(_movePointList[0]) //_movePointList[0]
                            //canvasView.canvasViewBox.matrix.mapPoint(_touchPointList[0])
                            //val p2 = _tempMatrix.mapPoint(_touchPointList[0])//_touchPointList[0]

                            val p1 = view.canvasViewBox.mapCoordinateSystemPoint(_movePoint)
                            val p1x = p1.x
                            val p1y = p1.y

                            val p2 = view.canvasViewBox.mapCoordinateSystemPoint(_touchPoint)
                            val p2x = p2.x
                            val p2y = p2.y

                            val dx1 = p1x - p2x
                            val dy1 = p1y - p2y

                            if (dx1 != 0f || dy1 != 0f) {
                                handle = true
                                view.translateItemBy(selectedItemRender, dx1, dy1)
                                L.i("移动->x:$dx1 y:$dy1")
                            }
                        }
                    } else {
                        handle = true
                    }
                    _touchPoint.set(_movePoint)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //notify
                if (holdControlPoint != null) {
                    selectedItemRender?.let {
                        it.onControlFinish(holdControlPoint)
                    }
                }
                touchControlPoint = null
                view.disableParentInterceptTouchEvent(false)
            }
        }

        //控制点
        selectedItemRender?.let {
            holdControlPoint?.onTouch(view, it, event)
        }

        //result
        val result = selectedItemRender != null || holdControlPoint != null
        if (result) {
            view.disableParentInterceptTouchEvent()
        }
        return result && handle
    }

    /**通过坐标, 找到控制点
     * [touchPoint] 视图坐标点*/
    fun findItemControlPoint(canvasViewBox: CanvasViewBox, touchPoint: PointF): ControlPoint? {
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

        val closeControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_CLOSE }
            ?: createControlPoint(ControlPoint.POINT_TYPE_CLOSE)
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
            ControlPoint.POINT_TYPE_CLOSE -> CloseControlPoint()
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