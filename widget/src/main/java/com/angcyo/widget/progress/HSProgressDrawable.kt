package com.angcyo.widget.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import com.angcyo.drawable.base.BaseSectionDrawable
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.dp
import com.angcyo.widget.R
import kotlin.math.min

/**
 * 第一阶段: 从左开始, 拉长透明, 并高度减少
 * 第二阶段: 从右开始, 拉长透明, 并高度减少
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class HSProgressDrawable : BaseSectionDrawable() {

    /**进度颜色*/
    var progressColor = Color.WHITE

    /**圆角大小*/
    var roundSize = 5 * dp

    init {
        sections = floatArrayOf(0.5f, 0.5f)

        interpolatorList = listOf(DecelerateInterpolator(), DecelerateInterpolator())
    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.HSProgressDrawable)
        progressColor =
            typedArray.getColor(R.styleable.HSProgressDrawable_r_progress_color, progressColor)
        roundSize = typedArray.getDimensionPixelOffset(
            R.styleable.HSProgressDrawable_r_progress_round_size,
            roundSize.toInt()
        ).toFloat()
        typedArray.recycle()

        if (isInEditMode) {
            progress = 50
        }
    }

    override fun onDrawProgressSection(
        canvas: Canvas,
        index: Int,
        startProgress: Float,
        endProgress: Float,
        totalProgress: Float,
        sectionProgress: Float
    ) {

        //颜色透明控制
        textPaint.color = progressColor.alpha(255 * (1 - sectionProgress + 0.2f))

        val right = viewWidth - paddingRight

        var top = paddingTop
        val threshold = 0.9f

        if (sectionProgress > threshold) {
            //高度变化控制
            top = (top + viewDrawHeight * (min(
                (sectionProgress - threshold) / (1 - threshold),
                0.99f
            ))).toInt()
        }

        //绘制
        when {
            isInEditMode -> drawRectF.set(
                paddingLeft.toFloat(),
                top.toFloat(),
                right * totalProgress,
                (viewHeight - paddingBottom).toFloat()
            )
            index == 0 -> drawRectF.set(
                paddingLeft.toFloat(),
                top.toFloat(),
                right * sectionProgress,
                (viewHeight - paddingBottom).toFloat()
            )
            index == 1 -> drawRectF.set(
                right - viewDrawWidth * sectionProgress,
                top.toFloat(),
                right.toFloat(),
                (viewHeight - paddingBottom).toFloat()
            )
        }
        canvas.drawRoundRect(drawRectF, roundSize, roundSize, textPaint)
    }

}