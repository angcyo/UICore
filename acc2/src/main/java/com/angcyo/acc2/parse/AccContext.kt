package com.angcyo.acc2.parse

import android.graphics.Rect
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AccContext {

    /**比例计算限制*/
    var bounds: Rect? = null

    /**查找到多少个节点后, 中断遍历查找, 提升效率
     * 小数, 表示不开启*/
    var findLimit: Int = -1

    /**递归查找深度, 从1开始的*/
    var findDepth: Int = -1

    /**坐标计算时, 比例计算时的参考矩形*/
    fun getBound(): Rect {
        return bounds ?: Rect(0, 0, _screenWidth, _screenHeight)
    }
}