package com.angcyo.acc2.parse

import com.angcyo.acc2.control.AccControl
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.patternList
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.getLongNum
import kotlin.math.max
import kotlin.random.Random

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AccParse(val accControl: AccControl) {

    /**条件解析器*/
    var conditionParse = ConditionParse(this)

    /**查找解析器*/
    var findParse = FindParse(this)

    /**处理解析器*/
    var handleParse = HandleParse(this)

    /**过滤解析器*/
    var filterParse = FilterParse(this)

    /**矩形解析器*/
    var rectParse = RectParse(this)

    /**节点上下文*/
    val accContext = AccContext()

    /**表达式解析, 数值计算, 简单的数学计算*/
    val expParse = ExpParse().apply {
        aboutRatio = 10 * dp
        //ratioRef = 1f
    }

    fun defaultIntervalDelay(): Long {
        return when (Device.performanceLevel()) {
            Device.PERFORMANCE_HIGH -> 800
            Device.PERFORMANCE_MEDIUM -> 1200
            Device.PERFORMANCE_LOW -> 1_500
            else -> 2_000
        }
    }

    /**解析文本
     * $0 从[com.angcyo.acc2.bean.TaskBean.wordList] 取第一个
     * $-2 从[com.angcyo.acc2.bean.TaskBean.wordList] 取倒数第二个
     * $0~$-2 取范围内的字符
     * $[xxx] 从[com.angcyo.acc2.bean.TaskBean.textMap]获取[xxx]键值对应的值
     * */
    fun parseText(arg: String?): List<String> {
        if (arg.isNullOrEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<String>()

        var isHandle = false

        val taskBean = accControl._taskBean

        //$0~$-2
        val indexStringList = arg.patternList("\\$[-]?\\d+")
        if (indexStringList.isNotEmpty()) {
            val wordList = taskBean?.wordList ?: emptyList()
            //$xxx 的情况
            if (arg.havePartition()) {
                //$0~$1
                if (indexStringList.size >= 2) {
                    isHandle = true
                    val startIndex = indexStringList[0].getLongNum()?.revise(wordList.size) ?: 0
                    val endIndex = indexStringList[1].getLongNum()?.revise(wordList.size) ?: 0

                    wordList.forEachIndexed { index, word ->
                        if (index in startIndex..endIndex) {
                            result.add(word)
                        }
                    }
                } else {
                    isHandle = true
                    indexStringList.forEach { indexString ->
                        indexString.getLongNum()?.let { index ->
                            wordList.getOrNull(index.toInt())?.let { word ->
                                result.add(word)
                            }
                        }
                    }
                }
            } else {
                //$0
                isHandle = true
                indexStringList.forEach { indexString ->
                    indexString.getLongNum()?.let { index ->
                        wordList.getOrNull(index.toInt())?.let { word ->
                            result.add(word)
                        }
                    }
                }
            }
        }

        //$[xxx], 在map中获取文本
        val mapKeyList = arg.patternList("(?<=\\$\\[).+(?=\\])")
        if (mapKeyList.isNotEmpty()) {
            isHandle = true
            mapKeyList.forEach { key ->
                taskBean?.textMap?.get(key)?.let { value ->
                    result.add(value)
                }
            }
        }

        if (!isHandle) {
            //都未处理
            result.add(arg)
        }

        if (result.isEmpty()) {
            accControl.accPrint.log(accControl, "无法解析文本参数[$arg].")
        }

        return result
    }

    /**
     * 解析时间格式
     * 格式[5000,500,5] 解释:5000+500*[1-5),
     * 返回解析后的时间, 毫秒*/
    fun parseTime(arg: String?, def: Long = 0): Long {
        return if (arg.isNullOrEmpty()) {
            def
        } else {
            val split = arg.split(",")

            //时长
            val start = split.getOrNull(0)?.toLongOrNull() ?: def

            //基数
            val base = split.getOrNull(1)?.toLongOrNull() ?: defaultIntervalDelay()

            //倍数
            val factor = split.getOrNull(2)?.toLongOrNull() ?: 1 //nextLong(2, 5)

            start + base * Random.nextLong(1, max(2L, factor + 1))
        }
    }
}