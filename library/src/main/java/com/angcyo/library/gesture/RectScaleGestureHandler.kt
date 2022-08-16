package com.angcyo.library.gesture

import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.model.toRectPoint

/**
 * 矩形缩放手势处理, 支持旋转参数
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/16
 */
class RectScaleGestureHandler {

    companion object {

        /**touch在矩形的4条边上*/
        const val RECT_LEFT = 1
        const val RECT_TOP = 2
        const val RECT_RIGHT = 3
        const val RECT_BOTTOM = 4

        /**touch在矩形的4个角上*/
        const val RECT_LT = 5
        const val RECT_RT = 6
        const val RECT_RB = 7
        const val RECT_LB = 8
    }

    /**当矩形缩放改变时的回调
     * [rect] 实时改变的矩形
     * [end] 手势是否结束*/
    var onRectScaleChangeAction: (rect: RectF, end: Boolean) -> Unit = { rect, end ->
        L.i(rect)
    }

    //目标矩形
    var _targetRect: RectF? = null

    //矩形旋转的角度
    var _rotate: Float = 0f

    //按下的位置
    var _rectPosition: Int = 0

    /**在[MotionEvent.ACTION_DOWN]时, 初始化操作数据*/
    @CallPoint
    fun initialize(rect: RectF, rotate: Float, rectPosition: Int) {
        if (rectPosition in RECT_LEFT..RECT_LB) {
            _targetRect = rect
            _rotate = rotate
            _rectPosition = rectPosition
        } else {
            _targetRect = null
        }
    }

    /**手势回调*/
    @CallPoint
    fun onTouchEvent(actionMasked: Int, x: Float, y: Float): Boolean {
        val rect = _targetRect ?: return false
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _onTouchDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //操作结束
                _targetRect = null

                //end
                onRectScaleChangeAction(rect, true)
            }
        }
        return _targetRect != null
    }

    /**手势按下时的坐标*/
    var _touchDownX: Float = 0f
    var _touchDownY: Float = 0f

    /**手势按下时, 矩形参考点的坐标*/
    var _rectDownX: Float = 0f
    var _rectDownY: Float = 0f

    /**手势按下时, 记录对角*/
    fun _onTouchDown(x: Float, y: Float) {
        val rect = _targetRect ?: return

        _touchDownX = x
        _touchDownY = y

        val rectPoint = rect.toRectPoint(_rotate)
        when (_rectPosition) {
            RECT_LT -> {
                _rectDownX = rectPoint.leftTop.x
                _rectDownY = rectPoint.leftTop.y
            }
        }
    }

}