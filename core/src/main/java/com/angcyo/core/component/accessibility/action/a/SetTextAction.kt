package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.setNodeText

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class SetTextAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_SET_TEXT
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        var value = false

        //执行set text时的文本
        val text = if (arg.isNullOrEmpty()) {
            autoParseAction.getInputText(constraintBean)
        } else {
            autoParseAction.getTextFromWord(arg)
        }

        handleNodeList.forEach {
            value = it.setNodeText(text) || value
        }

        autoParseAction.handleActionLog("设置文本[$text]:$value")
        return value
    }
}