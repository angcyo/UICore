package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.randomGet
import com.angcyo.library.ex.randomString
import com.angcyo.library.ex.setNodeText
import com.angcyo.library.ex.subEnd

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class InputAction : BaseAction() {

    companion object {
        var lastInputText: String? = null
    }

    init {
        lastInputText = null
    }

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_INPUT) || action.cmd(Action.ACTION_SET_TEXT)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        val arg = action.subEnd(Action.ARG_SPLIT)

        //执行set text时的文本
        val text = if (arg.isNullOrEmpty()) {
            randomString() //随机生成文本
        } else {
            //从列表中, 随机获取一个
            control.accSchedule.accParse.parseText(arg).randomGet(1).firstOrNull()
        }

        lastInputText = text

        nodeList?.forEach { node ->
            val result = node.setNodeText(text)
            success = result || success
            control.log("输入文本[$text]:$result\n${node.toLog()}")
        }
    }
}