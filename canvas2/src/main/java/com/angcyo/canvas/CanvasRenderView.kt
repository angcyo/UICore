package com.angcyo.canvas

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.canvas.render.core.CanvasRenderDelegate

/**
 * 布局入口
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasRenderView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    /**核心类*/
    var delegate = CanvasRenderDelegate(this)

    override fun computeScroll() {
        super.computeScroll()
        delegate.computeScroll()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        delegate.onSizeChanged(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        delegate.onDraw(canvas)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        delegate.dispatchTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        delegate.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        delegate.onDetachedFromWindow()
    }
}