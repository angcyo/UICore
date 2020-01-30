package com.angcyo.widget.image

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import com.angcyo.library.ex.dpi
import com.angcyo.widget.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
open class ShapeImageView : AppCompatImageView {

    /**圆角大小, 足够大时, 就是正方形就会绘制成圆*/
    var imageRadius = 5 * dpi

    /**绘制边框*/
    var drawBorder: Boolean = true
    var borderWidth: Int = 2 * dpi
    var borderColor: Int = Color.WHITE

    var _outlineRect = Rect()
    var _outlineRectF = RectF()

    val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            isFilterBitmap = true
        }
    }

    /**强大的Mask*/
    var maskDrawable: Drawable? = null
    val _maskPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
    }

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ShapeImageView)
        imageRadius = typedArray.getDimensionPixelOffset(
            R.styleable.ShapeImageView_r_image_radius,
            imageRadius
        )
        drawBorder = typedArray.getBoolean(R.styleable.ShapeImageView_r_draw_border, drawBorder)
        borderWidth = typedArray.getDimensionPixelOffset(
            R.styleable.ShapeImageView_r_border_width,
            borderWidth
        )
        borderColor = typedArray.getColor(R.styleable.ShapeImageView_r_border_color, borderColor)
        maskDrawable = typedArray.getDrawable(R.styleable.ShapeImageView_r_mask_drawable)
        typedArray.recycle()

        outlineProvider = ShapeOutline()
        clipToOutline = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _outlineRect.set(
            paddingLeft,
            paddingTop,
            measuredWidth - paddingRight,
            measuredHeight - paddingBottom
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        _outlineRectF.set(_outlineRect)

        //mask
        if (maskDrawable == null) {
            super.onDraw(canvas)
        } else {
            val save = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            } else {
                canvas.saveLayer(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    null,
                    Canvas.ALL_SAVE_FLAG
                )
            }
            maskDrawable!!.bounds = _outlineRect
            maskDrawable!!.draw(canvas)
            canvas.saveLayer(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                _maskPaint,
                Canvas.ALL_SAVE_FLAG
            )
            super.onDraw(canvas)
            onDrawInMask(canvas)
            canvas.restoreToCount(save)
        }
        //end...

        if (drawBorder) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth.toFloat()
            paint.color = borderColor
            _outlineRectF.inset(paint.strokeWidth / 2, paint.strokeWidth / 2)
            canvas.drawRoundRect(_outlineRectF, imageRadius.toFloat(), imageRadius.toFloat(), paint)
        }
    }

    open fun onDrawInMask(canvas: Canvas) {

    }

    inner class ShapeOutline : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(_outlineRect, imageRadius.toFloat())
        }
    }
}