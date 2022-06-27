package com.haibin.calendarview

import android.content.Context
import android.util.AttributeSet
import com.angcyo.library.ex.toColor

/**
 * https://github.com/huanghaibin-dev/CalendarView
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/04
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class RCalendarView(context: Context, attributeSet: AttributeSet? = null) :
    CalendarView(context, attributeSet) {

    companion object {

        /**
         * 上一个日期是否选中
         *
         * @param calendar 当前日期
         * @return 上一个日期是否选中
         */
        fun isSelectPreCalendar(delegate: CalendarViewDelegate, calendar: Calendar): Boolean {
            val preCalendar = CalendarUtil.getPreCalendar(calendar)
            delegate.updateCalendarScheme(preCalendar)
            return delegate.mSelectedStartRangeCalendar != null && isCalendarSelected(
                delegate,
                preCalendar
            )
        }

        /**
         * 下一个日期是否选中
         *
         * @param calendar 当前日期
         * @return 下一个日期是否选中
         */
        fun isSelectNextCalendar(delegate: CalendarViewDelegate, calendar: Calendar): Boolean {
            val nextCalendar = CalendarUtil.getNextCalendar(calendar)
            delegate.updateCalendarScheme(nextCalendar)
            return delegate.mSelectedStartRangeCalendar != null && isCalendarSelected(
                delegate,
                nextCalendar
            )
        }

        /**
         * 日历是否被选中
         *
         * @param calendar calendar
         * @return 日历是否被选中
         */
        fun isCalendarSelected(delegate: CalendarViewDelegate, calendar: Calendar): Boolean {
            if (delegate.mSelectedStartRangeCalendar == null) {
                return false
            }
            if (onCalendarIntercept(delegate, calendar)) {
                return false
            }
            return if (delegate.mSelectedEndRangeCalendar == null) {
                calendar.compareTo(delegate.mSelectedStartRangeCalendar) == 0
            } else calendar >= delegate.mSelectedStartRangeCalendar && calendar <= delegate.mSelectedEndRangeCalendar
        }

        /**
         * 是否拦截日期，此设置续设置mCalendarInterceptListener
         *
         * @param calendar calendar
         * @return 是否拦截日期
         */
        fun onCalendarIntercept(delegate: CalendarViewDelegate, calendar: Calendar): Boolean {
            return delegate.mCalendarInterceptListener != null && delegate.mCalendarInterceptListener.onCalendarIntercept(
                calendar
            )
        }
    }

    override fun init(context: Context, attrs: AttributeSet?) {
        mDelegate = RCalendarViewDelegate(context, attrs).apply {
            isInEditMode = this@RCalendarView.isInEditMode
        }
        super.init(context, attrs)
    }

    fun getCalendarViewDelegate(): RCalendarViewDelegate = mDelegate as RCalendarViewDelegate

    fun setWeekendTextColor(color: Int = "#E14A4C".toColor()) {
        mWeekBar?.setWeekendTextColor(color)
    }

}