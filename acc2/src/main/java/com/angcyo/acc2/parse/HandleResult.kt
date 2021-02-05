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
     * 进行失败的处理, 但是保留成功的状态*/
    var forceFail: Boolean = false

    /**处理成功的结构数据*/
    var handleBean: HandleBean? = null

    /**被处理的元素*/
    var nodeList: List<AccessibilityNodeInfoCompat>? = null

}