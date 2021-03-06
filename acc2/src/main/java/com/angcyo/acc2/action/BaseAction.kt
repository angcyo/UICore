package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.TextParamBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.BaseParse
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subStart

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseAction : BaseParse() {

    /**临时存储的文本处理参数, [runAction]之后清除*/
    var textParamBean: TextParamBean? = null

    /**是否需要拦截[action]执行*/
    abstract fun interceptAction(control: AccControl, action: String): Boolean

    /**执行操作*/
    open fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = false
    }

    open fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = runAction(control, nodeList, action)

    /**主线的[ActionBean]*/
    fun mainActionBean(control: AccControl) = control.accSchedule._scheduleActionBean

    /**正在运行的[ActionBean]*/
    fun runActionBean(control: AccControl) = control.accSchedule._runActionBean

    /**Dsl*/
    fun handleResult(action: HandleResult.() -> Unit): HandleResult {
        return HandleResult().apply(action)
    }

    /**[cmd]裸命令
     * 返回文本解析时, 需要处理的一些参数*/
    fun getHandleTextParamBeanByAction(cmd: String): TextParamBean? {
        if (textParamBean?.handleAction == null || textParamBean?.handleAction?.contains(cmd) == true) {
            return textParamBean
        }
        return null
    }

    fun HandleResult.addNode(node: AccessibilityNodeInfoCompat) {
        when (val list = nodeList) {
            null -> nodeList = mutableListOf(node)
            is MutableList -> list.add(node)
            else -> nodeList = list.toMutableList().apply {
                add(node)
            }
        }
    }

    /**判断字符串是否是执行的命令*/
    fun String.cmd(cmd: String): Boolean {
        val c = subStart(Action.ARG_SPLIT) ?: subStart(Action.ARG_SPLIT2) ?: this
        return c == cmd
    }
}