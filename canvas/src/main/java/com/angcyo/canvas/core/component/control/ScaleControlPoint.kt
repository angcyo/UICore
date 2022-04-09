package com.angcyo.canvas.core.component.control

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.renderer.items.IItemRenderer
import com.angcyo.library.ex.abs

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class ScaleControlPoint : ControlPoint() {

    //按下的坐标
    val _touchPoint = PointF()
    val _movePoint = PointF()

    /**按下时保存的距离*/
    var _touchDistance: Float = 0f

    override fun onTouch(
        view: CanvasView,
        itemRenderer: IItemRenderer,
        event: MotionEvent
    ): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchPoint.set(event.x, event.y)
                //按下的时候, 计算按下的点和元素中点坐标的距离
                val bounds = view.canvasViewBox.calcItemVisibleBounds(itemRenderer, _tempRect)
                _touchDistance = CanvasTouchHandler.spacing(
                    _touchPoint.x,
                    _touchPoint.y,
                    bounds.centerX(),
                    bounds.centerY()
                )
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)
                val bounds = view.canvasViewBox.calcItemVisibleBounds(itemRenderer, _tempRect)
                val moveDistance = CanvasTouchHandler.spacing(
                    _movePoint.x,
                    _movePoint.y,
                    bounds.centerX(),
                    bounds.centerY()
                )
                if ((moveDistance - _touchDistance).abs() > view.canvasTouchHandler.minScalePointerDistance) {
                    //开始缩放
                    val scale = moveDistance / _touchDistance
                    view.scaleItem(itemRenderer, scale, scale)

                    _touchDistance = moveDistance
                }
                _touchPoint.set(_movePoint)
            }
        }
        return true
    }

}