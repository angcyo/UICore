package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.AccSchedule
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg

/**
 * 多个[actionList]需要执行时, 用于中断后面的执行.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class InterruptAction : BaseAction() {

    /**记录查找成功的次数, 用于跳过前多少次*/
    val findCountMap = hashMapOf<String, Int>()

    /**记录处理次数*/
    val handleCountMap = hashMapOf<String, Int>()

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_INTERRUPT)
    }

    override fun onScheduleEnd(scheduled: AccSchedule) {
        super.onScheduleEnd(scheduled)
        findCountMap.clear()
        handleCountMap.clear()
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        //中断判断的类型
        val type = action.arg(Action.ACTION_INTERRUPT)

        //数据存储key
        val keyArg = action.arg("key")

        //表达式
        val expArg = action.arg("exp")
        var exp: String? = expArg

        val accParse = control.accSchedule.accParse

        //处理
        var countMap: HashMap<String, Int>? = null
        val targetKey =
            "${keyArg ?: controlContext.action?.actionId ?: this@InterruptAction.hashCode()}"

        when (type) {
            "find" -> countMap = findCountMap
            "handle" -> countMap = handleCountMap
        }

        if (countMap != null) {
            if (!nodeList.isNullOrEmpty()) {
                val count = (countMap[targetKey] ?: 0) + 1
                countMap[targetKey] = count

                //判断是否需要跳过当前的查询
                exp = accParse.textParse.parse(expArg, true).firstOrNull()

                val match = accParse.expParse.parseAndCompute(exp, count.toFloat())

                if (!accParse.expParse.invalidExp && match) {
                    //如果满足计算表达式

                    success = true
                    //中断
                    forceFail = true
                }
            }
        }

        controlContext.log {
            append("中断后续action[$action]/$exp $success 中断:$forceFail")
        }
    }

}