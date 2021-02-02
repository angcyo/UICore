package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.parse.HandleResult

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseAction {

    /**是否需要拦截[action]执行*/
    abstract fun interceptAction(control: AccControl, action: String): Boolean

    /**执行操作*/
    abstract fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult

    /**主线的[ActionBean]*/
    fun mainActionBean(control: AccControl) = control.accSchedule._scheduleActionBean

    /**正在运行的[ActionBean]*/
    fun runActionBean(control: AccControl) = control.accSchedule._runActionBean

    /**Dsl*/
    fun handleResult(action: HandleResult.() -> Unit): HandleResult {
        return HandleResult().apply(action)
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
}