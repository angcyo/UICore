package com.angcyo.core.component.accessibility.action.a

import android.app.Instrumentation
import android.view.KeyEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.AccessibilityHelper
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.http.rx.doBack

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class KeyAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_KEY
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        var value = false

        val keyCode = arg?.toIntOrNull() ?: -1
        val keyCodeStr = KeyEvent.keyCodeToString(keyCode)

        if (keyCode > 0) {
            doBack {
                try {
                    //This method can not be called from the main application thread

                    //发送给其他应用程序需要权限:
                    //Injecting to another application requires INJECT_EVENTS permission
                    val inst = Instrumentation()
                    inst.sendKeyDownUpSync(keyCode)
                } catch (e: Exception) {
                    AccessibilityHelper.log("Exception when sendKeyDownUpSync $keyCodeStr :$e")
                    e.printStackTrace()
                }
            }
            value = true
        }

        autoParseAction.handleActionLog("发送按键[$keyCodeStr]:$value")
        return value
    }
}