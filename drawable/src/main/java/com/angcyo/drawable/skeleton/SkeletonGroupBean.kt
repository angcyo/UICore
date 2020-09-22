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
    /**套娃*/
    var groupList: List<SkeletonGroupBean>? = null,

    /**组的排列方向*/
    var orientation: Int = -1,

    /**[Group]的定位数据, 小于1f,表示比例(参考值是控件宽高); 否则就是dp*/
    var groupLeft: Float = 0f,
    var groupTop: Float = 0f,
    var groupWidth: Float = 0f,
    var groupHeight: Float = 0f
)

/*----------------------------------------Group------------------------------------------------*/

/**追加一个新的[SkeletonGroupBean]*/
fun SkeletonGroupBean.group(
    orientation: Int = LinearLayout.VERTICAL,
    dsl: SkeletonGroupBean.() -> Unit
) {
    val list = groupList
    if (list is MutableList) {
        list.add(SkeletonGroupBean(orientation = orientation).apply(dsl))
    } else {
        groupList = mutableListOf(SkeletonGroupBean(orientation = orientation).apply(dsl))
    }
}

fun SkeletonGroupBean.horizontal(dsl: SkeletonGroupBean.() -> Unit) {
    group(LinearLayout.HORIZONTAL, dsl)
}

fun SkeletonGroupBean.vertical(dsl: SkeletonGroupBean.() -> Unit) {
    group(LinearLayout.VERTICAL, dsl)
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

fun SkeletonGroupBean.circle(dsl: SkeletonBean.() -> Unit) {
    skeleton {
        type = SkeletonBean.SKELETON_TYPE_CIRCLE
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