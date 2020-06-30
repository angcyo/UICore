package com.angcyo.core.component.accessibility

import android.R
import android.os.Build
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

fun Int.actionSymbolicName(): String {
    return when (this) {
        AccessibilityNodeInfoCompat.ACTION_FOCUS -> "ACTION_FOCUS"
        AccessibilityNodeInfoCompat.ACTION_CLEAR_FOCUS -> "ACTION_CLEAR_FOCUS"
        AccessibilityNodeInfoCompat.ACTION_SELECT -> "ACTION_SELECT"
        AccessibilityNodeInfoCompat.ACTION_CLEAR_SELECTION -> "ACTION_CLEAR_SELECTION"
        AccessibilityNodeInfoCompat.ACTION_CLICK -> "ACTION_CLICK"
        AccessibilityNodeInfoCompat.ACTION_LONG_CLICK -> "ACTION_LONG_CLICK"
        AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS -> "ACTION_ACCESSIBILITY_FOCUS"
        AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS -> "ACTION_CLEAR_ACCESSIBILITY_FOCUS"
        AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY -> "ACTION_NEXT_AT_MOVEMENT_GRANULARITY"
        AccessibilityNodeInfoCompat.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY -> "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY"
        AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT -> "ACTION_NEXT_HTML_ELEMENT"
        AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT -> "ACTION_PREVIOUS_HTML_ELEMENT"
        AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> "ACTION_SCROLL_FORWARD"
        AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> "ACTION_SCROLL_BACKWARD"
        AccessibilityNodeInfoCompat.ACTION_CUT -> "ACTION_CUT"
        AccessibilityNodeInfoCompat.ACTION_COPY -> "ACTION_COPY"
        AccessibilityNodeInfoCompat.ACTION_PASTE -> "ACTION_PASTE"
        AccessibilityNodeInfoCompat.ACTION_SET_SELECTION -> "ACTION_SET_SELECTION"
        AccessibilityNodeInfoCompat.ACTION_EXPAND -> "ACTION_EXPAND"
        AccessibilityNodeInfoCompat.ACTION_COLLAPSE -> "ACTION_COLLAPSE"
        AccessibilityNodeInfoCompat.ACTION_SET_TEXT -> "ACTION_SET_TEXT"
        R.id.accessibilityActionScrollUp -> "ACTION_SCROLL_UP"
        R.id.accessibilityActionScrollLeft -> "ACTION_SCROLL_LEFT"
        R.id.accessibilityActionScrollDown -> "ACTION_SCROLL_DOWN"
        R.id.accessibilityActionScrollRight -> "ACTION_SCROLL_RIGHT"
        R.id.accessibilityActionPageDown -> "ACTION_PAGE_DOWN"
        R.id.accessibilityActionPageUp -> "ACTION_PAGE_UP"
        R.id.accessibilityActionPageLeft -> "ACTION_PAGE_LEFT"
        R.id.accessibilityActionPageRight -> "ACTION_PAGE_RIGHT"
        R.id.accessibilityActionShowOnScreen -> "ACTION_SHOW_ON_SCREEN"
        R.id.accessibilityActionScrollToPosition -> "ACTION_SCROLL_TO_POSITION"
        R.id.accessibilityActionContextClick -> "ACTION_CONTEXT_CLICK"
        R.id.accessibilityActionSetProgress -> "ACTION_SET_PROGRESS"
        R.id.accessibilityActionMoveWindow -> "ACTION_MOVE_WINDOW"
        R.id.accessibilityActionShowTooltip -> "ACTION_SHOW_TOOLTIP"
        R.id.accessibilityActionHideTooltip -> "ACTION_HIDE_TOOLTIP"
        else -> "ACTION_UNKNOWN"
    }
}

fun AccessibilityNodeInfoCompat.actionStr(builder: StringBuilder) {
    builder.append("[")
    if (Build.VERSION.SDK_INT >= 21) {
        val actions = actionList
        for (i in actions.indices) {
            val action = actions[i]
            var actionName = action.id.actionSymbolicName()
            if (actionName == "ACTION_UNKNOWN" && action.label != null) {
                actionName = action.label.toString()
            }
            builder.append(actionName)
            if (i != actions.size - 1) {
                builder.append(", ")
            }
        }
    } else {
        var actionBits = actions
        while (actionBits != 0) {
            val action = 1 shl Integer.numberOfTrailingZeros(actionBits)
            actionBits = actionBits and action.inv()
            builder.append(action.actionSymbolicName())
            if (actionBits != 0) {
                builder.append(", ")
            }
        }
    }
    builder.append("]")
}