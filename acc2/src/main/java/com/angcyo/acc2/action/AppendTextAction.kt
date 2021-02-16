package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.putListMap
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.ex.patternList
import com.angcyo.library.ex.text
import com.angcyo.library.ex.toStr

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AppendTextAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_APPEND_TEXT)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        var key: String? = Action.ACTION_APPEND_TEXT
        key = action.arg(Action.ACTION_APPEND_TEXT) ?: key
        val regex = action.arg("regex")

        val textList = mutableListOf<String>()

        nodeList?.forEach {

            var text = it.text()

            if (!regex.isNullOrEmpty()) {
                text = text.patternList(regex).firstOrNull()
            }

            if (text != null) {
                //保存起来
                val textStr = text.toStr()
                control._taskBean?.putListMap(key, textStr)
                textList.add(textStr)
            }
        }

        success = textList.isNotEmpty()
        control.log("追加文本[$textList] key:[$key] regex:[${regex}]:$success")
    }
}