package com.angcyo.acc2.control

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.library.L
import com.angcyo.library.ex.des
import com.angcyo.library.ex.size
import com.angcyo.library.ex.wrapLog

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AccPrint(val accControl: AccControl? = null) {

    /**日志是否保存到内存*/
    var memory: Boolean = false

    var memoryLogCache: MutableList<String>? = null

    /**输出日志*/
    open fun log(log: String?) {
        if (memory) {
            if (memoryLogCache == null) {
                memoryLogCache = mutableListOf()
            }
            log?.let {
                memoryLogCache?.add(it)
            }
            if (memoryLogCache.size() > 100) {
                memoryLogCache?.removeFirstOrNull()
            }
        }
        if (log?.startsWith(" ") == true) {
            L.d(log)
        } else {
            L.i(log)
        }
    }

    /**下一个步骤的提示
     * [title] 步骤的标题
     * [des] 步骤的描述
     * [time] 执行步骤的延迟, 毫秒
     * */
    open fun next(actionBean: ActionBean, time: Long) {
        L.w("next[$time]->${actionBean.title}${(actionBean.summary ?: actionBean.des).des()}".wrapLog())
    }

    /**等待状态, 闪烁提示*/
    open fun sleep(time: Long) {
        //等待
    }

    /**匹配到了元素*/
    open fun findNode(nodeList: List<AccessibilityNodeInfoCompat>?) {

    }

    /**处理元素
     * [AppAccLog]中显示了节点提示矩形框
     * [AccTouchTipLayer]
     * [com.angcyo.acc2.app.showNodeRect]
     * [com.angcyo.acc2.parse.HandleParse.handleAction]
     * */
    open fun handleNode(nodeList: List<AccessibilityNodeInfoCompat>?) {

    }

    /**手势操作
     * [com.angcyo.acc2.app.AppAccPrint.touch]
     * [com.angcyo.acc2.app.component.AccTouchTipLayer.showTouch]*/
    open fun touch(x1: Float, y1: Float, x2: Float?, y2: Float?) {

    }
}