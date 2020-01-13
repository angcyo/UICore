package com.angcyo.widget.edit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import com.angcyo.library.ex.split
import com.angcyo.widget.R
import com.angcyo.widget.RArrayAdapter
import com.angcyo.widget.base.onFocusChange

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/06
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class AutoCompleteEditText : CompleteEditText {

    var autoCompleteText: String? = null
    var autoCompleteTextSeparator = "\\|"
    var autoCompleteShowOnFocus = false
    var autoCompleteFocusDelay = 0

    constructor(context: Context) : super(context) {
        initAutoEditText(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAutoEditText(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAutoEditText(context, attrs)
    }

    private fun initAutoEditText(context: Context, attrs: AttributeSet?) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoCompleteEditText)

        autoCompleteText =
            typedArray.getString(R.styleable.AutoCompleteEditText_r_auto_complete_text)

        typedArray.getString(R.styleable.AutoCompleteEditText_r_auto_complete_text_separator)
            ?.also {
                if (!TextUtils.isEmpty(it)) {
                    autoCompleteTextSeparator = it
                }
            }

        autoCompleteShowOnFocus = typedArray.getBoolean(
            R.styleable.AutoCompleteEditText_r_auto_complete_show_on_focus,
            autoCompleteShowOnFocus
        )
        autoCompleteFocusDelay = typedArray.getInt(
            R.styleable.AutoCompleteEditText_r_auto_complete_focus_delay,
            autoCompleteFocusDelay
        )

        typedArray.recycle()

        resetAutoCompleteTextAdapter()
    }

    /**
     * 输入过滤阈值
     */
    override fun getThreshold(): Int {
        return super.getThreshold()
    }

    override fun setThreshold(threshold: Int) {
        super.setThreshold(threshold)
    }

    override fun enoughToFilter(): Boolean {
        return super.enoughToFilter()
    }

    fun resetAutoCompleteTextAdapter() {
        if (!TextUtils.isEmpty(autoCompleteText)) {
            setDataList(
                autoCompleteText.split(autoCompleteTextSeparator, false),
                autoCompleteShowOnFocus, autoCompleteFocusDelay.toLong()
            )
        }
    }

    /**设置下拉数据源*/
    fun setDataList(list: List<CharSequence>, showOnFocus: Boolean = true, focusDelay: Long = 0L) {
        setAdapter(RArrayAdapter(context, list))

        if (showOnFocus) {
            onFocusChange {
                if (it) {
                    if (focusDelay > 0) {
                        postDelayed({ showDropDown() }, focusDelay)
                    } else {
                        showDropDown()
                    }
                }
            }
        }
    }

    override fun showDropDown() {
        if (adapter == null || adapter.count <= 0) {
            return
        }
        try {
            super.showDropDown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
