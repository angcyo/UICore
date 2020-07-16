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
    var offsetX: Float = 0f,
    var offsetY: Float = 0.3f
)