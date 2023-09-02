package com.angcyo.library.component.parser

import java.util.Calendar

/**
 *
 * ```
 * [YYYYescape] YYYY-MM-DDTHH:mm:ss:SS Z[Z] d dd ZZ A a
 *
 * //YYYYescape 2023-09-02T10:58:27:26 +08:00Z 6 Sa +0800 AM am
 * //YYYYescape 2023-09-02T10:41:24:45 28800000Z 0 Sa 28800000 AM am
 * ```
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/09/02
 */
class DslDateTemplateParser : DslTemplateParser() {

    private val calendar = Calendar.getInstance()

    init {
        replaceTemplateAction = { template ->
            when (template) {
                "YYYY" -> calendar.get(Calendar.YEAR).toString()
                "YY" -> calendar.get(Calendar.YEAR).toString().substring(2)
                "M" -> calendar.get(Calendar.MONTH).plus(1).toString()
                "MM" -> calendar.get(Calendar.MONTH).plus(1).toString().padStart(2, '0')
                //月份英文缩写
                "MMM" -> calendar.get(Calendar.MONTH).run {
                    when (this) {
                        Calendar.JANUARY -> "Jan"
                        Calendar.FEBRUARY -> "Feb"
                        Calendar.MARCH -> "Mar"
                        Calendar.APRIL -> "Apr"
                        Calendar.MAY -> "May"
                        Calendar.JUNE -> "Jun"
                        Calendar.JULY -> "Jul"
                        Calendar.AUGUST -> "Aug"
                        Calendar.SEPTEMBER -> "Sep"
                        Calendar.OCTOBER -> "Oct"
                        Calendar.NOVEMBER -> "Nov"
                        Calendar.DECEMBER -> "Dec"
                        else -> this.toString()
                    }
                }
                //月份英文全称
                "MMMM" -> calendar.get(Calendar.MONTH).run {
                    when (this) {
                        Calendar.JANUARY -> "January"
                        Calendar.FEBRUARY -> "February"
                        Calendar.MARCH -> "March"
                        Calendar.APRIL -> "April"
                        Calendar.MAY -> "May"
                        Calendar.JUNE -> "June"
                        Calendar.JULY -> "July"
                        Calendar.AUGUST -> "August"
                        Calendar.SEPTEMBER -> "September"
                        Calendar.OCTOBER -> "October"
                        Calendar.NOVEMBER -> "November"
                        Calendar.DECEMBER -> "December"
                        else -> this.toString()

                    }
                }

                //日
                "D" -> calendar.get(Calendar.DAY_OF_MONTH).toString()
                "DD" -> calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                //一周中的一天，星期天是 0
                "d" -> calendar.get(Calendar.DAY_OF_WEEK).run { (this - 1).toString() }
                //最简写的星期几
                "dd" -> calendar.get(Calendar.DAY_OF_WEEK).run {
                    when (this) {
                        Calendar.SUNDAY -> "Su"
                        Calendar.MONDAY -> "Mo"
                        Calendar.TUESDAY -> "Tu"
                        Calendar.WEDNESDAY -> "We"
                        Calendar.THURSDAY -> "Th"
                        Calendar.FRIDAY -> "Fr"
                        Calendar.SATURDAY -> "Sa"
                        else -> this.toString()
                    }
                }
                //简写的星期几
                "ddd" -> calendar.get(Calendar.DAY_OF_WEEK).run {
                    when (this) {
                        Calendar.SUNDAY -> "Sun"
                        Calendar.MONDAY -> "Mon"
                        Calendar.TUESDAY -> "Tue"
                        Calendar.WEDNESDAY -> "Wed"
                        Calendar.THURSDAY -> "Thu"
                        Calendar.FRIDAY -> "Fri"
                        Calendar.SATURDAY -> "Sat"
                        else -> this.toString()
                    }
                }
                //星期几全称
                "dddd" -> calendar.get(Calendar.DAY_OF_WEEK).run {
                    when (this) {
                        Calendar.SUNDAY -> "Sunday"
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        Calendar.SATURDAY -> "Saturday"
                        else -> this.toString()
                    }
                }

                //24小时制
                "H" -> calendar.get(Calendar.HOUR_OF_DAY).toString()
                "HH" -> calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
                //12小时制
                "h" -> calendar.get(Calendar.HOUR).toString()
                "hh" -> calendar.get(Calendar.HOUR).toString().padStart(2, '0')
                "m" -> calendar.get(Calendar.MINUTE).toString()
                "mm" -> calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
                "s" -> calendar.get(Calendar.SECOND).toString()
                "ss" -> calendar.get(Calendar.SECOND).toString().padStart(2, '0')
                //1位毫秒
                "S" -> calendar.get(Calendar.MILLISECOND).toString().substring(0, 1)
                "SS" -> calendar.get(Calendar.MILLISECOND).toString().substring(0, 2)
                "SSS" -> calendar.get(Calendar.MILLISECOND).toString()
                //UTC 的偏移量，±HH:mm
                "Z" -> calendar.get(Calendar.ZONE_OFFSET).run {
                    val hour = this / (60 * 60 * 1000)
                    val minute = this % (60 * 60 * 1000) / (60 * 1000)
                    "${if (hour >= 0) "+" else "-"}${
                        hour.toString().padStart(2, '0')
                    }:${minute.toString().padStart(2, '0')}"
                } //返回 28800000
                //UTC 的偏移量，±HHmm
                "ZZ" -> calendar.get(Calendar.ZONE_OFFSET).run {
                    val hour = this / (60 * 60 * 1000)
                    val minute = this % (60 * 60 * 1000) / (60 * 1000)
                    "${if (hour >= 0) "+" else "-"}${
                        hour.toString().padStart(2, '0')
                    }${minute.toString().padStart(2, '0')}"
                }
                //上/下午，大写
                "A" -> calendar.get(Calendar.AM_PM).run {
                    when (this) {
                        0 -> "AM"
                        1 -> "PM"
                        else -> this.toString()
                    }
                }
                //上/下午，小写
                "a" -> calendar.get(Calendar.AM_PM).run {
                    when (this) {
                        0 -> "am"
                        1 -> "pm"
                        else -> this.toString()
                    }
                }

                else -> template
            }
        }
    }

}

/**解析时间模板*/
fun String.parseDateTemplate(): String {
    return DslDateTemplateParser().parse(this)
}