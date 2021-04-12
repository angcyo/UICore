package com.angcyo.acc2.parse

import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.LoopBean
import com.angcyo.acc2.control.AccSchedule
import com.angcyo.acc2.control.log
import com.angcyo.library.ex.size
import com.angcyo.selenium.auto.Count
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

/**
 * 循环控制解析器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/04/12
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class LoopParse(val accParse: AccParse) : BaseParse() {

    /**循环次数统计*/
    val loopCount = hashMapOf<Long, Count>()

    /**返回true, 则表示被循环控制器处理了.
     * 返回false, 表示没有被处理, 需要自行处理*/
    fun parse(action: ActionBean, result: HandleResult, loop: LoopBean): Boolean {
        if (loop.check) {
            if (result.forceSuccess || result.success) {
                loopCountIncrement(action.actionId)
                return _parse(action, loop)
            }
            accParse.accControl.log("Loop check faild.")
        } else {
            loopCountIncrement(action.actionId)
            return _parse(action, loop)
        }
        return false
    }

    fun _parse(action: ActionBean, loop: LoopBean): Boolean {
        val successCount = loop.success
        val errorCount = loop.error
        if (successCount == null && errorCount == null) {
            //空
            return false
        }
        if (successCount != null) {
            val numList = successCount.getIntList()
            val targetCount = if (successCount.havePartition() && numList.size() >= 2) {
                // 3~10
                val first = numList[0]
                val second = numList[1]
                val min = min(first, second)
                val max = max(first, second)
                Random.nextInt(min, max + 1)
            } else if (numList.isNullOrEmpty()) {
                //随机
                nextInt()
            } else {
                numList.firstOrNull() ?: 0
            }

            val count = getLoopCount(action.actionId)
            if (count >= targetCount) {
                return false
            } else {
                return true
            }
        }
        if (errorCount != null) {
            val numList = errorCount.getIntList()
            val targetCount = if (errorCount.havePartition() && numList.size() >= 2) {
                // 3~10
                val first = numList[0]
                val second = numList[1]
                val min = min(first, second)
                val max = max(first, second)
                Random.nextInt(min, max + 1)
            } else if (numList.isNullOrEmpty()) {
                //随机
                nextInt()
            } else {
                numList.firstOrNull() ?: 0
            }

            val count = getLoopCount(action.actionId)
            if (count >= targetCount) {
                accParse.accControl.error("循环控制超限:$targetCount 次")
                return true
            } else {
                return false
            }
        }
        return false
    }

    /**累加Loop次数*/
    fun loopCountIncrement(actionId: Long) {
        val count = loopCount[actionId] ?: Count()
        count.doCount()
        loopCount[actionId] = count
    }

    /**获取[ActionBean]的loop次数*/
    fun getLoopCount(actionId: Long): Long = loopCount[actionId]?.count ?: -1

    override fun onScheduleEnd(scheduled: AccSchedule) {
        super.onScheduleEnd(scheduled)
        loopCount.clear()
    }
}