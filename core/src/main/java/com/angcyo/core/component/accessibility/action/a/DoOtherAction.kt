package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DoOtherAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_DO_OTHER
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        var value = false
        autoParseAction.accessibilityInterceptor?.actionOtherList?.forEach {
            if (arg == "*") {
                //所有的actionOtherList都要执行
                value = it.doActionWidth(
                    autoParseAction,
                    service,
                    autoParseAction.accessibilityInterceptor?.lastEvent,
                    handleNodeList.mapTo(ArrayList()) { nodeInfoCompat ->
                        nodeInfoCompat.unwrap()
                    }
                ) || value
            } else {
                //执行一个成功的actionOtherList后就不执行了
                value = value || it.doActionWidth(
                    autoParseAction,
                    service,
                    autoParseAction.accessibilityInterceptor?.lastEvent,
                    handleNodeList.mapTo(ArrayList()) { nodeInfoCompat ->
                        nodeInfoCompat.unwrap()
                    }
                )
            }
        }
        return value
    }
}