package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subEnd
import kotlin.random.Random

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class RandomFalseAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_RANDOM_FALSE)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.subEnd(Action.ARG_SPLIT)

        val value = if (arg.isNullOrEmpty()) {
            //未指定随机概率
            Random.nextBoolean()
        } else {
            //指定了随机的概率
            val factor = Random.nextInt(1, 101) //[1-100]
            control.accSchedule.accParse.expParse.parseAndCompute(
                arg,
                inputValue = factor.toFloat()
            )
        }

        if (value) {
            success = false
            forceFail = true
        } else {
            success = true
            forceFail = false
        }

        control.log("随机失败[$success]并且强制失败[${forceFail}]:${action}")
    }
}