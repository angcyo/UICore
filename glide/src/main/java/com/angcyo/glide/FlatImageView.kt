package com.angcyo.glide

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.OverScroller
import com.angcyo.library.ex.dpi
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *
 * 让图片在当前可视区域平滑移动显示, 类似QQ资料页背景图片效果
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

class FlatImageView : GlideImageView {

    companion object {
        private const val STATE_BACKWARD = 2
        private const val STATE_FORWARD = 1
    }

    private val mOverScroller = OverScroller(context, LinearInterpolator())
    //绘制时的值
    private var drawScrollX = 0f
    private var drawScrollY = 0f
    //目标滚动值
    private var targetScrollX = 0
    private var targetScrollY = 0

    /**开始平移*/
    var startFlat = false
        set(value) {
            field = value
            if (value && isReady) {
                forward()
            } else {
                mOverScroller.abortAnimation()
            }
        }

    //当前的方向
    private var scrollState = STATE_FORWARD

    /**平移持续时长*/
    var scrollDuration = 10000
    //开始的时间标识
    private var startScrollTime = 0L

    /**
     * Drawable高度与View高度, 相差多少像素时, 激活flat
     */
    private val flatThresholdValue = 200

    var _oldScaleType: ScaleType? = null

    val isReady: Boolean
        get() {
            val checkThreshold =
                drawableHeight() - measuredHeight > flatThresholdValue ||
                        drawableWidth() - measuredWidth > flatThresholdValue
            return drawable != null && measuredHeight > 0 && measuredWidth > 0 &&
                    checkThreshold
        }

    val isVerticalScroller: Boolean
        get() {
            val wf = drawableWidth() * 1f / measuredWidth
            val hf = drawableHeight() * 1f / measuredHeight
            return hf >= wf
        }

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        _oldScaleType = scaleType
    }

    override fun computeScroll() {
        super.computeScroll()
        if (startFlat && isReady) {
            val nowTime = System.currentTimeMillis()
            var reverse = false
            //L.i("x:" + mOverScroller.getCurrX() + " y:" + mOverScroller.getCurrY());
            val computeScrollOffset = mOverScroller.computeScrollOffset()
            val currX = mOverScroller.currX
            val currY = mOverScroller.currY
            if (currX > 0 || currY > 0) {
                reverse = true
            } else if (computeScrollOffset || nowTime - startScrollTime < scrollDuration) {
                postInvalidate()
                if (targetScrollX != 0) {
                    if (abs(currX) >= abs(targetScrollX)) {
                        reverse = true
                    }
                } else if (targetScrollY != 0) {
                    if (abs(currY) >= abs(targetScrollY)) {
                        reverse = true
                    }
                }
            } else {
                reverse = true
            }
            if (reverse) {
                if (scrollState == STATE_FORWARD) {
                    backward()
                } else {
                    forward()
                }
            }
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (startFlat && isReady && !mOverScroller.computeScrollOffset()) {
            forward()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (isReady) {
            setRight(max(drawableWidth(), measuredWidth))
            setBottom(max(drawableHeight(), measuredHeight))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        _checkStart()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mOverScroller.abortAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _checkStart()
    }

    fun _checkStart() {
        if (startFlat && isReady) {
            forward()
        }
    }

    /**
     * 图片前进滚动, 那么canvas, 就要负向 translate
     */
    fun forward() {
        scrollState = STATE_FORWARD
        if (isVerticalScroller) {
            targetScrollX = 0
            drawScrollX = 0f
            targetScrollY = -drawableHeight() + measuredHeight
        } else {
            targetScrollY = 0
            drawScrollY = 0f
            targetScrollX = -drawableWidth() + measuredWidth
        }
        startScroller()
    }

    fun backward() {
        scrollState = STATE_BACKWARD
        if (isVerticalScroller) {
            targetScrollX = 0
            drawScrollX = 0f
            targetScrollY = drawableHeight() - measuredHeight
        } else {
            targetScrollY = 0
            drawScrollY = 0f
            targetScrollX = drawableWidth() - measuredWidth
        }
        startScroller()
    }

    private fun startScroller() {
        startScrollTime = System.currentTimeMillis()
        mOverScroller.startScroll(
            drawScrollX.toInt(),
            drawScrollY.toInt(),
            targetScrollX,
            targetScrollY,
            scrollDuration
        )
        postInvalidateOnAnimation()
    }

    private fun drawableHeight(): Int {
        val drawable = drawable ?: return 0
        return drawable.intrinsicHeight * dpi
    }

    private fun drawableWidth(): Int {
        val drawable = drawable ?: return 0
        return drawable.intrinsicWidth * dpi
    }

    override fun onDraw(canvas: Canvas) {
        if (startFlat && isReady) {
            val drawable = drawable
            if (drawable == null || drawable === dslGlide.placeholderDrawable) {
                mOverScroller.abortAnimation()
                super.onDraw(canvas)
                return
            }
            drawScrollX = mOverScroller.currX.toFloat()
            drawScrollY = mOverScroller.currY.toFloat()
            canvas.save()
            var offsetX = 0
            var offsetY = 0
            if (isVerticalScroller) {
                offsetX = (drawableWidth() - measuredWidth) / 2
            } else {
                offsetY = (drawableHeight() - measuredHeight) / 2
            }
            canvas.translate(min(drawScrollX, 0f) - offsetX, min(drawScrollY, 0f) - offsetY)
            drawable.setBounds(0, 0, right, bottom)
            //canvas.translate(200, 200);
            super.onDraw(canvas)
            //drawable.draw(canvas);
            canvas.restore()
        } else {
            super.onDraw(canvas)
        }
    }

    override fun load(url: String?, action: DslGlide.() -> Unit) {
        dslGlide.apply {
            startFlat = false
            scaleType = _oldScaleType

            originalSize = true

            onLoadSucceed = { _, _ ->
                scaleType = ScaleType.FIT_XY
                startFlat = true
                false
            }

            action()
            load(url)
        }
    }
}