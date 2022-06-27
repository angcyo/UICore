package com.haibin.calendarview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RCalendarViewDelegate(context: Context, attributeSet: AttributeSet? = null) :
    CalendarViewDelegate(context, attributeSet) {

    var isInEditMode = false

    var outRangeTextColor = Color.parseColor("#CBCBCB")
    var outRangeTextLunarColor = Color.parseColor("#CBCBCB")

    override fun init() {
        //calendarItemHeight = 100 * dpi
        //monthViewShowMode = MODE_ALL_MONTH
        //selectMode = SELECT_MODE_DEFAULT

        //2021-10-18

        //顶上的星期栏
        if (mWeekBarClassPath.isNullOrEmpty()) {
            mWeekBarClassPath = WeekBar::class.java.name
        }
        //年视图
        if (mYearViewClassPath.isNullOrEmpty()) {
            mYearViewClassPath = RYearView::class.java.name
        }
        //月视图
        if (mMonthViewClassPath.isNullOrEmpty()) {
            mMonthViewClassPath = RMonthView::class.java.name
        }
        //周视图
        if (mWeekViewClassPath.isNullOrEmpty()) {
            mWeekViewClassPath = RWeekView::class.java.name
        }

        super.init()

        monthViewClass
        weekBarClass
        weekViewClass
        yearViewClass
    }

    fun getSelectedStartRangeCalendar(): Calendar? {
        return mSelectedStartRangeCalendar
    }

    fun getSelectedEndRangeCalendar(): Calendar? {
        return mSelectedEndRangeCalendar
    }
}