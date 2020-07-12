package com.angcyo.core.component.accessibility

import com.angcyo.library.L
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
        startApp()
    }

    override fun handleAccessibility(service: BaseAccessibilityService, ignore: Boolean) {
        // super.handleAccessibility(service, ignore)
    }

    fun startApp() {
        val service = lastService
        if (service == null) {
            L.e("service is not connected!")
        } else {
            service.openApp(startAppPackageName ?: service.packageName)
        }
    }
}