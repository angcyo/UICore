package com.angcyo.canvas.core.items

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/10
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ICanvasItem {

    /**当前旋转的角度, 在[CanvasView]中处理此属性
     * [com.angcyo.canvas.CanvasView.onDraw]*/
    var rotate: Float
}