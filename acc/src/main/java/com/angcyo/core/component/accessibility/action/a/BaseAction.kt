package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.action.arg
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class BaseAction {

    /**能够处理的[parseAction]*/
    var handleAction: String? = null

    //解析后需要执行的动作
    var parseAction: String? = null

    //动作携带的参数
    var arg: String? = null

    protected open fun reset() {
        parseAction = null
        arg = null
    }

    /**解析[action]*/
    protected open fun parseAction(autoParseAction: AutoParseAction, action: String?) {
        try {
            reset()
            if (!action.isNullOrEmpty()) {
                //解析2个点的坐标
                val indexOf = action.indexOf(":", 0, true)

                if (indexOf == -1) {
                    //未找到
                    this.parseAction = action
                } else {
                    //找到
                    this.parseAction = action.substring(0, indexOf)
                    arg = action.substring(indexOf + 1, action.length)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**是否需要拦截[action]*/
    open fun interceptAction(autoParseAction: AutoParseAction, action: String?): Boolean {
        parseAction(autoParseAction, action)
        return parseAction == handleAction
    }

    /**是否成功处理[action]*/
    open fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        return false
    }
}