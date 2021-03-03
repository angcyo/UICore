package com.angcyo.acc2.parse

import com.angcyo.acc2.action.Action
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.patternList
import com.angcyo.library.ex.size

/**
 * 简单的字符串表达式解析
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ExpParse {

    companion object {
        /**
         * 从表达式xx:>=200 或者 xx:%5>=2中, 获取>=200 %5>=2
         * */
        fun getValue(exp: String, key: String): List<String> {
            val valuePattern = "(?<=$key)(\\S+)".toPattern()
            return exp.patternList(valuePattern)
        }
    }

    val expValueList = mutableListOf<ExpValue>()

    /**约等于的范围*/
    var aboutRatio = 10f

    /**小于1f 大于0f的小数, 需要放大的倍数
     * [0.000001-0.999999]*/
    var ratioRef = 1f

    /**转换数值*/
    var wrapValue: (ExpValue) -> Float = {
        var value = it.value?.toFloatOrNull() ?: 0f
        value = if (value in 0.000001..0.999999 || it.unit == "r" /*强制使用比例计算*/) {
            value * ratioRef
        } else {
            value
        }
        when (it.unit) {
            "dp" -> value * dp
            else -> value
        }
    }

    /**
     * [exp] 解析表达式, 数值要在符号的后面
     * [>-10]
     * [>=100]
     * [<=10]
     * [=10]
     * [≈10]
     * [%3=1]
     * [%3>=2]
     * [10dp]
     * [-10.0]
     * */
    fun parse(exp: String?, op: String = Action.OP): List<ExpValue> {
        expValueList.clear()
        if (exp.isNullOrEmpty()) {
            return expValueList
        }

        //获取表达式
        val opPattern = "([$op]+)".toPattern() //<3

        //获取数值和单位
        val valueUnitPattern = "([-]?[\\d.]*\\d+[a-zA-Z]*)".toPattern() //3px

        //获取数值
        val valuePattern = "([-]?[\\d.]*\\d+)".toPattern() //3

        val opList = exp.patternList(opPattern)
        val valueUnitList = exp.patternList(valueUnitPattern)
        val valueList = mutableListOf<String>()
        val unitList = mutableListOf<String?>()

        valueUnitList.forEach { valueUnit ->
            valueUnit.patternList(valuePattern).firstOrNull()?.let { value ->
                valueList.add(value)
                unitList.add(valueUnit.replace(value, ""))
            }
        }

        expValueList.clear()
        for (i in 0 until valueList.size()) {
            expValueList.add(ExpValue(opList.getOrNull(i), valueList[i], unitList.getOrNull(i)))
        }

        return expValueList
    }

    /**判断输入值[inputValue]是否符合表达式
     * [exp] 比较的表达式 [>=100]
     * @return true 计算通过
     * */
    fun parseAndCompute(exp: String?, op: String = Action.OP, inputValue: Float): Boolean {
        parse(exp, op)
        return compute(inputValue)
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
                        val returnValue = inputValue.toLong() % wrapValue(first)
                        val get = expValueList[1]
                        compute(get.exp, returnValue, get.value?.toFloatOrNull() ?: 0f)
                    } else {
                        false
                    }
                }
                else -> compute(first.exp, inputValue, wrapValue(first))
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
            null -> value > 0f
            else -> inputValue == value
        }
    }

    data class ExpValue(
        /**操作符*/
        var exp: String?,
        /**比值*/
        var value: String?,
        /**值的单位*/
        var unit: String?
    )
}