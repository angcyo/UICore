package com.angcyo.core.component.accessibility.action.a

import android.content.Intent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.AutoParser
import com.angcyo.core.component.accessibility.action.ErrorActionException
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library.ex.getPrimaryClip
import com.angcyo.library.ex.openApp

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class StartAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_START
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        //启动应用程序

        //剪切板内容
        val primaryClip = getPrimaryClip()

        //包名
        val targetPackageName = AutoParser.parseTargetPackageName(
            arg,
            autoParseAction._targetPackageName()
        )

        var value = false
        targetPackageName?.let {
            value = service.openApp(
                it,
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            ) != null

            autoParseAction.handleActionLog("启动:[$targetPackageName]clip:$primaryClip:$value")
        }

        if (!value) {
            autoParseAction.doActionFinish(ErrorActionException("无法启动应用[$targetPackageName]"))
        }

        return value
    }
}