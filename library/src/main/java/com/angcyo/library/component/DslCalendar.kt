package com.angcyo.library.component

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/27
 */
class DslCalendar(time: Long = System.currentTimeMillis()) {
    val calendar = Calendar.getInstance()

    //<editor-fold desc="动态计算的获取属性">

    val year: Int get() = calendar[Calendar.YEAR] //2018
    val month: Int get() = calendar[Calendar.MONTH] + 1 //1-12月
    val day: Int get() = calendar[Calendar.DAY_OF_MONTH] //1-31天
    val week: Int get() = calendar[Calendar.WEEK_OF_YEAR] //一年中的第几周
    val weekDay: Int //1-7 周几 [周一-周七]
        get() {
            val dayOfWeek = calendar.firstDayOfWeek //一周中的第一天是
            val weekDay = calendar[Calendar.DAY_OF_WEEK] //1-7 周几
            var result = weekDay
            if (dayOfWeek == Calendar.SUNDAY/*周日*/) {
                result = weekDay - 1
                if (result <= 0) {
                    result = 7
                }
            }
            return result
        }

    val hour: Int get() = calendar[Calendar.HOUR_OF_DAY] //24小时制
    val minute: Int get() = calendar[Calendar.MINUTE] //0-59分
    val second: Int get() = calendar[Calendar.SECOND] //0-59秒
    val millisecond: Int get() = calendar[Calendar.MILLISECOND] //0-999毫秒

    //</editor-fold desc="动态计算的获取属性">

    //<editor-fold desc="属性计算">

    //</editor-fold desc="属性计算">

    init {
        setTime(time)
    }

    fun setTime(date: Date) {
        calendar.time = date
    }

    /**13位毫秒数*/
    fun setTime(time: Long) {
        if (time.toString().length == 10) {
            calendar.timeInMillis = time * 1000
        } else {
            calendar.timeInMillis = time
        }
    }

    /**设置字段值*/
    fun set(field: Int = Calendar.YEAR, value: Int) {
        calendar.set(field, value)
    }

    /**是否是今天*/
    fun isToday(today: DslCalendar = DslCalendar()): Boolean {
        return isThisMonth(today) && day == today.day
    }

    /**是否是这一月*/
    fun isThisMonth(today: DslCalendar = DslCalendar()): Boolean {
        return year == today.year && month == today.month
    }

    /**是否是这一周*/
    fun isThisWeek(today: DslCalendar = DslCalendar()): Boolean {
        return isThisMonth(today) && week == today.week
    }
}

fun nowCalendar(time: Long = System.currentTimeMillis()): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = if (time.toString().length == 10) {
            time * 1000
        } else {
            time
        }
    }
}

fun String.toTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): Long {
    val format: SimpleDateFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
    format.applyPattern(pattern)
    return try {
        format.parse(this)?.time ?: -1
    } catch (e: ParseException) {
        if (pattern.contains(" ")) {
            toTime(pattern.split(" ")[0])
        } else {
            e.printStackTrace()
            -1
        }
    }
}

fun String.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date {
    return Date(toTime(pattern))
}

fun String.toCalendar(pattern: String = "yyyy-MM-dd HH:mm:ss"): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = toTime(pattern)
    }
}