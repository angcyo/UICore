package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.action.arg
import com.angcyo.core.component.accessibility.fling
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FlingAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_FLING
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        return service.gesture.fling(
            p1.x,
            p1.y,
            p2.x,
            p2.y,
            autoParseAction.getGestureStartTime(arg?.arg(1))
        ).apply {
            autoParseAction.handleActionLog("fling[$p1 $p2]:$this")
        }
    }
}