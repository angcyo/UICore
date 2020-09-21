package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.getLongClickParent
import com.angcyo.core.component.accessibility.longClick
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * 长按节点
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class LongClickAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_LONG_CLICK
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        var value = false
        handleNodeList.forEach {
            value = it.getLongClickParent()?.longClick() ?: false || value
        }
        autoParseAction.handleActionLog("长按节点[${handleNodeList.firstOrNull()}]:$value")
        return value
    }
}