package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.AutoParser
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library.ex.elseNull

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class EnableAction : BaseConstraintAction() {

    init {
        handleAction = ConstraintBean.ACTION_ENABLE
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        if (arg.isNullOrEmpty()) {
            constraintBean.enable = true
        } else {
            arg!!.split(",").apply {
                forEach {
                    it.toLongOrNull()?.also { constraintId ->
                        val find =
                            onGetConstraintList?.invoke()?.find { it.constraintId == constraintId }

                        find?.let {
                            it.enable = true
                        }.elseNull {
                            AutoParser.enableConstraint(
                                constraintId,
                                true
                            )
                        }
                    }
                }
            }
        }
        autoParseAction.handleActionLog("激活ConstraintBean[${arg ?: "this"}]:true")
        return true
    }
}