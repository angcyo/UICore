package com.angcyo.canvas.core.component.control

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.R
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.L
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.abs

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
                _touchCenterDistance = CanvasTouchHandler.spacing(
                    _touchPoint.x,
                    _touchPoint.y,
                    bounds.centerX(),
                    bounds.centerY()
                )
                _touchWidthDistance = CanvasTouchHandler.spacing(
                    _touchPoint.x,
                    _touchPoint.y,
                    bounds.centerX(),
                    bounds.bottom
                )
                _touchHeightDistance = CanvasTouchHandler.spacing(
                    _touchPoint.x,
                    _touchPoint.y,
                    bounds.right,
                    bounds.centerY()
                )
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)
                val bounds = view.canvasViewBox.calcItemVisibleBounds(itemRenderer, _tempRect)

                val moveCenterDistance = CanvasTouchHandler.spacing(
                    _movePoint.x,
                    _movePoint.y,
                    bounds.centerX(),
                    bounds.centerY()
                )
                val moveWidthDistance = CanvasTouchHandler.spacing(
                    _movePoint.x,
                    _movePoint.y,
                    bounds.centerX(),
                    bounds.bottom
                )
                val moveHeightDistance = CanvasTouchHandler.spacing(
                    _movePoint.x,
                    _movePoint.y,
                    bounds.right,
                    bounds.centerY()
                )

                val minScalePointerDistance = view.canvasTouchHandler.minScalePointerDistance
                if (view.controlHandler.isLockScale()) {
                    if ((moveCenterDistance - _touchCenterDistance).abs() > minScalePointerDistance) {
                        //开始等比缩放
                        val scale = moveCenterDistance / _touchCenterDistance
                        view.scaleItem(itemRenderer, scale, scale)
                        L.i("缩放->$scale")
                        _touchCenterDistance = moveCenterDistance
                    }
                } else {
                    if ((moveWidthDistance - _touchWidthDistance).abs() > minScalePointerDistance ||
                        (moveHeightDistance - _touchHeightDistance).abs() > minScalePointerDistance
                    ) {
                        //开始任意缩放
                        val wScale = moveWidthDistance / _touchWidthDistance
                        val hScale = moveHeightDistance / _touchHeightDistance
                        view.scaleItem(itemRenderer, wScale, hScale)
                        L.i("缩放->w:$wScale h:$hScale")
                        _touchWidthDistance = moveWidthDistance
                        _touchHeightDistance = moveHeightDistance
                    }
                }

                _touchPoint.set(_movePoint)
            }
        }
        return true
    }

}