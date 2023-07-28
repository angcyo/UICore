package com.angcyo.item.keyboard

import android.content.Context
import android.view.View
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.popup.ShadowAnchorPopupConfig
import com.angcyo.item.R
import com.angcyo.library.annotation.DSL
import com.angcyo.library.component.hawk.HawkPropertyValue
import com.angcyo.library.ex.indexOfChild
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.widget.DslViewHolder

/**
 * 方向键盘微调控制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DirectionAdjustPopupConfig : ShadowAnchorPopupConfig() {

    companion object {
        /**方向键盘微调控制, 上下左右*/
        const val DIRECTION_UP = 1
        const val DIRECTION_DOWN = 2
        const val DIRECTION_LEFT = 3
        const val DIRECTION_RIGHT = 4

        /**设备居中*/
        const val DIRECTION_CENTER = 5

        /**顺时针/逆时针旋转*/
        const val DIRECTION_ROTATE_CW = 6
        const val DIRECTION_ROTATE_CCW = 7
    }

    /**方向改变回调, */
    var onDirectionAdjustAction: (direction: Int /*方向*/, step: Float /*步长*/) -> Unit =
        { _, _ -> }

    /**微调的步长, [1,2,5]*/
    var modifyStep: Float by HawkPropertyValue<Any, Float>(1f)

    /**是否要显示居中调整按钮*/
    var showDirectionCenterButton: Boolean = false

    init {
        contentLayoutId = R.layout.lib_keyboard_direction_modify_layout
        //onDismiss //
    }

    override fun initContentLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        super.initContentLayout(window, viewHolder)
        //步长选择
        viewHolder.v<DslTabLayout>(R.id.lib_tab_layout)?.apply {
            setCurrentItem(indexOfChild {
                tag?.toString()?.toFloatOrNull() == modifyStep
            } ?: 0)
            configTabLayoutConfig {
                //选中回调
                onSelectViewChange = { fromView, selectViewList, reselect, fromUser ->
                    selectViewList.firstOrNull()?.tag?.toString()?.toFloatOrNull()?.let {
                        modifyStep = it
                    }
                }
            }
        }
        viewHolder.visible(R.id.lib_direction_center_view, showDirectionCenterButton)
        //方向键盘微调控制
        viewHolder.click(R.id.lib_direction_up_view) {
            onDirectionAdjustAction(DIRECTION_UP, modifyStep)
        }
        viewHolder.click(R.id.lib_direction_down_view) {
            onDirectionAdjustAction(DIRECTION_DOWN, modifyStep)
        }
        viewHolder.click(R.id.lib_direction_left_view) {
            onDirectionAdjustAction(DIRECTION_LEFT, modifyStep)
        }
        viewHolder.click(R.id.lib_direction_right_view) {
            onDirectionAdjustAction(DIRECTION_RIGHT, modifyStep)
        }
        //
        viewHolder.click(R.id.lib_direction_center_view) {
            onDirectionAdjustAction(DIRECTION_CENTER, modifyStep)
        }
        viewHolder.click(R.id.lib_direction_rotate_cw_view) {
            onDirectionAdjustAction(DIRECTION_ROTATE_CW, modifyStep)
        }
        viewHolder.click(R.id.lib_direction_rotate_ccw_view) {
            onDirectionAdjustAction(DIRECTION_ROTATE_CCW, modifyStep)
        }
    }
}


/**Dsl
 * [com.angcyo.item.keyboard.DirectionAdjustPopupConfig.onDirectionAdjustAction]*/
@DSL
fun Context.directionAdjustWindow(
    anchor: View?,
    config: DirectionAdjustPopupConfig.() -> Unit
): TargetWindow {
    val popupConfig = DirectionAdjustPopupConfig()
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
}