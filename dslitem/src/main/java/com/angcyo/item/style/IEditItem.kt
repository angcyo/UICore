package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clearListeners
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.restoreSelection

/**
 * 输入框item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IEditItem {

    /**输入框内容*/
    var itemEditText: CharSequence?

    /**统一样式配置*/
    var itemEditTextStyle: EditStyleConfig

    /**文本改变*/
    var itemTextChange: (CharSequence) -> Unit

    /**文本改变去频限制, 负数表示不开启, 如果短时间内关闭界面了, 可能会获取不到最新的输入框数据*/
    var itemTextChangeShakeDelay: Long

    //用于恢复光标的位置
    var _lastEditSelectionStart: Int
    var _lastEditSelectionEnd: Int

    /**初始化*/
    fun initEditItem(itemHolder: DslViewHolder) {
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
                if (this@IEditItem is DslAdapterItem) {
                    itemChanging = true
                }
                itemTextChange(it)
            }

            restoreSelection(_lastEditSelectionStart, _lastEditSelectionEnd)
        }
    }

    fun clearEditListeners(itemHolder: DslViewHolder) {
        itemHolder.ev(R.id.lib_edit_view)?.clearListeners()
    }

    fun onItemTextChange(text: CharSequence) {

    }

    fun configEditTextStyle(action: EditStyleConfig.() -> Unit) {
        itemEditTextStyle.action()
    }
}