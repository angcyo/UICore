package com.angcyo.acc2.control

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.library.L
import com.angcyo.library.ex.des
import com.angcyo.library.ex.wrapLog

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AccPrint(val accControl: AccControl? = null) {

    /**输出日志*/
    open fun log(log: String?) {
        L.i(log)
    }

    /**下一个步骤的提示
     * [title] 步骤的标题
     * [des] 步骤的描述
     * [time] 执行步骤的延迟, 毫秒
     * */
    open fun next(title: String?, des: String?, time: Long) {
        L.w("next[$time]->$title${des.des()}".wrapLog())
    }

    /**匹配到了元素*/
    open fun findNode(nodeList: List<AccessibilityNodeInfoCompat>?) {

    }

    /**处理元素*/
    open fun handleNode(nodeList: List<AccessibilityNodeInfoCompat>?) {

    }
}