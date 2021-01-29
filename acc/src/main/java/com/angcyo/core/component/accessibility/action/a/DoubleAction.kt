package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * 手势双击
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DoubleAction : BaseTouchAction() {

    init {
        handleAction = ConstraintBean.ACTION_DOUBLE
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        return double(autoParseAction, service, p1.x, p1.y).apply {
            autoParseAction.handleActionLog("双击[$p1]:$this")
        }
    }
}