package com.angcyo.acc2.action

import android.net.Uri
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.ex.startIntent

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/05
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class UrlAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_URL)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val url = action.arg(Action.ACTION_URL)
        val targetUrl = control.accSchedule.accParse.textParse.parse(url).firstOrNull()
        val pkg = action.arg("pkg") ?: control._taskBean?.packageName

        val packageNameList = control.accSchedule.accParse.textParse.parsePackageName(
            pkg,
            control._taskBean?.packageName
        )

        //启动对应的应用程序
        if (targetUrl.isNullOrEmpty()) {
            control.log("无需要打开的Url[$action]:${success}")
        } else {

            packageNameList.firstOrNull()?.let {
                try {
                    success = control.accService()?.startIntent(true) {
                        setPackage(it)
                        data = Uri.parse(targetUrl)
                    } != null
                } catch (e: Exception) {
                    control.log("打开[$targetUrl]失败:${e.stackTraceToString()}")
                }

                control.log("使用:[$it]打开[$targetUrl]:$success")
            }
        }
    }
}