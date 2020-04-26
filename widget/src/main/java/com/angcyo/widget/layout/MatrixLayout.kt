package com.angcyo.widget.layout

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.graphics.withSave
import androidx.core.math.MathUtils
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs
import kotlin.math.max

/**
 *
 * 向下拖拽缩放子视图, 可以用于实现拖拽图片返回界面的效果.
 *
 * 默认限制了向上移动.(即临界值时, 不能再往上移动了)可以通过变量解开限制.
 *
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/22
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class MatrixLayout(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    val _matrix = Matrix()

    /**
     * 视图轮廓对应的矩形
     */
    var viewRectF = RectF()

    /**
     * 拖拽后, 绘制的矩形
     */
    var drawRectF = RectF()

    /**
     * 允许最大放大到多少倍
     */
    var maxScale = 1f

    /**
     * 允许最小缩放到多少倍
     */
    var minScale = 0.4f

    /**
     * 最小y轴移动多少, 控制手指向上滑动时, 是否可以y轴移动
     */
    var minTranslateY = 0f

    val gestureDetectorCompat: GestureDetectorCompat
    var onMatrixTouchListener: OnMatrixTouchListener? = null

    init {
        gestureDetectorCompat = GestureDetectorCompat(context,
            object : SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    //L.e("dx:" + distanceX + " dy:" + distanceY /*+ " " + checkTouchEvent + " " + matrixChange*/);
                    return if (isMatrixChange || (distanceY < 0 && abs(distanceY) > abs(distanceX))) {
                        doOnScroll(e1, e2, distanceX, distanceY)
                        true
                    } else {
                        false
                    }
                }
            })
    }

    private fun doOnScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) {
        if (e1 == null || e2 == null) { //在某些情况下, 这玩意竟然会为空
            return
        }
        val moveY = e2.y - e1.y
        var scale = (measuredHeight - e2.y) / (measuredHeight - e1.y)
        scale = MathUtils.clamp(scale, minScale, maxScale)
        setMatrix(scale, e2.x - e1.x, max(moveY, minTranslateY))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRectF.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withSave {
            canvas.concat(_matrix)
            super.dispatchDraw(canvas)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (needTouchEvent(ev)) {
            gestureDetectorCompat.onTouchEvent(ev)
        } else super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        return if (checkTouchEvent() || isMatrixChange) {
            gestureDetectorCompat.onTouchEvent(event)
            val actionMasked = event.actionMasked
            if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL
            ) {
                if (onMatrixTouchListener?.onTouchEnd(
                        this,
                        Matrix(_matrix),
                        RectF(viewRectF),
                        RectF(drawRectF)
                    ) == true
                ) {
                    //nothing
                } else {
                    //重置 Matrix
                    reset(drawRectF)
                }
            }
            true
        } else {
            result
        }
    }

    private fun reset(fromRectF: RectF) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.interpolator = LinearInterpolator()
        animator.duration = 300
        val startScale = fromRectF.width() / viewRectF.width()
        val startTranX =
            fromRectF.left + fromRectF.width() / 2 - viewRectF.width() / 2
        val startTranY =
            fromRectF.top + fromRectF.height() / 2 - viewRectF.height() / 2
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            setMatrix(
                startScale + value * (1 - startScale),
                startTranX + value * (0 - startTranX),
                startTranY + value * (0 - startTranY)
            )
        }
        animator.start()
    }

    /**
     * @param scale 需要缩放到的倍数
     * @param tranX 需要移动到的x位置 (在缩放效果后, 相对于左上角的坐标)
     * @param tranY 需要移动到的y位置 (在缩放效果后, 相对于左上角的坐标)
     */
    private fun setMatrix(
        scale: Float,
        tranX: Float,
        tranY: Float
    ) {
        //Log.e("angcyo", "scale:" + scale + " tranX:" + tranX + " tranY:" + tranY);
        _matrix.setScale(scale, scale)
        _matrix.mapRect(drawRectF, viewRectF)
        _matrix.postTranslate(
            viewRectF.width() / 2 - drawRectF.width() / 2,
            viewRectF.height() / 2 - drawRectF.height() / 2
        )
        _matrix.postTranslate(tranX, tranY)
        _matrix.mapRect(drawRectF, viewRectF)
        postInvalidateOnAnimation()
        onMatrixTouchListener?.onMatrixChange(this, _matrix, viewRectF, drawRectF)
    }

    /**
     * 是否要开启事件检查, 关闭/开始功能
     */
    private fun checkTouchEvent(): Boolean {
        return onMatrixTouchListener?.checkTouchEvent(this) ?: true
    }

    /**
     * 矩阵是否改变过
     */
    val isMatrixChange: Boolean
        get() {
            _matrix.mapRect(drawRectF, viewRectF)
            return drawRectF != viewRectF
        }

    private fun needTouchEvent(ev: MotionEvent): Boolean {
        val matrixChange = isMatrixChange
        val checkTouchEvent = checkTouchEvent()
        val handle = checkTouchEvent || matrixChange
        return handle && ev.pointerCount == 1
    }

    /**
     * 恢复默认状态
     */
    fun resetMatrix() {
        _matrix.reset()
        postInvalidate()
    }

    interface OnMatrixTouchListener {

        /**是否要开启事件检查, 关闭/开始功能, 返回true, 允许检查touch事件*/
        fun checkTouchEvent(matrixLayout: MatrixLayout): Boolean

        fun onMatrixChange(
            matrixLayout: MatrixLayout,
            matrix: Matrix,
            fromRect: RectF,
            toRect: RectF
        )

        /**
         * @return 返回true, 拦截布局的默认处理方式
         */
        fun onTouchEnd(
            matrixLayout: MatrixLayout,
            matrix: Matrix,
            fromRect: RectF,
            toRect: RectF
        ): Boolean
    }
}