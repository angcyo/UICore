package com.angcyo.drawable.loading

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.angcyo.drawable.R
import com.angcyo.drawable.base.BaseProgressDrawable
import com.angcyo.library._refreshRateRatio
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toColorInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseTGLoadingDrawable : BaseProgressDrawable() {

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    /**是否需要动画*/
    var loading: Boolean = true
        set(value) {
            field = value
            invalidateSelf()
        }

    /**加载的背景颜色*/
    var loadingBgColor: Int = "#80000000".toColorInt()

    /**加载的颜色*/
    var loadingColor: Int = Color.WHITE

    /**加载的宽度*/
    var loadingWidth: Float = 3 * dp

    /**加载进度偏移背景的距离*/
    var loadingOffset: Float = 0f

    /**不明确进度时的绘制扫描的角度*/
    var indeterminateSweepAngle = 3f

    /**角度绘制动画步进的进度*/
    var angleStep = 4f

    //
    val _loadingRectF = RectF()

    //当前的角度
    var _angle = 0f

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.TGSolidLoadingDrawable)
        loadingBgColor = typedArray.getColor(
            R.styleable.TGSolidLoadingDrawable_r_loading_bg_color,
            loadingBgColor
        )
        loadingColor = typedArray.getColor(
            R.styleable.TGSolidLoadingDrawable_r_loading_color,
            loadingColor
        )

        loadingWidth = typedArray.getDimensionPixelOffset(
            R.styleable.TGSolidLoadingDrawable_r_loading_width,
            loadingWidth.toInt()
        ).toFloat()
        loadingOffset = typedArray.getDimensionPixelOffset(
            R.styleable.TGSolidLoadingDrawable_r_loading_offset,
            loadingOffset.toInt()
        ).toFloat()

        indeterminateSweepAngle = typedArray.getFloat(
            R.styleable.TGSolidLoadingDrawable_r_loading_indeterminate_sweep_angle,
            indeterminateSweepAngle
        )
        angleStep = typedArray.getFloat(
            R.styleable.TGSolidLoadingDrawable_r_loading_angle_step,
            angleStep
        )

        typedArray.recycle()
    }

    open fun doAngle() {
        //动画
        _angle += angleStep / _refreshRateRatio
        _angle %= 360
        invalidateSelf()
    }

}