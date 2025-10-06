package com.angcyo.acc2.parse

import android.graphics.PointF
import android.graphics.Rect
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.AccSchedule
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.isNumber
import com.angcyo.library.ex.size
import com.angcyo.library.utils.Device
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AccParse(val accControl: AccControl) : BaseParse() {

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

    /**文本解析器*/
    var textParse = TextParse(this)

    /**表单解析*/
    var formParse = FormParse()

    /**循环解析器*/
    var loopParse = LoopParse(this)

    /**表达式解析, 数值计算, 简单的数学计算*/
    val expParse = ExpParse(this).apply {
        aboutRatio = 10 * dp
        //ratioRef = 1f
    }

    val parseList = mutableListOf<BaseParse>()

    /**节点上下文*/
    val accContext = AccContext()

    /**默认时间随机因子*/
    var defTimeRandomFactor = 4L

    init {
        parseList.add(conditionParse)
        parseList.add(findParse)
        parseList.add(handleParse)
        parseList.add(filterParse)
        parseList.add(rectParse)
        parseList.add(operateParse)
        parseList.add(caseParse)
        parseList.add(textParse)
        parseList.add(expParse)
    }

    fun defaultIntervalDelay(): Long {
        return when (Device.performanceLevel()) {
            Device.PERFORMANCE_HIGH -> 300
            Device.PERFORMANCE_MEDIUM -> 1500
            Device.PERFORMANCE_LOW -> 2500
            else -> 1000
        }
    }

    /**
     * 解析时间格式
     * - 格式:[5000,500,5] 解释:5000+500*[1-5]毫秒,
     * - 格式:[300,] 解释:300+base*[1-5]毫秒
     * - 格式:[5] 纯数字则表示s秒
     * - 格式:[5~15] 5s到15s之间随机
     * 返回解析后的时间, 毫秒*/
    fun parseTime(arg: String?, def: Long = 0): Long {
        return when {
            arg.isNullOrEmpty() -> def
            arg.havePartition() || arg.isNumber() -> { //~
                val numList = arg.getIntList()
                if (numList.size() >= 2) {
                    val first = numList[0]
                    val second = numList[1]
                    val min = min(first, second)
                    val max = max(first, second)

                    nextInt(min, max + 1) * 1000L
                } else if (numList.size() == 1) {
                    //5 100
                    if (arg.length < 3) {
                        //5
                        numList.first() * 1000L
                    } else {
                        //100
                        numList.first() * 1L
                    }
                } else {
                    def
                }
            }

            else -> {
                val split = arg.split(",")

                //时长
                val start = split.getOrNull(0)?.toLongOrNull() ?: def

                //基数
                val baseStr = split.getOrNull(1)
                val base =
                    if (split.size() > 1 && baseStr.isNullOrEmpty()) 0 else baseStr?.toLongOrNull()
                        ?: defaultIntervalDelay()

                //倍数
                val factor = split.getOrNull(2)?.toLongOrNull() ?: nextLong(2, defTimeRandomFactor)

                start + base * nextLong(1, max(2L, factor + 1))
            }
        }
    }

    /** 从参数中, 解析设置的点位信息. 通常用于手势坐标. 手势坐标, 尽量使用 屏幕宽高用来参考计算
     * [move:10,10~100,100]
     * [fling:10,10~100,100]
     * */
    fun parsePoint(arg: String?, bound: Rect? = null): List<PointF> {
        val from = accControl._taskBean?.touchFrom ?: 5
        val until = accControl._taskBean?.touchUntil ?: 10

        val rect = bound ?: accContext.getBound()

        val screenWidth: Int = _screenWidth
        val screenHeight: Int = _screenHeight

        //默认的第一个点
        val fX: Float = screenWidth * 1 / 3f + nextInt(from, until)
        val fY: Float = screenHeight * 3 / 5f - nextInt(from, until)

        //默认的第二个点
        val tX: Float = screenWidth * 2 / 3f + nextInt(from, until)
        val tY: Float = screenHeight * 2 / 5f + nextInt(from, until)

        val p1 = PointF(fX, fY)
        var p2: PointF? = null

        try {
            arg?.apply {
                (if (this.contains(Action.POINT_SPLIT)) split(Action.POINT_SPLIT) else split("-")).apply {
                    //p1
                    getOrNull(0)?.toPointF(
                        rect.width(), rect.height()
                    )?.apply {
                        p1.set(this)
                    }

                    //p2
                    getOrNull(1)?.toPointF(
                        rect.width(), rect.height()
                    )?.apply {
                        p2 = this
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (p2 == null) {
            listOf(p1)
        } else {
            listOf(p1, p2!!)
        }
    }

    override fun onScheduleStart(scheduled: AccSchedule) {
        super.onScheduleStart(scheduled)
        parseList.forEach {
            it.onScheduleStart(scheduled)
        }
    }

    override fun onScheduleEnd(scheduled: AccSchedule) {
        super.onScheduleEnd(scheduled)
        parseList.forEach {
            it.onScheduleEnd(scheduled)
        }
    }
}