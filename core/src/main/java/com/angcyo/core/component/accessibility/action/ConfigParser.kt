package com.angcyo.core.component.accessibility.action

import android.os.Build
import com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.isNumber
import com.angcyo.library.getAppVersionCode
import com.angcyo.library.utils.CpuUtils
import com.angcyo.library.utils.Device

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/05
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

object ConfigParser {

    fun verifySystem(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return true
        }

        fun _verify(v: String, def: Boolean = false): Boolean {
            val all = v.split(";")

            var result = true
            for (con in all) {
                if (con.isNotEmpty()) {
                    result = when {
                        con.startsWith("api") -> {
                            //约束系统api
                            MathParser.verifyRangeValue(
                                Build.VERSION.SDK_INT.toFloat(),
                                con.drop(3)
                            )
                        }
                        con.startsWith("code") -> {
                            //约束应用程序版本
                            MathParser.verifyRangeValue(getAppVersionCode().toFloat(), con.drop(4))
                        }
                        con.startsWith("brand") -> {
                            //约束手机品牌
                            Build.BRAND == con.drop(5)
                        }
                        con.startsWith("model") -> {
                            //约束手机型号
                            Build.MODEL == con.drop(5)
                        }
                        con.startsWith("cpu") -> {
                            //约束手机cpu最大频率
                            /*CpuUtils.cpuMaxFreq >= 2_800_000L*/
                            MathParser.verifyRangeValue(CpuUtils.cpuMaxFreq.toFloat(), con.drop(3))
                        }
                        con.startsWith("mem") -> {
                            //约束手机cpu最大的内存大小
                            MathParser.verifyRangeValue(
                                Device.getTotalMemory().toFloat(),
                                con.drop(3)
                            )
                        }
                        //字母少的, 放在后面匹配
                        con.startsWith("w") -> {
                            //约束手机屏幕宽度
                            MathParser.verifyRangeValue(_screenWidth.toFloat(), con.drop(1))
                        }
                        con.startsWith("h") -> {
                            //约束手机屏幕高度
                            MathParser.verifyRangeValue(_screenHeight.toFloat(), con.drop(1))
                        }
                        else -> {
                            //无法识别的指令
                            def
                        }
                    }

                    if (!result) {
                        break
                    }
                }
            }

            return result
        }

        val groups = value.split("!or!")

        var result = false
        for (group in groups) {
            if (_verify(group, result)) {
                result = true
                break
            }
        }

        return result
    }

    fun verifyAction(
        baseAccessibilityInterceptor: BaseAccessibilityInterceptor?,
        value: String?
    ): Boolean {
        if (baseAccessibilityInterceptor == null || value.isNullOrEmpty()) {
            return true
        }

        val action = baseAccessibilityInterceptor.currentAccessibilityAction
        val actionId = if (action is AutoParseAction) action.actionBean?.actionId ?: -1 else -1
        val index = baseAccessibilityInterceptor.actionIndex
        val size = baseAccessibilityInterceptor.actionList.size

        var result = false
        if (value.contains("~")) {
            val list = value.split("~")
            val start = list.getOrNull(0)
            val end = list.getOrNull(1)

            if (start?.isNumber() == true && end?.isNumber() == true) {
                val startNum = start.toLongOrNull() ?: Long.MAX_VALUE
                val endNum = end.toLongOrNull() ?: Long.MAX_VALUE

                if (startNum > size && endNum > size) {
                    //actionId ~ actionId
                    if (action is AutoParseAction) {
                        if (actionId in startNum..endNum) {
                            result = true
                        }
                    }
                } else if (startNum < size && endNum < size) {
                    //actionIndex ~ actionIndex

                    val startIndex = if (startNum >= 0) startNum else size + startNum
                    val endIndex = if (endNum >= 0) endNum else size + endNum

                    if (index in startIndex..endIndex) {
                        result = true
                    }
                }
            } else {
                val startVerify =
                    MathParser.verifyValue(index.toFloat(), start, size = size.toFloat())
                val endVerify = MathParser.verifyValue(index.toFloat(), end, size = size.toFloat())
                result = startVerify && endVerify
            }
        } else {
            if (value.isNumber()) {
                val num = value.toLongOrNull() ?: Long.MAX_VALUE

                if (num > size) {
                    //actionId
                    if (action is AutoParseAction) {
                        if (actionId == num) {
                            result = true
                        }
                    }
                } else {
                    //action index
                    if (num >= 0) {
                        if (num == index.toLong()) {
                            result = true
                        }
                    } else {
                        if (num + size == index.toLong()) {
                            result = true
                        }
                    }
                }
            } else {
                result = MathParser.verifyValue(index.toFloat(), value)
            }
        }
        return result
    }

}