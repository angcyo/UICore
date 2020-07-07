package com.angcyo.core.component.accessibility.parse

import android.accessibilityservice.AccessibilityService
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.library.ex.isListEmpty

/**
 * 参数严格的约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ConstraintBean(

    /**约束的文本, 这个文本可以是对应的id, 或者node上的文本内容
     * 文本需要全部命中
     * */
    var text: List<String>? = null,

    /**类名约束, 和[text]为一一对应的关系.为空, 表示不约束类名
     * 匹配规则时包含, 只要当前设置的cls包含视图中的cls就算命中
     * */
    var cls: List<String>? = null
)

/**查找满足约束的Node*/
fun ConstraintBean.findConstraintNode(
    result: MutableList<AccessibilityNodeInfoCompat> = mutableListOf(),
    isIdText: Boolean = false, //完整id 是需要包含包名的
    service: AccessibilityService
): List<AccessibilityNodeInfoCompat> {
    val rootNodeInfo = service.rootNodeInfo() ?: return result
    if (text.isListEmpty()) {
        return result
    }
    val packageName = service.packageName
    val text = text!!

    for (index: Int in text.indices) {
        try {
            val subText: String = if (isIdText) packageName.id(text[index]) else text[index]
            rootNodeInfo.findNode(result) {
                if (isIdText) {
                    //id 全等匹配
                    val idName = it.viewIdName()
                    if (subText == idName) {
                        if (match(it, index)) {
                            1
                        } else {
                            -1
                        }
                    } else {
                        -1
                    }

                } else {
                    //文本包含匹配
                    if (it.haveText(subText)) {
                        if (match(it, index)) {
                            1
                        } else {
                            -1
                        }
                    } else {
                        -1
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

/**其他规则匹配*/
fun ConstraintBean.match(node: AccessibilityNodeInfoCompat, index: Int): Boolean {
    val cls = cls?.getOrNull(index)
    return if (cls != null && !cls.contains(node.className)) {
        //但是类名不同
        false
    } else {
        //类名命中
        if (true) {
            //other
        }
        true
    }
}

