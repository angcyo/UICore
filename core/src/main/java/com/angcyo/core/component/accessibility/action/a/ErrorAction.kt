package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.ErrorActionException
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ErrorAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_ERROR
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        //直接失败操作
        handleResult.jumpNextHandle = true
        //异常退出
        val error = arg ?: "ACTION_ERROR"
        autoParseAction.handleActionLog("强制异常退出[$error]:true")
        autoParseAction.doActionFinish(ErrorActionException(error))
        return true
    }
}