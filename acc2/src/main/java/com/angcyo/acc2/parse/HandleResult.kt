package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.HandleBean

/**
 * 解析, 处理 结果对象
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class HandleResult : BaseResult() {

    /**强制执行结果为失败
     * 进行失败的处理, 但是保留成功的状态
     * [com.angcyo.acc2.action.FalseAction]*/
    var forceFail: Boolean = false

    /**
     * 强制执行成功, 这个成功的状态, 可以传递给正在调度的[ActionBean]
     * [com.angcyo.acc2.action.TrueAction]*/
    var forceSuccess: Boolean = false

    /**处理成功的结构数据*/
    var handleBean: HandleBean? = null

    /**被处理的元素*/
    var nodeList: List<AccessibilityNodeInfoCompat>? = null

    override fun copyTo(target: BaseResult) {
        super.copyTo(target)
        if (target is HandleResult) {
            target.forceFail = forceFail
            target.forceSuccess = forceSuccess
            target.handleBean = handleBean
            target.nodeList = nodeList
        }
    }

}