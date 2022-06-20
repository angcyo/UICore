package com.angcyo.item

import android.view.Gravity
import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.CheckGroupItemConfig
import com.angcyo.item.style.ICheckGroupItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.library.ex.dpi
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.layout.DslFlowLayout

/**
 * 一组选项
 * 支持直接界面操作单选/多选的item
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslCheckGroupItem : DslAdapterItem(), ILabelItem, ICheckGroupItem {

    /**单行等宽模式*/
    var itemSingleLineEquWidth = false

    /**Gravity*/
    var itemFlowLayoutGravity: Int = Gravity.CENTER_HORIZONTAL

    /**分割线的大小*/
    var itemFlowDividerHorizontalSize: Int = 1

    var itemFlowDividerVerticalSize: Int = 1

    /**间隙的大小*/
    var itemFlowHorizontalSpace: Int = 22 * dpi

    var itemFlowVerticalSpace: Int = 10 * dpi

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    override var checkGroupItemConfig: CheckGroupItemConfig = CheckGroupItemConfig()

    init {
        itemLayoutId = R.layout.dsl_check_group_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun _initItemConfig(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.v<DslFlowLayout>(checkGroupItemConfig.itemCheckGroupViewId)
            ?.apply {
                gravity = itemFlowLayoutGravity

                flowLayoutDelegate.itemEquWidth = itemSingleLineEquWidth
                flowLayoutDelegate.singleLine = itemSingleLineEquWidth

                flowLayoutDelegate.dividerHorizontalSize = itemFlowDividerHorizontalSize
                flowLayoutDelegate.dividerVerticalSize = itemFlowDividerVerticalSize

                flowLayoutDelegate.itemHorizontalSpace = itemFlowHorizontalSpace
                flowLayoutDelegate.itemVerticalSpace = itemFlowVerticalSpace
            }
        super._initItemConfig(itemHolder, itemPosition, adapterItem, payloads)
    }

    /**是否需要拦截选中*/
    override fun onCheckInterceptSelectView(
        itemView: View,
        index: Int,
        select: Boolean,
        fromUser: Boolean
    ): Boolean {
        return super.onCheckInterceptSelectView(itemView, index, select, fromUser)
    }

    /**选中后的view改变的回调*/
    override fun onCheckSelectViewChange(
        fromView: View?,
        selectViewList: List<View>,
        reselect: Boolean,
        fromUser: Boolean
    ) {
        super.onCheckSelectViewChange(fromView, selectViewList, reselect, fromUser)
    }

    /**选中后的index改变的回调*/
    override fun onCheckSelectIndexChange(
        fromIndex: Int,
        selectIndexList: List<Int>,
        reselect: Boolean,
        fromUser: Boolean
    ) {
        super.onCheckSelectIndexChange(fromIndex, selectIndexList, reselect, fromUser)
    }
}