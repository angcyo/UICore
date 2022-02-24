package com.angcyo.item

import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.findSameClassItem
import com.angcyo.item.style.DesItemConfig
import com.angcyo.item.style.IDesItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.after
import com.angcyo.library.ex.before
import com.angcyo.library.ex.toColor
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setBgDrawable
import com.angcyo.widget.base.visible

/**
 * 流程item, 上一步, 下一步, 当前步骤的状态提示item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslWorkflowItem : DslNestedRecyclerItem(), ILabelItem, IDesItem {

    companion object {
        /**流程未开始*/
        const val WORKFLOW_NOT_START = 1

        /**当前处理流程*/
        const val WORKFLOW_CURRENT = 2

        /**流程已完成*/
        const val WORKFLOW_PASS = 3

        /**流程的方向: 从上开始, 下结束*/
        const val WORKFLOW_ORIENTATION_TOP_TO_BOTTOM = 1

        /**流程的方向: 从下开始, 上结束*/
        const val WORKFLOW_ORIENTATION_BOTTOM_TO_TOP = 2
    }

    override var desItemConfig: DesItemConfig = DesItemConfig().apply {
        itemDesViewId = R.id.lib_work_flow_des
        itemDesStyle.goneOnTextEmpty = true
    }

    override var labelItemConfig: LabelItemConfig = LabelItemConfig().apply {
        itemLabelViewId = R.id.lib_work_flow_label
    }

    /**当前流程的状态*/
    var itemWorkflowStatus: Int = WORKFLOW_NOT_START

    /**流程的时间, 支持span*/
    var itemWorkflowTime: CharSequence? = null

    /**流程流动的方向, 从下到上, 从上道下*/
    var itemWorkflowOrientation: Int = WORKFLOW_ORIENTATION_BOTTOM_TO_TOP

    /**获取流程提示图标*/
    var onGetWorkflowStateDrawable: (currentState: Int) -> Drawable? = {
        when (it) {
            WORKFLOW_NOT_START -> _drawable(R.drawable.lib_work_flow_not_start)
            WORKFLOW_CURRENT -> _drawable(R.drawable.lib_work_flow_current)
            WORKFLOW_PASS -> _drawable(R.drawable.lib_work_flow_pass)
            else -> _drawable(R.drawable.lib_work_flow_not_start)
        }
    }

    /**获取状态提示线的颜色
     * [currentState] 当前流程的状态
     * [afterState] 下一个流程的状态*/
    var onGetWorkflowLineDrawable: (currentState: Int, afterState: Int) -> Drawable? =
        { currentState, afterState ->
            "#F2F2F2".toColor().toDrawable()
        }

    init {
        itemLayoutId = R.layout.dsl_work_flow_item
        itemBottomInsert = 0
        itemLeftOffset = 0
        onlyDrawOffsetArea = true
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //time
        itemHolder.tv(R.id.lib_work_flow_time)?.apply {
            text = itemWorkflowTime
            visible(itemWorkflowTime != null)
        }

        //流程提示图标
        itemHolder.img(R.id.lib_work_flow_status)
            ?.setImageDrawable(onGetWorkflowStateDrawable(itemWorkflowStatus))

        //开始计算
        itemDslAdapter?.findSameClassItem(this)?.apply {
            val beforeItem = before(this@DslWorkflowItem)
            val afterItem = after(this@DslWorkflowItem)

            itemHolder.gone(R.id.lib_work_flow_head_line, beforeItem == null)
            itemHolder.gone(R.id.lib_work_flow_footer_line, afterItem == null)

            if (itemWorkflowOrientation == WORKFLOW_ORIENTATION_TOP_TO_BOTTOM) {
                //上往下 流程
                if (beforeItem != null) {
                    //有头部线
                    itemHolder.view(R.id.lib_work_flow_head_line)?.setBgDrawable(
                        onGetWorkflowLineDrawable(
                            beforeItem.itemWorkflowStatus,
                            itemWorkflowStatus
                        )
                    )
                }
                if (afterItem != null) {
                    itemHolder.view(R.id.lib_work_flow_footer_line)?.setBgDrawable(
                        onGetWorkflowLineDrawable(
                            itemWorkflowStatus,
                            afterItem.itemWorkflowStatus
                        )
                    )
                }
            } else {
                //下往上 流程
                if (beforeItem != null) {
                    //有头部线
                    itemHolder.view(R.id.lib_work_flow_head_line)?.setBgDrawable(
                        onGetWorkflowLineDrawable(
                            itemWorkflowStatus,
                            beforeItem.itemWorkflowStatus
                        )
                    )
                }
                if (afterItem != null) {
                    itemHolder.view(R.id.lib_work_flow_footer_line)?.setBgDrawable(
                        onGetWorkflowLineDrawable(
                            afterItem.itemWorkflowStatus,
                            itemWorkflowStatus
                        )
                    )
                }
            }
        }
    }
}

var DslWorkflowItem.itemWorkflowLabel: CharSequence?
    get() = labelItemConfig.itemLabelText
    set(value) {
        labelItemConfig.itemLabelText = value
    }

var DslWorkflowItem.itemWorkflowDes: CharSequence?
    get() = desItemConfig.itemDes
    set(value) {
        desItemConfig.itemDes = value
    }
