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
import com.angcyo.widget.base.InvalidateProperty
import com.angcyo.widget.base.drawHeight
import com.angcyo.widget.base.drawWidth
import com.angcyo.widget.base.save
import kotlin.math.min

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
    var drawBorder: Boolean by InvalidateProperty(true)

    /**边框的宽度*/
    var borderWidth: Int by InvalidateProperty(2 * dpi)

    /**是否是圆*/
    var isCircle: Boolean by InvalidateProperty(false)

    /**边框的颜色*/
    var borderColor: Int by InvalidateProperty(Color.WHITE)

    /**激活shape绘制功能*/
    var enableShape: Boolean = true
        set(value) {
            field = value
            if (value) {
                outlineProvider = ShapeOutline()
                clipToOutline = true
            } else {
                outlineProvider = null
                clipToOutline = false
            }
        }

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
        enableShape = typedArray.getBoolean(R.styleable.ShapeImageView_r_enable_shape, enableShape)
        isCircle = typedArray.getBoolean(R.styleable.ShapeImageView_r_is_circle, isCircle)
        borderWidth = typedArray.getDimensionPixelOffset(
            R.styleable.ShapeImageView_r_border_width,
            borderWidth
        )
        borderColor = typedArray.getColor(R.styleable.ShapeImageView_r_border_color, borderColor)
        maskDrawable = typedArray.getDrawable(R.styleable.ShapeImageView_r_mask_drawable)
        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (enableShape) {
            val right = w - paddingRight
            val bottom = h - paddingBottom
            if (isCircle) {
                val centerX = paddingLeft + drawWidth / 2
                val centerY = paddingTop + drawHeight / 2
                val radius = min(drawWidth / 2, drawHeight / 2)
                _outlineRect.set(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius
                )
            } else {
                _outlineRect.set(paddingLeft, paddingTop, right, bottom)
            }
        } else {
            _outlineRect.set(0, 0, w, h)
        }
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
            val save = save(canvas)
            maskDrawable!!.bounds = _outlineRect
            maskDrawable!!.draw(canvas)
            save(canvas, _maskPaint)
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
            if (isCircle) {
                canvas.drawCircle(
                    _outlineRectF.centerX(),
                    _outlineRectF.centerY(),
                    _outlineRectF.width() / 2,
                    paint
                )
            } else {
                canvas.drawRoundRect(
                    _outlineRectF,
                    imageRadius.toFloat(),
                    imageRadius.toFloat(),
                    paint
                )
            }
        }
    }

    /**绘制在mask内的自定义内容*/
    open fun onDrawInMask(canvas: Canvas) {

    }

    inner class ShapeOutline : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            if (isCircle) {
                outline.setOval(
                    _outlineRect.left,
                    _outlineRect.top,
                    _outlineRect.right,
                    _outlineRect.bottom
                )
            } else {
                outline.setRoundRect(_outlineRect, imageRadius.toFloat())
            }
        }
    }
}