package com.angcyo.item.style

import android.text.InputFilter
import android.text.InputType
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.DslBaseEditItem
import com.angcyo.item.R
import com.angcyo.item.form.IFormItem
import com.angcyo.item.form.formItemConfig
import com.angcyo.library.ex._dimen
import com.angcyo.library.ex.elseNull
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clearListeners
import com.angcyo.widget.base.onFocusChange
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.restoreSelection
import com.angcyo.widget.edit.IEditDelegate

/**
 * 输入框item
 *
 * [IOperateEditItem]
 * [com.angcyo.item.style.IOperateEditItem]
 * [OperateEditItemConfig]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

/**文本改变通知回调*/
typealias TextChangeAction = (CharSequence) -> Unit

interface IEditItem : IAutoInitItem {

    /**配置项*/
    var editItemConfig: EditItemConfig

    /**初始化*/
    @ItemInitEntryPoint
    fun initEditItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.ev(editItemConfig.itemEditTextViewId)?.apply {
            clearListeners()

            //[EditStyleConfig]样式初始化
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

            onFocusChange {
                if (it) {
                    editItemConfig._lastEditSelectionStart = selectionStart
                    editItemConfig._lastEditSelectionEnd = selectionEnd
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
                onSelfItemEditTextChange(itemHolder, it)
            }

            //焦点
            editItemConfig.itemHookFocused?.let {
                if (it && !isFocused) {
                    val selectionStart = editItemConfig._lastEditSelectionStart
                    val selectionEnd = editItemConfig._lastEditSelectionEnd
                    requestFocus()
                    post {
                        restoreSelection(selectionStart, selectionEnd)
                        editItemConfig._lastEditSelectionStart = selectionStart
                        editItemConfig._lastEditSelectionEnd = selectionEnd
                    }
                }
            }.elseNull {
                restoreSelection(
                    editItemConfig._lastEditSelectionStart,
                    editItemConfig._lastEditSelectionEnd
                )
            }
        }
    }

    fun configEditTextStyle(action: EditStyleConfig.() -> Unit) {
        editItemConfig.itemEditTextStyle.action()
    }

    /**清除之前的监听*/
    fun clearEditListeners(itemHolder: DslViewHolder) {
        itemHolder.ev(editItemConfig.itemEditTextViewId)?.clearListeners()
    }

    /**编辑的文本改变后*/
    fun onSelfItemEditTextChange(itemHolder: DslViewHolder, text: CharSequence) {
        editItemConfig.itemTextChangeAction?.invoke(text)
    }

    /**焦点hook, 下次notify后恢复焦点*/
    fun hookEditItemFocus(itemHolder: DslViewHolder?) {
        itemHolder?.ev(editItemConfig.itemEditTextViewId)?.apply {
            editItemConfig.itemHookFocused = isFocused
        }
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

/**
 * 输入类型
 * [InputType.TYPE_CLASS_TEXT]
 * [InputType.TYPE_CLASS_NUMBER]
 *
 * [InputType.TYPE_TEXT_FLAG_MULTI_LINE]
 *
 * [InputType.TYPE_NUMBER_FLAG_DECIMAL]
 * [InputType.TYPE_NUMBER_FLAG_SIGNED]
 * */
var IEditItem.itemEditInputType: Int
    get() = editItemConfig.itemEditTextStyle.editInputType
    set(value) {
        editItemConfig.itemEditTextStyle.editInputType = value
    }

/**最大输入字符长度*/
var IEditItem.itemMaxInputLength: Int
    get() = editItemConfig.itemEditTextStyle.editMaxInputLength
    set(value) {
        editItemConfig.itemEditTextStyle.editMaxInputLength = value
    }

/**最大编辑行数
 * - [multiLineEditMode]*/
var IEditItem.itemMaxEditLines: Int?
    get() = editItemConfig.itemEditTextStyle.editMaxLine
    set(value) {
        editItemConfig.itemEditTextStyle.editMaxLine = value
    }

/**输入过滤*/
var IEditItem.itemInputFilterList: MutableList<InputFilter>
    get() = editItemConfig.itemEditTextStyle.editInputFilterList
    set(value) {
        editItemConfig.itemEditTextStyle.editInputFilterList = value
    }

/**输入过滤*/
var IEditItem.itemEditDigits: String?
    get() = editItemConfig.itemEditTextStyle.editDigits
    set(value) {
        editItemConfig.itemEditTextStyle.editDigits = value
    }

var IEditItem.itemTextChangeAction: TextChangeAction?
    get() = editItemConfig.itemTextChangeAction
    set(value) {
        editItemConfig.itemTextChangeAction = value
    }

/**多行编辑模式
 *
 * - [itemMaxEditLines]
 * */
fun IEditItem.multiLineEditMode(
    maxLine: Int = Int.MAX_VALUE,
    minHeight: Int = _dimen(R.dimen.lib_multi_line_edit_min_height)
) {
    val multiLine = maxLine > 1
    itemMaxEditLines = maxLine
    editItemConfig.itemEditTextStyle.viewMinHeight = if (multiLine) minHeight else 0
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

    /**是否具有焦点*/
    var itemHookFocused: Boolean? = null

    /**统一样式配置*/
    var itemEditTextStyle: EditStyleConfig = EditStyleConfig()

    /**文本改变*/
    var itemTextChangeAction: TextChangeAction? = null

    /**文本改变去频限制, 负数表示不开启, 如果短时间内关闭界面了, 可能会获取不到最新的输入框数据*/
    var itemTextChangeShakeDelay: Long = DslBaseEditItem.DEFAULT_INPUT_SHAKE_DELAY

    //用于恢复光标的位置
    var _lastEditSelectionStart: Int = -1

    var _lastEditSelectionEnd: Int = -1
}