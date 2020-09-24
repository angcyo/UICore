package com.angcyo.core.component.accessibility.action.a

import android.graphics.PointF
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.bounds
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.touch

/**
 * 在节点区域执行手势[touch]操作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ClickTouchAction : BaseTouchAction() {

    override fun interceptAction(autoParseAction: AutoParseAction, action: String?): Boolean {
        super.interceptAction(autoParseAction, action)
        return parseAction == ConstraintBean.ACTION_CLICK2 || parseAction == ConstraintBean.ACTION_CLICK3
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        //触发节点区域的手势操作
        var value = false
        val click = parseAction == ConstraintBean.ACTION_CLICK3

        var x = 0f
        var y = 0f

        handleNodeList.forEach {
            val bound = it.bounds()

            var specifyPoint: PointF? = null
            if (!arg.isNullOrEmpty()) {
                autoParseAction.parsePoint(arg, bound.width(), bound.height()).let {
                    specifyPoint = it[0]
                }
            }

            if (specifyPoint == null) {
                x = bound.centerX().toFloat()
                y = bound.centerY().toFloat()
            } else {
                x = specifyPoint!!.x + bound.left
                y = specifyPoint!!.y + bound.top
            }

            value = if (click) {
                //点击节点区域
                click(autoParseAction, service, x, y)
            } else {
                //双击节点区域
                double(autoParseAction, service, x, y)
            } || value
        }
        if (click) {
            autoParseAction.handleActionLog("点击节点区域[${x},${y}]:$value")
        } else {
            autoParseAction.handleActionLog("双击节点区域[${x},${y}]:$value")
        }
        return value
    }
}