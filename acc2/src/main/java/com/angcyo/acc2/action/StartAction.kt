package com.angcyo.acc2.action

import android.content.Intent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.getPrimaryClip
import com.angcyo.library.ex.openApp
import com.angcyo.library.ex.subEnd

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class StartAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_START)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val packageNameParam = action.subEnd(Action.ARG_SPLIT)
        val packageNameList = control.accSchedule.accParse.textParse.parsePackageName(
            packageNameParam,
            control._taskBean?.packageName
        )

        //剪切板内容
        val primaryClip = getPrimaryClip()

        //启动对应的应用程序
        if (packageNameList.isNullOrEmpty()) {
            control.log("无需要启动的应用:${success}")
        } else {
            packageNameList.forEach { packageName ->
                val result = control.accService()?.openApp(
                    packageName,
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                ) != null
                success = success || result
                val clipTip = if (primaryClip.isNullOrEmpty()) {
                    ""
                } else {
                    ",剪切板[$primaryClip]"
                }
                control.log("启动应用[$packageName]${clipTip}:${result}")
            }
        }
    }
}