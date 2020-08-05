package com.angcyo.core.component.accessibility.action

import com.angcyo.library.ex.nowTime

/**
 * 计数统计
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/18
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ActionCount {

    /**计数器, 1就是 执行了1次*/
    var count: Long = -1

    /**开始计数时的时间, 毫秒*/
    var startTime: Long = -1

    /**最大计数限制*/
    var maxCountLimit: Long = -1

    var limitOut: (() -> Unit)? = null

    /**统计计数*/
    fun doCount() {
        if (count == -1L) {
            startTime = nowTime()

            count = 0
        }

        if (isMaxLimit()) {
            limitOut?.invoke()
        }
        count++
    }

    /**是否超出最大限制的值*/
    fun isMaxLimit() = maxCountLimit == 0L ||
            maxCountLimit in 1 until count

    /**清空计数*/
    fun clear() {
        count = -1
        limitOut = null
    }
}