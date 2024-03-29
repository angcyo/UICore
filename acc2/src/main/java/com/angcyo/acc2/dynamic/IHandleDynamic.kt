package com.angcyo.acc2.dynamic

import androidx.annotation.Keep
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.handleResult
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult

/**
 * 直接接管[com.angcyo.acc2.bean.HandleBean]的处理
 *
 * [com.angcyo.acc2.bean.HandleBean.handleClsList]
 *
 * [com.angcyo.acc2.parse.HandleParse.parse]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

@Keep
interface IHandleDynamic {

    /**
     * [originList] 最原始的节点集合
     * [handleNodeList] [handleBean]处理过后的节点集合
     * */
    fun handle(
        control: AccControl,
        controlContext: ControlContext,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleBean: HandleBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>?,
    ): HandleResult = handleResult { }
}