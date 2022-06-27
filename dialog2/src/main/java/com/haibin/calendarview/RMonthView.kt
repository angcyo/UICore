package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas

/**
 * 选中时,是矩形的月视图
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/04
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class RMonthView(context: Context) : DefaultMonthView(context) {

    /**显示阴历*/
    var showLunar = true

    override fun initPaint() {
        super.initPaint()
        mCurMonthTextPaint
    }

    /**1: 选中时绘制
     * [draw]*/
    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        return super.onDrawSelected(canvas, calendar, x, y, hasScheme)
    }

    /**2: 有事务时绘制
     * [draw]*/
    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        super.onDrawScheme(canvas, calendar, x, y)
    }

    /**3: 绘制日历文本
     * [draw]*/
    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        val top = y - mItemHeight / 6

        val textPaint = when {
            isSelected -> mSelectTextPaint //选中的笔
            calendar.isCurrentMonth -> mCurMonthTextPaint //当前月的笔
            else -> mOtherMonthTextPaint //其他月的笔
        }

        //阳历
        canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, textPaint)

        //阴历
        if (showLunar) {
            val lunarPaint = when {
                isSelected -> mSelectedLunarTextPaint
                calendar.isCurrentMonth -> mCurMonthLunarTextPaint
                else -> mOtherMonthLunarTextPaint
            }

            canvas.drawText(
                calendar.lunar,
                cx.toFloat(),
                mTextBaseLine + y.toFloat() + (mItemHeight / 10).toFloat(),
                lunarPaint
            )
        }
    }
}