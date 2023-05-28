package com.angcyo.widget.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import com.angcyo.drawable.CheckerboardDrawable
import com.angcyo.library.ex.mapPoint

/**
 * 回调点击在在图片上的坐标点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/05/24
 */
class TouchImageView(context: Context, attributeSet: AttributeSet? = null) :
    TouchCompatImageView(context, attributeSet) {

    /**核心回调*/
    var onTouchPointAction: (PointF) -> Unit = {}

    private val touchPoint = PointF(0f, 0f)
    private val tempMatrix = Matrix()

    init {
        isClickable = true
        if (background == null) {
            background = CheckerboardDrawable.create()//棋盘背景
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return super.dispatchTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            touchPoint.set(event.x, event.y)
            imageMatrix.invert(tempMatrix)//关键
            tempMatrix.mapPoint(touchPoint)
            onTouchPointAction(touchPoint)
        }
        return result
    }
}