package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import com.angcyo.core.BuildConfig

/**
 * 提供日志输出
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class LogAccessibilityInterceptor : BaseAccessibilityInterceptor() {

    var enable: Boolean = BuildConfig.DEBUG

    override fun handleAccessibility(service: BaseAccessibilityService, ignore: Boolean) {
        super.handleAccessibility(service, ignore)
    }

    override fun handleFilterNode(
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        //super.handleFilterNode(service, nodeList)
        if (enable) {
            nodeList.mainNode()?.let {
                AccessibilityHelper.log(it.toString())
            }
        }
    }
}