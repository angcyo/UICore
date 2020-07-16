package com.angcyo.ilayer.container

import com.angcyo.ilayer.ILayer

/**
 * 拖拽约束, 控制[layer]的位置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IDragConstraint {

    /**正在拖拽*/
    fun onDragMoveTo(container: IContainer, layer: ILayer, gravity: Int, x: Int, y: Int) {

    }

    /**拖拽结束后, 回调. 可以控制[layer]在[container]中的位置*/
    fun onDragEnd(container: IContainer, layer: ILayer, position: OffsetPosition)
}