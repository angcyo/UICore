package com.angcyo.ilayer.container

import android.view.Gravity

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class OffsetPosition(

    var gravity: Int = Gravity.LEFT or Gravity.TOP,

    /**偏移的比例*/
    var offsetX: Float = 0f,
    var offsetY: Float = 0.3f,

    /**偏移位置是相对于控件中心点的位置*/
    var reCenter: Boolean = false
)