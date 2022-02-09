package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.DslBaseEditItem
import com.angcyo.item.R
import com.angcyo.item.form.IFormItem
import com.angcyo.item.form.formItemConfig
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clearListeners
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.restoreSelection
import com.angcyo.widget.edit.IEditDelegate

/**
 * 输入框item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IEditItem : IAutoInitItem {

    var editItemConfig: EditItemConfig

    fun onItemTextChange(text: CharSequence) {
        editItemConfig.itemTextChange?.invoke(text)
    }

    /**初始化*/
    fun initEditItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.ev(editItemConfig.itemEditTextViewId)?.apply {
            clearListeners()

            editItemConfig.itemEditTextStyle.updateStyle(this)

            if (this is IEditDelegate) {
                val customEditDelegate = this.getCustomEditDelegate()
                if (this@IEditItem is IFormItem) {
                    customEditDelegate.isNoEditMode =
                        editItemConfig.itemNoEditModel ?: !formItemConfig.formCanEdit
                } else {
                    editItemConfig.itemNoEditModel?.apply {
                        customEditDelegate.isNoEditMode = this
                    }
                }
            }

            onTextChange {
                editItemConfig._lastEditSelectionStart = selectionStart
                editItemConfig._lastEditSelectionEnd = selectionEnd

                editItemConfig.itemEditText = it
            }

            //放在最后监听, 防止首次setInputText, 就触发事件.
            onTextChange(shakeDelay = editItemConfig.itemTextChangeShakeDelay) {
                if (this@IEditItem is DslAdapterItem) {
                    itemChanging = true
                }
                onItemTextChange(it)
            }

            restoreSelection(
                editItemConfig._lastEditSelectionStart,
                editItemConfig._lastEditSelectionEnd
            )
        }
    }

    fun clearEditListeners(itemHolder: DslViewHolder) {
        itemHolder.ev(editItemConfig.itemEditTextViewId)?.clearListeners()
    }

    fun configEditTextStyle(action: EditStyleConfig.() -> Unit) {
        editItemConfig.itemEditTextStyle.action()
    }
}

var IEditItem.itemEditText: CharSequence?
    get() = editItemConfig.itemEditText
    set(value) {
        editItemConfig.itemEditText = value
    }

var IEditItem.itemEditHint: CharSequence?
    get() = editItemConfig.itemEditTextStyle.hint
    set(value) {
        editItemConfig.itemEditTextStyle.hint = value
    }

var IEditItem.itemMaxInputLength: Int
    get() = editItemConfig.itemEditTextStyle.editMaxInputLength
    set(value) {
        editItemConfig.itemEditTextStyle.editMaxInputLength = value
    }

class EditItemConfig : IDslItemConfig {

    /**[R.id.lib_edit_view]*/
    var itemEditTextViewId: Int = R.id.lib_edit_view

    /**输入框内容*/
    var itemEditText: CharSequence? = null
        set(value) {
            field = value
            itemEditTextStyle.text = value
        }

    /**是否可编辑*/
    var itemNoEditModel: Boolean? = null
        set(value) {
            field = value
            if (value == true) {
                itemEditTextStyle.hint = null
            }
        }

    /**统一样式配置*/
    var itemEditTextStyle: EditStyleConfig = EditStyleConfig()

    /**文本改变*/
    var itemTextChange: ((CharSequence) -> Unit)? = null

    /**文本改变去频限制, 负数表示不开启, 如果短时间内关闭界面了, 可能会获取不到最新的输入框数据*/
    var itemTextChangeShakeDelay: Long = DslBaseEditItem.DEFAULT_INPUT_SHAKE_DELAY

    //用于恢复光标的位置
    var _lastEditSelectionStart: Int = -1

    var _lastEditSelectionEnd: Int = -1
}