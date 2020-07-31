package com.angcyo.core.component.accessibility

import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/31
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class ILogPrint {
    open fun log(msg: CharSequence?) {
        L.v(msg)
    }
}