package com.angcyo.library.component

/**
 * 用来计算每秒的发送速度
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/10
 */
class Speed {

    /**实时计算的当前速度
     * 负数表示未初始化*/
    var speed: Long = -1

    /**速度计算阈值, 每多久计算一次*/
    var threshold: Long = 1_000

    /**[count] 当前读取字节数量
     * [sum] 总共的字节数量
     * */
    fun update(count: Long) {
        val nowTime = System.currentTimeMillis()
        _speedSum += count
        if (_time1 <= 0) {
            _time1 = nowTime
            speed = count
        } else if (nowTime - _time1 >= threshold) {
            //计算速度
            speed = (_speedSum / (nowTime - _time1)) * 1000
            _time1 = nowTime
            _speedSum = 0
        }
    }

    /**重置计算*/
    fun reset() {
        speed = -1
        _time1 = -1
        _speedSum = 0
    }

    var _speedSum: Long = 0

    var _time1: Long = -1
}