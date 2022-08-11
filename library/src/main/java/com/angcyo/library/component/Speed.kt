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

    /**总共接收的字节数*/
    var total: Long = 0

    /**目标的总大小*/
    var targetTotal: Long = 0

    /**速度计算阈值, 每多久计算一次*/
    var threshold: Long = 1_000

    /**进度 0~100*/
    var progress: Int = 0

    /**[count] 当前读取字节数量
     * [sum] 总共的字节数量
     * */
    fun update(count: Long, sum: Long = -1): Boolean {
        val nowTime = System.currentTimeMillis()
        var result = false
        _speedSum += count
        total += count
        targetTotal = sum

        progress = (total * 100 / sum).toInt()

        if (startTime <= 0) {
            startTime = nowTime
        }
        if (sum in 0..total) {
            //结束
            endTime = nowTime
        }

        if (_tickTime <= 0) {
            _tickTime = nowTime
            speed = count
            //result = true
        } else if ((nowTime - _tickTime) >= threshold) {
            //计算速度
            speed = (_speedSum / (nowTime - _tickTime)) * 1000
            _tickTime = nowTime
            _speedSum = 0
            result = true
        }
        return result
    }

    /**重置计算*/
    fun reset() {
        speed = -1
        total = 0
        progress = 0
        targetTotal = 0
        _tickTime = -1
        startTime = -1
        endTime = -1
        _speedSum = 0
    }

    var _speedSum: Long = 0

    var _tickTime: Long = -1

    /**开始的时间, 用来计算总耗时*/
    var startTime: Long = -1
    var endTime: Long = -1

    /**耗时, 毫秒*/
    fun duration(): Long {
        if (startTime <= 0) {
            return 0
        }
        if (endTime > 0) {
            return endTime - startTime
        }
        return System.currentTimeMillis() - startTime
    }
}