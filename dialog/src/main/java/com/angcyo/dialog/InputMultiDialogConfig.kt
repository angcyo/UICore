package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import com.angcyo.library.ex.append
import com.angcyo.library.ex.eachChild
import com.angcyo.library.ex.find
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.showSoftInput
import com.angcyo.widget.base.string
import com.angcyo.widget.pager.TextIndicator

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class InputMultiDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**
     * 最大输入字符限制
     * */
    var maxInputLength = mutableListOf(-1, -1)

    /**
     * 强制指定输入框的高度
     * */
    var inputViewHeight = mutableListOf(-1, -1)

    /**
     * 文本框hint文本
     */
    var hintInputString = mutableListOf<CharSequence?>("请输入...", "请输入...")

    /**
     * 缺省的文本框内容, 同时也决定输入框的个数
     */
    var defaultInputString = mutableListOf<CharSequence?>("", "")

    /**
     * 默认是否显示键盘
     * */
    var showSoftInput = true

    /**
     * 使用英文字符数过滤, 一个汉字等于2个英文, 一个emoji表情等于2个汉字
     */
    var useCharLengthFilter = mutableListOf(false, false)

    /**
     * 输入框内容回调, 返回 true, 则不会自动 调用 dismiss
     * */
    var onInputResult: (dialog: Dialog, inputTextList: MutableList<String>) -> Boolean = { _, _ ->
        false
    }

    /**文本输入类型*/
    var inputType = mutableListOf(InputType.TYPE_CLASS_TEXT, InputType.TYPE_CLASS_TEXT)

    /**需要填充的布局id*/
    var inputItemLayoutId: Int = R.layout.lib_dialog_input_multi_item

    init {
        dialogLayoutId = R.layout.lib_dialog_input_multi_layout
        positiveButtonListener = { dialog, dialogViewHolder ->
            val result = mutableListOf<String>()
            dialogViewHolder.group(R.id.input_wrapper_layout)?.eachChild { _, child ->
                result.add(child.find<EditText>(R.id.edit_text_view)?.string() ?: "")
            }

            if (onInputResult.invoke(dialog, result)) {
                //被拦截
            } else {
                dialog.hideSoftInput()
                dialog.dismiss()
            }
        }
        animStyleResId = R.style.LibDialogInputAnimation
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //
        appendInputLayout(dialog, dialogViewHolder)

        //
        if (showSoftInput) {
            dialogViewHolder.post { dialogViewHolder.ev(R.id.edit_text_view)?.showSoftInput() }
        }
    }

    override fun onDialogDestroy(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.onDialogDestroy(dialog, dialogViewHolder)
    }

    /**填充输入控件*/
    open fun appendInputLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        val inputDialogConfig = InputDialogConfig(dialogContext)
        dialogViewHolder.group(R.id.input_wrapper_layout)?.apply {
            defaultInputString.forEachIndexed { index, _ ->
                append(inputItemLayoutId) {
                    val editView = find<EditText>(R.id.edit_text_view)
                    val indicatorView = find<TextIndicator>(R.id.single_text_indicator_view)

                    inputDialogConfig.useCharLengthFilter = useCharLengthFilter[index]
                    inputDialogConfig.maxInputLength = maxInputLength[index]
                    inputDialogConfig.inputViewHeight = inputViewHeight[index]

                    inputDialogConfig.inputType = inputType[index]
                    inputDialogConfig.hintInputString = hintInputString[index]
                    inputDialogConfig.defaultInputString = defaultInputString[index]

                    inputDialogConfig._configView(editView, indicatorView, null)
                }
            }
        }
    }
}