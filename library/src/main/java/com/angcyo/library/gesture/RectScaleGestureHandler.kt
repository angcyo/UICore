package com.angcyo.library.gesture

import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import kotlin.math.absoluteValue
import kotlin.math.max

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

    /**改变之后的矩形*/
    val changedRect = RectF()

    //矩形旋转的角度
    var _rotate: Float = 0f

    //按下的位置
    var _rectPosition: Int = 0

    //保持比例
    var _keepRatio: Boolean = true

    var _scaledTouchSlop: Int = 0

    init {
        //_scaledTouchSlop = ViewConfiguration.get(app()).scaledTouchSlop //28
        _scaledTouchSlop = 1
    }

    /**在[MotionEvent.ACTION_DOWN]时, 初始化操作数据*/
    @CallPoint
    fun initialize(rect: RectF, rotate: Float, rectPosition: Int, keepRatio: Boolean) {
        if (rectPosition in RECT_LEFT..RECT_LB) {
            _targetRect = RectF(rect)
            changedRect.set(rect)
            _rotate = rotate
            _rectPosition = rectPosition
            _keepRatio = keepRatio
        } else {
            _targetRect = null
        }
    }

    /**手势回调, 拆开[x] [y] 方便对数据进行转换
     *
     * [actionMasked] [android.view.MotionEvent.getActionMasked]
     * */
    @CallPoint
    fun onTouchEvent(actionMasked: Int, x: Float, y: Float): Boolean {
        _targetRect ?: return false
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchMoveX = x
                _touchMoveY = y
                _onTouchDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (_onTouchMove(x, y)) {
                    onRectScaleChangeAction(changedRect, false)
                }
                _touchMoveX = x
                _touchMoveY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //操作结束
                _targetRect = null

                //end
                onRectScaleChangeAction(changedRect, true)
            }
        }
        return _targetRect != null
    }

    var _touchMoveX = 0f
    var _touchMoveY = 0f

    /**手势按下时的坐标, (反向旋转后的点)*/
    var _touchDownX: Float = 0f
    var _touchDownY: Float = 0f

    /**手势按下时, 矩形参考点的坐标. (未旋转)*/
    var _rectDownX: Float = 0f
    var _rectDownY: Float = 0f

    //临时存储2个值
    val _tempValues = floatArrayOf(0f, 0f)

    /**手势按下时, 记录对角坐标*/
    fun _onTouchDown(x: Float, y: Float): Boolean {
        val rect = _targetRect ?: return false

        _invertRotatePoint(x, y, rect.centerX(), rect.centerY())
        _touchDownX = _tempValues[0]
        _touchDownY = _tempValues[1]

        val result = when (_rectPosition) {
            //4个角
            RECT_LT -> {
                //按在矩形的左上角, 则参考点在右下角
                _rectDownX = rect.right
                _rectDownY = rect.bottom
                true
            }
            RECT_RT -> {
                //按在矩形的右上角, 则参考点在左下角
                _rectDownX = rect.left
                _rectDownY = rect.bottom
                true
            }
            RECT_RB -> {
                //按在矩形的右下角, 则参考点在左上角
                _rectDownX = rect.left
                _rectDownY = rect.top
                true
            }
            RECT_LB -> {
                //按在矩形的左下角, 则参考点在右上角
                _rectDownX = rect.right
                _rectDownY = rect.top
                true
            }
            //4个边
            RECT_LEFT -> {
                //左边, 参考点在右边
                _rectDownX = rect.right
                _rectDownY = rect.centerY()
                true
            }
            RECT_TOP -> {
                //上边, 参考点在下边
                _rectDownX = rect.centerX()
                _rectDownY = rect.bottom
                true
            }
            RECT_RIGHT -> {
                //右边, 参考点在左边
                _rectDownX = rect.left
                _rectDownY = rect.centerY()
                true
            }
            RECT_BOTTOM -> {
                //下边, 参考点在上边
                _rectDownX = rect.centerX()
                _rectDownY = rect.top
                true
            }
            else -> false
        }

        return result
    }

    val _matrix = Matrix()
    val _tempRect = RectF()

    fun _onTouchMove(x: Float, y: Float): Boolean {
        val rect = _targetRect ?: return false

        if ((x - _touchMoveX).absoluteValue >= _scaledTouchSlop ||
            (y - _touchMoveY).absoluteValue >= _scaledTouchSlop
        ) {
            //slop
        } else {
            return false
        }

        _invertRotatePoint(x, y, rect.centerX(), rect.centerY())
        val touchX = _tempValues[0]
        val touchY = _tempValues[1]

        var dx = touchX - _touchDownX
        var dy = touchY - _touchDownY

        if (_rectPosition == RECT_LEFT || _rectPosition == RECT_LT || _rectPosition == RECT_LB) {
            //touch在左边
            dx = _touchDownX - touchX
        }
        if (_rectPosition == RECT_TOP || _rectPosition == RECT_LT || _rectPosition == RECT_RT) {
            //touch在上边
            dy = _touchDownY - touchY
        }

        val newWidth = rect.width() + dx
        val newHeight = rect.height() + dy

        var scaleX = newWidth / rect.width()
        var scaleY = newHeight / rect.height()

        var keepRatio = _keepRatio

        if (_rectPosition < RECT_LT) {
            //在边上拖动, 不激活保持比例
            keepRatio = false
        }

        when (_rectPosition) {
            RECT_LEFT, RECT_RIGHT -> {
                scaleY = 1f
            }
            RECT_TOP, RECT_BOTTOM -> {
                //只需要改变高度
                scaleX = 1f
            }
        }

        //保持比例
        if (keepRatio) {
            val scale = max(scaleX, scaleY)
            scaleX = scale
            scaleY = scale
        }

        //缩放, 在原始的矩形数据上进行缩放
        _matrix.reset()
        _matrix.setScale(scaleX, scaleY, _rectDownX, _rectDownY)
        changedRect.set(rect)
        _matrix.mapRect(changedRect)
        L.i("scaleX:$scaleX scaleY:$scaleY $changedRect")

        //按照原始的矩形中点进行旋转, 这样能保证锚点坐标固定
        _matrix.reset()
        _matrix.setRotate(_rotate, rect.centerX(), rect.centerY())
        _tempRect.set(changedRect)
        _matrix.mapRect(_tempRect)

        //缩放且旋转后的矩形, 和仅缩放后的矩形, 进行中点偏移, 就能实现最终效果
        val centerDx = _tempRect.centerX() - changedRect.centerX()
        val centerDy = _tempRect.centerY() - changedRect.centerY()

        //后平移
        _matrix.reset()
        _matrix.setTranslate(centerDx, centerDy)
        _matrix.mapRect(changedRect)
        return true
    }

    /**旋转点的坐标
     * 值放在[_tempValues]中*/
    fun _rotatePoint(x: Float, y: Float, pivotX: Float, pivotY: Float) {
        _matrix.reset()
        _matrix.setRotate(_rotate, pivotX, pivotY)
        _tempValues[0] = x
        _tempValues[1] = y
        _matrix.mapPoints(_tempValues)
    }

    /**反向旋转点的坐标
     * 值放在[_tempValues]中*/
    fun _invertRotatePoint(x: Float, y: Float, pivotX: Float, pivotY: Float) {
        _matrix.reset()
        _matrix.setRotate(_rotate, pivotX, pivotY)
        _matrix.invert(_matrix)
        _tempValues[0] = x
        _tempValues[1] = y
        _matrix.mapPoints(_tempValues)
    }
}