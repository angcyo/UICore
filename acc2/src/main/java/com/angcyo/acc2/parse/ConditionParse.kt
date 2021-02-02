package com.angcyo.acc2.parse

import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.ConditionBean
import com.angcyo.library.ex.subEnd
import com.angcyo.library.ex.subStart
import kotlin.random.Random

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ConditionParse(val accParse: AccParse) {

    /**判断给定的条件, 是否有满足
     * [conditionList] 一组条件, 只要满足其中一个, 就会返回true*/
    fun parse(conditionList: List<ConditionBean>?): ConditionResult {
        val result = ConditionResult()
        if (conditionList.isNullOrEmpty()) {
            result.success = true
        } else {
            for (condition in conditionList) {
                if (parse(condition)) {
                    result.success = true
                    result.conditionBean = condition
                    break
                }
            }
        }
        return result
    }

    fun parse(condition: ConditionBean): Boolean {
        var result = true

        val accControl = accParse.accControl

        condition.apply {
            //random
            if (random) {
                result = Random.nextBoolean()
            }

            //textMapList
            if (!textMapList.isNullOrEmpty()) {
                for (key in textMapList!!) {
                    val value = accControl._taskBean?.textMap?.get(key)
                    if (value == null) {
                        //指定key对应的value没有值, 则条件不满足
                        result = false
                        break
                    }
                }
            }

            //actionResultList
            if (result && !actionResultList.isNullOrEmpty()) {
                for (actionId in actionResultList!!) {
                    if (accControl.accSchedule.actionResultMap[actionId] != true) {
                        //指定id的action执行失败, 则条件不满足
                        result = false
                        break
                    }
                }
            }

            //检查次数是否满足表达式 [100:>=9] [.:>=9]
            fun checkCount(expression: String, countGetAction: (actionId: Long) -> Long): Boolean {
                val actionExp = expression.subStart(Action.ARG_SPLIT)
                val actionId = actionExp?.toLongOrNull()
                val exp = expression.subEnd(Action.ARG_SPLIT)
                if (!exp.isNullOrEmpty()) {
                    val actionBean = if (actionExp == ".") {
                        accControl.accSchedule._scheduleActionBean
                    } else {
                        accControl.findAction(actionId)
                    }

                    if (actionBean != null) {
                        val count = countGetAction(actionBean.actionId)
                        if (!accParse.expParse.parseAndCompute(exp, inputValue = count.toFloat())) {
                            //运行次数不满足条件
                            return false
                        }
                    }
                }
                return true
            }

            //actionRunList
            if (result && !actionRunList.isNullOrEmpty()) {
                for (expression in actionRunList!!) {
                    if (!checkCount(expression) {
                            accControl.accSchedule.getRunCount(it)
                        }) {
                        //运行次数不满足条件
                        result = false
                        break
                    }
                }
            }

            //actionJumpList
            if (result && !actionJumpList.isNullOrEmpty()) {
                for (expression in actionJumpList!!) {
                    if (!checkCount(expression) {
                            accControl.accSchedule.getJumpCount(it)
                        }) {
                        //跳转次数不满足条件
                        result = false
                        break
                    }
                }
            }

            //actionEnableList
            if (result && !actionEnableList.isNullOrEmpty()) {
                for (actionId in actionEnableList!!) {
                    val actionBean = accControl.findAction(actionId)
                    if (actionBean != null) {
                        if (!actionBean.enable) {
                            //未激活, 则不满足条件
                            result = false
                            break
                        }
                    }
                }
            }
        }
        return result
    }
}