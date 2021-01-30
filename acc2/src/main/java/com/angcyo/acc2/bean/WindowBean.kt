package com.angcyo.acc2.bean

/**
 * 选择对应的[AccessibilityWindowInfo], 所有条件必须都满足.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class WindowBean(
    /**通过包名, 选择window,
     * 支持正则*/
    var packageName: String? = null,
)