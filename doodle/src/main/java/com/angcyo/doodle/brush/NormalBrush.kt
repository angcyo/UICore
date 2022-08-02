package com.angcyo.doodle.brush

/**
 * 普通的笔刷
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/30
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class NormalBrush : PenBrush() {

    init {
        //去掉贝塞尔曲线效果
        enableBezier = false
    }
}