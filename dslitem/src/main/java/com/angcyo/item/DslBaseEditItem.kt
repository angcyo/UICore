package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.EditStyleConfig
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clearListeners
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.restoreSelection

/**
 * 输入框item基类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslBaseEditItem : DslBaseLabelItem() {

    companion object {
        /**允许默认输入的字符长度*/
        var DEFAULT_MAX_INPUT_LENGTH = 30

        /**输入框文本改变节流时长, 毫秒*/
        var DEFAULT_INPUT_SHAKE_DELAY = 300L
    }

    var itemEditText: CharSequence? = null
        set(value) {
            field = value
            itemEditTextStyle.text = value
        }

    /**统一样式配置*/
    var itemEditTextStyle = EditStyleConfig()

    /**文本改变*/
    var itemTextChange: (CharSequence) -> Unit = {
        onItemTextChange(it)
    }

    /**文本改变去频限制, 负数表示不开启*/
    var itemTextChangeShakeDelay = DEFAULT_INPUT_SHAKE_DELAY

    //用于恢复光标的位置
    var _lastEditSelectionStart = -1
    var _lastEditSelectionEnd = -1

    init {
        itemLayoutId = R.layout.dsl_edit_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        itemHolder.ev(R.id.lib_edit_view)?.apply {
            itemEditTextStyle.updateStyle(this)

            clearListeners()

            onTextChange {
                _lastEditSelectionStart = selectionStart
                _lastEditSelectionEnd = selectionEnd

                itemEditText = it
            }

            //放在最后监听, 防止首次setInputText, 就触发事件.
            onTextChange(shakeDelay = itemTextChangeShakeDelay) {
                itemChanging = true
                itemTextChange(it)
            }

            restoreSelection(_lastEditSelectionStart, _lastEditSelectionEnd)
        }
    }

    override fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewDetachedToWindow(itemHolder, itemPosition)
        itemHolder.ev(R.id.lib_edit_view)?.clearListeners()
    }

    open fun onItemTextChange(text: CharSequence) {

    }

    open fun configEditTextStyle(action: EditStyleConfig.() -> Unit) {
        itemEditTextStyle.action()
    }
}

/**快速获取对应Item的值*/
fun DslAdapterItem.itemEditText(): CharSequence? {
    return if (this is DslBaseEditItem) {
        this.itemEditText
    } else {
        null
    }
}