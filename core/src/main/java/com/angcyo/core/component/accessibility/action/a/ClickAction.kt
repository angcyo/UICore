package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.AutoParser
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isDebugType

/**
 * 点击节点
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ClickAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_CLICK
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        //触发节点自带的click
        var value = false
        handleNodeList.forEach { node ->
            if (arg.isNullOrEmpty()) {
                value = node.getClickParent()?.click() ?: false || value
            } else {
                AutoParser.getStateParentNode(listOf(arg!!), node).apply {
                    if (first) {
                        value = node.getClickParent()?.click() ?: false || value
                    } else {
                        //携带了状态约束参数, 并且没有匹配到状态
                        value = true
                    }
                }
            }
        }
        val first = handleNodeList.firstOrNull()
        autoParseAction.handleActionLog("点击节点[${first?.text() ?: first?.bounds()}]:$value")
        if (isDebug()) {
            autoParseAction.handleActionLog(first?.unwrap()?.logAllNode() ?: "no node.")
        }
        return value
    }
}