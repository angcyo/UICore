package com.angcyo.dialog

import android.app.Dialog
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.IScrollBehaviorListener
import com.angcyo.behavior.effect.TouchBackBehavior
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.behavior
import com.angcyo.widget.base.mH

/**
 *
 * 下拉返回对话框基类
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/25
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface ITouchBackDialogConfig {

    /**获取[TouchBackBehavior]*/
    fun touchBackBehavior(dialogViewHolder: DslViewHolder): TouchBackBehavior? {
        var touchBackBehavior: TouchBackBehavior? = null
        dialogViewHolder.view(R.id.touch_back_layout).behavior()?.apply {
            if (this is TouchBackBehavior) {
                touchBackBehavior = this
            }
        }
        return touchBackBehavior
    }

    /**初始化*/
    fun initTouchBackLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        //暗淡量，从 0 表示没有暗淡到 1 表示完全暗淡。
        val dimAmount = dialog.window?.attributes?.dimAmount ?: 1f
        dialogViewHolder.itemView.setTag(R.id.lib_tag_temp, dimAmount)

        touchBackBehavior(dialogViewHolder)?.apply {
            addScrollListener(object : IScrollBehaviorListener {
                override fun onBehaviorScrollTo(
                    scrollBehavior: BaseScrollBehavior<*>,
                    x: Int, y: Int, scrollType: Int
                ) {
                    //L.i("-> x:$x y:$y")
                    val maxY = childView.mH()
                    onTouchBackTo(dialog, dialogViewHolder, y, maxY)

                    //关闭对话框
                    if (y >= maxY) {
                        dialog.cancel()
                    }
                }
            })
        }
    }

    /**下拉过程回调*/
    fun onTouchBackTo(dialog: Dialog, holder: DslViewHolder, y: Int, maxY: Int) {
        val dimAmount: Float = holder.itemView.getTag(R.id.lib_tag_temp) as Float
        dialog.window?.setDimAmount(dimAmount - dimAmount * y / maxY)
    }

    /**主动back*/
    fun touchBack(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        touchBackBehavior(dialogViewHolder) ?: dialog.cancel()
    }

}