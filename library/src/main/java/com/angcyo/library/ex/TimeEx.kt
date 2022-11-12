package com.angcyo.library.ex

import com.angcyo.library.L
import com.angcyo.library.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToLong

/**
 * https://github.com/JodaOrg/joda-time
 * Created by angcyo on ：2018/04/12 13:36
 * 修改备注：
 * Version: 1.0.0
 */

/**一天的毫秒数 72,000 72,000,000*/
const val DAY_MILLIS = 24 * 60 * 60 * 1000L

/**一周的毫秒数*/
const val WEEK_MILLIS = 7 * DAY_MILLIS

/**多少天对应的毫秒数*/
fun day(count: Int = 1): Long = count * DAY_MILLIS

fun nowTime() = System.currentTimeMillis()

/**返回毫秒对应多少天数*/
fun Long.toDay(): Int {
    return ceil((this * 1.0 / DAY_MILLIS)).toInt()
}

/**返回毫秒对应多少年数*/
fun Long.toYear(): Int {
    return ceil(toDay() * 1.0 / 365).toInt()
}

/**当前时间和现在时间对比, 还剩多少天*/
fun Long.toNowDay(): Int {
    return (this - nowTime()).toDay()
}

/**返回毫秒对应的天数*/
fun Int.toDay(): Int {
    return this.toLong().toDay()
}

fun Long.toFullDate(): String {
    return this.fullTime()
}

/**时间全格式输出*/
fun Long.fullTime(pattern: String = "yyyy-MM-dd HH:mm:ss.SSS"): String {
    return toTime(pattern)
}

fun nowTimeString(pattern: String = "yyyy-MM-dd HH:mm:ss.SSS"): String {
    return nowTime().fullTime(pattern)
}

/**格式化时间输出*/
fun Long.toTime(pattern: String = "yyyy-MM-dd HH:mm"): String {
    val format: SimpleDateFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
    format.applyPattern(pattern)
    return format.format(Date(this))
}

fun Date.toTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val format: SimpleDateFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
    format.applyPattern(pattern)
    return format.format(this)
}

/**将字符串换算成毫秒*/
fun String.toMillis(pattern: String = "yyyyMMdd"): Long {
    val format: SimpleDateFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
    format.applyPattern(pattern)
    var time = 0L
    try {
        time = format.parse(this)?.time ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return time
}

/**毫秒*/
fun Calendar.toTime(): Long {
    return timeInMillis
}

fun Long.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    return cal
}

fun Long.toDate(): Date {
    return Date(this)
}

fun Calendar.toDate(): Date {
    return time
}

fun Date.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal
}

/**从13位时间戳中,获取当前时间对应的 y m d h s*/
fun Long.spiltTime(): IntArray {
    val cal = toCalendar()

    val year = cal[Calendar.YEAR] //2018
    val month = cal[Calendar.MONTH] + 1 //1-12月
    val day = cal[Calendar.DAY_OF_MONTH] //1-31天
    val h = cal[Calendar.HOUR_OF_DAY] //24小时制
    val m = cal[Calendar.MINUTE] //0-59分
    val s = cal[Calendar.SECOND] //0-59秒
    val sss = cal[Calendar.MILLISECOND] //0-999毫秒

    //Gets what the first day of the week is; e.g., SUNDAY in the U.S., MONDAY in France.
    //Gets what the first day of the week is; e.g., SUNDAY in the U.S., MONDAY in France.
    val dayOfWeek = cal.firstDayOfWeek
    val weekDay = cal[Calendar.DAY_OF_WEEK] //1-7 周几

    var week = weekDay
    if (dayOfWeek == Calendar.SUNDAY) {
        week = weekDay - 1
        if (week <= 0) {
            week = 7
        }
    }

    //SUNDAY 1
    //MONDAY = 2
    //TUESDAY = 3
    //SATURDAY = 7 星期六
    return intArrayOf(
        year /*0*/,
        month /*1*/,
        day /*2*/,
        h /*3*/,
        m /*4*/,
        s /*5*/,
        sss /*6*/,
        week /*7*/
    )
}

fun Long.year() = spiltTime()[0]
fun Long.month() = spiltTime()[1]
fun Long.day() = spiltTime()[2]
fun Long.hour() = spiltTime()[3]
fun Long.minute() = spiltTime()[4]
fun Long.second() = spiltTime()[5]
fun Long.millisecond() = spiltTime()[6]
fun Long.week() = spiltTime()[7]

fun Calendar.year() = this[Calendar.YEAR]//2018
fun Calendar.month() = this[Calendar.MONTH] + 1//1-12月
fun Calendar.day() = this[Calendar.DAY_OF_MONTH]//1-31天
fun Calendar.hour() = this[Calendar.HOUR_OF_DAY]//24小时制 0-23
fun Calendar.minute() = this[Calendar.MINUTE]//0-59分
fun Calendar.second() = this[Calendar.SECOND]//0-59秒
fun Calendar.millisecond() = this[Calendar.MILLISECOND]//0-999毫秒
fun Calendar.week(): Int {//1-7 周几
    val dayOfWeek = firstDayOfWeek
    val weekDay = this[Calendar.DAY_OF_WEEK] //1-7 周几

    var week = weekDay
    if (dayOfWeek == Calendar.SUNDAY) {
        week = weekDay - 1
        if (week <= 0) {
            week = 7
        }
    }
    return week
}

/**创建一个日历对象*/
fun calendarStart(
    year: Int = nowTime().year(), month: Int = 0, dayOfMonth: Int = 1,
    hourOfDay: Int = 0, minute: Int = 0, second: Int = 0
): Calendar {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, second)
    return cal
}

fun calendarEnd(
    year: Int = nowTime().year(), month: Int = 11, dayOfMonth: Int = 31,
    hourOfDay: Int = 23, minute: Int = 59, second: Int = 59
): Calendar {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, second)
    return cal
}

fun String.parseTime(pattern: String = "yyyy-MM-dd"): Long {
    val format: SimpleDateFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
    format.applyPattern(pattern)
    return try {
        format.parse(this)?.time ?: -1
    } catch (e: ParseException) {
        e.printStackTrace()
        -1
    }
}

/**将毫秒, 拆成 d h m s sss数组*/
fun Long.toTimes(): LongArray {

    if (this <= 0) {
        //
        return longArrayOf(0, 0, 0, 0, 0)
    }

    //剩余多少毫秒
    val ms = this % 1000

    //共多少秒
    val mill = this / 1000

    //共多少分
    val min = mill / 60

    //共多少小时
    val hour = min / 60

    //共多少天
    val day = hour / 24

    val h = hour % 24
    val m = min % 60
    val s = mill % 60

    return longArrayOf(ms, s, m, h, day)
}

/**转成毫秒*/
fun Int.toMillisecond() = this * 1000L

/**
 * 将毫秒转成 x天x时x分x秒x毫秒.
 *
 *<pre>
 *  11:00:00
 *  toElapsedTime(pattern = intArrayOf(-1, 1, 1), units = arrayOf("", "", ":", ":", ":") )
 *</pre>
 *
 * [-1] 强制不需要
 * [1] 强制需要
 * [其他值] 智能判断
 * pattern :0智能判断 1强制 -1忽略
 *
 * @param pattern 默认为智能判断值<=0时, 不返回. 如果需要强制返回, 设置1, 强制不返回设置-1
 * @param refill 24小时制, 前面补齐0
 * */
fun Long.toElapsedTime(
    pattern: IntArray = intArrayOf(-1, 1, 1),
    refill: BooleanArray = booleanArrayOf(true, true, true, true, true),
    units: Array<String> = arrayOf(
        _string(R.string.lib_time_ms),
        _string(R.string.lib_time_s),
        _string(R.string.lib_time_m),
        _string(R.string.lib_time_h),
        _string(R.string.lib_time_d)
    ) /*最大5个计量*/
): String {

    fun toH24(h24: Boolean, value: Long): String {
        return if (!h24 || value >= 10) "$value" else "0${value}"
    }

    val builder = StringBuilder()

    if (this <= 0) {
        for (i in pattern.indices) {
            val need = pattern.getOrNull(i) ?: 0
            val h24 = refill.getOrNull(i) ?: false
            val unit = units.getOrNull(i) ?: ":"

            if (need == 1) {
                builder.append(toH24(h24, 0))
                if (unit.isNotBlank()) {
                    builder.append(unit)
                }
                return builder.toString()
            }
        }
        for (i in pattern.indices) {
            val need = pattern.getOrNull(i) ?: 0
            val h24 = refill.getOrNull(i) ?: false
            val unit = units.getOrNull(i) ?: ":"

            if (need != -1) {
                builder.append(toH24(h24, 0))
                if (unit.isNotBlank()) {
                    builder.append(unit)
                }
                return builder.toString()
            }
        }
        return "$this"
    }

    val times = toTimes()

    for (i in times.lastIndex downTo 0) {
        val value = times[i]
        val h24 = refill.getOrNull(i) ?: false
        val unit = units.getOrNull(i) ?: ":"
        val need = pattern.getOrNull(i) ?: 0

        when {
            need == -1 -> {
                //强制不要
            }
            value > 0 || need == 1 -> {
                //智能 or 强制要
                builder.append(toH24(h24, value))
                if (unit.isNotBlank()) {
                    builder.append(unit)
                }
            }
        }
    }

    return builder.toString()
}

/**分:秒 的时间格式, 分秒时间格式输出*/
fun Long?.toMinuteTime(
    pattern: IntArray = intArrayOf(-1, 1, 1),
    units: Array<String> = arrayOf("", "", ":", ":", ":")
) = this?.toElapsedTime(
    pattern = pattern,
    units = units
)

/**分:秒'毫秒 的时间格式, 分秒时间格式输出*/
fun Long?.toMsTime(
    pattern: IntArray = intArrayOf(0, 1, 1),
    units: Array<String> = arrayOf("", "'", ":", ":", ":")
) = this?.toElapsedTime(
    pattern = pattern,
    units = units
)

/**计算2个时间之间相差多少毫秒
 * [2020-6-6 02:20] [2020-7-7 02:20]
 * */
fun timeDifference(start: String, end: String, pattern: String = "yyyy-MM-dd HH:mm"): Long {
    val s = start.toMillis(pattern)
    val e = end.toMillis(pattern)

    return if (s == 0L || e == 0L) {
        L.w("解析失败")
        0L
    } else {
        e - s
    }
}

/**两个时间相差多少毫秒*/
fun String.diffTime(end: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): Long {
    return timeDifference(this, end, pattern)
}

/**耗时围绕*/
fun wrapDuration(action: () -> Unit): String {
    val startTime = nowTime()
    action()
    val nowTime = nowTime()
    return (nowTime - startTime).toElapsedTime(intArrayOf(1, 1, 1))
}

/**毫秒转成缩短的时间
 * 缩短时间显示*/
fun Long.shotTimeString(
    showToday: Boolean = false, //显示今天字符串
    abbreviate: Boolean = true, //缩短显示
    datePattern: String = "yyyy-MM-dd",
    timePattern: String = "HH:mm",
): String {
    val dataString: String
    val timeStringBy24: String
    val currentTime = Date(this)
    val today = Date()
    val todayStart = Calendar.getInstance()
    todayStart[Calendar.HOUR_OF_DAY] = 0
    todayStart[Calendar.MINUTE] = 0
    todayStart[Calendar.SECOND] = 0
    todayStart[Calendar.MILLISECOND] = 0
    val todayBegin = todayStart.time //今天开始的日期
    val yesterdayBegin = Date(todayBegin.time - 3600 * 24 * 1000) //昨天开始的日期
    val preYesterday = Date(yesterdayBegin.time - 3600 * 24 * 1000) //前天开始的日期
    dataString = if (!currentTime.before(todayBegin)) {
        if (showToday) {
            _string(R.string.lib_time_today)
        } else {
            ""
        }
    } else if (!currentTime.before(yesterdayBegin)) {
        _string(R.string.lib_time_yesterday)
    } else if (!currentTime.before(preYesterday)) {
        _string(R.string.lib_time_before_yesterday)
    } else if (isSameWeekDates(currentTime, today)) {
        getWeekOfDate(currentTime)
    } else {
        val dateFormatter = SimpleDateFormat(datePattern, Locale.getDefault())
        dateFormatter.format(currentTime)
    }
    val timeFormatter24 = SimpleDateFormat(timePattern, Locale.getDefault())
    timeStringBy24 = timeFormatter24.format(currentTime)
    return if (abbreviate) {
        if (!currentTime.before(todayBegin)) {
            getTodayTimeBucket(currentTime)
        } else {
            dataString
        }
    } else {
        "$dataString $timeStringBy24"
    }
}

/** 返回返回聊天界面的时间展示
 * 毫秒*/
fun Long.chatTimeString(): String {
    val calendar = Calendar.getInstance()
    val currentDayIndex = calendar[Calendar.DAY_OF_YEAR]
    val currentYear = calendar[Calendar.YEAR]
    calendar.time = Date(this)
    val msgYear = calendar[Calendar.YEAR]
    val msgDayIndex = calendar[Calendar.DAY_OF_YEAR]
    val msgMinute = calendar[Calendar.MINUTE]
    var msgTimeStr = calendar[Calendar.HOUR_OF_DAY].toString() + ":"
    msgTimeStr = if (msgMinute < 10) {
        msgTimeStr + "0" + msgMinute
    } else {
        msgTimeStr + msgMinute
    }
    //val msgDayInWeek = calendar[Calendar.DAY_OF_WEEK]
    msgTimeStr = if (currentDayIndex == msgDayIndex) {
        msgTimeStr
    } else {
        if (currentDayIndex - msgDayIndex == 1 && currentYear == msgYear) {
            "昨天$msgTimeStr"
        } else if (false /*currentDayIndex - msgDayIndex > 1 && currentYear == msgYear*/) { //本年消息,注释掉统一按照 "年/月/日" 格式显示
            //不同周显示具体月，日，注意函数：calendar.get(Calendar.MONTH) 一月对应0，十二月对应11
            "${calendar[Calendar.MONTH] + 1}/${calendar[Calendar.DAY_OF_MONTH]} $msgTimeStr"
            //msgTimeStr = (Integer.valueOf(calendar.get(Calendar.MONTH) + 1)) + context.getString(R.string.date_month_short) + " "+ calendar.get(Calendar.DAY_OF_MONTH) + context.getString(R.string.date_day_short) + " " + msgTimeStr + " ";
        } else { // 1、非正常时间，如currentYear < msgYear，或者currentDayIndex < msgDayIndex
            //2、非本年消息（currentYear > msgYear），如：历史消息是2018，今年是2019，显示年、月、日
            "$msgYear/${calendar[Calendar.MONTH] + 1}/${calendar[Calendar.DAY_OF_MONTH]} $msgTimeStr"
            //msgTimeStr = msgYear + context.getString(R.string.date_year_short) + (Integer.valueOf(calendar.get(Calendar.MONTH) + 1)) + context.getString(R.string.date_month_short) + calendar.get(Calendar.DAY_OF_MONTH) + context.getString(R.string.date_day_short) + msgTimeStr + " ";
        }
    }
    return msgTimeStr
}

/**
 * 根据日期获得星期
 *
 * @param date
 * @return
 */
fun getWeekOfDate(date: Date): String {
    val weekDaysName = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    // String[] weekDaysCode = { "0", "1", "2", "3", "4", "5", "6" };
    val calendar = Calendar.getInstance()
    calendar.time = date
    val intWeek = calendar[Calendar.DAY_OF_WEEK] - 1
    return weekDaysName[intWeek]
}

/**
 * 判断两个日期是否在同一周
 *
 * @param date1
 * @param date2
 * @return
 */
fun isSameWeekDates(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = date1
    cal2.time = date2
    val subYear = cal1[Calendar.YEAR] - cal2[Calendar.YEAR]
    if (0 == subYear) {
        if (cal1[Calendar.WEEK_OF_YEAR] == cal2[Calendar.WEEK_OF_YEAR]) return true
    } else if (1 == subYear && 11 == cal2[Calendar.MONTH]) {
        // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
        if (cal1[Calendar.WEEK_OF_YEAR] == cal2[Calendar.WEEK_OF_YEAR]) return true
    } else if (-1 == subYear && 11 == cal1[Calendar.MONTH]) {
        if (cal1[Calendar.WEEK_OF_YEAR] == cal2[Calendar.WEEK_OF_YEAR]) return true
    }
    return false
}

/**
 * 根据不同时间段，显示不同时间
 *
 * @param date
 * @return
 */
fun getTodayTimeBucket(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val timeFormatter0to11 = SimpleDateFormat("KK:mm", Locale.getDefault())
    val timeFormatter1to12 = SimpleDateFormat("hh:mm", Locale.getDefault())
    return when (calendar[Calendar.HOUR_OF_DAY]) {
        in 0..4 -> "凌晨 " + timeFormatter0to11.format(date)
        in 5..11 -> "上午 " + timeFormatter0to11.format(date)
        in 12..17 -> "下午 " + timeFormatter1to12.format(date)
        in 18..23 -> "晚上 " + timeFormatter1to12.format(date)
        else -> ""
    }
}

/**在日期上进行 加/减
 * [amount] 数量, -5表示减5天
 * */
fun Calendar.addDay(amount: Int) {
    add(Calendar.DAY_OF_MONTH, amount)
}

/**返回2个日期的间隔天数
 * [other] 小值*/
fun Calendar.distance(other: Calendar): Long {
    val t1 = timeInMillis
    val t2 = other.timeInMillis
    return ((t1 - t2) * 1f / DAY_MILLIS).ceil().roundToLong()
}