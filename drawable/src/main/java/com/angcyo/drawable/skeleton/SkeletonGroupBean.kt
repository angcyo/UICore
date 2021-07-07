package com.angcyo.drawable.skeleton

import android.widget.LinearLayout

/**
 * 管理[SkeletonBean], 类似于[ViewGroup]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

data class SkeletonGroupBean(

    /**需要绘制的骨架*/
    var skeletonList: List<SkeletonBean>? = null,

    //------------------------------------------

    /**套娃, 当存在[groupList]时, [skeletonList]将会被忽略*/
    var groupList: List<SkeletonGroupBean>? = null,

    /**组内元素的排列方向
     * [LinearLayout.HORIZONTAL]
     * [LinearLayout.VERTICAL]*/
    var orientation: Int = -1
)

/*----------------------------------------Group------------------------------------------------*/

/**追加一个新的[SkeletonGroupBean]*/
fun SkeletonGroupBean.group(
    orientation: Int = -1,
    dsl: SkeletonGroupBean.() -> Unit
): SkeletonGroupBean {
    val list = groupList
    val groupBean = SkeletonGroupBean(orientation = orientation).apply(dsl)
    if (list is MutableList) {
        list.add(groupBean)
    } else {
        groupList = mutableListOf(groupBean)
    }
    return groupBean
}

fun SkeletonGroupBean.horizontal(dsl: SkeletonGroupBean.() -> Unit) {
    group(LinearLayout.HORIZONTAL, dsl)
}

fun SkeletonGroupBean.vertical(dsl: SkeletonGroupBean.() -> Unit) {
    group(LinearLayout.VERTICAL, dsl)
}

fun SkeletonGroupBean.clear() {
    skeletonList = null
    groupList = null
}

/*--------------------------------------Skeleton-----------------------------------------------*/

/**追加一个新的[SkeletonBean]*/
fun SkeletonGroupBean.skeleton(dsl: SkeletonBean.() -> Unit) {
    val list = skeletonList
    if (list is MutableList) {
        list.add(SkeletonBean().apply(dsl))
    } else {
        skeletonList = mutableListOf(SkeletonBean().apply(dsl))
    }
}

fun SkeletonGroupBean.line(dsl: SkeletonBean.() -> Unit) {
    skeleton {
        type = SkeletonBean.SKELETON_TYPE_LINE
        dsl()
    }
}

fun SkeletonGroupBean.circle(radius: String = "0.025", dsl: SkeletonBean.() -> Unit) {
    skeleton {
        type = SkeletonBean.SKELETON_TYPE_CIRCLE
        size = radius
        dsl()
    }
}

fun SkeletonGroupBean.rect(roundSize: String = "5dp", dsl: SkeletonBean.() -> Unit) {
    skeleton {
        type = SkeletonBean.SKELETON_TYPE_RECT
        size = roundSize
        dsl()
    }
}

fun SkeletonGroupBean.rectStroke(stroke: String = "5dp", dsl: SkeletonBean.() -> Unit) {
    skeleton {
        type = SkeletonBean.SKELETON_TYPE_RECT_STROKE
        size = stroke
        dsl()
    }
}