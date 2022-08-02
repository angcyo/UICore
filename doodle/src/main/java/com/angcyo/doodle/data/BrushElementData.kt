package com.angcyo.doodle.data

import android.graphics.Path

/**
 * 笔刷需要的绘制数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class BrushElementData : BaseElementData() {

    /**所有原始点位集合*/
    var brushPointList: List<TouchPoint>? = null

    /**算法处理之后的路径*/
    var brushPath: Path? = Path()
}