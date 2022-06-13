package com.angcyo.drawable.loading

import android.graphics.Canvas
import android.graphics.RectF
import com.angcyo.drawable.base.BaseSectionDrawable
import com.angcyo.library.L
import com.angcyo.library.R
import com.angcyo.library.ex.*

/**
 *
 * 模仿PostMan加载进度动画, 3个竖条, 高度从大到小, 伴随透明变化
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class PostLoadingDrawable : BaseSectionDrawable() {

    /**每个竖条的宽度*/
    var itemWidth: Int = 4 * dpi

    /**最小的高度*/
    var itemMinHeight = 14 * dpi

    /**竖条之间的间隙*/
    var itemGap: Int = 2 * dpi

    /**圆角*/
    var itemRound: Float = 2 * dp

    /**颜色*/
    var itemColor = _color(R.color.bg_primary_color)

    var paint = paint().apply {
        //color
    }

    init {
        sections = floatArrayOf(0.33f, 0.33f, 0.33f)
    }

    override fun getIntrinsicWidth(): Int {
        return sections.size * itemWidth + itemGap * (sections.size - 1)
    }

    override fun getIntrinsicHeight(): Int {
        return itemMinHeight
    }

    val _tempRect = RectF()

    /**绘制一个item*/
    fun drawItem(canvas: Canvas, index: Int, progress: Float) {
        if (!isInEditMode) {
            L.w("index:$index progress:$progress")
        }

        val left = index * (itemWidth + itemGap)
        val centerY = bounds.height() / 2
        val height = bounds.height() * progress
        val color = itemColor.alphaRatio(progress)
        paint.color = color
        _tempRect.set(
            left.toFloat(),
            centerY - height / 2,
            (left + itemWidth).toFloat(),
            centerY + height / 2
        )
        canvas.drawRoundRect(_tempRect, itemRound, itemRound, paint)
    }

    override fun onDrawProgressSection(
        canvas: Canvas,
        index: Int,
        startProgress: Float,
        endProgress: Float,
        totalProgress: Float,
        sectionProgress: Float
    ) {
        super.onDrawProgressSection(
            canvas,
            index,
            startProgress,
            endProgress,
            totalProgress,
            sectionProgress
        )
    }

    override fun onDrawSection(
        canvas: Canvas,
        maxSection: Int,
        index: Int,
        totalProgress: Float,
        progress: Float
    ) {
        super.onDrawSection(canvas, maxSection, index, totalProgress, progress)
        drawItem(canvas, index, progress)
    }
}