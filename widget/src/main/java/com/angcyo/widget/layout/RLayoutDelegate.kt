package com.angcyo.widget.layout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.angcyo.tablayout.calcLayoutWidthHeight
import com.angcyo.tablayout.screenHeight
import com.angcyo.tablayout.screenWidth
import com.angcyo.widget.R
import com.angcyo.widget.base.exactly
import com.angcyo.widget.base.save
import kotlin.math.absoluteValue

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */
class RLayoutDelegate {

    lateinit var view: View

    /**额外的底层背景[Drawable]*/
    var bDrawable: Drawable? = null
        set(value) {
            field = value
            view.postInvalidateOnAnimation()
        }

    /**支持最大高度[com.angcyo.tablayout.TabLayoutLibExKt.calcSize]*/
    var rMaxHeight: String? = null
        set(value) {
            field = value
            view.requestLayout()
        }

    var rMaxWidth: String? = null
        set(value) {
            field = value
            view.requestLayout()
        }

    /**宽高的比例[1:1] [1:2.5], 宽高必须有一个要是1f*/
    var layoutDimensionRatio: String? = null
        set(value) {
            field = value
            view.requestLayout()
        }

    /**布局蒙层*/
    var maskDrawable: Drawable? = null
        set(value) {
            field = value
            if (value != null && _maskPaint == null) {
                _maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    isFilterBitmap = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                }
                view.setWillNotDraw(false)
            }
        }

    var _maskPaint: Paint? = null

    fun initAttribute(view: View, attributeSet: AttributeSet?) {
        this.view = view
        val typedArray =
            view.context.obtainStyledAttributes(attributeSet, R.styleable.RLayout)

        bDrawable = typedArray.getDrawable(R.styleable.RLayout_r_background)
        rMaxHeight = typedArray.getString(R.styleable.RLayout_r_max_height)
        rMaxWidth = typedArray.getString(R.styleable.RLayout_r_max_width)
        maskDrawable = typedArray.getDrawable(R.styleable.RLayout_r_layout_mask_drawable)
        layoutDimensionRatio = typedArray.getString(R.styleable.RLayout_r_dimension_ratio)

        if (typedArray.hasValue(R.styleable.RLayout_r_clip_to_outline)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.clipToOutline = true
            }
        }

        typedArray.recycle()
    }

    /**布局蒙版*/
    fun maskLayout(canvas: Canvas, drawSuper: () -> Unit = {}) {
        maskDrawable?.run {
            val width: Int = view.width
            val height: Int = view.height
            val saveCount = view.save(canvas)
            setBounds(
                view.paddingLeft,
                view.paddingTop,
                width - view.paddingRight,
                height - view.paddingBottom
            )
            draw(canvas)
            view.save(canvas, _maskPaint)
            drawSuper()
            canvas.restoreToCount(saveCount)
        } ?: drawSuper()
    }

    /**[bDrawable]属性支持*/
    fun draw(canvas: Canvas) {
        bDrawable?.run {
            setBounds(0, 0, view.width, view.height)
            draw(canvas)
        }
    }

    /**[rMaxHeight]属性支持*/
    fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = (view.parent as? View)?.measuredWidth ?: 0
        val parentHeight = (view.parent as? View)?.measuredHeight ?: 0

        val maxWidthHeight = view.calcLayoutWidthHeight(
            rMaxWidth, rMaxHeight,
            if (parentWidth > 0) parentWidth else view.screenWidth,
            if (parentHeight > 0) parentHeight else view.screenHeight,
            0, 0
        )

        val maxWidth = maxWidthHeight[0]
        val maxHeight = maxWidthHeight[1]

        val isWidthOut = maxWidth > -1 && view.measuredWidth > maxWidth
        val isHeightOut = maxHeight > -1 && view.measuredHeight > maxHeight

        if (isWidthOut || isHeightOut) {
            //宽高有一些项超标

            //替换布局参数, 可以提高性能
            val wSpec = if (isWidthOut) {
                view.layoutParams.width = maxWidth
                exactly(maxWidth)
            } else {
                widthMeasureSpec
            }

            val hSpec = if (isHeightOut) {
                view.layoutParams.height = maxHeight
                exactly(maxHeight)
            } else {
                heightMeasureSpec
            }

            //如果使用重新测量的方式,布局就会每次都至少测量2次
            view.measure(wSpec, hSpec)
        }

        //比例计算
        layoutDimensionRatio?.run {
            var ratio: Float = -1f

            var widthFactor = 1f
            var heightFactor = 1f

            if (contains(":")) {
                split(":").also {
                    widthFactor = it[0].toFloatOrNull() ?: -1f
                    heightFactor = it[1].toFloatOrNull() ?: 0f
                    ratio = widthFactor / heightFactor
                }
            } else {
                toFloatOrNull()?.run {
                    heightFactor = 1f
                    widthFactor = this
                    ratio = widthFactor / heightFactor
                }
            }

            if (ratio > 0f) {
                val vWidth = view.measuredWidth
                val vHeight = view.measuredHeight
                val rawRatio = vWidth * 1f / vHeight

                if ((rawRatio - ratio).absoluteValue > 0.1f) {
                    //比例需要调整

                    val wSpec: Int
                    val hSpec: Int

                    if (widthFactor == 1f) {
                        //以宽度作为基数调整
                        wSpec = exactly(vWidth)
                        hSpec = exactly((vWidth * heightFactor).toInt())
                    } else {
                        //以高度作为基数调整
                        wSpec = exactly((vHeight * widthFactor).toInt())
                        hSpec = exactly(vHeight)
                    }
                    view.measure(wSpec, hSpec)
                }
            }
        }
    }
}