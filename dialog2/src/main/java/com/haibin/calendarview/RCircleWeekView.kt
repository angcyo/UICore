package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.min

/**
 * 选中时,是圆角的周视图
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/10/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class RCircleWeekView(context: Context) : RWeekView(context) {

    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean
    ): Boolean {
        mSelectedPaint.style = Paint.Style.FILL
        val cx = x + mItemWidth / 2
        val cy = mItemHeight / 2

        val radius = if (isTouchDown && mCurrentItem == mItems.indexOf(index)) {
            //点击当前选中的item, 缩放效果提示
            min(mItemWidth / 2, mItemHeight / 2).toFloat() - mPadding * 2
        } else {
            min(mItemWidth / 2, mItemHeight / 2).toFloat() - mPadding
        }

        canvas.drawCircle(
            cx.toFloat(),
            cy.toFloat(),
            radius,
            mSelectedPaint
        )
        return true
    }
}