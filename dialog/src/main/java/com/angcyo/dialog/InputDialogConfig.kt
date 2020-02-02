package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*
import com.angcyo.widget.edit.CharLengthFilter
import com.angcyo.widget.pager.TextIndicator

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class InputDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**
     * 最大输入字符限制
     * */
    var maxInputLength = -1

    /**
     * 强制指定输入框的高度
     * */
    var inputViewHeight = -1

    /**
     * 文本框hint文本
     */
    var hintInputString: CharSequence? = "请输入..."

    /**
     * 缺省的文本框内容
     */
    var defaultInputString: CharSequence? = ""

    /**
     * 默认是否显示键盘
     * */
    var showSoftInput = true

    /**
     * 是否允许输入为空
     */
    var canInputEmpty = true

    /**
     * 使用英文字符数过滤, 一个汉字等于2个英文, 一个emoji表情等于2个汉字
     */
    var useCharLengthFilter = false

    /**
     * 输入框内容回调, 返回 true, 则不会自动 调用 dismiss
     * */
    var onInputResult: (dialog: Dialog, inputText: CharSequence) -> Boolean = { _, _ ->
        false
    }

    /**文本输入类型*/
    var inputType = InputType.TYPE_CLASS_TEXT

    /**输入框过滤器*/
    var inputFilterList = mutableListOf<InputFilter>()

    init {
        dialogLayoutId = R.layout.lib_dialog_input_layout
        positiveButtonListener = { dialog, dialogViewHolder ->
            if (onInputResult.invoke(dialog, dialogViewHolder.ev(R.id.edit_text_view).string())) {

            } else {
                dialog.dismiss()
            }
        }
        animStyleResId = R.style.LibDialogInputAnimation
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        val editView = dialogViewHolder.ev(R.id.edit_text_view)
        val indicatorView = dialogViewHolder.v<TextIndicator>(R.id.single_text_indicator_view)
        val positiveButton = dialogViewHolder.view(R.id.positive_button)

        _configView(editView, indicatorView, positiveButton)

        if (showSoftInput) {
            dialogViewHolder.post { editView?.showSoftInput() }
        }
    }

    fun _configView(editView: EditText?, indicatorView: TextIndicator?, positiveButton: View?) {
        //空输入
        if (!canInputEmpty) {
            editView?.onTextChange {
                positiveButton?.isEnabled = it.isNotBlank()
            }
            positiveButton?.isEnabled = !defaultInputString.isNullOrBlank()
        }

        //过滤器
        inputFilterList.forEach {
            editView?.addFilter(it)
        }

        //输入限制
        if (maxInputLength >= 0) {
            if (useCharLengthFilter) {
                editView?.addFilter(CharLengthFilter(maxInputLength))
            } else {
                editView?.addFilter(InputFilter.LengthFilter(maxInputLength))
            }

            indicatorView?.visibility = View.VISIBLE
            indicatorView?.setupEditText(editView, maxInputLength)
        }

        //单行or多行
        if (inputViewHeight > 0) {
            editView?.setWidthHeight(height = inputViewHeight)
            editView?.gravity = Gravity.TOP
            editView?.setSingleLineMode(false)
        } else {
            editView?.gravity = Gravity.CENTER_VERTICAL
            editView?.setSingleLineMode(true)
        }

        editView?.inputType = inputType
        editView?.hint = hintInputString
        editView?.setInputText(defaultInputString)
    }
}