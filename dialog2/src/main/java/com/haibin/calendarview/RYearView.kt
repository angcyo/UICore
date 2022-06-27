package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.dialog2.R
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.textWidth
import com.haibin.calendarview.CalendarUtil.isLeapYear
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class RYearView(context: Context) : DefaultYearView(context) {

    companion object {
        var LEAP_YEAR_STRING = "闰年"
    }

    private var textPadding: Int = 3 * dpi
    private val tempCalendar = Calendar()
    var radius: Float = 0f

    override fun onDrawWeek(canvas: Canvas, week: Int, x: Int, y: Int, width: Int, height: Int) {
    }

    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
    }

    override fun onDrawMonth(
        canvas: Canvas,
        year: Int,
        month: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        val text = context.resources.getStringArray(R.array.month_string_array)[month - 1]

        //判断当前年月是否在可选择的范围内(第一天和最后一天, 有一天在范围内,就行)
        tempCalendar.year = year
        tempCalendar.month = month
        tempCalendar.day = 1

        val isFirstInRange = CalendarUtil.isCalendarInRange(tempCalendar, mDelegate)

        tempCalendar.day = CalendarUtil.getMonthDaysCount(year, month)
        val isLastInRange = CalendarUtil.isCalendarInRange(tempCalendar, mDelegate)

        val isInRange = isFirstInRange || isLastInRange

        canvas.drawText(
            text,
            (x + mItemWidth / 2 - textPadding).toFloat(),
            y + mMonthTextBaseLine,
            mMonthTextPaint.apply {
                textSize = mDelegate.mYearViewMonthTextSize.toFloat()
                color = if (isInRange) {
                    mDelegate.yearViewMonthTextColor
                } else {
                    mDelegate.outRangeTextColor
                }
            }
        )
        if (month == 2 && isLeapYear(year)) {
            val w = mMonthTextPaint.textWidth(text)

            canvas.drawText(
                LEAP_YEAR_STRING,
                (x + mItemWidth / 2 - textPadding).toFloat() + w + 6 * dp,
                y + mMonthTextBaseLine,
                mMonthTextPaint.apply { textSize = mDelegate.mYearViewDayTextSize.toFloat() }
            )
        }
    }

    override fun onPreviewHook() {
        super.onPreviewHook()
        radius = (min(mItemWidth, mItemHeight) / 2).toFloat()

        mSelectedPaint.apply {
            color = mDelegate.selectedThemeColor
            style = Paint.Style.FILL
        }
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2

        val isInRange = CalendarUtil.isCalendarInRange(calendar, mDelegate)

        val isCalendarSelected = RCalendarView.isCalendarSelected(mDelegate, calendar)
        if (isCalendarSelected) {
            val isSelectedNext = RCalendarView.isSelectNextCalendar(mDelegate, calendar)
            val isSelectedPre = RCalendarView.isSelectPreCalendar(mDelegate, calendar)
            RRangeMonthView.drawSelectedBackground(
                canvas,
                x,
                y,
                isSelectedPre,
                isSelectedNext,
                mItemWidth,
                mItemHeight,
                radius,
                mSelectedPaint
            )
        } else if (calendar == mDelegate.mSelectedCalendar) {
            RRangeMonthView.drawSelectedBackground(
                canvas,
                x,
                y,
                false,
                false,
                mItemWidth,
                mItemHeight,
                radius,
                mSelectedPaint
            )
        }

        if (!isInRange) {
            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), baselineY,
                mOtherMonthTextPaint.apply {
                    color = mDelegate.outRangeTextColor
                }
            )
        } else {
            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), baselineY,
                if (calendar.isCurrentDay) mCurDayTextPaint.apply {
                    color = if (isCalendarSelected) {
                        mDelegate.mSelectedTextColor
                    } else {
                        mDelegate.mYearViewCurDayTextColor
                    }
                } else mCurMonthTextPaint.apply {
                    color = if (isCalendarSelected) {
                        mDelegate.mSelectedTextColor
                    } else {
                        mDelegate.mYearViewDayTextColor
                    }
                }
            )
        }
    }
}