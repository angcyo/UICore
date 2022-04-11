package com.angcyo.core.widget

import android.view.View
import androidx.annotation.IdRes
import com.angcyo.widget.DslViewHolder
import com.angcyo.library.ex.Anim

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/25
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**[triggerViewId] 触发控件id
 *[arrowViewId] 箭头控件id
 * [isExpand] 当前是否展开状态
 * [expandLayoutId] 展开时的布局id
 * [rotation] 展开后箭头旋转的角度, 为null不进行设置
 * */
fun DslViewHolder.initExpand(
    @IdRes triggerViewId: Int,
    @IdRes arrowViewId: Int,
    isExpand: Boolean,
    @IdRes expandLayoutId: Int = View.NO_ID,
    defRotation: Float = 0f, /*默认状态时的旋转角度*/
    rotation: Float = 90f, /*展示时, 需要动画旋转的角度*/
    payloads: Boolean = false, /*负载, 在RV中请设置为true*/
    doExpand: (expand: Boolean) -> Boolean = { false } /*是否可以[expand]回调, 返回true表示可以进行操作*/
) {
    if (!payloads) {
        view(arrowViewId)?.rotation = if (isExpand) defRotation + rotation else defRotation
    }
    visible(expandLayoutId, isExpand)

    click(triggerViewId) {
        if (!doExpand(!isExpand)) {
            view(arrowViewId)?.animate()?.apply {
                if (isExpand) {
                    rotationBy(-rotation)
                } else {
                    rotationBy(rotation)
                }
                duration = Anim.ANIM_DURATION
                start()
            }
            initExpand(
                triggerViewId,
                arrowViewId,
                !isExpand,
                expandLayoutId,
                defRotation,
                rotation,
                payloads = true,
                doExpand
            )
        }
    }
}


