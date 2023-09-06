package com.angcyo.item.style

import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.angcyo.dialog.R
import com.angcyo.item.DslBaseEditItem
import com.angcyo.library.ex.add
import com.angcyo.library.ex.remove
import com.angcyo.widget.base.addFilter
import com.angcyo.widget.base.clearListeners
import com.angcyo.widget.base.removeFilter
import com.angcyo.widget.base.setInputText
import com.angcyo.widget.base.setMaxLine
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

    /**输入限制, 此属性和[editInputFilterList]互斥
     * [R.string.lib_number_digits]
     * [R.string.lib_password_digits]
     * [R.string.lib_en_digits]*/
    var editDigits: String? = null

    /**输入框不可编辑*/
    var noEditModel: Boolean = false

    /**最大输入行数, <=1 单行
     * default: 2147483647 [Int.MAX_VALUE]
     * */
    var editMaxLine: Int? = null
        set(value) {
            field = value
            if (value == null || value <= 1) {
                textGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                editInputType = editInputType.remove(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
            } else {
                textGravity = Gravity.TOP or Gravity.LEFT
                editInputType = editInputType.add(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
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
                if (editMaxLine == null) {
                    editMaxLine = maxLines
                }
                setMaxLine(editMaxLine!!)

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

                //digits 放在[inputType]后面
                editDigits?.let {
                    keyListener = DigitsKeyListener.getInstance(it)
                }

                if (this is EditText) {
                    setInputText(this@EditStyleConfig.text)
                }
            }
        }
    }
}