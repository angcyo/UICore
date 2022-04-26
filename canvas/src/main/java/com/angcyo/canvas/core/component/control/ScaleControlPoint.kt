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
import com.angcyo.canvas.utils._tempMatrix
import com.angcyo.canvas.utils._tempValues
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.library.ex.ADJUST_TYPE_CENTER
import com.angcyo.library.ex.ADJUST_TYPE_LT
import com.angcyo.library.ex._drawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class ScaleControlPoint : ControlPoint() {

    companion object {
        /**计算2个在反向旋转后的x,y距离*/
        fun calcRotateBeforeDistance(
            x1: Float, y1: Float,
            x2: Float, y2: Float,
            rotateCenterX: Float, rotateCenterY: Float,
            rotate: Float,
            result: FloatArray = _tempValues
        ): FloatArray {
            val matrix = _tempMatrix
            matrix.reset()
            matrix.postRotate(rotate, rotateCenterX, rotateCenterY)
            matrix.invert(matrix)//逆

            val p1 = matrix.mapPoint(x1, y1)
            val p1x = p1.x
            val p1y = p1.y
            val p2 = matrix.mapPoint(x2, y2)
            val p2x = p2.x
            val p2y = p2.y

            result[0] = p2x - p1x
            result[1] = p2y - p1y

            return result
        }
    }

    /**是否是在元素的中点开始缩放, 否则就是在元素的左上角缩放*/
    var isCenterScale: Boolean = false

    var isLockScaleRatio: Boolean = true
        set(value) {
            field = value
            drawable = if (value) {
                _drawable(R.drawable.control_point_scale)
            } else {
                _drawable(R.drawable.control_point_scale_any)
            }
        }

    val _touchPoint = PointF()
    val _movePoint = PointF()

    //按下时目标的宽高
    var _touchRectWidth = 0f
    var _touchRectHeight = 0f

    /**矩形缩放的锚点, 通常是左上角的坐标. 旋转后的点坐标*/
    val rectScaleAnchorPoint = PointF()

    //手指按下时, 距离矩形的宽高差距
    var touchDiffWidth = 0f
    var touchDiffHeight = 0f

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
                val bounds = itemRenderer.getBounds()
                _touchRectWidth = bounds.width()
                _touchRectHeight = bounds.height()

                //左上角锚点坐标
                rectScaleAnchorPoint.set(bounds.left, bounds.top)
                itemRenderer.mapRotatePoint(rectScaleAnchorPoint, rectScaleAnchorPoint)

                val x1: Float
                val y1: Float
                view.canvasViewBox.mapCoordinateSystemPoint(rectScaleAnchorPoint).apply {
                    x1 = x
                    y1 = y
                }

                val x2: Float
                val y2: Float
                view.canvasViewBox.mapCoordinateSystemPoint(_touchPoint).apply {
                    x2 = x
                    y2 = y
                }

                val cX: Float
                val cY: Float
                view.canvasViewBox.mapCoordinateSystemPoint((x2 + x1) / 2, (y2 + y1) / 2).apply {
                    cX = x
                    cY = y
                }

                calcRotateBeforeDistance(
                    x1,
                    y1,
                    x2,
                    y2,
                    cX,
                    cY,
                    itemRenderer.rotate
                ).apply {
                    touchDiffWidth = this[0] - bounds.width()
                    touchDiffHeight = this[1] - bounds.height()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                _movePoint.set(event.x, event.y)

                val dx = _movePoint.x - _touchPoint.x
                val dy = _movePoint.y - _touchPoint.y

                if (dx != 0f || dy != 0f) {
                    val x1: Float
                    val y1: Float
                    view.canvasViewBox.mapCoordinateSystemPoint(rectScaleAnchorPoint).apply {
                        x1 = x
                        y1 = y
                    }

                    val x2: Float
                    val y2: Float
                    view.canvasViewBox.mapCoordinateSystemPoint(_movePoint).apply {
                        x2 = x
                        y2 = y
                    }

                    val cX: Float
                    val cY: Float
                    view.canvasViewBox.mapCoordinateSystemPoint((x2 + x1) / 2, (y2 + y1) / 2)
                        .apply {
                            cX = x
                            cY = y
                        }

                    //计算两点之间的长宽距离
                    calcRotateBeforeDistance(
                        x1,
                        y1,
                        x2,
                        y2,
                        cX,
                        cY,
                        itemRenderer.rotate
                    ).apply {
                        var newWidth = this[0] - touchDiffWidth
                        var newHeight = this[1] - touchDiffHeight

                        if (isLockScaleRatio) {
                            //等比调整
                            if (_touchRectWidth > _touchRectHeight) {
                                newHeight = _touchRectHeight * newWidth / _touchRectWidth
                            } else {
                                newWidth = _touchRectWidth * newHeight / _touchRectHeight
                            }
                        }

                        view.smartAssistant.smartChangeBounds(
                            itemRenderer,
                            isLockScaleRatio,
                            newWidth,
                            newHeight,
                            dx,
                            dy,
                            if (isCenterScale) ADJUST_TYPE_CENTER else ADJUST_TYPE_LT
                        )
                    }
                    _touchPoint.set(_movePoint)
                }

                /*//直接修改宽高, 这样才会跟手, 但是不适用与矩形旋转后的计算
                val boxScaleX = view.canvasViewBox.getScaleX()
                val boxScaleY = view.canvasViewBox.getScaleY()

                val dx = (_movePoint.x - _touchPoint.x) * 1f / boxScaleX
                val dy = (_movePoint.y - _touchPoint.y) * 1f / boxScaleY

                if (dx != 0f || dy != 0f) {
                    var newWidth = _touchWidth + dx
                    var newHeight = _touchHeight + dy

                    if (isLockScaleRatio) {
                        //等比调整
                        if (dx.absoluteValue > dy.absoluteValue) {
                            newHeight = _touchHeight * newWidth / _touchWidth
                        } else {
                            newWidth = _touchWidth * newHeight / _touchHeight
                        }
                    }
                    L.i("调整宽高->w:$newWidth h:${newHeight} $isCenterScale")
                    view.changeItemBounds(itemRenderer, newWidth, newHeight, isCenterScale)

                    val bounds = itemRenderer.getBounds()
                    _touchWidth = bounds.width()
                    _touchHeight = bounds.height()

                    _touchPoint.set(_movePoint)
                }*/

                //旧方法
                //handleActionMove(view, itemRenderer, event)
            }
        }
        return true
    }

    private fun handleActionMove(
        view: CanvasView,
        itemRenderer: BaseItemRenderer<*>,
        event: MotionEvent
    ) {
        /*val bounds = itemRenderer.getVisualBounds()

        val moveCenterDistance =
            calcCenterDistance(view, itemRenderer, _movePoint, bounds)
        val moveWidthDistance =
            calcWidthDistance(view, itemRenderer, _movePoint, bounds)
        val moveHeightDistance =
            calcHeightDistance(view, itemRenderer, _movePoint, bounds)

        if (view.controlHandler.isLockScaleRatio()) {
            //开始等比缩放
            var scale = moveCenterDistance / _touchCenterDistance
            *//*if (_movePoint.x <= bounds.left || _movePoint.y <= bounds.top) {
                //反向取值, 旋转后会影响此判断
                scale = 1f
            }*//*
            if (!scale.isFinite()) {
                scale = 1f
            }
            scale = min(scale, maxScaleThreshold)
            view.scaleItemBy(itemRenderer, scale, scale, isCenterScale)
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
                view.scaleItemBy(itemRenderer, wScale, hScale, true)
            } else {
                view.scaleItemBy(itemRenderer, wScale, hScale)
            }
            L.i("缩放->w:$wScale h:$hScale")
            _touchWidthDistance = moveWidthDistance
            _touchHeightDistance = moveHeightDistance
        }*/
    }

    /**计算手势点到矩形中点的距离*/
    private fun calcCenterDistance(
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

    private fun calcWidthDistance(
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

    private fun calcHeightDistance(
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