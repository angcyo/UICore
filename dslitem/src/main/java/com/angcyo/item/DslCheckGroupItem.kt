package com.angcyo.item

import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.find
import com.angcyo.library.ex.string
import com.angcyo.library.ex.toStr
import com.angcyo.library.extend.IToText
import com.angcyo.library.extend.IToValue
import com.angcyo.tablayout.DslSelector
import com.angcyo.tablayout.logi
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.resetChild
import com.angcyo.widget.layout.DslFlowLayout

/**
 *
 * 支持直接界面操作单选/多选的item
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslCheckGroupItem : DslAdapterItem(), ILabelItem {

    /**选项列表*/
    var itemCheckItems = listOf<Any>()

    /**选中的列表*/
    var itemCheckedItems = mutableListOf<Any>()

    //内部使用
    var _itemCheckedIndexList = mutableListOf<Int>()

    /**将选项[item], 转成可以显示在界面的 文本类型*/
    var itemCheckItemToText: (item: Any) -> CharSequence? = { item ->
        if (item is IToText) {
            item.toText()
        } else {
            item.string()
        }
    }

    /**将选项[item], 转成表单上传的数据*/
    var itemCheckItemToValue: (item: Any) -> Any? = { item ->
        if (item is IToValue) {
            item.toValue()
        } else {
            item.toStr()
        }
    }

    /**选项布局*/
    var itemCheckLayoutId: Int = R.layout.layout_check

    /**单选/多选支持*/
    val itemSelectorHelper = DslSelector()

    /**是否是多选模式*/
    var itemMultiMode = false

    /**多选时, 最小选中数量*/
    var itemMinSelectLimit = 0

    /**单行等宽模式*/
    var itemSingleLineEquWidth = false

    var itemFirstNotifyChange = true

    /**Gravity*/
    var itemFlowLayoutGravity: Int = Gravity.CENTER_HORIZONTAL

    /**分割线的大小*/
    var itemFlowDividerHorizontalSize: Int = 1

    var itemFlowDividerVerticalSize: Int = 1

    /**间隙的大小*/
    var itemFlowHorizontalSpace: Int = 22 * dpi

    var itemFlowVerticalSpace: Int = 10 * dpi

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

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

        itemHolder.v<DslFlowLayout>(R.id.lib_flow_layout)
            ?.apply {

                gravity = itemFlowLayoutGravity

                flowLayoutDelegate.itemEquWidth = itemSingleLineEquWidth
                flowLayoutDelegate.singleLine = itemSingleLineEquWidth

                flowLayoutDelegate.dividerHorizontalSize = itemFlowDividerHorizontalSize
                flowLayoutDelegate.dividerVerticalSize = itemFlowDividerVerticalSize

                flowLayoutDelegate.itemHorizontalSpace = itemFlowHorizontalSpace
                flowLayoutDelegate.itemVerticalSpace = itemFlowVerticalSpace

                resetChild(itemCheckItems.size, itemCheckLayoutId) { itemView, itemIndex ->
                    val item = itemCheckItems[itemIndex]
                    itemView.tag = itemCheckItemToValue(item)  //保存数据
                    itemView.find<TextView>(R.id.lib_text_view)?.text =
                        itemCheckItemToText(item)
                }

                /**安装选择组件*/
                itemSelectorHelper.install(this) {
                    dslMultiMode = itemMultiMode
                    dslMinSelectLimit = if (itemMultiMode) itemMinSelectLimit else 1

                    onSelectItemView = this@DslCheckGroupItem::onCheckInterceptSelectView
                    onSelectViewChange = this@DslCheckGroupItem::onCheckSelectViewChange
                    onSelectIndexChange = this@DslCheckGroupItem::onCheckSelectIndexChange
                }

                val indexList = mutableListOf<Int>()
                itemCheckedItems.forEach {
                    indexList.add(itemCheckItems.indexOf(it))
                }
                itemSelectorHelper.selector(indexList, fromUser = itemFirstNotifyChange)
                itemFirstNotifyChange = false
            }
    }

    /**是否需要拦截选中*/
    open fun onCheckInterceptSelectView(
        itemView: View,
        index: Int,
        select: Boolean,
        fromUser: Boolean
    ): Boolean {
        return false
    }

    /**选中后的view改变的回调*/
    open fun onCheckSelectViewChange(
        fromView: View?,
        selectViewList: List<View>,
        reselect: Boolean,
        fromUser: Boolean
    ) {

    }

    /**选中后的index改变的回调*/
    open fun onCheckSelectIndexChange(
        fromIndex: Int,
        selectIndexList: List<Int>,
        reselect: Boolean,
        fromUser: Boolean
    ) {
        "选择:[$fromIndex]->${selectIndexList} reselect:$reselect fromUser:$fromUser".logi()

        _itemCheckedIndexList.clear()
        _itemCheckedIndexList.addAll(selectIndexList)

        //清空之前
        itemCheckedItems.clear()

        //当前选中项
        selectIndexList.forEach {
            itemCheckedItems.add(itemCheckItems[it])
        }

        //更新依赖
        if (fromUser) {
            itemChanging = true
        }
    }
}