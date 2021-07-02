package com.angcyo.acc2.action

import android.content.Intent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.app
import com.angcyo.library.ex.baseConfig
import com.angcyo.library.ex.subEnd

/**
 * [Intent]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class IntentAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_INTENT)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val intentAction = action.subEnd(Action.ARG_SPLIT)

        val intent = Intent(intentAction)
        intent.baseConfig(app())

        success = try {
            app().startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        control.log("启动意图[$intentAction]:${success}")
    }
}