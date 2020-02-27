package com.angcyo.library.ex

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by angcyo on ：2018/04/12 13:36
 * 修改备注：
 * Version: 1.0.0
 */

fun nowTime() = System.currentTimeMillis()

/**返回毫秒对应的天数*/
fun Long.toDay(): Int {
    return (this / (24 * 60 * 60 * 1000)).toInt()
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

/**获取当前时间对应的 y m d h s*/
public fun Long.spiltTime(): IntArray {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this

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

public fun String.parseTime(pattern: String = "yyyy-MM-dd"): Long {
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
public fun Long.toTimes(): LongArray {

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

/**
 * 将毫秒转成 x天x时x分x秒x毫秒.
 *
 *<pre>
 *  toElapsedTime( pattern = intArrayOf(-1, 1, 1), units = arrayOf("", "", ":", ":", ":") )
 *</pre>
 *
 * @param pattern 默认为智能判断值<=0时, 不返回. 如果需要强制返回, 设置1, 强制不返回设置-1
 * @param h24 24小时制
 * */
fun Long.toElapsedTime(
    pattern: IntArray = intArrayOf(),
    h24: BooleanArray = booleanArrayOf(true, true, true, true, true),
    units: Array<String> = arrayOf("毫秒", "秒", "分", "时", "天")
): String {
    val times = toTimes()
    val builder = StringBuilder()

    fun toH24(h24: Boolean, value: Long): String {
        return if (!h24 || value >= 10) "$value" else "0${value}"
    }

    for (i in times.lastIndex downTo 0) {
        val value = times[i]
        val h24 = h24.getOrNull(i) ?: false
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