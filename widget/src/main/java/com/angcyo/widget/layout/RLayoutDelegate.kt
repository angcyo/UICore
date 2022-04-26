package com.angcyo.widget.layout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.angcyo.drawable.DrawLineDrawable
import com.angcyo.library.ex.calcLayoutWidthHeight
import com.angcyo.library.ex.save
import com.angcyo.tablayout.exactlyMeasure
import com.angcyo.tablayout.screenHeight
import com.angcyo.tablayout.screenWidth
import com.angcyo.widget.R
import com.angcyo.widget.base.exactly
import kotlin.math.absoluteValue

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */
class RLayoutDelegate : ClipLayoutDelegate() {

    /**额外的底层背景[Drawable]*/
    var bDrawable: Drawable? = null
        set(value) {
            field = value
            delegateView.postInvalidateOnAnimation()
            if (value != null) {
                delegateView.setWillNotDraw(false)
            }
        }

    /**支持最大高度[com.angcyo.library.ex.RKtxKt.calcSize]*/
    var rMaxHeight: String? = null
        set(value) {
            field = value
            delegateView.requestLayout()
        }

    var rMaxWidth: String? = null
        set(value) {
            field = value
            delegateView.requestLayout()
        }

    var rMinHeight: String? = null
        set(value) {
            field = value
            delegateView.requestLayout()
        }

    var rMinWidth: String? = null
        set(value) {
            field = value
            delegateView.requestLayout()
        }

    var rLayoutWidth: String? = null
        set(value) {
            field = value
            delegateView.requestLayout()
        }

    var rLayoutHeight: String? = null
        set(value) {
            field = value
            delegateView.requestLayout()
        }

    /**宽高的比例[1:1] [1:2.5], 宽高必须有一个要是1f*/
    var layoutDimensionRatio: String? = null
        set(value) {
            field = value
            delegateView.requestLayout()
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
                delegateView.setWillNotDraw(false)
            }
        }

    var _maskPaint: Paint? = null

    /**横线绘制*/
    var drawLineDrawable = DrawLineDrawable()

    override fun initAttribute(view: View, attributeSet: AttributeSet?) {
        super.initAttribute(view, attributeSet)
        val typedArray =
            view.context.obtainStyledAttributes(attributeSet, R.styleable.RLayoutDelegate)

        bDrawable = typedArray.getDrawable(R.styleable.RLayoutDelegate_r_background)
        rMaxHeight = typedArray.getString(R.styleable.RLayoutDelegate_r_max_height)
        rMinHeight = typedArray.getString(R.styleable.RLayoutDelegate_r_min_height)
        rLayoutHeight = typedArray.getString(R.styleable.RLayoutDelegate_r_layout_height)
        rMaxWidth = typedArray.getString(R.styleable.RLayoutDelegate_r_max_width)
        rMinWidth = typedArray.getString(R.styleable.RLayoutDelegate_r_min_width)
        rLayoutWidth = typedArray.getString(R.styleable.RLayoutDelegate_r_layout_width)
        maskDrawable = typedArray.getDrawable(R.styleable.RLayoutDelegate_r_layout_mask_drawable)
        layoutDimensionRatio = typedArray.getString(R.styleable.RLayoutDelegate_r_dimension_ratio)

        if (typedArray.hasValue(R.styleable.RLayoutDelegate_r_clip_to_outline)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.clipToOutline = true
            }
        }

        typedArray.recycle()

        drawLineDrawable.callback = view
        drawLineDrawable.initAttribute(view.context, attributeSet)
    }

    /**布局蒙版*/
    override fun maskLayout(canvas: Canvas, drawSuper: () -> Unit) {
        super.maskLayout(canvas) {
            maskDrawable?.run {
                val width: Int = delegateView.width
                val height: Int = delegateView.height
                val saveCount = delegateView.save(canvas)
                setBounds(
                    delegateView.paddingLeft,
                    delegateView.paddingTop,
                    width - delegateView.paddingRight,
                    height - delegateView.paddingBottom
                )
                draw(canvas)
                delegateView.save(canvas, _maskPaint)
                drawSuper()
                canvas.restoreToCount(saveCount)
            } ?: drawSuper()

            drawLineDrawable.setBounds(
                0,
                0,
                delegateView.measuredWidth,
                delegateView.measuredHeight
            )
            drawLineDrawable.draw(canvas)
        }
    }

    /**[bDrawable]属性支持*/
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        bDrawable?.run {
            setBounds(0, 0, delegateView.width, delegateView.height)
            draw(canvas)
        }
    }

    /**返回xml中配合的[r_layout_width] [r_layout_height]解析后的宽高*/
    fun layoutWidthHeight(): IntArray {
        val parentWidth = (delegateView.parent as? View)?.measuredWidth ?: 0
        val parentHeight = (delegateView.parent as? View)?.measuredHeight ?: 0

        val layoutWidthHeight = delegateView.context.calcLayoutWidthHeight(
            rLayoutWidth, rLayoutHeight,
            if (parentWidth > 0) parentWidth else delegateView.screenWidth,
            if (parentHeight > 0) parentHeight else delegateView.screenHeight,
            0, 0
        )

        return layoutWidthHeight
    }

    /**返回替换后的测量宽高Spec*/
    fun layoutWidthHeightSpec(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        var widthSpec: Int = widthMeasureSpec
        var heightSpec: Int = heightMeasureSpec

        val layoutWidthHeight = layoutWidthHeight()
        val width = layoutWidthHeight[0]
        val height = layoutWidthHeight[1]

        if (width != -1 || height != -1) {
            if (width != -1) {
                //设置[r_layout_width]
                widthSpec = exactlyMeasure(width)
            }
            if (height != -1) {
                //设置[r_layout_height]
                heightSpec = exactlyMeasure(height)
            }
        }
        return intArrayOf(widthSpec, heightSpec)
    }

    /**返回约束了比例,替换后的测量宽高Spec*/
    fun layoutDimensionRatioSpec(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        var wSpec: Int = widthMeasureSpec
        var hSpec: Int = heightMeasureSpec

        layoutDimensionRatio?.run {
            //约束了比例计算
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

                val vWidth = View.MeasureSpec.getSize(widthMeasureSpec)
                val vHeight = View.MeasureSpec.getSize(heightMeasureSpec)
                val rawRatio = vWidth * 1f / vHeight

                if ((rawRatio - ratio).absoluteValue > 0.00000001f) {
                    //比例需要调整

                    if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY) {
                        //以高度作为基数调整
                        wSpec = exactly((vHeight * widthFactor).toInt())
                        hSpec = exactly(vHeight)
                    } else if (View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY) {
                        //以宽度作为基数调整
                        wSpec = exactly(vWidth)
                        hSpec = exactly((vWidth * heightFactor).toInt())
                    }
                }
            }
        }

        return intArrayOf(wSpec, hSpec)
    }

    /**[rMaxHeight]属性支持*/
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        val parentWidth = (delegateView.parent as? View)?.measuredWidth ?: 0
        val parentHeight = (delegateView.parent as? View)?.measuredHeight ?: 0

        val maxWidthHeight = delegateView.context.calcLayoutWidthHeight(
            rMaxWidth, rMaxHeight,
            if (parentWidth > 0) parentWidth else delegateView.screenWidth,
            if (parentHeight > 0) parentHeight else delegateView.screenHeight,
            0, 0
        )

        //---------------------最大宽高限制------------------------

        val maxWidth = maxWidthHeight[0]
        val maxHeight = maxWidthHeight[1]

        val isWidthOut = maxWidth > -1 && delegateView.measuredWidth > maxWidth
        val isHeightOut = maxHeight > -1 && delegateView.measuredHeight > maxHeight

        if (isWidthOut || isHeightOut) {
            //宽高有一项超标

            //替换布局参数, 可以提高性能
            val wSpec = if (isWidthOut) {
                delegateView.layoutParams.width = maxWidth
                exactly(maxWidth)
            } else {
                widthMeasureSpec
            }

            val hSpec = if (isHeightOut) {
                delegateView.layoutParams.height = maxHeight
                exactly(maxHeight)
            } else {
                heightMeasureSpec
            }

            //如果使用重新测量的方式,布局就会每次都至少测量2次
            delegateView.measure(wSpec, hSpec)
        }

        //---------------------比例限制------------------------

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
                val vWidth = delegateView.measuredWidth
                val vHeight = delegateView.measuredHeight
                val rawRatio = vWidth * 1f / vHeight

                if ((rawRatio - ratio).absoluteValue > 0.00000001f) {
                    //比例需要调整

                    val wSpec: Int
                    val hSpec: Int

                    if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY && heightFactor == 1f) {
                        //以高度作为基数调整
                        wSpec = exactly((vHeight * widthFactor).toInt())
                        hSpec = exactly(vHeight)
                    } else {//widthFactor == 1f
                        //以宽度作为基数调整
                        wSpec = exactly(vWidth)
                        hSpec = exactly((vWidth * heightFactor).toInt())
                    }

                    delegateView.measure(wSpec, hSpec)
                }
            }
        }

        return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val parentWidth = (delegateView.parent as? View)?.measuredWidth ?: 0
        val parentHeight = (delegateView.parent as? View)?.measuredHeight ?: 0

        val minWidthHeight = delegateView.context.calcLayoutWidthHeight(
            rMinWidth, rMinHeight,
            if (parentWidth > 0) parentWidth else delegateView.screenWidth,
            if (parentHeight > 0) parentHeight else delegateView.screenHeight,
            0, 0
        )
        //---------------------最小宽高限制------------------------

        val minWidth = minWidthHeight[0]
        val minHeight = minWidthHeight[1]

        if (minWidth != -1) {
            delegateView.minimumWidth = minWidth
        }

        if (minHeight != -1) {
            delegateView.minimumHeight = minHeight
        }
    }
}