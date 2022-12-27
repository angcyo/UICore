package com.angcyo.canvas.core.component

import android.graphics.PointF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasEntryPoint
import com.angcyo.canvas.core.ICanvasTouch
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.dp
import com.angcyo.library.gesture.DoubleGestureDetector2
import com.angcyo.vector.VectorHelper
import kotlin.math.min

/** [CanvasView]手势处理类, 双击缩放地图, 双指平移, 捏合放大缩小等
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/02
 */
class CanvasTouchHandler(val canvasDelegate: CanvasDelegate) : BaseComponent(), ICanvasTouch {

    companion object {

        //当前手势意图
        const val TOUCH_TYPE_NONE = 0
        const val TOUCH_TYPE_TRANSLATE = 1
        const val TOUCH_TYPE_SCALE = 2
    }

    /**当双指捏合的距离大于此值时, 才视为是缩放手势*/
    var minScalePointerDistance = 8 * dp

    /**双击时, 需要放大的比例*/
    var doubleScaleValue = 1.5f

    /**当手指移动的距离大于此值时, 才视为是平移手势*/
    var dragTriggerDistance = 1 * dp

    //手势意图
    var _touchType = TOUCH_TYPE_NONE

    //按下的坐标
    val _touchPoint = PointF()

    //多点处理
    val _touchPointList: MutableList<PointF> = mutableListOf()
    val _movePointList: MutableList<PointF> = mutableListOf()

    //左上角初始点处理, 点击后恢复原位置
    val initialPointHandler = InitialPointHandler()

    //是否双击了
    var isDoubleTouch: Boolean = false

    /**双击检测*/
    val doubleGestureDetector = DoubleGestureDetector2(canvasDelegate.view.context) { event ->
        if (canvasDelegate.controlHandler.selectedItemRender == null) {
            if (canvasDelegate.isEnableTouchFlag(CanvasDelegate.TOUCH_FLAG_SCALE)) {
                isDoubleTouch = true
                //双击
                canvasDelegate.getCanvasViewBox().scaleBy(
                    doubleScaleValue,
                    doubleScaleValue,
                    event.x,
                    event.y,
                    true
                )
                //L.e("${event.x} ${event.y}")
            }
        }
    }

    /**入口*/
    @CanvasEntryPoint
    override fun onCanvasTouchEvent(canvasDelegate: CanvasDelegate, event: MotionEvent): Boolean {
        if (canvasDelegate.controlHandler.isTouchInControlPoint()) {
            //有控制点按下时, 不处理手势
            return false
        }

        initialPointHandler.onTouch(canvasDelegate, event)
        doubleGestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchPoint.set(event.x, event.y)
                obtainPointList(event, _touchPointList)
                handleActionDown(event, canvasDelegate)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //多指按下
                obtainPointList(event, _touchPointList)
                handleActionDown(event, canvasDelegate)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                obtainPointList(event, _touchPointList)
            }
            MotionEvent.ACTION_MOVE -> {
                obtainPointList(event, _movePointList)
                if (handleActionMove(event, canvasDelegate)) {
                    obtainPointList(event, _touchPointList)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDoubleTouch = false
                _touchPointList.clear()
                _movePointList.clear()
                _touchType = TOUCH_TYPE_NONE
                canvasDelegate.selectGroupRenderer.endSelect()
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
    fun handleActionDown(event: MotionEvent, canvasDelegate: CanvasDelegate) {
        _touchDistance = 0f

        val selectedRenderer = canvasDelegate.getSelectedRenderer()
        if (_touchPointList.size >= 2) {
            _touchDistance = VectorHelper.spacing(_touchPointList[0], _touchPointList[1])
            VectorHelper.midPoint(_touchPointList[0], _touchPointList[1], _touchMiddlePoint)
            isDoubleTouch = false

            if (selectedRenderer != null) {
                val touchPoint = acquireTempPointF()
                touchPoint.set(event.getX(event.actionIndex), event.getY(event.actionIndex))
                val nextSelectedRenderer = canvasDelegate.findItemRenderer(touchPoint)
                if (nextSelectedRenderer != null) {
                    canvasDelegate.selectGroupRenderer.addSelectedRenderer(nextSelectedRenderer)
                }
                touchPoint.release()
            } else {
                if (canvasDelegate.isEnableTouchFlag(CanvasDelegate.TOUCH_FLAG_MULTI_SELECT)) {
                    canvasDelegate.selectGroupRenderer.endSelect()
                }
            }
        } else {
            if (selectedRenderer == null) {
                //未选中渲染器
                if (canvasDelegate.isEnableTouchFlag(CanvasDelegate.TOUCH_FLAG_MULTI_SELECT)) {
                    canvasDelegate.selectGroupRenderer.startSelect(
                        _touchPointList[0].x,
                        _touchPointList[0].y
                    )
                }
            }
        }
    }

    /**各个点位移动的距离*/
    val _moveDistanceList: MutableList<PointF> = mutableListOf()

    /**处理手势移动, 平移/缩放
     * @return 表示是否消耗了当前的事件
     * */
    fun handleActionMove(event: MotionEvent, canvasDelegate: CanvasDelegate): Boolean {
        _moveDistanceList.clear()

        val canvasViewBox = canvasDelegate.getCanvasViewBox()

        val dx1 = _movePointList[0].x - _touchPointList[0].x
        val dy1 = _movePointList[0].y - _touchPointList[0].y

        var handle = false

        if (_movePointList.size >= 2) {
            //双指 操作, 平移和缩放

            //处理双指缩放
            val moveDistance = VectorHelper.spacing(_movePointList[0], _movePointList[1])
            if ((moveDistance - _touchDistance).abs() >= minScalePointerDistance &&
                canvasDelegate.isEnableTouchFlag(CanvasDelegate.TOUCH_FLAG_SCALE) //激活了缩放手势
            ) {
                //开始缩放
                _touchType = TOUCH_TYPE_SCALE
                val scale = moveDistance / _touchDistance
                //canvasViewBox.coordinateSystemPointToViewPoint(_touchMiddlePoint2)
                //VectorHelper.midPoint(_movePointList[0], _movePointList[1], _touchMiddlePoint)
                val point = _touchMiddlePoint
                canvasViewBox.scaleBy(scale, scale, point.x, point.y)

                _touchDistance = moveDistance
                handle = true
            }

            if (!handle) {
                //没有被缩放处理
                val dx2 = _movePointList[1].x - _touchPointList[1].x
                val dy2 = _movePointList[1].y - _touchPointList[1].y

                val dx = min(dx1, dx2)
                val dy = min(dy1, dy2)

                if ((dx.abs() > dragTriggerDistance || dy.abs() > dragTriggerDistance) &&
                    canvasDelegate.isEnableTouchFlag(CanvasDelegate.TOUCH_FLAG_TRANSLATE)//激活了平移手势
                ) {
                    //开始平移
                    _touchType = TOUCH_TYPE_TRANSLATE
                    //不能用两个点的方向去判断平移意图, 而应该用各个方向的最大移动距离来判断
                    /*if (VectorHelper.isHorizontalIntent(_movePointList[0], _movePointList[1])) {
                        canvasViewBox.translateBy(dx, 0f, false)
                    } else {
                        canvasViewBox.translateBy(0f, dy, false)
                    }*/

                    if (dx.abs() > dy.abs()) {
                        //水平平移意图
                        canvasViewBox.translateBy(dx, 0f, false)
                    } else {
                        //垂直平移意图
                        canvasViewBox.translateBy(0f, dy, false)
                    }

                    handle = true
                }
            }
        } else {
            if (canvasDelegate.isEnableTouchFlag(CanvasDelegate.TOUCH_FLAG_MULTI_SELECT) &&
                event.pointerCount < 2
            ) {
                //移动多选框
                canvasDelegate.selectGroupRenderer.moveSelect(
                    _movePointList[0].x,
                    _movePointList[0].y
                )
                handle = true
            }
        }

        return handle
    }
}