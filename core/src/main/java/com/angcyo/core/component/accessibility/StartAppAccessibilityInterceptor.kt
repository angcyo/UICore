package com.angcyo.core.component.accessibility

import com.angcyo.library.ex.openApp

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class StartAppAccessibilityInterceptor : BaseAccessibilityInterceptor() {

    var startAppPackageName: String? = null

    override fun onServiceConnected(service: BaseAccessibilityService) {
        super.onServiceConnected(service)
        service.openApp(startAppPackageName ?: service.packageName)
    }
}