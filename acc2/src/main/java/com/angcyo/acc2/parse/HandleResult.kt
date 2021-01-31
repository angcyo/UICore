package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/**
 * 解析, 处理 结果对象
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class HandleResult : BaseResult() {

    /**被处理的元素*/
    var nodeList: List<AccessibilityNodeInfoCompat>? = null

}