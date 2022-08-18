package com.angcyo.library.gesture

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex._tempPoint
import com.angcyo.library.ex._tempRectF
import com.angcyo.library.ex.rotate
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

        /**查找当前点, 按在矩形的那个位置上
         * [threshold] 阈值/误差*/
        fun findRectPosition(
            rect: RectF,
            rotate: Float,
            x: Float,
            y: Float,
            threshold: Float
        ): Int {
            val rotateRect = rect.rotate(rotate, result = _tempRectF)

            //先判断是否在4个角上
            if ((x - rotateRect.left).absoluteValue <= threshold) {
                if ((y - rotateRect.top).absoluteValue <= threshold) {
                    return RECT_LT
                }
                if ((y - rotateRect.bottom).absoluteValue <= threshold) {
                    return RECT_LB
                }
                if (y >= rotateRect.top && x <= rotateRect.bottom) {
                    return RECT_LEFT
                }
            }
            if ((x - rotateRect.right).absoluteValue <= threshold) {
                if ((y - rotateRect.top).absoluteValue <= threshold) {
                    return RECT_RT
                }
                if ((y - rotateRect.bottom).absoluteValue <= threshold) {
                    return RECT_RB
                }
                if (y >= rotateRect.top && x <= rotateRect.bottom) {
                    return RECT_RIGHT
                }
            }

            //再判断是否在4个边上
            if (x >= rotateRect.left && x <= rotateRect.right) {
                if ((y - rotateRect.top).absoluteValue <= threshold) {
                    return RECT_TOP
                }
                if ((y - rotateRect.bottom).absoluteValue <= threshold) {
                    return RECT_BOTTOM
                }
            }
            return 0
        }

        /**根据点击位置, 获取参考点*/
        fun getRectPositionPoint(
            rect: RectF,
            rectPosition: Int,
            result: PointF = _tempPoint
        ): PointF? {
            val handle = when (rectPosition) {
                //4个角
                RECT_LT -> {
                    //按在矩形的左上角, 则参考点在右下角
                    result.x = rect.right
                    result.y = rect.bottom
                    true
                }
                RECT_RT -> {
                    //按在矩形的右上角, 则参考点在左下角
                    result.x = rect.left
                    result.y = rect.bottom
                    true
                }
                RECT_RB -> {
                    //按在矩形的右下角, 则参考点在左上角
                    result.x = rect.left
                    result.y = rect.top
                    true
                }
                RECT_LB -> {
                    //按在矩形的左下角, 则参考点在右上角
                    result.x = rect.right
                    result.y = rect.top
                    true
                }
                //4个边
                RECT_LEFT -> {
                    //左边, 参考点在右边
                    result.x = rect.right
                    result.y = rect.centerY()
                    true
                }
                RECT_TOP -> {
                    //上边, 参考点在下边
                    result.x = rect.centerX()
                    result.y = rect.bottom
                    true
                }
                RECT_RIGHT -> {
                    //右边, 参考点在左边
                    result.x = rect.left
                    result.y = rect.centerY()
                    true
                }
                RECT_BOTTOM -> {
                    //下边, 参考点在上边
                    result.x = rect.centerX()
                    result.y = rect.top
                    true
                }
                else -> false
            }
            if (handle) {
                return result
            }
            return null
        }
    }

    //region ---可读取/配置属性---

    /**目标矩形*/
    var targetRect = RectF()

    /**改变之后的矩形*/
    val changedRect = RectF()

    /**手势按下时, 原始矩形参考点的坐标. (未旋转)
     * 矩形上的锚点坐标
     * */
    var rectAnchorX = 0f
    var rectAnchorY = 0f

    /**改变后的矩形是否水平翻转了*/
    var isFlipHorizontal = false

    /**改变后的矩形是否垂直翻转了*/
    var isFlipVertical = false

    /**记录当前变化的缩放比例*/
    var rectScaleX: Float = 0f

    var rectScaleY: Float = 0f

    /**改变的时候, 是否要保持宽高比例*/
    var keepScaleRatio: Boolean = true
        set(value) {
            field = value
            keepScaleRatioOnFrame = value
        }

    /**按在边框上拖动时, 是否也要保持比例*/
    var keepScaleRatioOnFrame: Boolean = false

    /**当矩形缩放改变时的回调
     * [rect] 实时改变的矩形
     * [end] 手势是否结束*/
    var onRectScaleChangeAction: (rect: RectF, end: Boolean) -> Unit = { rect, end ->
        L.i(rect)
    }

    /**是否要限制当前改变到的矩形[rect]
     * 返回[true]表示限制
     * 返回[false]表示不限制, 允许改变*/
    var onRectScaleLimitAction: (rect: RectF) -> Boolean = {
        false
    }

    /**限制新宽度的回调*/
    var onLimitWidthAction: (newWidth: Float, dx: Float, dy: Float) -> Float = { newWidth, dx, dy ->
        newWidth
    }

    /**限制新高度的回调*/
    var onLimitHeightAction: (newHeight: Float, dx: Float, dy: Float) -> Float =
        { newHeight, dx, dy ->
            newHeight
        }

    /**是否需要转换[point]*/
    var onTransformPoint: (point: PointF) -> Unit = {

    }

    //endregion ---可读取/配置属性---

    //region ---内部---

    //是否初始化了
    var _isInitialize = false

    //矩形旋转的角度
    var _rotate: Float = 0f

    //按下的位置
    var _rectPosition: Int = 0

    //灵敏度
    var _scaledTouchSlop: Int = 0

    //[_scaledTouchSlop]
    var _touchMoveX = 0f
    var _touchMoveY = 0f

    /**手势按下时的坐标, (反向旋转后的点)*/
    var _touchDownX: Float = 0f
    var _touchDownY: Float = 0f

    //临时存储2个值
    val _tempValues = floatArrayOf(0f, 0f)

    val _matrix = Matrix()
    val _tempRect = RectF()
    val _pendingRect = RectF()
    val _point = PointF()

    //endregion ---内部---

    init {
        //_scaledTouchSlop = ViewConfiguration.get(app()).scaledTouchSlop //28
        _scaledTouchSlop = 1
    }

    //region ---core---

    /**在[MotionEvent.ACTION_DOWN]时, 初始化操作数据*/
    @CallPoint
    fun initialize(rect: RectF, rotate: Float, rectPosition: Int) {
        if (rectPosition in RECT_LEFT..RECT_LB) {
            _isInitialize = true
            targetRect.set(rect)
            changedRect.set(rect)
            _rotate = rotate
            _rectPosition = rectPosition
            isFlipHorizontal = false
            isFlipVertical = false
        } else {
            _isInitialize = false
        }
    }

    /**手势回调, 拆开[x] [y] 方便对数据进行转换
     *
     * [actionMasked] [android.view.MotionEvent.getActionMasked]
     * */
    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        val handle = _isInitialize
        if (!handle) {
            return false
        }

        //
        _point.x = event.x
        _point.y = event.y
        onTransformPoint(_point)
        val x = _point.x
        val y = _point.y

        //
        when (event.actionMasked) {
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
                _isInitialize = false

                //end
                onRectScaleChangeAction(changedRect, true)
            }
        }
        return handle
    }

    //endregion ---core---

    //region ---method---

    /**手势按下时, 记录对角坐标*/
    fun _onTouchDown(x: Float, y: Float): Boolean {
        if (!_isInitialize) {
            return false
        }
        val rect = targetRect

        _invertRotatePoint(x, y, rect.centerX(), rect.centerY())
        _touchDownX = _tempValues[0]
        _touchDownY = _tempValues[1]

        val point = getRectPositionPoint(rect, _rectPosition) ?: return false

        rectAnchorX = point.x
        rectAnchorY = point.y

        return true
    }

    fun _onTouchMove(x: Float, y: Float): Boolean {
        if (!_isInitialize) {
            return false
        }
        val rect = targetRect

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

        val newWidth = onLimitWidthAction(rect.width() + dx, dx, dy)
        val newHeight = onLimitHeightAction(rect.height() + dy, dx, dy)

        var scaleX = newWidth / rect.width()
        var scaleY = newHeight / rect.height()

        var keepRatio = keepScaleRatio

        if (_rectPosition < RECT_LT) {
            //在边上拖动
            keepRatio = keepScaleRatioOnFrame
        }

        //仅拖拽4个边
        when (_rectPosition) {
            RECT_LEFT, RECT_RIGHT -> {
                scaleY = if (keepRatio) {
                    scaleX
                } else {
                    1f
                }
            }
            RECT_TOP, RECT_BOTTOM -> {
                //只需要改变高度
                scaleX = if (keepRatio) {
                    scaleY
                } else {
                    1f
                }
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
        _matrix.setScale(scaleX, scaleY, rectAnchorX, rectAnchorY)
        _pendingRect.set(rect)
        _matrix.mapRect(_pendingRect)
        //L.i("scaleX:$scaleX scaleY:$scaleY $_pendingRect")

        //按照原始的矩形中点进行旋转, 这样能保证锚点坐标固定
        _matrix.reset()
        _matrix.setRotate(_rotate, rect.centerX(), rect.centerY())
        _tempRect.set(_pendingRect)
        _matrix.mapRect(_tempRect)

        //缩放且旋转后的矩形, 和仅缩放后的矩形, 进行中点偏移, 就能实现最终效果
        val changedCenterX = _pendingRect.centerX()
        val changedCenterY = _pendingRect.centerY()
        val centerDx = _tempRect.centerX() - changedCenterX
        val centerDy = _tempRect.centerY() - changedCenterY

        //后平移
        _matrix.reset()
        _matrix.setTranslate(centerDx, centerDy)
        _matrix.mapRect(_pendingRect)

        //限制检查
        _handleScaleAndFlip(_pendingRect)
        if (onRectScaleLimitAction(_pendingRect)) {
            //限制了
        } else {
            //没有限制
            changedRect.set(_pendingRect)
        }
        _handleScaleAndFlip(changedRect)
        return true
    }

    /**处理缩放属性和翻转属性*/
    fun _handleScaleAndFlip(rect: RectF) {
        rectScaleX = rect.width() / targetRect.width()
        rectScaleY = rect.height() / targetRect.height()

        //判断矩形是否翻转了
        val cx = rect.centerX()
        val cy = rect.centerY()
        isFlipHorizontal = cx < rect.left
        isFlipVertical = cy < rect.top
        if (_rectPosition == RECT_LEFT || _rectPosition == RECT_LT || _rectPosition == RECT_LB) {
            //touch在左边
            isFlipHorizontal = cx > rect.right
        }
        if (_rectPosition == RECT_TOP || _rectPosition == RECT_LT || _rectPosition == RECT_RT) {
            //touch在上边
            isFlipVertical = cy > rect.bottom
        }
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

    //endregion ---method---
}