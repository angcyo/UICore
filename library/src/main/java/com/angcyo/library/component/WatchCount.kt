package com.angcyo.library.component

import com.angcyo.library.ex.nowTime

/**
 * 观察次数
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/12/08
 */
object WatchCount {
    /**记录次数
     * [key] hashCode
     * [value] "time,count"
     * */
    val countMap = hashMapOf<Int, String>()
}

/**观察短时间之内, 点击指定的次数
 * [count] 需要观察的次数
 * [duration] 需要观察的时间
 * */
fun Any.watchCount(count: Int, duration: Long = 800, action: () -> Unit) {
    val key = hashCode()
    val value = WatchCount.countMap[key]

    var beforeCount = 0L
    var beforeTime = 0L
    if (value.isNullOrBlank()) {
        beforeTime = nowTime()
    } else {
        value.split(",").let {
            beforeTime = it.first().toLong()
            beforeCount = it.last().toLong()
        }
    }

    //
    val nowTime = System.currentTimeMillis()
    if (nowTime - beforeTime < duration) {
        beforeCount++
    } else {
        beforeCount = 1
    }
    beforeTime = nowTime
    if (beforeCount >= count) {
        beforeCount = 1
        action()
    }

    //save
    WatchCount.countMap[key] = "${beforeTime},${beforeCount}"
}