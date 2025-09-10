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
import com.angcyo.library.ex.dpi
import com.angcyo.library.extend.IToDrawable
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span

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

    /**是否显示item的文本Drawable
     * [IToDrawable]*/
    var itemShowTextDrawable: Boolean = true

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
                val str = if (itemWheelUnit == null) {
                    itemWheelToTextAction(this)
                } else {
                    "${itemWheelToTextAction(this)}${itemWheelUnit}"
                }
                //--IToDrawable
                val item = this
                if (itemShowTextDrawable && item is IToDrawable) {
                    val drawable = item.toDrawable()
                    if (drawable != null) {
                        span {
                            appendDrawable(drawable)
                            appendSpace(4 * dpi)
                            append(str)
                        }
                    } else {
                        str
                    }
                } else {
                    str
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