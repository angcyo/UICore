package com.angcyo.drawable.loading

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.angcyo.drawable.R
import com.angcyo.drawable.base.BaseItemDrawable
import com.angcyo.library.ex._color
import com.angcyo.library.ex.alphaRatio
import com.angcyo.library.ex.dpi
import kotlin.math.max

/**
 *
 * 模仿PostMan加载进度动画, 3个竖条, 高度从大到小, 伴随透明变化
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class PostLoadingDrawable : BaseItemDrawable() {

    /**每个竖条的宽度*/
    var itemWidth: Int = 4 * dpi

    /**最小的高度*/
    var itemHeight = 18 * dpi

    /**竖条之间的间隙*/
    var itemGap: Int = 2 * dpi

    /**圆角*/
    var itemRound: Int = 2 * dpi

    /**颜色*/
    var itemColor = _color(R.color.bg_primary_color)

    /**保持的最小高度进度*/
    var itemMinHeightProgress: Int = 30

    /**保持最小颜色进度*/
    var itemMinColorProgress: Int = 30

    init {
        val delay = 400L
        items = mutableListOf(
            DrawItem().apply {
                progress = 50
                startDelay = 0 * delay
            },
            DrawItem().apply {
                progress = 50
                startDelay = 1 * delay
            },
            DrawItem().apply {
                progress = 50
                startDelay = 2 * delay
            },
        )
    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.PostLoadingDrawable)
        itemColor = typedArray.getColor(
            R.styleable.PostLoadingDrawable_r_loading_item_color,
            itemColor
        )
        itemWidth = typedArray.getDimensionPixelOffset(
            R.styleable.PostLoadingDrawable_r_loading_item_width,
            itemWidth
        )
        itemHeight = typedArray.getDimensionPixelOffset(
            R.styleable.PostLoadingDrawable_r_loading_item_height,
            itemHeight
        )
        itemRound = typedArray.getDimensionPixelOffset(
            R.styleable.PostLoadingDrawable_r_loading_item_round,
            itemRound
        )
        itemGap = typedArray.getDimensionPixelOffset(
            R.styleable.PostLoadingDrawable_r_loading_item_gap,
            itemGap
        )
        loadingStep =
            typedArray.getInt(R.styleable.PostLoadingDrawable_r_loading_step, 3)
        typedArray.recycle()
    }

    override fun getIntrinsicWidth(): Int {
        return items.size * itemWidth + itemGap * (max(0, items.size - 1))
    }

    override fun getIntrinsicHeight(): Int {
        return itemHeight
    }

    override fun onDrawItem(canvas: Canvas, drawItem: DrawItem, index: Int) {
        val heightProgress = max(itemMinHeightProgress, drawItem.progress) / 100f
        val colorProgress = max(itemMinColorProgress, drawItem.progress) / 100f
        val left = index * (itemWidth + itemGap)
        val centerY = bounds.height() / 2
        val height = bounds.height() * heightProgress
        val color = itemColor.alphaRatio(colorProgress)
        textPaint.color = color
        drawRectF.set(
            left.toFloat(),
            centerY - height / 2,
            (left + itemWidth).toFloat(),
            centerY + height / 2
        )
        canvas.drawRoundRect(drawRectF, itemRound.toFloat(), itemRound.toFloat(), textPaint)
    }
}