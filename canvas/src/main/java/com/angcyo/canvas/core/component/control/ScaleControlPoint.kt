package com.angcyo.canvas.core.component.control

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.R
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.L
import com.angcyo.library.ex._drawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class ScaleControlPoint : ControlPoint() {

    //按下的坐标
    val _touchPoint = PointF()
    val _movePoint = PointF()

    /**按下时保存距离中点的距离*/
    var _touchCenterDistance: Float = 0f

    /**按下时保存距离宽度中点的距离*/
    var _touchWidthDistance: Float = 0f

    /**按下时保存距离高度中点的距离*/
    var _touchHeightDistance: Float = 0f

    /**是否是在元素的中点开始缩放, 否则就是在元素的左上角缩放*/
    var isCenterScale: Boolean = false

    var isLock: Boolean = true
        set(value) {
            field = value
            drawable = if (value) {
                _drawable(R.drawable.control_point_scale)
            } else {
                _drawable(R.drawable.control_point_scale_any)
            }
        }

    init {
        drawable = _drawable(R.drawable.control_point_scale)
    }

    override fun onTouch(
        view: CanvasView,
        itemRenderer: IItemRenderer<*>,
        event: MotionEvent
    ): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchPoint.set(event.x, event.y)
                //按下的时候, 计算按下的点和元素中点坐标的距离
                val bounds = view.canvasViewBox.calcItemVisibleBounds(itemRenderer, _tempRect)

                _touchCenterDistance =
                    calcCenterDistance(itemRenderer, _touchPoint.x, _touchPoint.y, bounds)
                _touchWidthDistance =
                    calcWidthDistance(itemRenderer, _touchPoint.x, _touchPoint.y, bounds)
                _touchHeightDistance =
                    calcHeightDistance(itemRenderer, _touchPoint.x, _touchPoint.y, bounds)
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)
                val bounds = view.canvasViewBox.calcItemVisibleBounds(itemRenderer, _tempRect)

                val moveCenterDistance =
                    calcCenterDistance(itemRenderer, _movePoint.x, _movePoint.y, bounds)
                val moveWidthDistance =
                    calcWidthDistance(itemRenderer, _movePoint.x, _movePoint.y, bounds)
                val moveHeightDistance =
                    calcHeightDistance(itemRenderer, _movePoint.x, _movePoint.y, bounds)

                if (view.controlHandler.isLockScale()) {
                    //开始等比缩放
                    var scale = moveCenterDistance / _touchCenterDistance
                    if (_movePoint.x <= bounds.left || _movePoint.y <= bounds.top) {
                        //反向取值
                        scale = -scale
                    }
                    if (isCenterScale) {
                        view.scaleItemWithCenter(itemRenderer, scale, scale)
                    } else {
                        view.scaleItem(itemRenderer, scale, scale)
                    }
                    L.i("缩放->$scale")
                    _touchCenterDistance = moveCenterDistance
                } else {
                    //开始任意缩放
                    var wScale = moveWidthDistance / _touchWidthDistance
                    var hScale = moveHeightDistance / _touchHeightDistance
                    if (_movePoint.x <= bounds.left) {
                        //反向取值
                        wScale = -wScale
                    }
                    if (_movePoint.y <= bounds.top) {
                        hScale = -hScale
                    }
                    if (isCenterScale) {
                        view.scaleItemWithCenter(itemRenderer, wScale, hScale)
                    } else {
                        view.scaleItem(itemRenderer, wScale, hScale)
                    }
                    L.i("缩放->w:$wScale h:$hScale")
                    _touchWidthDistance = moveWidthDistance
                    _touchHeightDistance = moveHeightDistance
                }

                _touchPoint.set(_movePoint)
            }
        }
        return true
    }

    /**计算点到矩形中点的距离*/
    fun calcCenterDistance(
        itemRenderer: IItemRenderer<*>,
        x: Float,
        y: Float,
        bounds: RectF
    ): Float {
        if (isCenterScale) {
            _tempPoint.set(bounds.centerX(), bounds.centerY())
        } else {
            _tempPoint.set(bounds.left, bounds.top)
        }
        itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)
        return CanvasTouchHandler.spacing(x, y, _tempPoint.x, _tempPoint.y)
    }

    fun calcWidthDistance(
        itemRenderer: IItemRenderer<*>,
        x: Float,
        y: Float,
        bounds: RectF
    ): Float {
        if (isCenterScale) {
            _tempPoint.set(bounds.centerX(), bounds.bottom)
        } else {
            _tempPoint.set(bounds.left, y)
        }
        itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)
        return CanvasTouchHandler.spacing(x, y, _tempPoint.x, _tempPoint.y)
    }

    fun calcHeightDistance(
        itemRenderer: IItemRenderer<*>,
        x: Float,
        y: Float,
        bounds: RectF
    ): Float {
        if (isCenterScale) {
            _tempPoint.set(bounds.right, bounds.centerY())
        } else {
            _tempPoint.set(x, bounds.top)
        }
        itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)
        return CanvasTouchHandler.spacing(x, y, _tempPoint.x, _tempPoint.y)
    }

}