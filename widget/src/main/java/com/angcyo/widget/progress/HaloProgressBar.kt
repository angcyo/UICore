package com.angcyo.widget.progress

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.angcyo.library.ex.dpi
import com.angcyo.widget.R
import com.angcyo.widget.base.*
import kotlin.math.sqrt

/**
 * Created by angcyo on 2017-11-18.
 *
 * 模仿QQ 发送图片的 光晕进度条
 *
 * https://blog.csdn.net/angcyo/article/details/78588899
 * https://github.com/angcyo/HaloProgressBarDemo
 */
class HaloProgressBar(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    companion object {
        fun getTranColor(@ColorInt color: Int, alpha: Int): Int {
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        }

        //勾股定理
        fun c(a: Float, b: Float): Float {
            return sqrt((a * a + b * b).toDouble()).toFloat()
        }
    }

    /**内圈半径*/
    var circleInnerRadius = 16 * dpi

    /**内圈增长*/
    var circleInnerRadiusIncrease = 2 * dpi

    /**保持内圈镂空后最小的宽度*/
    var keepInnerCircleWidth = 3 * dpi

    /**外圈半径*/
    var circleOuterRadius = 18 * dpi

    /**保持外圈镂空后最小的宽度*/
    var keepOuterCircleWidth = 0

    /**内圈增长*/
    var circleOuterRadiusIncrease = 0

    var circleBgColor = getColor(R.color.transparent40)

    /**圆圈颜色*/
    var circleColor = Color.WHITE

    /**光晕圆圈颜色*/
    var circleHaloColor = getTranColor(Color.WHITE, 0x30)

    /**进度文本大小*/
    var textSize = 12 * dpi

    /**进度文本颜色*/
    var textColor = circleColor

    /**进度 0 - 100*/
    var progress = 0
        set(value) {
            field = value
            if (field >= 100) {
                startHaloFinishAnimator()
            }
        }

    /**总是绘制进度*/
    var drawTextAlways = true

    /**背景圆角大小*/
    var drawRectRound = 0

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.HaloProgressBar)
        circleBgColor =
            typedArray.getColor(R.styleable.HaloProgressBar_halo_bg_color, circleBgColor)
        circleColor =
            typedArray.getColor(R.styleable.HaloProgressBar_halo_circle_color, circleColor)
        circleHaloColor =
            typedArray.getColor(R.styleable.HaloProgressBar_halo_circle_halo_color, circleHaloColor)

        textColor =
            typedArray.getColor(R.styleable.HaloProgressBar_halo_text_color, circleColor)
        textSize =
            typedArray.getDimensionPixelOffset(R.styleable.HaloProgressBar_halo_text_size, textSize)

        circleInnerRadius = typedArray.getDimensionPixelOffset(
            R.styleable.HaloProgressBar_halo_circle_inner_radius,
            circleInnerRadius
        )
        circleInnerRadiusIncrease = typedArray.getDimensionPixelOffset(
            R.styleable.HaloProgressBar_halo_circle_inner_radius_increase,
            circleInnerRadiusIncrease
        )
        keepInnerCircleWidth = typedArray.getDimensionPixelOffset(
            R.styleable.HaloProgressBar_halo_keep_inner_circle_width,
            keepInnerCircleWidth
        )
        circleOuterRadius = typedArray.getDimensionPixelOffset(
            R.styleable.HaloProgressBar_halo_circle_outer_radius,
            circleOuterRadius
        )
        circleOuterRadiusIncrease = typedArray.getDimensionPixelOffset(
            R.styleable.HaloProgressBar_halo_circle_outer_radius_increase,
            circleInnerRadiusIncrease * 3
        )
        keepOuterCircleWidth = typedArray.getDimensionPixelOffset(
            R.styleable.HaloProgressBar_halo_keep_outer_circle_width,
            keepInnerCircleWidth / 2
        )

        drawRectRound = typedArray.getDimensionPixelOffset(
            R.styleable.HaloProgressBar_halo_rect_round,
            drawRectRound
        )

        drawTextAlways =
            typedArray.getBoolean(R.styleable.HaloProgressBar_halo_draw_text_always, drawTextAlways)
        progress = typedArray.getInt(R.styleable.HaloProgressBar_halo_progress, progress)

        typedArray.recycle()
    }

    private fun View.getColor(id: Int): Int = ContextCompat.getColor(context, id)

    /**返回居中绘制文本的y坐标*/
    private fun View.getDrawCenterTextCy(paint: Paint): Float {
        val rawHeight = measuredHeight - paddingTop - paddingBottom
        return paddingTop + rawHeight / 2 + (paint.descent() - paint.ascent()) / 2 - paint.descent()
    }

    private fun View.getDrawCenterTextCx(paint: Paint, text: String): Float {
        val rawWidth = measuredWidth - paddingLeft - paddingRight
        return paddingLeft + rawWidth / 2 - paint.measureText(text) / 2
    }

    private var circleCanvas: Canvas? = null
    private var circleBitmap: Bitmap? = null
    private var circleHaloCanvas: Canvas? = null
    private var circleHaloBitmap: Bitmap? = null

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    private val clearXF by lazy { PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val dstOutXF by lazy { PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }
    private val srcOutXF by lazy { PorterDuffXfermode(PorterDuff.Mode.SRC_OUT) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (progress < 100) {
            if (animator.isStarted || isInEditMode) {
                //动画开始了, 才绘制

                //绘制背景
                _drawBackground(canvas)

                //绘制光晕
                circleHaloCanvas?.let {
                    it.save()
                    it.translate(drawCenterX.toFloat(), drawCenterY.toFloat())

                    paint.xfermode = clearXF
                    it.drawPaint(paint)
                    paint.xfermode = null

                    //绘制光晕
                    paint.color = circleHaloColor
                    it.drawCircle(
                        0f,
                        0f,
                        circleOuterRadius + circleOuterRadiusIncrease * animatorValue + keepOuterCircleWidth,
                        paint
                    )

                    //镂空
                    paint.xfermode = srcOutXF
                    it.drawCircle(
                        0f,
                        0f,
                        circleInnerRadius.toFloat(),
                        paint
                    )
                    paint.xfermode = null

                    it.restore()

                    canvas.drawBitmap(circleHaloBitmap!!, 0f, 0f, null)
                }

                //绘制圆
                circleCanvas?.let {
                    it.save()
                    it.translate(drawCenterX.toFloat(), drawCenterY.toFloat())

                    paint.xfermode = clearXF
                    it.drawPaint(paint)
                    paint.xfermode = null

                    //绘制圆圈
                    paint.color = circleColor
                    it.drawCircle(
                        0f,
                        0f,
                        circleInnerRadius + circleInnerRadiusIncrease * animatorValue + keepInnerCircleWidth,
                        paint
                    )

                    //镂空
                    paint.xfermode = srcOutXF
                    it.drawCircle(0f, 0f, circleInnerRadius.toFloat(), paint)
                    paint.xfermode = null

                    it.restore()

                    canvas.drawBitmap(circleBitmap!!, 0f, 0f, null)
                }

                if (drawTextAlways || progress in 1..100) {
                    //绘制进度文本
                    paint.apply {
                        paint.style = Paint.Style.FILL_AND_STROKE
                        paint.textSize = this@HaloProgressBar.textSize.toFloat()
                        paint.color = this@HaloProgressBar.textColor
                        paint.strokeWidth = 1f
                    }
                    val text = "${progress}%"
                    canvas.drawText(
                        text,
                        this.getDrawCenterTextCx(paint, text),
                        this.getDrawCenterTextCy(paint),
                        paint
                    )
                }
            } else {
                //动画没开始, 不绘制
            }
        }

        if (animatorFinish.isRunning) {
            circleCanvas?.let {
                paint.xfermode = clearXF

                _drawBackground(it)

                it.save()
                it.translate(drawCenterX.toFloat(), drawCenterY.toFloat())

                //用圆圈镂空背景
                paint.xfermode = srcOutXF
                paint.color = Color.TRANSPARENT
                it.drawCircle(0f, 0f, circleFinishDrawRadius.toFloat(), paint)
                paint.xfermode = null

                it.restore()

                canvas.drawBitmap(circleBitmap!!, 0f, 0f, null)
            }
        }
    }

    fun _drawBackground(canvas: Canvas) {
        //绘制圆圈背景
        paint.xfermode = null
        paint.color = circleBgColor
        canvas.drawRoundRect(
            drawLeft.toFloat(),
            drawTop.toFloat(),
            drawRight.toFloat(),
            drawBottom.toFloat(),
            drawRectRound.toFloat(),
            drawRectRound.toFloat(),
            paint
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleBitmap?.recycle()
        if (measuredWidth != 0 && measuredHeight != 0) {
            circleBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            circleCanvas = Canvas(circleBitmap!!)
        }

        circleHaloBitmap?.recycle()
        if (measuredWidth != 0 && measuredHeight != 0) {
            circleHaloBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            circleHaloCanvas = Canvas(circleHaloBitmap!!)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (circleBitmap != null && circleBitmap!!.isRecycled) {
            circleBitmap =
                Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            circleCanvas = Canvas(circleBitmap!!)
        }
        if (circleHaloBitmap != null && circleHaloBitmap!!.isRecycled) {
            circleHaloBitmap =
                Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            circleHaloCanvas = Canvas(circleHaloBitmap!!)
        }
        //startHaloAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopHaloAnimator()
        stopHaloFinishAnimator()
        circleHaloBitmap?.recycle()
        circleBitmap?.recycle()
        progress = 0
    }

    /*动画进度*/
    private var animatorValue: Float = 0f

    private val animator by lazy {
        ObjectAnimator.ofFloat(0f, 1f).apply {
            duration = 700
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                animatorValue = it.animatedValue as Float
//                progress++
//                if (progress > 100) {
//                    progress = 0
//                }
                postInvalidate()
            }
        }
    }

    /**启动光晕动画*/
    fun startHaloAnimator() {
        if (progress >= 100) {
            return
        }
        if (animator.isStarted || animator.isRunning) {
        } else {
            animator.start()
        }
    }

    /**停止光晕呼吸动画*/
    fun stopHaloAnimator() {
        animator.cancel()
    }

    /**结束动画圆圈的半径*/
    private var circleFinishDrawRadius = circleInnerRadius

    private val animatorFinish by lazy {
        ObjectAnimator.ofFloat(
            circleInnerRadius.toFloat(),
            c((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())
        ).apply {
            duration = 300
            addUpdateListener {
                circleFinishDrawRadius = (it.animatedValue as Float).toInt()
                postInvalidate()
            }
        }
    }

    /**进度100%后的扩赛动画*/
    fun startHaloFinishAnimator() {
        stopHaloAnimator()
        if (animatorFinish.isStarted || animatorFinish.isRunning) {
        } else {
            animatorFinish.start()
        }
    }

    /**关闭扩赛动画*/
    fun stopHaloFinishAnimator() {
        animatorFinish.cancel()
    }
}
