package com.angcyo.acc2.parse

import com.angcyo.library.ex.patternList
import com.angcyo.library.ex.size
import kotlin.math.min

/**
 * 简单的字符串表达式解析
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ExpParse {

    val expValueList = mutableListOf<ExpValue>()

    /**约等于的范围*/
    var aboutRatio = 10f

    /**转换数值*/
    var wrapValue: (Float) -> Float = { it }

    /**
     * [exp] 解析表达式, 数值要在符号的后面
     * [>-10]
     * [>=100]
     * [<=10]
     * [=10]
     * [≈10]
     * [%3=1]
     * [%3>=2]
     * */
    fun parse(exp: String, op: String) {
        //获取表达式
        val opPattern = "([$op]+)".toPattern()
        //获取数字
        val valuePattern = "([-]?[\\d.]*\\d+)".toPattern()

        val opList = exp.patternList(opPattern)
        val valueList = exp.patternList(valuePattern)

        expValueList.clear()
        for (i in 0 until min(opList.size(), valueList.size())) {
            expValueList.add(ExpValue(opList[i], valueList[i]))
        }
    }

    /**输入一个值, 判断是否满足上述的表达式*/
    fun compute(inputValue: Float): Boolean {
        if (expValueList.isEmpty()) {
            return false
        }
        val first = expValueList.first()
        return try {
            when (first.exp) {
                "%" -> {
                    if (expValueList.size() >= 2) {
                        val returnValue =
                            inputValue.toLong() % wrapValue(first.value?.toFloatOrNull() ?: 1f)
                        val get = expValueList[1]
                        compute(get.exp, returnValue, get.value?.toFloatOrNull() ?: 0f)
                    } else {
                        false
                    }
                }
                else -> compute(
                    first.exp,
                    inputValue,
                    wrapValue(first.value?.toFloatOrNull() ?: 0f)
                )
            }
        } catch (e: Exception) {
            false
        }
    }

    fun compute(
        op: String?,
        inputValue: Float,
        value: Float,
        aboutRef: Float = aboutRatio
    ): Boolean {
        return when (op) {
            ">=" -> inputValue >= value
            "<=" -> inputValue <= value
            ">" -> inputValue > value
            "<" -> inputValue < value
            "=" -> inputValue == value
            "≈" -> inputValue in (value - aboutRef)..(value + aboutRef)
            else -> inputValue == value
        }
    }

    data class ExpValue(
        var exp: String?,
        var value: String?
    )
}