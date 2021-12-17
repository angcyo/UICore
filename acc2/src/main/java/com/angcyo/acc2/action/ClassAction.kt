package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.control.log
import com.angcyo.acc2.dynamic.IHandleActionDynamic
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subEnd

/**
 * 通过动态创建[Class], 动态调用的方式. 插入可执行代码
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ClassAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_CLS)
    }

    override fun runAction(control: AccControl, controlContext: ControlContext, nodeList: List<AccessibilityNodeInfoCompat>?, action: String): HandleResult {
        try {
            val clsName = action.subEnd(Action.ARG_SPLIT)!!
            val cls = Class.forName(clsName)
            val obj = cls.newInstance()

            return if (obj is IHandleActionDynamic) {
                obj.runAction(control, controlContext, nodeList, action).apply {
                    control.log("[ClassAction][$action]返回:$success")
                }
            } else {
                control.log("[ClassAction]异常:${clsName}不是[IHandleDynamic]类")
                handleResult {
                    success = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            control.log("[ClassAction]异常${action}:${e.message}")
            return handleResult {
                success = false
            }
        }
    }

}