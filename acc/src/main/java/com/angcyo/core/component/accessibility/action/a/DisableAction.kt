package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.AutoParser
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DisableAction : BaseConstraintAction() {

    init {
        handleAction = ConstraintBean.ACTION_DISABLE
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        if (arg.isNullOrEmpty()) {
            constraintBean.enable = false
        } else {
            arg!!.split(",").apply {
                forEach {
                    it.toLongOrNull()?.also { constraintId ->
                        AutoParser.enableConstraint(constraintId, false)
                        onGetConstraintList?.invoke()
                            ?.find { it.constraintId == constraintId }?.enable =
                            false
                    }
                }
            }
        }
        autoParseAction.handleActionLog("禁用ConstraintBean[${arg ?: "this"}]:true")
        return true
    }
}