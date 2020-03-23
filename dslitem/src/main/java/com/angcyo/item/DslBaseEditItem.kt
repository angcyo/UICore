package com.angcyo.item

import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
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

open class DslBaseEditItem : DslAdapterItem() {

    companion object {
        /**允许默认输入的字符长度*/
        var DEFAULT_MAX_INPUT_LENGTH = 30
    }

    /**最大输入行数, <=1 单行*/
    var itemMaxLine: Int = 1
        set(value) {
            field = value
            itemTextGravity = if (value <= 1) {
                Gravity.LEFT or Gravity.CENTER_VERTICAL
            } else {
                Gravity.TOP or Gravity.LEFT
            }
        }

    var itemTextGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL

    /**文本输入类型*/
    var itemInputType = InputType.TYPE_CLASS_TEXT

    /**最大输入字符数*/
    var itemMaxInputLength = DEFAULT_MAX_INPUT_LENGTH

    /**输入过滤器*/
    var itemInputFilterList = mutableListOf<InputFilter>()

    var itemEditHint: CharSequence? = null
    var itemEditText: CharSequence? = null

    /**输入框不可编辑*/
    var itemNoEditModel: Boolean = false

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

            //清空text change监听
            clearListeners()

            //过滤器
            filters = itemInputFilterList.toTypedArray()

            //单行 or 多行
            setMaxLine(itemMaxLine)

            gravity = itemTextGravity
            inputType = itemInputType
            isEnabled = !itemNoEditModel
            if (this is IEditDelegate) {
                this.getCustomEditDelegate().isNoEditMode = itemNoEditModel
            }

            if (this is DslEditText) {
                setMaxLength(itemMaxInputLength)
            } else {
                addFilter(InputFilter.LengthFilter(itemMaxInputLength))
            }

            hint = itemEditHint
            setInputText(itemEditText)

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
}