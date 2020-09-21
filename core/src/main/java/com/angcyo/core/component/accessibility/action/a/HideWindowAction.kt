package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.base.AccessibilityWindowLayer
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library.ex.fullTime

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class HideWindowAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_HIDE_WINDOW
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        AccessibilityWindowLayer.hide()
        arg?.toLongOrNull()?.apply {
            val actionSize = autoParseAction.accessibilityInterceptor?.actionList?.size ?: 0
            if (this in 1..actionSize.toLong()) {
                AccessibilityWindowLayer.hideCount(this)

                autoParseAction.handleActionLog("隐藏浮窗Count[${this}]:true")
            } else {
                //指定需要隐藏的时长, 毫秒
                AccessibilityWindowLayer.hideTime(this)

                autoParseAction.handleActionLog("隐藏浮窗Time[${AccessibilityWindowLayer._hideToTime.fullTime()}]:true")
            }
        }
        return true
    }
}