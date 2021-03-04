package com.angcyo.acc2.parse

import android.graphics.PointF
import android.graphics.Rect
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.action.InputAction
import com.angcyo.acc2.bean.getTextList
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.app
import com.angcyo.library.component.appBean
import com.angcyo.library.ex.*
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.getLongNum
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.Random.Default.nextLong

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

    /**操作记录解析器*/
    var operateParse = OperateParse(this)

    /**情况/场景解析器*/
    var caseParse = CaseParse(this)

    /**表达式解析, 数值计算, 简单的数学计算*/
    val expParse = ExpParse().apply {
        aboutRatio = 10 * dp
        //ratioRef = 1f
    }

    /**节点上下文*/
    val accContext = AccContext()

    fun defaultIntervalDelay(): Long {
        return when (Device.performanceLevel()) {
            Device.PERFORMANCE_HIGH -> 200
            Device.PERFORMANCE_MEDIUM -> 300
            Device.PERFORMANCE_LOW -> 500
            else -> 600
        }
    }

    /**解析文本
     * $0 从[com.angcyo.acc2.bean.TaskBean.wordList] 取第一个
     * $-2 从[com.angcyo.acc2.bean.TaskBean.wordList] 取倒数第二个
     * $0~$-2 取范围内的字符
     * $[xxx] 从[com.angcyo.acc2.bean.TaskBean.textMap]获取[xxx]键值对应的值
     *
     * [replace] 是否只是替换掉原来的字符
     * */
    fun parseText(arg: String?, replace: Boolean = false): List<String?> {
        if (arg.isNullOrEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<String?>()

        var isHandle = false

        val taskBean = accControl._taskBean
        val wordList: List<String?>

        //替换后的字符串
        var replaceResult = arg

        //$[xxx], 在map中获取文本
        val mapKeyList = arg.patternList("(?<=\\$\\[).+(?=\\])")
        if (mapKeyList.isNotEmpty()) {
            isHandle = true
            val keyResult = mutableListOf<String?>()
            mapKeyList.forEach { key ->
                when (key) {
                    Action.APP_NAME -> {
                        val appName =
                            parsePackageName(null, accControl._taskBean?.packageName).firstOrNull()
                                ?.appBean()?.appName?.str()
                        if (appName.isNullOrBlank()) {
                            taskBean?.getTextList(key)?.firstOrNull()?.let { value ->
                                keyResult.add(value)
                                if (replace) {
                                    replaceResult = replaceResult?.replace("$[$key]", value)
                                }
                            }
                        } else {
                            //程序名
                            keyResult.add(appName)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", appName)
                            }
                        }
                    }
                    Action.NOW_TIME -> {
                        val text = nowTimeString()
                        keyResult.add(text)
                        if (replace) {
                            replaceResult = replaceResult?.replace("$[$key]", text)
                        }
                    }
                    Action.LAST_INPUT -> {
                        val text = InputAction.lastInputText
                        if (text != null) {
                            keyResult.add(text)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", text)
                            }
                        }
                    }
                    else -> {
                        getTextOfListMap(key)?.apply {
                            keyResult.addAll(this)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", this.str())
                            }
                        } ?: getTextOfMap(key)?.let { value ->
                            keyResult.add(value)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", value)
                            }
                        }
                    }
                }
            }

            //如果指定了$[xxx]
            wordList = keyResult.toList()
            result.addAll(keyResult)
        } else {
            wordList = taskBean?.wordList ?: emptyList()
        }

        //$0~$-2
        val indexStringList = arg.patternList("\\$[-]?\\d+")
        if (indexStringList.isNotEmpty()) {
            //$xxx 的情况
            if (arg.havePartition()) {
                //$0~$1
                if (indexStringList.size >= 2) {
                    isHandle = true
                    val startIndex = indexStringList[0].getLongNum()?.revise(wordList.size) ?: 0
                    val endIndex = indexStringList[1].getLongNum()?.revise(wordList.size) ?: 0

                    val rangeWordList = mutableListOf<String?>()
                    wordList.forEachIndexed { index, word ->
                        if (index in startIndex..endIndex) {
                            result.add(word)
                            rangeWordList.add(word)
                        }
                    }
                    if (replace) {
                        replaceResult = replaceResult?.replace(
                            "$$startIndex${Action.POINT_SPLIT}$${endIndex}",
                            rangeWordList.str()
                        )
                    }
                } else {
                    isHandle = true
                    indexStringList.forEach { indexString ->
                        indexString.getLongNum()?.let { index ->
                            wordList.getOrNull(index.toInt())?.let { word ->
                                result.add(word)

                                if (replace) {
                                    replaceResult = replaceResult?.replace("$$index", word)
                                }
                            }
                        }
                    }
                }
            } else {
                //$0 $2 $3
                isHandle = true
                indexStringList.forEach { indexString ->
                    indexString.getLongNum()?.let { index ->
                        wordList.getOrNull(index.toInt())?.let { word ->
                            result.add(word)

                            if (replace) {
                                replaceResult = replaceResult?.replace("$$index", word)
                            }
                        }
                    }
                }
            }
        }

        if (!isHandle) {
            //都未处理
            result.add(arg)
        }

        if (result.isEmpty() || (replace && arg == replaceResult)) {
            accControl.log("无法解析文本参数[$arg]↓\n${taskBean?.wordList}\n${taskBean?.textMap}\n${taskBean?.textListMap}")
        }

        if (replace) {
            accControl.log("文本替换后[$arg]->[$replaceResult]")
            return listOf(replaceResult)
        }

        return result
    }

    fun getTextOfMap(key: String) = accControl._taskBean?.textMap?.get(key)
    fun getTextOfListMap(key: String) = accControl._taskBean?.textListMap?.get(key)

    /**
     * 解析时间格式
     * 格式[5000,500,5] 解释:5000+500*[1-5],
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
            val factor = split.getOrNull(2)?.toLongOrNull() ?: nextLong(2, 5)

            start + base * nextLong(1, max(2L, factor + 1))
        }
    }

    /**将参数转换成对应的包名*/
    fun parsePackageName(
        arg: String? = null,
        target: String? = findParse.windowBean()?.packageName ?: accControl._taskBean?.packageName
    ): List<String> {
        val result = mutableListOf<String>()

        val nameArg = arg ?: target
        nameArg?.split(Action.PACKAGE_SPLIT)?.forEach { name ->
            val packageName = if (name.isEmpty()) {
                target
            } else if (name == Action.PACKAGE_MAIN) {
                app().packageName
            } else if (name == Action.PACKAGE_TARGET) {
                if (target.isNullOrEmpty()) {
                    //优先使用task的包名, 确保不是空
                    accControl._taskBean?.packageName?.split(Action.PACKAGE_SPLIT)?.firstOrNull()
                        ?: target
                } else {
                    target
                }
            } else if (name == Action.PACKAGE_ACTIVE) {
                accControl.accService()?.rootInActiveWindow?.packageName
            } else {
                name
            }?.toStr()

            if (!packageName.isNullOrBlank()) {
                result.add(packageName)
            }
        }

        return result
    }

    /** 从参数中, 解析设置的点位信息. 通常用于手势坐标. 手势坐标, 尽量使用 屏幕宽高用来参考计算
     * [move:10,10~100,100]
     * [fling:10,10~100,100]
     * */
    fun parsePoint(arg: String?, bound: Rect? = null): List<PointF> {
        val rect = bound ?: accContext.getBound()

        val screenWidth: Int = _screenWidth
        val screenHeight: Int = _screenHeight

        val fX: Float = screenWidth * 1 / 3f + Random.nextInt(5, 10)
        val tX: Float = screenWidth * 2 / 3f + Random.nextInt(5, 10)
        val fY: Float = screenHeight * 3 / 5f - Random.nextInt(5, 10)
        val tY: Float = screenHeight * 2 / 5f + Random.nextInt(5, 10)

        val p1 = PointF(fX, fY)
        val p2 = PointF(tX, tY)


        try {
            arg?.apply {
                (if (this.contains(Action.POINT_SPLIT)) split(Action.POINT_SPLIT) else split("-")).apply {
                    //p1
                    getOrNull(0)?.toPointF(
                        rect.width(),
                        rect.height()
                    )?.apply {
                        p1.set(this)
                    }

                    //p2
                    getOrNull(1)?.toPointF(
                        rect.width(),
                        rect.height()
                    )?.apply {
                        p2.set(this)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf(p1, p2)
    }
}