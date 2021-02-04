package com.angcyo.acc2.parse

import android.os.Build
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.ConditionBean
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.component.appBean
import com.angcyo.library.ex.subEnd
import com.angcyo.library.ex.subStart
import com.angcyo.library.getAppVersionCode
import com.angcyo.library.utils.CpuUtils
import com.angcyo.library.utils.Device
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
     * [conditionList] 一组条件, 只要满足其中一个, 就会返回true
     * @return true 表示有条件满足*/
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

    /**约束条件是否解析通过
     * @return true 条件满足*/
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

            //system
            if (result && system != null) {
                result = verifySystem(system)
            }

            //app
            if (result && app != null) {
                val packageList = accParse.parsePackageName(
                    null,
                    accParse.findParse.windowBean()?.packageName
                        ?: accControl._taskBean?.packageName
                )

                //有一个包, 验证通过即可
                var verifyApp = false
                for (packageName in packageList) {
                    verifyApp = verifyApp(packageName, app)
                    if (verifyApp) {
                        break
                    }
                }
                result = verifyApp
            }
        }
        return result
    }

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
                        con.startsWith("windowFullscreen:") -> {
                            //约束浮窗全屏状态
                            true
                        }
                        con.startsWith("windowTouchable:") -> {
                            //约束浮窗全屏状态
                            true
                        }
                        con.startsWith("api") -> {
                            //约束系统api
                            accParse.expParse.parseAndCompute(
                                con.drop(3),
                                inputValue = Build.VERSION.SDK_INT.toFloat()
                            )
                        }
                        con.startsWith("code") -> {
                            //约束应用程序版本
                            accParse.expParse.parseAndCompute(
                                con.drop(4),
                                inputValue = getAppVersionCode().toFloat()
                            )
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
                            accParse.expParse.parseAndCompute(
                                con.drop(3),
                                inputValue = CpuUtils.cpuMaxFreq.toFloat()
                            )
                        }
                        con.startsWith("mem") -> {
                            //约束手机cpu最大的内存大小
                            accParse.expParse.parseAndCompute(
                                con.drop(3),
                                inputValue = Device.getTotalMemory().toFloat()
                            )
                        }
                        //字母少的, 放在后面匹配
                        con.startsWith("w") -> {
                            //约束手机屏幕宽度
                            accParse.expParse.parseAndCompute(
                                con.drop(1),
                                inputValue = _screenWidth.toFloat()
                            )
                        }
                        con.startsWith("h") -> {
                            //约束手机屏幕高度
                            accParse.expParse.parseAndCompute(
                                con.drop(1),
                                inputValue = _screenHeight.toFloat()
                            )
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

    fun verifyApp(packageName: String?, value: String?): Boolean {
        if (packageName.isNullOrEmpty() || value.isNullOrEmpty()) {
            return true
        }

        val appBean = packageName.appBean() ?: return false

        fun _verify(v: String, def: Boolean = false): Boolean {
            val all = v.split(";")

            var result = true
            for (con in all) {
                if (con.isNotEmpty()) {
                    result = when {
                        con.startsWith("code") -> {
                            //约束应用程序版本
                            accParse.expParse.parseAndCompute(
                                con.drop(4),
                                inputValue = appBean.versionCode.toFloat()
                            )
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

}