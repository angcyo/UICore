package com.angcyo.ilayer.container

import android.graphics.Rect
import com.angcyo.ilayer.ILayer

/**
 * 包裹[ILayer]的容器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

interface IContainer {

    /**添加一个[ILayer]到容器, 如果已经add了, 则会触发init方法*/
    fun add(layer: ILayer)

    /**从容器中移除一个[ILayer]*/
    fun remove(layer: ILayer)

    /**更新[ILayer]在容器中的位置. 使用[gravity]和边界比例[offsetX] [offsetY]控制位置*/
    fun update(layer: ILayer, position: OffsetPosition)

    /**获取容器的矩形坐标*/
    fun getContainerRect(): Rect
}