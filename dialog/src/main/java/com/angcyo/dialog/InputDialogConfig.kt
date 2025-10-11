package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.View
import android.widget.EditText
import com.angcyo.library.ex._string
import com.angcyo.library.ex.hawkGetList
import com.angcyo.library.ex.hawkPutList
import com.angcyo.library.ex.setWidthHeight
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.addFilter
import com.angcyo.widget.base.appendInputText
import com.angcyo.widget.base.dslViewHolder
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.resetChild
import com.angcyo.widget.base.setInputHint
import com.angcyo.widget.base.setInputText
import com.angcyo.widget.base.setSingleLineMode
import com.angcyo.widget.base.showSoftInput
import com.angcyo.widget.base.string
import com.angcyo.widget.edit.CharLengthFilter
import com.angcyo.widget.flow
import com.angcyo.widget.pager.TextIndicator

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class InputDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    companion object {
        const val DEFAULT_SOFT_INPUT_DELAY = 240L
    }

    /**
     * 最大输入字符限制
     * */
    var maxInputLength = -1

    /**
     * 强制指定输入框的高度, 默认是单行输入
     * 大于0, 会启动多行输入模式
     * */
    var inputViewHeight = -1

    /**
     * 文本框hint文本
     */
    var hintInputString: CharSequence? = _string(R.string.dialog_input_hint)

    /**
     * 缺省的文本框内容
     */
    var defaultInputString: CharSequence? = ""

    /**
     * 默认是否显示键盘
     * */
    var showSoftInput = true

    /**延迟多久才显示软键盘*/
    var showSoftInputDelay = DEFAULT_SOFT_INPUT_DELAY

    /** 是否允许输入为空 */
    var canInputEmpty = true

    /**是否剔除输入文本的首尾空格*/
    var trimInputText = true

    /**
     * 使用英文字符数过滤, 一个汉字等于2个英文, 一个emoji表情等于2个汉字
     */
    var useCharLengthFilter = false

    /**
     * 输入框内容回调
     * 返回true: 拦截默认操作
     * 返回false: 执行默认操作dismiss
     * */
    var onInputResult: (dialog: Dialog, inputText: CharSequence) -> Boolean = { _, _ ->
        false
    }

    /**文本输入类型
     * [InputType.TYPE_CLASS_TEXT]
     * [InputType.TYPE_CLASS_NUMBER]
     * [InputType.TYPE_NUMBER_FLAG_DECIMAL]
     * [InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL]
     * */
    var inputType = InputType.TYPE_CLASS_TEXT

    /**不指定则自动设置*/
    var inputGravity: Int? = null

    /**输入框过滤器*/
    var inputFilterList = mutableListOf<InputFilter>()

    /**输入限制, 此属性和[inputType]互斥
     * [R.string.lib_number_digits]
     * [R.string.lib_password_digits]
     * [R.string.lib_en_digits]*/
    var digits: String? = null

    /**输入历史*/
    var inputHistoryList: List<CharSequence>? = null

    /**最大显示的输入历史数量*/
    var inputHistoryMaxLimit: Int = 10

    /**hawk key, 自动读取到[inputHistoryList]和保存*/
    var inputHistoryHawkKey: String? = null
        set(value) {
            field = value
            value?.hawkGetList(maxCount = inputHistoryMaxLimit)?.let {
                inputHistoryList = it
            }
        }

    /**输入历史布局id*/
    var inputHistoryLayoutId: Int = R.layout.lib_input_history_layout

    init {
        dialogLayoutId = R.layout.lib_dialog_input_layout
        positiveButtonListener = { dialog, dialogViewHolder ->
            val result = dialogViewHolder.ev(R.id.edit_text_view).string(trimInputText)
            if (!canInputEmpty && result.isBlank()) {
                //空
            } else if (onInputResult.invoke(dialog, result)) {
                //被拦截
            } else {
                inputHistoryHawkKey?.let {
                    //保存历史
                    it.hawkPutList(result)
                }
                dialog.hideSoftInput()
                dialog.dismiss()
            }
        }
        animStyleResId = R.style.LibDialogInputAnimation
        hookWindowInsets = false
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        if (dialogTitle == null) {
            //防止标题为空时, 隐藏了控制按钮
            dialogTitle = hintInputString
        }
        super.initDialogView(dialog, dialogViewHolder)

        val editView = dialogViewHolder.ev(R.id.edit_text_view)

        //edit
        updateEditView(dialogViewHolder)

        //
        if (showSoftInput) {
            dialogViewHolder.postDelay(showSoftInputDelay) { editView?.showSoftInput() }
        }

        //history
        dialogViewHolder.invisible(R.id.lib_flow_layout, inputHistoryList.isNullOrEmpty())
        dialogViewHolder.flow(R.id.lib_flow_layout)?.apply {
            resetChild(inputHistoryList, inputHistoryLayoutId) { itemView, item, itemIndex ->
                itemView.dslViewHolder().apply {
                    tv(R.id.lib_text_view)?.text = item

                    //删除
                    click(R.id.lib_delete_view) {
                        inputHistoryList?.filterTo(mutableListOf()) { it != item }?.let {
                            inputHistoryHawkKey?.hawkPutList(it)
                        }
                        removeView(itemView)
                    }

                    //上屏
                    clickItem {
                        editView?.appendInputText(item)
                    }
                }
            }
        }
    }

    override fun onDialogDestroy(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.onDialogDestroy(dialog, dialogViewHolder)
    }

    /**更新输入框属性*/
    open fun updateEditView(dialogViewHolder: DslViewHolder) {
        val editView = dialogViewHolder.ev(R.id.edit_text_view)
        val indicatorView = dialogViewHolder.v<TextIndicator>(R.id.single_text_indicator_view)
        val positiveButton = dialogViewHolder.view(R.id.dialog_positive_button)

        editView?.filters = arrayOf() //清空
        _configView(editView, indicatorView, positiveButton)
    }

    open fun _configView(
        editView: EditText?,
        indicatorView: TextIndicator?,
        positiveButton: View?
    ) {
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
            inputType = inputType or TYPE_TEXT_FLAG_MULTI_LINE or TYPE_TEXT_FLAG_IME_MULTI_LINE
            editView?.setWidthHeight(height = inputViewHeight)
            editView?.gravity = inputGravity ?: Gravity.TOP
            editView?.setSingleLineMode(false)
        } else {
            editView?.gravity = inputGravity ?: Gravity.CENTER_VERTICAL
            editView?.setSingleLineMode(true)
        }

        editView?.inputType = inputType
        //editView?.hint = hintInputString
        editView?.setInputHint(hintInputString)

        //digits 放在[inputType]后面
        digits?.let {
            editView?.keyListener = DigitsKeyListener.getInstance(it)
        }

        editView?.setInputText(defaultInputString)
    }
}