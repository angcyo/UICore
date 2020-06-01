package com.angcyo.item

import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*

/**
 * 带有label的单行输入item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelEditItem : DslBaseEditItem() {

    /**编辑提示按钮*/
    var itemEditTipIcon: Int = R.drawable.lib_icon_edit_tip

    /**右边图标点击事件, 如果设置回调. 会影响默认的事件处理*/
    var itemRightIcoClick: ((DslViewHolder, View) -> Unit)? = null

    init {
        itemLayoutId = R.layout.dsl_label_edit_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.img(R.id.lib_right_ico_view)?.apply {
            if (itemRightIcoClick == null) {
                if (itemEditTextStyle.noEditModel) {
                    gone()
                } else {
                    visible()
                    clickIt {
                        itemHolder.focus<View>(R.id.lib_edit_view)?.showSoftInput()
                    }
                }
            } else {
                clickIt {
                    itemRightIcoClick?.invoke(itemHolder, it)
                }
            }
            setImageDrawable(loadDrawable(itemEditTipIcon))
        }
    }
}