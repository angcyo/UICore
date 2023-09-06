package com.angcyo.dialog2.dslitem

import android.content.Context
import android.view.View
import com.angcyo.dialog2.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.DslBaseLabelItem
import com.angcyo.item.style.ILoadItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.LoadItemConfig
import com.angcyo.item.style.TextItemConfig
import com.angcyo.library.ex.ResultThrowable
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelWheelItem : DslBaseLabelItem(), ITextItem, ILoadItem, IWheelItem {

    /**点击item之前拦截处理, 返回true拦截默认处理*/
    var itemClickBefore: (clickView: View) -> Boolean = { false }

    override var textItemConfig: TextItemConfig = TextItemConfig()

    override var loadItemConfig: LoadItemConfig = LoadItemConfig()

    override var wheelItemConfig: WheelItemConfig = WheelItemConfig()

    init {
        itemLayoutId = R.layout.dsl_wheel_item

        itemClick = { view ->
            if (itemEnable && !itemClickBefore(view)) {

                val itemLoadAction = loadItemConfig.itemLoadAction
                if (itemLoadAction == null) {
                    showWheelDialog(view.context)
                } else {
                    if (loadItemConfig.itemUseLoadCache && !itemWheelList.isNullOrEmpty()) {
                        showWheelDialog(view.context)
                    } else {
                        //开始异步加载
                        startItemLoading {
                            //加载之后的回调
                            if (it == null) {
                                showWheelDialog(view.context)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.tv(textItemConfig.itemTextViewId)?.apply {
            text = itemWheelList?.getOrNull(itemSelectedIndex)?.run {
                if (itemWheelUnit == null) {
                    itemWheelToTextAction(this)
                } else {
                    "${itemWheelToTextAction(this)}${itemWheelUnit}"
                }
            } ?: textItemConfig.itemText //默认文本
        }
        itemHolder.visible(R.id.lib_right_ico_view, itemEnable)
    }

    /**显示dialog*/
    open fun showWheelDialog(context: Context) {
        wheelItemConfig.itemWheelDialogTitle = labelItemConfig.itemLabelText
        showItemWheelDialog(context)
    }

    /**调用此方法, 通知item选中改变*/
    fun notifyWheelItemSelected(index: Int) {
        val old = itemSelectedIndex
        itemSelectedIndex = index
        itemChanging = old != index
    }

    override fun startItemLoading(loading: Boolean, result: ResultThrowable?) {
        if (loading) {
            //加载数据的时候, 清空之前的数据
            itemWheelList = null
        }
        super.startItemLoading(loading, result)
    }
}