package com.angcyo.acc2.parse

import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.WindowBean
import com.angcyo.acc2.core.BaseAccService
import com.angcyo.library.ex.have
import com.angcyo.library.ex.wrap

/**
 * 查找元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FindParse(val accParse: AccParse) {

    fun findWindowBy(packageName: String?): List<AccessibilityWindowInfo> {
        val result = mutableListOf<AccessibilityWindowInfo>()
        if (!packageName.isNullOrEmpty()) {
            BaseAccService.lastService?.windows?.forEach {
                if (it.root?.packageName?.have(packageName) == true) {
                    result.add(it)
                }
            }
        }
        return result
    }

    fun findRootNodeBy(packageName: String?): List<AccessibilityNodeInfoCompat>? {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (packageName == null) {
            //无包名字段, 不处理
            return null
        } else if (packageName.isEmpty()) {
            //如果包名为空字符, 则支持所有window
            BaseAccService.lastService?.windows?.forEach {
                it.root?.let { node -> result.add(node.wrap()) }
            }
            return result
        } else {
            findWindowBy(packageName).forEach { window -> result.add(window.root.wrap()) }
        }
        return result
    }

    /**根据[WindowBean]的描述, 获取根节点集合*/
    fun findRootNode(windowBean: WindowBean?): List<AccessibilityNodeInfoCompat>? {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (windowBean == null) {
            //未指定, 采用默认处理
            val taskPackageName = accParse.accControl._taskBean?.packageName
            return findRootNodeBy(taskPackageName)
        } else {
            //解析window约束
            if (windowBean.packageName != null) {
                //指定了要重新获取根节点
                result.addAll(findRootNodeBy(windowBean.packageName) ?: emptyList())
            }
        }
        return result
    }
}