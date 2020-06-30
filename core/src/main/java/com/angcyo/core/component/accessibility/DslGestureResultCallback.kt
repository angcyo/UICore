package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
@SuppressLint("NewApi")
class DslGestureResultCallback(val result: GestureResult) :
    AccessibilityService.GestureResultCallback() {
    override fun onCancelled(gestureDescription: GestureDescription?) {
        super.onCancelled(gestureDescription)
        result(gestureDescription, true, true)
    }

    override fun onCompleted(gestureDescription: GestureDescription?) {
        super.onCompleted(gestureDescription)
        result(gestureDescription, false, false)
    }
}

