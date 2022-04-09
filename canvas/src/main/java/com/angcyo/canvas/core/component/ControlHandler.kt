package com.angcyo.canvas.core.component

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.contains
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.R
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.control.CloseControlPoint
import com.angcyo.canvas.core.component.control.LockControlPoint
import com.angcyo.canvas.core.component.control.RotateControlPoint
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.core.renderer.items.IItemRenderer
import com.angcyo.canvas.utils.mapRectF
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi

/**
 * 控制渲染的数据组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlHandler : BaseComponent() {

    /**当前选中的[IItemRenderer]*/
    var selectedItemRender: IItemRenderer? = null

    /**绘制宽高时的偏移量*/
    var sizeOffset = 4 * dp

    /**当前按下的控制点*/
    var touchControlPoint: ControlPoint? = null

    //<editor-fold desc="控制点">

    /**所有的控制点*/
    val controlPointList = mutableListOf<ControlPoint>()

    /**控制点的大小, 背景圆的直径*/
    var controlPointSize = 20 * dp

    /**图标padding的大小*/
    var controlPointPadding: Int = 4 * dpi

    /**相对于目标点的偏移距离*/
    var controlPointOffset = 4 * dp

    //缓存
    val _controlPointOffsetRect = RectF()

    //按下的坐标
    val _touchPoint = PointF()
    val _movePoint = PointF()

    //</editor-fold desc="控制点">

    /**手势处理*/
    fun onTouch(view: CanvasView, event: MotionEvent): Boolean {
        var holdControlPoint = touchControlPoint

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchPoint.set(event.x, event.y)
                val touchPoint = _touchPoint

                if (selectedItemRender != null) {
                    //已经有选中, 则查找控制点
                    val controlPoint = findItemControlPoint(view.canvasViewBox, touchPoint)
                    touchControlPoint = controlPoint
                    holdControlPoint = controlPoint
                }

                if (touchControlPoint == null) {
                    val itemRenderer = findItemRenderer(view.canvasViewBox, touchPoint)
                    view.selectedItem(itemRenderer)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)
                if (touchControlPoint == null) {
                    //没有在控制点上按压时, 才处理本体的移动
                    selectedItemRender?.let {
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

                        view.translateItem(it, dx1, dy1)
                    }
                }
                _touchPoint.set(_movePoint)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
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
        return result
    }

    /**通过坐标, 找到对应的元素*/
    fun findItemRenderer(canvasViewBox: CanvasViewBox, touchPoint: PointF): IItemRenderer? {
        val point = canvasViewBox.mapCoordinateSystemPoint(touchPoint, _tempPoint)
        canvasViewBox.canvasView.itemsRendererList.reversed().forEach {
            if (it.getRendererBounds().contains(point)) {
                return it
            }
        }
        return null
    }

    /**通过坐标, 找到控制点
     * [touchPoint] 视图坐标点*/
    fun findItemControlPoint(canvasViewBox: CanvasViewBox, touchPoint: PointF): ControlPoint? {
        //val point = canvasViewBox.mapCoordinateSystemPoint(touchPoint, _tempPoint)
        controlPointList.forEach {
            if (it.bounds.contains(touchPoint)) {
                return it
            }
        }
        return null
    }

    /**计算4个控制点的矩形位置坐标
     * [itemRect] 目标元素坐标系的矩形坐标*/
    fun calcControlPointLocation(canvasViewBox: CanvasViewBox, itemRenderer: IItemRenderer) {
        val srcRect = itemRenderer.getRendererBounds()
        val _srcRect = canvasViewBox.matrix.mapRectF(srcRect, _tempRect)
        _controlPointOffsetRect.set(_srcRect)

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
        updateControlPoint(
            closeControl,
            canvasViewBox,
            itemRenderer,
            _controlPointOffsetRect.left,
            _controlPointOffsetRect.top
        )
        updateControlPoint(
            rotateControl,
            canvasViewBox,
            itemRenderer,
            _controlPointOffsetRect.right,
            _controlPointOffsetRect.top,
        )
        updateControlPoint(
            scaleControl,
            canvasViewBox,
            itemRenderer,
            _controlPointOffsetRect.right,
            _controlPointOffsetRect.bottom,
        )
        updateControlPoint(
            lockControl,
            canvasViewBox,
            itemRenderer,
            _controlPointOffsetRect.left,
            _controlPointOffsetRect.bottom,
        )

        controlPointList.clear()
        controlPointList.add(closeControl)
        controlPointList.add(rotateControl)
        controlPointList.add(scaleControl)
        controlPointList.add(lockControl)
    }

    /**创建一个控制点*/
    fun createControlPoint(type: Int): ControlPoint {
        return when (type) {
            ControlPoint.POINT_TYPE_CLOSE -> CloseControlPoint().apply {
                this.type = type
                drawable = _drawable(R.drawable.control_point_close)
            }
            ControlPoint.POINT_TYPE_ROTATE -> RotateControlPoint().apply {
                this.type = type
                drawable = _drawable(R.drawable.control_point_rotate)
            }
            ControlPoint.POINT_TYPE_SCALE -> ScaleControlPoint().apply {
                this.type = type
                drawable = _drawable(R.drawable.control_point_scale)
            }
            ControlPoint.POINT_TYPE_LOCK -> LockControlPoint().apply {
                this.type = type
                drawable = _drawable(R.drawable.control_point_lock)
            }
            else -> ControlPoint().apply {
                this.type = type
            }
        }
    }

    /**更新控制点的位置*/
    fun updateControlPoint(
        controlPoint: ControlPoint,
        canvasViewBox: CanvasViewBox,
        itemRenderer: IItemRenderer,
        x: Float,
        y: Float
    ) {
        _tempPoint.set(x, y)
        val point = itemRenderer.transformer.mapPointF(_tempPoint, _tempPoint)

        controlPoint.bounds.set(
            point.x - controlPointSize / 2,
            point.y - controlPointSize / 2,
            point.x + controlPointSize / 2,
            point.y + controlPointSize / 2
        )
    }
}