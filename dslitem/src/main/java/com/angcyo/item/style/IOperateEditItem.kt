package com.angcyo.item.style

import android.text.InputFilter
import android.text.method.DigitsKeyListener
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.DslBaseEditItem
import com.angcyo.item.R
import com.angcyo.item.form.IFormItem
import com.angcyo.item.form.formItemConfig
import com.angcyo.library.ex.elseNull
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.addFilter
import com.angcyo.widget.base.clearListeners
import com.angcyo.widget.base.onFocusChange
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.removeFilter
import com.angcyo.widget.base.restoreSelection
import com.angcyo.widget.base.setInputText
import com.angcyo.widget.edit.CharLengthFilter
import com.angcyo.widget.edit.DslEditText
import com.angcyo.widget.edit.IEditDelegate

/**
 * 简单编辑框输入item, 不具备样式操作
 * [IEditItem]
 * [EditStyleConfig]
 *
 * [IOperateEditItem]
 * [OperateEditItemConfig]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
interface IOperateEditItem : IAutoInitItem {

    /**配置项*/
    var operateEditItemConfig: OperateEditItemConfig

    /**初始化*/
    @ItemInitEntryPoint
    fun initOperateEditItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.ev(operateEditItemConfig.itemEditTextViewId)?.apply {
            clearListeners()

            //限制最大输入字符数
            if (operateEditItemConfig.itemEditMaxInputLength > 0) {
                if (this is DslEditText) {
                    setMaxLength(operateEditItemConfig.itemEditMaxInputLength)
                } else {
                    addFilter(InputFilter.LengthFilter(operateEditItemConfig.itemEditMaxInputLength))
                }
            } else {
                removeFilter {
                    this is InputFilter.LengthFilter || this is CharLengthFilter
                }
            }

            //digits 放在[inputType]后面
            operateEditItemConfig.itemEditDigits?.let {
                keyListener = DigitsKeyListener.getInstance(it)
            }

            setInputText(operateEditItemConfig.itemEditText, false)
            hint = operateEditItemConfig.itemEditHint

            if (this is IEditDelegate) {
                val customEditDelegate = this.getCustomEditDelegate()
                if (this@IOperateEditItem is IFormItem) {
                    customEditDelegate.isNoEditMode =
                        operateEditItemConfig.itemNoEditModel ?: !formItemConfig.formCanEdit
                } else {
                    operateEditItemConfig.itemNoEditModel?.apply {
                        customEditDelegate.isNoEditMode = this
                    }
                }
            }

            onFocusChange {
                if (it) {
                    operateEditItemConfig._lastEditSelectionStart = selectionStart
                    operateEditItemConfig._lastEditSelectionEnd = selectionEnd
                }
            }

            onTextChange {
                operateEditItemConfig._lastEditSelectionStart = selectionStart
                operateEditItemConfig._lastEditSelectionEnd = selectionEnd

                operateEditItemConfig.itemEditText = it
            }

            //放在最后监听, 防止首次setInputText, 就触发事件.
            onTextChange(shakeDelay = operateEditItemConfig.itemTextChangeShakeDelay) {
                onSelfOperateItemEditTextChange(itemHolder, it)
            }

            //焦点
            operateEditItemConfig.itemHookFocused?.let {
                if (it && !isFocused) {
                    val selectionStart = operateEditItemConfig._lastEditSelectionStart
                    val selectionEnd = operateEditItemConfig._lastEditSelectionEnd
                    requestFocus()
                    post {
                        restoreSelection(selectionStart, selectionEnd)
                        operateEditItemConfig._lastEditSelectionStart = selectionStart
                        operateEditItemConfig._lastEditSelectionEnd = selectionEnd
                    }
                }
            }.elseNull {
                restoreSelection(
                    operateEditItemConfig._lastEditSelectionStart,
                    operateEditItemConfig._lastEditSelectionEnd
                )
            }
        }
    }

    /**清除之前的监听*/
    fun clearOperateEditListeners(itemHolder: DslViewHolder) {
        itemHolder.ev(operateEditItemConfig.itemEditTextViewId)?.clearListeners()
    }

    /**编辑的文本改变后*/
    fun onSelfOperateItemEditTextChange(itemHolder: DslViewHolder, text: CharSequence) {
        if (this@IOperateEditItem is DslAdapterItem) {
            itemChanging = true
        }
        operateEditItemConfig.itemTextChangeAction?.invoke(text)
    }

    /**焦点hook, 下次notify后恢复焦点*/
    fun hookOperateEditItemFocus(itemHolder: DslViewHolder?) {
        itemHolder?.ev(operateEditItemConfig.itemEditTextViewId)?.apply {
            operateEditItemConfig.itemHookFocused = isFocused
        }
    }
}

var IOperateEditItem.itemEditText: CharSequence?
    get() = operateEditItemConfig.itemEditText
    set(value) {
        operateEditItemConfig.itemEditText = value
    }

var IOperateEditItem.itemEditHint: CharSequence?
    get() = operateEditItemConfig.itemEditHint
    set(value) {
        operateEditItemConfig.itemEditHint = value
    }

var IOperateEditItem.itemTextChangeAction: TextChangeAction?
    get() = operateEditItemConfig.itemTextChangeAction
    set(value) {
        operateEditItemConfig.itemTextChangeAction = value
    }

var IOperateEditItem.itemEditDigits: String?
    get() = operateEditItemConfig.itemEditDigits
    set(value) {
        operateEditItemConfig.itemEditDigits = value
    }

var IOperateEditItem.itemEditMaxInputLength
    get() = operateEditItemConfig.itemEditMaxInputLength
    set(value) {
        operateEditItemConfig.itemEditMaxInputLength = value
    }

class OperateEditItemConfig : IDslItemConfig {

    /**[R.id.lib_edit_view]*/
    var itemEditTextViewId: Int = R.id.lib_edit_view

    /**输入框内容*/
    var itemEditText: CharSequence? = null

    /**输入框提示*/
    var itemEditHint: CharSequence? = null

    /**输入限制
     * [R.string.lib_number_digits]
     * [R.string.lib_password_digits]
     * [R.string.lib_en_digits]
     *
     * [com.angcyo.item.style.EditStyleConfig.editDigits]
     * */
    var itemEditDigits: String? = null

    /**最大输入字符数*/
    var itemEditMaxInputLength = DslBaseEditItem.DEFAULT_MAX_INPUT_LENGTH

    /**是否可编辑*/
    var itemNoEditModel: Boolean? = null

    /**是否具有焦点*/
    var itemHookFocused: Boolean? = null

    /**文本改变*/
    var itemTextChangeAction: TextChangeAction? = null

    /**文本改变去频限制, 负数表示不开启, 如果短时间内关闭界面了, 可能会获取不到最新的输入框数据*/
    var itemTextChangeShakeDelay: Long = DslBaseEditItem.DEFAULT_INPUT_SHAKE_DELAY

    //用于恢复光标的位置
    var _lastEditSelectionStart: Int = -1

    var _lastEditSelectionEnd: Int = -1
}