package com.angcyo.item.style

import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.angcyo.item.DslBaseEditItem
import com.angcyo.widget.base.*
import com.angcyo.widget.edit.CharLengthFilter
import com.angcyo.widget.edit.DslEditText
import com.angcyo.widget.edit.IEditDelegate

/**
 * 输入框样式配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class EditStyleConfig : TextStyleConfig() {

    /**文本输入类型*/
    var editInputType = InputType.TYPE_CLASS_TEXT

    /**最大输入字符数, -1取消限制*/
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

    override fun updateStyle(view: View) {
        super.updateStyle(view)

        if (view is TextView) {
            with(view) {
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

                if (editMaxInputLength > 0) {
                    if (this is DslEditText) {
                        setMaxLength(editMaxInputLength)
                    } else {
                        addFilter(InputFilter.LengthFilter(editMaxInputLength))
                    }
                } else {
                    removeFilter {
                        this is InputFilter.LengthFilter || this is CharLengthFilter
                    }
                }

                if (this is EditText) {
                    setInputText(this@EditStyleConfig.text)
                }
            }
        }
    }
}