package com.haibin.calendarview.fragment

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dialog2.R
import com.angcyo.library.L
import com.angcyo.library.ex._color
import com.angcyo.library.toastQQ
import com.angcyo.library.ex.setBgDrawable
import com.angcyo.widget.layout.RCoordinatorLayout
import com.angcyo.widget.layout.isEnableCoordinator
import com.haibin.calendarview.*

/**
 * 日历基类界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/10/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseCalendarFragment : BaseDslFragment() {

    var calendarView: RCalendarView? = null

    init {
        contentLayoutId = R.layout.lib_calendar_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentConfig.titleBarBackgroundDrawable = _color(R.color.calendar_bg_color).toDrawable()
        fragmentConfig.titleItemIconColor = Color.WHITE
        fragmentConfig.titleItemTextColor = Color.WHITE
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        onInitCalendarLayout()
    }

    override fun onInitFragment(savedInstanceState: Bundle?) {
        super.onInitFragment(savedInstanceState)
    }

    open fun onInitCalendarLayout() {
        _vh.v<RCoordinatorLayout>(R.id.lib_coordinator_wrap_layout)?.isEnableCoordinator = false
        _recycler.setBgDrawable(fragmentConfig.fragmentBackgroundDrawable)

        calendarView = _vh.v(R.id.calendar_view)
        calendarView?.apply {
            monthViewPager?.orientation = LinearLayout.VERTICAL

            //设置周末的颜色
            //setWeekendTextColor()

            //_updateCurrentMonth()
            _vh.click(R.id.pre_month_view) {
                toPreMonth()
            }
            _vh.click(R.id.next_month_view) {
                toNextMonth()
            }
            _vh.click(R.id.current_month_view) {
                toYearSelect(selectedCalendar.year)
            }

            onCalendarSelectListener { calendar, isClick, isOutOfRange ->
                L.i("选中日期: ", calendar, " :", isClick, " :", isOutOfRange)
                //_updateCurrentMonth()
                selectCalendar(calendar)
            }
            onWeekChangeListener {
                L.i(it)
                //_updateCurrentMonth()
            }
            onMonthChangeListener { year, month ->
                updateCurrentMonth(year, month)
                onMonthChange(year, month)
            }

            toToday()
            val year = selectedCalendar.year
            val month = selectedCalendar.month
            updateCurrentMonth(year, month)
            onMonthChange(year, month)
        }
    }

    var _lastSelectCalendar: Calendar? = null

    open fun selectCalendar(calendar: Calendar) {
        if (_lastSelectCalendar == calendar) {

        } else {
            _lastSelectCalendar = calendar
            onCalendarSelect(calendar)
        }
    }

    open fun updateCurrentMonth(year: Int, month: Int) {
        _vh.tv(R.id.current_month_view)?.text = "${year}年${month}月"
    }

    /**月份改变回调*/
    open fun onMonthChange(year: Int, month: Int) {
        toastQQ("$year/${month}")
    }

    /**选中日历的回调*/
    open fun onCalendarSelect(calendar: Calendar) {
        toastQQ(calendar.toString())
    }

}