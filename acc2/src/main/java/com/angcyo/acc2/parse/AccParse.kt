package com.angcyo.acc2.parse

import android.graphics.PointF
import android.graphics.Rect
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.control.AccControl
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.dp
import com.angcyo.library.utils.Device
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

    /**文本解析器*/
    var textParse = TextParse(this)

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