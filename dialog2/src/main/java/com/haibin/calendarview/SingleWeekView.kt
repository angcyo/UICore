package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.toBrightness
import com.angcyo.library.ex.toColor

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/10/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class SingleWeekView(context: Context) : DefaultWeekView(context) {

    /**矩形的圆角*/
    var radius = 4 * dp

    init {
        mPadding *= 2
        mRadio = 2.5f * dp
    }

    /**选中日期时,绘制*/
    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean
    ): Boolean {
        //return super.onDrawSelected(canvas, calendar, x, y, hasScheme)
        drawSelected(canvas, x)
        return true
    }

    fun drawSelected(
        canvas: Canvas,
        x: Int,
        color: Int = mDelegate.selectedThemeColor
    ) {
        mSelectedPaint.style = Paint.Style.FILL
        mSelectedPaint.color = color
        canvas.drawRoundRect(
            (x + mPadding).toFloat(),
            (mPadding).toFloat(),
            (x + mItemWidth - mPadding).toFloat(),
            (mItemHeight - mPadding).toFloat(),
            radius, radius,
            mSelectedPaint
        )
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {
        //super.onDrawScheme(canvas, calendar, x)
    }

    fun drawScheme(
        canvas: Canvas,
        calendar: Calendar,
        cx: Float,
        cy: Float,
        color: Int = mDelegate.schemeThemeColor
    ) {
        mSchemeBasicPaint.color = color
        canvas.drawCircle(cx, cy, mRadio, mSchemeBasicPaint)
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        //super.onDrawText(canvas, calendar, x, y, hasScheme, isSelected)

        val cx = x + mItemWidth / 2f
        //val top = y //- mItemHeight / 6

        val textPaint = when {
            isSelected -> mSelectTextPaint
            calendar.isBefore(mDelegate.currentDay) -> mOtherMonthTextPaint
            calendar.isCurrentDay -> mCurDayTextPaint
            calendar.isCurrentMonth -> mCurMonthTextPaint
            else -> mOtherMonthTextPaint
        }

        val schemeColor = when {
            isSelected -> mSelectTextPaint.color
            calendar.isBefore(mDelegate.currentDay) -> mDelegate.schemeThemeColor.toBrightness(0.8f)
            else -> mDelegate.schemeThemeColor
        }

        val textHeight = textPaint.textHeight()
        val textTop = mTextBaseLine - textHeight / 4

        if (!isSelected && calendar.isCurrentDay) {
            drawSelected(canvas, x, "#2C3C4A".toColor())
        }

        canvas.drawText(calendar.day.toString(), cx, textTop, textPaint)
        if (hasScheme) {
            drawScheme(canvas, calendar, cx, textTop + 8 * dp, schemeColor)
        }
    }
}