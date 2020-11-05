package com.angcyo.core.component.accessibility.action

import com.angcyo.library.utils.getFloatNum

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/05
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

object MathParser {

    /**验证[value]是否满足数学表达式
     * [>=2] 数量大于等于2
     * [>3] 数量大于3
     * [2] 数量等于2
     * [=2] 数量等于2
     * [<3] 数量小于3
     * 空字符 表示直接过
     * null 忽略此条件
     * [expression] 字符串数值表达式, 比如: >=2
     * [value] 需要比较的数值,比如: 3
     * [ref] 当[value]是小数时, 需要乘以的基数
     * [size] 当[value]是负数时, 需要加上的基数
     * */
    fun verifyValue(value: Float, expression: String?, ref: Float = 1f, size: Float = 0f): Boolean {
        var result = false
        if (expression != null) {
            if (expression.isEmpty()) {
                result = true
            } else {
                val num = expression.getFloatNum()
                num?.also {
                    //如果是小数, 则按照比例计算
                    val fixNum = when {
                        it < 0 -> it + size
                        it < 1 -> it * ref
                        else -> it
                    }
                    if (expression.startsWith(">=")) {
                        if (value >= fixNum) {
                            result = true
                        }
                    } else if (expression.startsWith(">")) {
                        if (value > fixNum) {
                            result = true
                        }
                    } else if (expression.startsWith("<=")) {
                        if (value <= fixNum) {
                            result = true
                        }
                    } else if (expression.startsWith("<")) {
                        if (value < fixNum) {
                            result = true
                        }
                    } else {
                        if (value == fixNum) {
                            result = true
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * [expression] 支持范围 ~ 分割
     * */
    fun verifyRangeValue(
        value: Float,
        expression: String?,
        ref: Float = 1f,
        size: Float = 0f
    ): Boolean {
        return if (expression?.contains("~") == true) {
            val list = expression.split("~")
            val start = list.getOrNull(0)
            val end = list.getOrNull(1)
            verifyValue(value, start, ref, size) && verifyValue(value, end, ref, size)
        } else {
            verifyValue(value, expression, ref, size)
        }
    }
}