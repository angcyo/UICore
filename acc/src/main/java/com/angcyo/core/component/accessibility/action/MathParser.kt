package com.angcyo.core.component.accessibility.action

import android.graphics.Rect
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.dp
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
     * [≈2] 数量大约2±dp
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
                    val fixNum = parseValue(expression, ref, size) ?: it
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
                    } else if (expression.startsWith("≈")) {
                        val max = fixNum + dp
                        val min = fixNum - dp
                        if (value in min..max) {
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

    //临时存放
    var _tempNodeRect = Rect()

    /**验证目标矩形, 是否符合给定的矩形表达式
     * [expression] 矩形验证表达式 [0.0,0.95394737~1.0,1.0] [>0.0,>0.95394737~<1.0,<1.0]*/
    fun verifyRectValue(
        rect: Rect,
        expression: String?,
        maxWidth: Int,
        maxHeight: Int
    ): Boolean {
        var result = false
        if (expression.isNullOrEmpty()) {
            //空字符只要宽高大于0, 就命中
            result = !rect.isEmpty
        } else {
            //兼容 ~ 和 -
            if (expression.contains("~")) {
                expression.split("~")
            } else {
                expression.split("-")
            }.apply {
                if (haveExpression(expression)) {
                    val leftExp = getOrNull(0)?.arg(0, ",")
                    val topExp = getOrNull(0)?.arg(1, ",")

                    val rightExp = getOrNull(1)?.arg(0, ",")
                    val bottomExp = getOrNull(1)?.arg(1, ",")

                    if (!leftExp.isNullOrEmpty() && !topExp.isNullOrEmpty()) {
                        val leftNum = parseValue(leftExp, maxWidth.toFloat())
                        val topNum = parseValue(topExp, maxHeight.toFloat())

                        val rightNum = parseValue(rightExp, maxWidth.toFloat())
                        val bottomNum = parseValue(bottomExp, maxHeight.toFloat())

                        var leftResult = false
                        var topResult = false
                        var rightResult = false
                        var bottomResult = false

                        //check
                        fun verify(
                            exp: String,/*计算表达式*/
                            calcValue: Float?,/*需要计算的值*/
                            currentValue: Float,/*当前参考比较的值*/
                            min: Float,/*最小取值*/
                            max: Float/*最大取值*/
                        ): Boolean {
                            if (calcValue == null) {
                                return false
                            }
                            var pass = false
                            if (exp.startsWith(">=")) {
                                pass = currentValue >= calcValue
                            } else if (exp.startsWith(">")) {
                                pass = currentValue > calcValue
                            } else if (exp.startsWith("<=")) {
                                pass = currentValue <= calcValue
                            } else if (exp.startsWith("<")) {
                                pass = currentValue < calcValue
                            } else if (exp.startsWith("≈")) {
                                val refMin = currentValue - dp
                                val refMax = currentValue + dp
                                if (calcValue in refMin..refMax) {
                                    pass = true
                                }
                            } else if (exp.startsWith("=")) {
                                pass = calcValue == currentValue
                            } else {
                                pass = calcValue in min..max
                            }
                            return pass
                        }

                        val rLeft = rect.left.toFloat()
                        val rRight = rect.right.toFloat()
                        val rTop = rect.top.toFloat()
                        val rBottom = rect.bottom.toFloat()

                        //数据有效
                        if (rightExp.isNullOrEmpty() || bottomExp.isNullOrEmpty()) {
                            //只设置了一个点
                            rightResult = true
                            bottomResult = true

                            leftResult = verify(leftExp, leftNum, rLeft, rLeft, rRight)
                            topResult = verify(topExp, topNum, rTop, rTop, rBottom)
                        } else {
                            //矩形
                            leftResult = verify(
                                leftExp,
                                leftNum,
                                rLeft,
                                rLeft,
                                rRight
                            ) && verify("<=", rightNum, leftNum ?: Float.MIN_VALUE, rLeft, rRight)

                            topResult = verify(
                                topExp,
                                topNum,
                                rTop,
                                rTop,
                                rBottom
                            ) && verify("<=", bottomNum, topNum ?: Float.MIN_VALUE, rTop, rBottom)

                            rightResult = verify(
                                rightExp,
                                rightNum,
                                rRight,
                                rLeft,
                                rRight
                            ) && verify(">=", leftNum, rightNum ?: Float.MAX_VALUE, rLeft, rRight)

                            bottomResult = verify(
                                bottomExp,
                                bottomNum,
                                rBottom,
                                rTop,
                                rBottom
                            ) && verify(">=", topNum, bottomNum ?: Float.MAX_VALUE, rTop, rBottom)
                        }

                        result = leftResult && topResult && rightResult && bottomResult
                    }

                } else {
                    //没有表达式走默认验证规则
                    val p1 = getOrNull(0)?.toPointF(maxWidth, maxHeight)
                    val p2 = getOrNull(1)?.toPointF(maxWidth, maxHeight)

                    if (p1 != null) {
                        if (p2 == null) {
                            //只设置了单个点
                            if (rect.contains(p1.x.toInt(), p1.y.toInt())) {
                                result = true
                            }
                        } else {
                            _tempNodeRect.set(
                                p1.x.toInt(),
                                p1.y.toInt(),
                                p2.x.toInt(),
                                p2.y.toInt()
                            )
                            //设置了多个点, 那么只要2个矩形相交, 就算命中
                            if (rect.intersect(_tempNodeRect)) {
                                result = true
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * [expression] 支持 负数, 小数, 整数, dp数
     * */
    fun parseValue(expression: String?, ref: Float = 1f, size: Float = 0f): Float? {
        if (expression.isNullOrEmpty()) {
            return null
        }
        val num = expression.getFloatNum()
        val isDp = expression.contains("dp")
        return num?.run {
            //如果是小数, 则按照比例计算
            when {
                this < 0 -> this + size //负数情况
                this <= 1 -> this * ref //小数情况
                isDp -> this * dp //dp数
                else -> this //其他
            }
        }
    }

    /**从[nums]中, 获取更亲近于[ref]的数*/
    fun getIntimateNum(ref: Float, vararg nums: Float): Float {
        var result = ref

        if (nums.size == 1) {
            result = nums[0]
        } else if (nums.size > 1) {
            var min = (nums[0] - result).abs()
            nums.forEach {
                val m = (it - result).abs()
                if (m < min) {
                    min = m
                    result = it
                }
            }
        }
        return result
    }

    /**是否是表达式字符创*/
    fun haveExpression(str: String?): Boolean {
        if (str.isNullOrEmpty()) {
            return false
        }
        return str.contains(">") ||
                str.contains("<") ||
                str.contains("=") ||
                str.contains("≈")
    }

}