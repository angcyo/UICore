package com.angcyo.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.angcyo.library.attachInEditMode
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dpi
import com.jaredrummler.android.widget.AnimatedSvgView

/**
 * @author [angcyo](mailto:angcyo@126.com)
 * @since 2022/06/28
 */
class TextPathAnimateView : AnimatedSvgView {

    /**设置动画文本*/
    var animateText: String? = null
        set(value) {
            field = value
            updateTextPath()
            //start();
        }

    //
    val animateTextPath: Path = Path()
    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textBounds: Rect = Rect()
    val pathBounds: RectF = RectF()

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context, attrs)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTextPath()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isInEditMode) {
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.color = mFillColors.getOrNull(0) ?: Color.RED
            canvas.drawPath(animateTextPath, paint)
        }
    }

    /**初始化*/
    fun initView(context: Context, attrs: AttributeSet?) {
        attachInEditMode()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextPathAnimateView)
        paint.textSize = typedArray.getDimensionPixelOffset(
            R.styleable.TextPathAnimateView_r_path_text_size,
            18 * dpi
        ).toFloat() //设置字体大小, 影响Path的大小
        animateText =
            typedArray.getString(R.styleable.TextPathAnimateView_r_path_animate_text) ?: animateText
        typedArray.recycle()

        //setFillStart(); //开始fill的时间
        //setFillTime(); //fill需要多少时间

        //光线的颜色
        setTraceColors(intArrayOf(_color(R.color.colorAccent)))
        //最后填充的颜色
        setFillColors(intArrayOf(_color(R.color.colorAccent)))
    }

    fun updateTextPath() {
        val text = animateText
        if (text.isNullOrEmpty()) {
            return
        }
        animateTextPath.rewind()
        paint.getTextBounds(text, 0, text.length, textBounds)
        paint.getTextPath(
            text, 0, text.length,
            -textBounds.left.toFloat(),
            -textBounds.top.toFloat(),
            animateTextPath
        )
        setGlyphPaths(animateTextPath)
        animateTextPath.computeBounds(pathBounds, true)
        setViewportSize(
            pathBounds.width() + mFillPaint.strokeWidth * 2,
            pathBounds.height() + mFillPaint.strokeWidth * 2 + textBounds.bottom
        )
        if (measuredWidth > 0 && measuredHeight > 0) {
            rebuildGlyphData()
        }
    }

    /**开始动画*/
    override fun start() {
        super.start()
    }

    override fun reset() {
        super.reset()
    }

    override fun setToFinishedFrame() {
        super.setToFinishedFrame()
    }
}