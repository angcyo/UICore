package com.angcyo.acc2.control

import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.CheckBean
import com.angcyo.acc2.bean.FindBean
import com.angcyo.acc2.bean.HandleBean

/**
 * 控制器上下文, 包含上下文的一些对象
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/04/13
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ControlContext {

    var control: AccControl? = null

    /**当前上下文中的[ActionBean]*/
    var action: ActionBean? = null

    /**当前上下文中的[CheckBean]*/
    var check: CheckBean? = null

    /**当前上下文中的[FindBean]*/
    var find: FindBean? = null

    /**当前上下文中的[HandleBean]*/
    var handle: HandleBean? = null

    fun isPrimaryAction() = control?.accSchedule?.isPrimaryAction(action) == true

    fun copy(init: ControlContext.() -> Unit = {}): ControlContext {
        return ControlContext().also {
            it.control = control
            it.action = action
            it.check = check
            it.find = find
            it.handle = handle
            init()
        }
    }

    fun log(init: StringBuilder.() -> Unit = {}): String {
        return buildString {
            val indent = if (isPrimaryAction()) "" else "  "
            append(indent)
            append(control?.accSchedule?.indexTip())
            append(" ")
            if (action == null) {
                appendLine("Action:无")
            } else {
                appendLine("${action?.actionLog()}")
            }
            if (check == null) {
                appendLine("${indent}Check:无")
            } else {
                appendLine("${indent}${check?.checkLog()}")
            }
            if (find == null) {
                //appendLine("${indent}Find:无")
            } else {
                appendLine("${indent}${find?.findLog()}")
            }
            if (handle == null) {
                //appendLine("${indent}Handle:无")
            } else {
                appendLine("${indent}Handle:${handle?.actionList}")
            }
            append(indent)
            append("->")
            init()
        }.apply {
            control?.accSchedule?.printActionLog(this, action, isPrimaryAction())
        }
    }
}