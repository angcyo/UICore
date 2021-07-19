package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.EditStyleConfig
import com.angcyo.item.style.IEditItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clearListeners

/**
 * 输入框item基类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslBaseEditItem : DslBaseLabelItem(), IEditItem {

    companion object {
        /**允许默认输入的字符长度*/
        var DEFAULT_MAX_INPUT_LENGTH = 30

        /**输入框文本改变节流时长, 毫秒*/
        var DEFAULT_INPUT_SHAKE_DELAY = 300L
    }

    override var itemEditText: CharSequence? = null
        set(value) {
            field = value
            itemEditTextStyle.text = value
        }

    /**统一样式配置*/
    override var itemEditTextStyle = EditStyleConfig()

    /**文本改变*/
    override var itemTextChange: (CharSequence) -> Unit = {
        onItemTextChange(it)
    }

    /**文本改变去频限制, 负数表示不开启, 如果短时间内关闭界面了, 可能会获取不到最新的输入框数据*/
    override var itemTextChangeShakeDelay = DEFAULT_INPUT_SHAKE_DELAY

    //用于恢复光标的位置
    override var _lastEditSelectionStart = -1
    override var _lastEditSelectionEnd = -1

    init {
        itemLayoutId = R.layout.dsl_edit_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        initEditItem(itemHolder)
    }

    override fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewDetachedToWindow(itemHolder, itemPosition)
        //itemHolder.ev(R.id.lib_edit_view)?.clearListeners()
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
        itemHolder.ev(R.id.lib_edit_view)?.clearListeners()
    }
}

/**快速获取对应Item的值*/
fun DslAdapterItem.itemEditText(): CharSequence? {
    return when (this) {
        is IEditItem -> this.itemEditText
        else -> null
    }
}