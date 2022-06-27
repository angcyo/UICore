package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.library.ex.dpi
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RRangeMonthView(context: Context) : RangeMonthView(context) {

    companion object {

        fun drawSelectedBackground(
            canvas: Canvas,
            x: Int,
            y: Int,
            isSelectedPre: Boolean,
            isSelectedNext: Boolean,
            mItemWidth: Int,
            mItemHeight: Int,
            radius: Float,
            paint: Paint
        ): Boolean {

            val itemWidth = mItemWidth + 1 * dpi
            val cx = x + itemWidth / 2
            val cy = y + mItemHeight / 2

            if (isSelectedPre && isSelectedNext) {
                //前后都有选中
                canvas.drawRect(
                    x.toFloat(),
                    (cy - radius),
                    (x + itemWidth).toFloat(),
                    (cy + radius),
                    paint
                )
            } else {
                if (isSelectedNext) {
                    //下一个是选中状态, 绘制右半矩形
                    canvas.drawRect(
                        cx.toFloat(),
                        (cy - radius),
                        (x + itemWidth).toFloat(),
                        (cy + radius),
                        paint
                    )
                } else if (isSelectedPre) {
                    //上一个是选中状态, 绘制左半矩形
                    canvas.drawRect(
                        x.toFloat(),
                        (cy - radius),
                        cx.toFloat(),
                        (cy + radius),
                        paint
                    )
                }
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, paint)
            }
            return true
        }
    }


    /**显示阴历*/
    var showLunar = true

    var radius: Float = 0f

    override fun initPaint() {
        super.initPaint()
        mCurMonthTextPaint
    }

    override fun onPreviewHook() {
        radius = (min(mItemWidth, mItemHeight) / 5 * 2).toFloat()
        mSelectedPaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun isSelectPreCalendar(calendar: Calendar, calendarIndex: Int): Boolean {
        if (isInEditMode) {
            return calendar.day in 2..10
        }
        return super.isSelectPreCalendar(calendar, calendarIndex)
    }

    override fun isSelectNextCalendar(calendar: Calendar, calendarIndex: Int): Boolean {
        if (isInEditMode) {
            return calendar.day in 1 until 10
        }
        return super.isSelectNextCalendar(calendar, calendarIndex)
    }

    override fun isCalendarSelected(calendar: Calendar): Boolean {
        if (isInEditMode) {
            return calendar.day <= 10
        }
        return super.isCalendarSelected(calendar)
    }

    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelectedPre: Boolean,
        isSelectedNext: Boolean
    ): Boolean {
        drawSelectedBackground(
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
        return true
    }

    override fun onDrawScheme(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        isSelected: Boolean
    ) {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, mSchemePaint)
    }

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

        val isInRange = CalendarUtil.isCalendarInRange(calendar, mDelegate)

        val textPaint = when {
            //被选中的日期
            isSelected -> mSelectTextPaint
            //今天的日期
            calendar.isCurrentDay -> mCurDayTextPaint
            //当前月的日期
            calendar.isCurrentMonth -> mCurMonthTextPaint.apply {
                color = if (isInRange) {
                    mDelegate.mCurrentMonthTextColor
                } else {
                    mDelegate.outRangeTextColor
                }
            }
            //其他月的日期
            else -> mOtherMonthTextPaint
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