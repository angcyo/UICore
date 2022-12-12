package com.angcyo.widget.slider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.angcyo.library.L
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.disableParentInterceptTouchEvent
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi
import com.angcyo.widget.base.isTouchFinish
import kotlin.math.abs

/**
 * 滑块控件, 矩形底, 上面显示一个滑块
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/28
 */
abstract class BaseSliderView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    /**是否绘制面板的边框*/
    var showPanelBorder: Boolean = true

    /**边框的颜色*/
    var panelBorderColor: Int = -0x919192

    /**滑块的颜色*/
    var sliderColor: Int = -0x424243

    /**滑块的宽度*/
    var sliderWidth: Int = 5 * dpi

    /**滑块笔的宽度*/
    var sliderStyleWidth: Int = 2 * dpi

    /**滑块高度差值, 上下的总和*/
    var sliderOffsetHeight: Int = 8 * dpi

    /**滑块的圆角*/
    var sliderRound: Float = 2 * dp

    /**当前滑块的进度[0~100]*/
    var sliderProgress: Int = 0
        set(value) {
            field = value
            postInvalidate()
        }

    //
    val borderPaint: Paint = Paint()
    val panelRect: Rect = Rect()

    val sliderPaint: Paint = Paint()

    init {
        initView()
    }

    open fun initView() {
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 1 * dp

        sliderPaint.style = Paint.Style.STROKE
        sliderPaint.strokeWidth = sliderStyleWidth.toFloat()
        sliderPaint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var wSpec = widthMeasureSpec
        var hSpec = heightMeasureSpec
        if (widthMode != MeasureSpec.EXACTLY) {
            wSpec = MeasureSpec.makeMeasureSpec(100 * dpi, MeasureSpec.EXACTLY)
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            hSpec = MeasureSpec.makeMeasureSpec(40 * dpi, MeasureSpec.EXACTLY)
        }
        super.onMeasure(wSpec, hSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        panelRect.set(
            paddingLeft + sliderWidth / 2 + sliderStyleWidth / 2,
            paddingTop + sliderOffsetHeight / 2 + sliderStyleWidth / 2,
            w - paddingRight - sliderWidth / 2 - sliderStyleWidth / 2,
            h - paddingBottom - sliderOffsetHeight / 2 - sliderStyleWidth / 2
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPanel(canvas)
        drawPanelBorder(canvas)
        drawSlider(canvas)
    }

    /**绘制底层面板*/
    open fun drawPanel(canvas: Canvas) {

    }

    /**绘制面板边框*/
    open fun drawPanelBorder(canvas: Canvas) {
        val rect: Rect = panelRect
        if (showPanelBorder) {
            borderPaint.color = panelBorderColor
            val pWidth = borderPaint.strokeWidth
            canvas.drawRect(
                rect.left - pWidth / 2,
                rect.top - pWidth / 2,
                rect.right + pWidth / 2,
                rect.bottom + pWidth / 2,
                borderPaint
            )
        }
    }

    val _sliderRect: RectF = RectF()

    /**绘制上层滑块*/
    open fun drawSlider(canvas: Canvas) {
        L.w(sliderProgress)
        //val p: Point =  Point()  //hueToPoint(hue)
        sliderPaint.color = sliderColor
        sliderPaint.strokeWidth = sliderStyleWidth.toFloat()
        //val pWidth = sliderPaint.strokeWidth

        val centerX = panelRect.left + panelRect.width() * (sliderProgress / 100f)

        val r = _sliderRect
        r.left = centerX - sliderWidth / 2
        r.right = centerX + sliderWidth / 2
        r.top = (panelRect.top - sliderOffsetHeight / 2).toFloat()
        r.bottom = (panelRect.bottom + sliderOffsetHeight / 2).toFloat()

        canvas.drawRoundRect(r, sliderRound, sliderRound, sliderPaint)
    }

    //<editor-fold desc="Touch事件">

    //手势检测
    val _gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                _onTouchMoveTo(e.x, e.y, false)
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val absX = abs(distanceX)
                val absY = abs(distanceY)

                var handle = false
                if (absX > absY) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    _onTouchMoveTo(e2.x, e2.y, false)
                    handle = true
                }
                return handle
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        if (event.isTouchFinish()) {
            disableParentInterceptTouchEvent(false)
            _onTouchMoveTo(event.x, event.y, true)
        }

        return _gestureDetector.onTouchEvent(event)
    }

    /**手指移动*/
    open fun _onTouchMoveTo(x: Float, y: Float, isFinish: Boolean) {
        val value = clamp(x, panelRect.left.toFloat(), panelRect.right.toFloat())
        val progress: Int = ((value - panelRect.left) * 100 / panelRect.width()).toInt()
        sliderProgress = clamp(progress, 0, 100)
    }

    //</editor-fold desc="Touch事件">

}