package com.angcyo.acc2

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.TaskBean
import com.angcyo.library.ex.des
import com.angcyo.library.ex.getChildOrNull

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**枚举所有子节点[AccessibilityNodeInfoCompat]
 * [each] 返回是否中断查询, 返回true中断each, 返回false继续
 * [depth] 递归深度, 从1开始
 * */
fun AccessibilityNodeInfoCompat.eachChildDepth(each: (node: AccessibilityNodeInfoCompat, depth: Int) -> Boolean) {
    eachChildDepth(1, each)
}

fun AccessibilityNodeInfoCompat.eachChildDepth(
    depth: Int,
    each: (node: AccessibilityNodeInfoCompat, depth: Int) -> Boolean
): Boolean {
    var result = false
    if (depth <= 1) {
        //递归顶级开始节点
        result = each(this, depth)
        if (result) {
            return true
        }
    }

    for (i in 0 until childCount) {
        val child = getChildOrNull(i)
        if (child != null) {
            result = each(child, depth)
            if (result) {
                break
            } else {
                if (child.childCount > 0) {
                    result = child.eachChildDepth(depth + 1, each)
                }
            }
            if (result) {
                break
            }
        }
    }
    return result
}

fun TaskBean.log() = buildString {
    append(title)
    append("($taskId)")
    append(des.des())
}
