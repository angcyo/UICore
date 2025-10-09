package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.TextParamBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.BaseParse
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.patternList
import com.angcyo.library.ex.subStart
import com.angcyo.library.ex.text
import com.angcyo.library.ex.toStr

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseAction : BaseParse() {

    /**当前[BaseAction]处理的结果, 是否不影响[HandleBean]的处理*/
    var ignoreResult: Boolean = false

    /**临时存储的文本处理参数, [runAction]之后清除*/
    var textParamBean: TextParamBean? = null

    /**是否需要拦截[action]执行*/
    abstract fun interceptAction(control: AccControl, action: String): Boolean

    /**执行操作*/
    @Deprecated("废弃,请使用4个参数的函数")
    open fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = false
    }

    /**[runAction]*/
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
        val c1 = subStart(Action.ARG_SPLIT)
        if (c1 == cmd) {
            return true
        }
        val c2 = subStart(Action.ARG_SPLIT2)
        if (c2 == cmd) {
            return true
        }
        return this == cmd
    }
}

/**Dsl*/
fun handleResult(action: HandleResult.() -> Unit): HandleResult {
    return HandleResult().apply(action)
}

/**转换成节点对应的文本列表*/
fun List<AccessibilityNodeInfoCompat>?.toNodeTextList(regex: String? = null): List<String> {
    //收集节点文本
    val textStrList = mutableListOf<String>()

    for (node in this ?: emptyList()) {
        var text = node.text()

        if (!regex.isNullOrEmpty()) {
            try {
                text = text.patternList(regex).firstOrNull()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (text != null) {
            textStrList.add(text.toStr())
        }
    }

    return textStrList
}