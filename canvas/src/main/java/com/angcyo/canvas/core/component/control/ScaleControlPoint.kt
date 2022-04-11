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

                _touchCenterDistance = calcCenterDistance(_touchPoint.x, _touchPoint.y, bounds)
                _touchWidthDistance = calcWidthDistance(_touchPoint.x, _touchPoint.y, bounds)
                _touchHeightDistance = calcHeightDistance(_touchPoint.x, _touchPoint.y, bounds)
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)
                val bounds = view.canvasViewBox.calcItemVisibleBounds(itemRenderer, _tempRect)

                val moveCenterDistance = calcCenterDistance(_movePoint.x, _movePoint.y, bounds)
                val moveWidthDistance = calcWidthDistance(_movePoint.x, _movePoint.y, bounds)
                val moveHeightDistance = calcHeightDistance(_movePoint.x, _movePoint.y, bounds)

                if (view.controlHandler.isLockScale()) {
                    //开始等比缩放
                    val scale = moveCenterDistance / _touchCenterDistance
                    if (isCenterScale) {
                        view.scaleItemWithCenter(itemRenderer, scale, scale)
                    } else {
                        view.scaleItem(itemRenderer, scale, scale)
                    }
                    L.i("缩放->$scale")
                    _touchCenterDistance = moveCenterDistance
                } else {
                    //开始任意缩放
                    val wScale = moveWidthDistance / _touchWidthDistance
                    val hScale = moveHeightDistance / _touchHeightDistance
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
    fun calcCenterDistance(x: Float, y: Float, bounds: RectF): Float {
        return if (isCenterScale) {
            CanvasTouchHandler.spacing(x, y, bounds.centerX(), bounds.centerY())
        } else {
            CanvasTouchHandler.spacing(x, y, bounds.left, bounds.top)
        }
    }

    fun calcWidthDistance(x: Float, y: Float, bounds: RectF): Float {
        return if (isCenterScale) {
            CanvasTouchHandler.spacing(x, y, bounds.centerX(), bounds.bottom)
        } else {
            CanvasTouchHandler.spacing(x, y, bounds.left, y)
        }
    }

    fun calcHeightDistance(x: Float, y: Float, bounds: RectF): Float {
        return if (isCenterScale) {
            CanvasTouchHandler.spacing(x, y, bounds.right, bounds.centerY())
        } else {
            CanvasTouchHandler.spacing(x, y, x, bounds.top)
        }
    }

}