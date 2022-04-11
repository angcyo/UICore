package com.angcyo.canvas.core.component

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

/** [CanvasView]手势处理类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/02
 */
class CanvasTouchHandler(val canvasView: CanvasView) : BaseComponent() {

    companion object {

        //当前手势意图
        const val TOUCH_TYPE_NONE = 0
        const val TOUCH_TYPE_TRANSLATE = 1
        const val TOUCH_TYPE_SCALE = 2

        /**获取2个点的中点坐标*/
        fun midPoint(x1: Float, y1: Float, x2: Float, y2: Float, result: PointF) {
            result.x = (x1 + x2) / 2f
            result.y = (y1 + y2) / 2f
        }

        fun midPoint(p1: PointF, p2: PointF, result: PointF) {
            midPoint(p1.x, p1.y, p2.x, p2.y, result)
        }

        /**获取2个点之间的距离, 勾股定律*/
        fun spacing(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val x: Float = x2 - x1
            val y: Float = y2 - y1
            return sqrt((x * x + y * y).toDouble()).toFloat()
        }

        fun spacing(p1: PointF, p2: PointF): Float {
            return spacing(p1.x, p1.y, p2.x, p2.y)
        }

        /**获取2个点之间的角度, 非弧度
         * [0~180°] [0~-180°]
         * https://blog.csdn.net/weixin_38351681/article/details/115512792
         *
         * [0~-90]    点2 在第一象限
         * [-90~-180] 点2 在第二象限
         * [90~180]   点2 在第三象限
         * [0~90]     点2 在第四象限
         * */
        fun angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val degrees = 180.0 / Math.PI * atan2((y2 - y1), (x2 - x1))
            return degrees.toFloat()
        }

        fun angle(p1: PointF, p2: PointF): Float {
            return angle(p1.x, p1.y, p2.x, p2.y)
        }

        /**判断2个点是否是想要横向平移
         * 否则就是纵向平移*/
        fun isHorizontalIntent(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
            return (x2 - x1).abs() < (y2 - y1).abs()
        }

        fun isHorizontalIntent(p1: PointF, p2: PointF): Boolean {
            return isHorizontalIntent(p1.x, p1.y, p2.x, p2.y)
        }
    }

    /**当双指捏合的距离大于此值时, 才视为是缩放手势*/
    var minScalePointerDistance = 3.5 * dp

    /**当手指移动的距离大于此值时, 才视为是平移手势*/
    var dragTriggerDistance = 1 * dp

    //手势意图
    var _touchType = TOUCH_TYPE_NONE

    //按下的坐标
    val _touchPoint = PointF()

    //多点处理
    val _touchPointList: MutableList<PointF> = mutableListOf()
    val _movePointList: MutableList<PointF> = mutableListOf()

    val initialPointHandler = InitialPointHandler()

    /**入口*/
    fun onTouch(view: CanvasView, event: MotionEvent): Boolean {
        initialPointHandler.onTouch(view, event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchPoint.set(event.x, event.y)
                obtainPointList(event, _touchPointList)
                handleActionDown()
                view.disableParentInterceptTouchEvent()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //多指按下
                obtainPointList(event, _touchPointList)
                handleActionDown()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                obtainPointList(event, _touchPointList)
            }
            MotionEvent.ACTION_MOVE -> {
                obtainPointList(event, _movePointList)
                handleActionMove()
                obtainPointList(event, _touchPointList)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                _touchPointList.clear()
                _movePointList.clear()
                _touchType = TOUCH_TYPE_NONE
                view.disableParentInterceptTouchEvent(false)
            }
        }
        return true
    }

    /**获取所有手指的点位信息*/
    fun obtainPointList(event: MotionEvent, list: MutableList<PointF>) {
        list.clear()
        for (i in 0 until event.pointerCount) {
            list.add(PointF(event.getX(i), event.getY(i)))
        }
    }

    /**按下时, 2指之间的距离*/
    var _touchDistance: Float = 0f

    /**按下时, 2指中心点坐标*/
    val _touchMiddlePoint = PointF()

    /**手势(多指)按下时, 记录一些数据*/
    fun handleActionDown() {
        _touchDistance = 0f
        if (_touchPointList.size >= 2) {
            _touchDistance = spacing(_touchPointList[0], _touchPointList[1])
            midPoint(_touchPointList[0], _touchPointList[1], _touchMiddlePoint)
        }
    }

    /**各个点位移动的距离*/
    val _moveDistanceList: MutableList<PointF> = mutableListOf()

    /**处理手势移动, 平移/缩放*/
    fun handleActionMove() {
        _moveDistanceList.clear()

        if (_movePointList.size >= 2) {
            //双指 操作

            //处理双指缩放
            if (_touchType == TOUCH_TYPE_NONE ||
                _touchType == TOUCH_TYPE_SCALE ||
                _touchDistance > canvasView.canvasViewBox.getContentWidth() / 3
            ) {
                val moveDistance = spacing(_movePointList[0], _movePointList[1])
                if ((moveDistance - _touchDistance).abs() > minScalePointerDistance) {
                    //开始缩放
                    _touchType = TOUCH_TYPE_SCALE
                    val scale = moveDistance / _touchDistance
                    canvasView.canvasViewBox.scaleBy(
                        scale,
                        scale,
                        _touchMiddlePoint.x,
                        _touchMiddlePoint.y
                    )
                    _touchDistance = moveDistance
                }
            }

            //处理双指平移
            if (_touchType == TOUCH_TYPE_NONE || _touchType == TOUCH_TYPE_TRANSLATE) {
                val dx1 = _movePointList[0].x - _touchPointList[0].x
                val dy1 = _movePointList[0].y - _touchPointList[0].y

                val dx2 = _movePointList[1].x - _touchPointList[1].x
                val dy2 = _movePointList[1].y - _touchPointList[1].y

                val dx = min(dx1, dx2)
                val dy = min(dy1, dy2)

                if (dx.abs() > dragTriggerDistance || dy.abs() > dragTriggerDistance) {
                    //开始平移
                    _touchType = TOUCH_TYPE_TRANSLATE
                    if (isHorizontalIntent(_movePointList[0], _movePointList[1])) {
                        canvasView.canvasViewBox.translateBy(dx, 0f)
                    } else {
                        canvasView.canvasViewBox.translateBy(0f, dy)
                    }
                }
            }
        }

        /*val dx = event.x - touchPoint.x
        val dy = event.y - touchPoint.y
        canvasView.canvasViewBox.translateBy(dx, dy)
        touchPoint.set(event.x, event.y)*/
    }
}