package com.angcyo.canvas.core.component.control

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.R
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.L
import com.angcyo.library.ex._drawable
import kotlin.math.min

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

    /**最大的缩放阈值, 防止瞬间变大*/
    var maxScaleThreshold = 2f

    var isLockScaleRatio: Boolean = true
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
        itemRenderer: BaseItemRenderer<*>,
        event: MotionEvent
    ): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchPoint.set(event.x, event.y)
                //按下的时候, 计算按下的点和元素中点坐标的距离
                val bounds = itemRenderer.getVisualBounds()

                _touchCenterDistance =
                    calcCenterDistance(view, itemRenderer, _touchPoint, bounds)
                _touchWidthDistance =
                    calcWidthDistance(view, itemRenderer, _touchPoint, bounds)
                _touchHeightDistance =
                    calcHeightDistance(view, itemRenderer, _touchPoint, bounds)
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)
                val bounds = itemRenderer.getVisualBounds()

                val moveCenterDistance =
                    calcCenterDistance(view, itemRenderer, _movePoint, bounds)
                val moveWidthDistance =
                    calcWidthDistance(view, itemRenderer, _movePoint, bounds)
                val moveHeightDistance =
                    calcHeightDistance(view, itemRenderer, _movePoint, bounds)

                if (view.controlHandler.isLockScaleRatio()) {
                    //开始等比缩放
                    var scale = moveCenterDistance / _touchCenterDistance
                    if (_movePoint.x <= bounds.left || _movePoint.y <= bounds.top) {
                        //反向取值
                        scale = 1f
                    }
                    if (!scale.isFinite()) {
                        scale = 1f
                    }
                    scale = min(scale, maxScaleThreshold)
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
                        wScale = 1f
                    }
                    if (_movePoint.y <= bounds.top) {
                        hScale = 1f
                    }
                    if (!wScale.isFinite()) {
                        wScale = 1f
                    }
                    if (!hScale.isFinite()) {
                        hScale = 1f
                    }
                    wScale = min(wScale, maxScaleThreshold)
                    hScale = min(hScale, maxScaleThreshold)
                    if (isCenterScale || !isLockScaleRatio) {
                        //todo 解锁状态下, 暂时没有办法解决左上角缩放, 坐标飘逸的bug
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

    /**计算手势点到矩形中点的距离*/
    fun calcCenterDistance(
        view: CanvasView,
        itemRenderer: IItemRenderer<*>,
        touchPoint: PointF,
        bounds: RectF
    ): Float {
        if (isCenterScale) {
            _tempPoint.set(bounds.centerX(), bounds.centerY())
        } else {
            _tempPoint.set(bounds.left, bounds.top)
        }
        //itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)

        val x2 = _tempPoint.x
        val y2 = _tempPoint.y

        val x1 = touchPoint.x
        val y1 = touchPoint.y

        return CanvasTouchHandler.spacing(x1, y1, x2, y2)
    }

    fun calcWidthDistance(
        view: CanvasView,
        itemRenderer: IItemRenderer<*>,
        touchPoint: PointF,
        bounds: RectF
    ): Float {
        if (isCenterScale) {
            _tempPoint.set(bounds.centerX(), bounds.bottom)
        } else {
            _tempPoint.set(bounds.left, touchPoint.y)
        }
        //itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)
        val x2 = _tempPoint.x
        val y2 = _tempPoint.y

        val x1 = touchPoint.x
        val y1 = touchPoint.y

        return CanvasTouchHandler.spacing(x1, y1, x2, y2)
    }

    fun calcHeightDistance(
        view: CanvasView,
        itemRenderer: IItemRenderer<*>,
        touchPoint: PointF,
        bounds: RectF
    ): Float {
        if (isCenterScale) {
            _tempPoint.set(bounds.right, bounds.centerY())
        } else {
            _tempPoint.set(touchPoint.x, bounds.top)
        }
        //itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)
        val x2 = _tempPoint.x
        val y2 = _tempPoint.y

        val x1 = touchPoint.x
        val y1 = touchPoint.y

        return CanvasTouchHandler.spacing(x1, y1, x2, y2)
    }

}