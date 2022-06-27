package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.min


/**
 * 选中时,是圆角的月视图
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/04
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class RCircleMonthView(context: Context) : RMonthView(context) {

    /**1: 选中时绘制
     * [draw]*/
    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        mSelectedPaint.style = Paint.Style.FILL
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2

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