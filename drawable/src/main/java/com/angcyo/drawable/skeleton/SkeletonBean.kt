package com.angcyo.drawable.skeleton

import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toColorInt
import com.angcyo.library.utils.getFloatNum

/**
 * 骨架最小单位绘制结构, 类似于[View]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class SkeletonBean(
    var type: Int = -1,
    /**线的宽度或者圆的半径或者圆角大小*/
    var size: String? = null,
    var fillColor: Int = "#E7E7E7".toColorInt(),
    /**[Bean]的定位数据, 小于1f,表示比例; 否则就是dp; 如果是圆的话, 表示圆心的坐标*/
    var left: String? = null,//相对于当前[SkeletonGroupBean]的左边距离
    var top: String? = null,
    var width: String? = null,
    var height: String? = null,

    /**绘制时的偏移*/
    var offsetX: String? = null,
    var offsetY: String? = null,

    /**矩形时的圆角大小*/
    var round: String? = null,

    //计算之后的属性值
    var _left: Float = -1f,
    var _top: Float = -1f,
    var _width: Float = -1f,
    var _height: Float = -1f,
    var _size: Float = -1f,
    var _offsetX: Float = -1f,
    var _offsetY: Float = -1f,
    var _round: Float = -1f,
) {
    companion object {
        const val SKELETON_TYPE_LINE = 1 //绘制线
        const val SKELETON_TYPE_CIRCLE = 2 //绘制圆
        const val SKELETON_TYPE_RECT = 4 //绘制矩形
    }
}

/**
 * 支持格式
 * 0.1w
 * 0.3h
 * 10
 * 20
 * */
fun String?.layoutSize(
    refWidth: Int = _screenWidth,
    refHeight: Int = _screenHeight,
    defRef: Int = _screenWidth
): Float {
    if (this.isNullOrEmpty()) {
        return 0f
    }

    var ref = defRef
    if (contains("w") || contains("W")) {
        ref = refWidth
    } else if (contains("h") || contains("H")) {
        ref = refHeight
    }

    val num = this.getFloatNum() ?: return 0f

    return if (num < 1f) {
        num * ref
    } else {
        if (contains("dp")) {
            num * dp
        } else {
            num
        }
    }
}