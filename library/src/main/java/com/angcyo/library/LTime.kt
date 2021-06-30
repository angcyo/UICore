package com.angcyo.library

import com.angcyo.library.ex.nowTime
import java.util.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/29
 */
object LTime {
    private val stack = Stack<Long>()

    /**记录时间*/
    fun tick(): Long {
        val nowTime = nowTime()
        if (L.debug) {
            stack.push(nowTime)
        }
        return nowTime
    }

    /**获取与最近一次时间匹配的时间间隔(ms)*/
    fun time(): String {
        if (!L.debug) {
            return "not debug!"
        }
        val startTime = if (stack.isEmpty()) nowTime() else stack.pop()
        val nowTime = nowTime()
        return time(startTime, nowTime)
    }

    fun time(startTime: Long, endTIme: Long = nowTime()): String {
        val s = (endTIme - startTime) / 1000
        //val ms = ((endTIme - startTime) % 1000) * 1f / 1000
        val ms = (endTIme - startTime) % 1000

        //val m = s / 60
        //val h = m / 24

        return "${s}s${ms}ms"
        //return "${String.format("%.3f", s + ms)}s"
    }

    //-------------------------------

    var _startTime: Long? = null

    var _count: Long = 0

    /**1秒之内, 调用的次数大于[threshold]时, 才输出日志*/
    fun dump(threshold: Int = 60) {
        _count++
        val nowTime = nowTime()
        if (_startTime == null) {
            _startTime = nowTime
        } else {
            val startTime = _startTime ?: 0
            if (nowTime - startTime > 2_000) {
                //间隔很长, 清空计数
                _startTime = nowTime
                _count = 0
                _count++
            } else if (nowTime - startTime >= 1_000) {
                //1秒
                if (_count >= threshold) {
                    L.w("dump...$_count")

                    //清空计数
                    _startTime = nowTime
                    _count = 0
                    _count++
                }
            }
        }
    }
}