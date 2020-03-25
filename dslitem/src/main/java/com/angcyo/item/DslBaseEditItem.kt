package com.angcyo.item

import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*
import com.angcyo.widget.edit.DslEditText
import com.angcyo.widget.edit.IEditDelegate

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

    init {
        itemLayoutId = R.layout.dsl_edit_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.ev(R.id.lib_edit_view)?.apply {
            itemEditTextStyle.updateStyle(this)

            //放在最后监听, 防止首次setInputText, 就触发事件.
            onTextChange {
                itemEditText = it
                itemChanging = true
                itemTextChange(it)
            }
        }
    }

    open fun onItemTextChange(text: CharSequence) {

    }

    open fun configEditTextStyle(action: EditStyleConfig.() -> Unit) {
        itemEditTextStyle.action()
    }
}

/**输入框样式配置*/
class EditStyleConfig : TextStyleConfig() {

    /**文本输入类型*/
    var editInputType = InputType.TYPE_CLASS_TEXT

    /**最大输入字符数*/
    var editMaxInputLength = DslBaseEditItem.DEFAULT_MAX_INPUT_LENGTH

    /**输入过滤器*/
    var editInputFilterList = mutableListOf<InputFilter>()

    /**输入框不可编辑*/
    var noEditModel: Boolean = false

    /**最大输入行数, <=1 单行*/
    var editMaxLine: Int = 1
        set(value) {
            field = value
            textGravity = if (value <= 1) {
                Gravity.LEFT or Gravity.CENTER_VERTICAL
            } else {
                Gravity.TOP or Gravity.LEFT
            }
        }

    override fun updateStyle(textView: TextView) {
        super.updateStyle(textView)

        with(textView) {
            //清空text change监听
            clearListeners()

            //过滤器
            filters = editInputFilterList.toTypedArray()

            //单行 or 多行
            setMaxLine(editMaxLine)

            inputType = editInputType
            isEnabled = !noEditModel

            if (this is IEditDelegate) {
                this.getCustomEditDelegate().isNoEditMode = noEditModel
            }

            if (this is DslEditText) {
                setMaxLength(editMaxInputLength)
            } else {
                addFilter(InputFilter.LengthFilter(editMaxInputLength))
            }

            if (this is EditText) {
                setInputText(this@EditStyleConfig.text)
            }
        }
    }
}