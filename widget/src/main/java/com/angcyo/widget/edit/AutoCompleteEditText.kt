package com.angcyo.widget.edit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import android.widget.ListPopupWindow
import com.angcyo.library.L
import com.angcyo.library.ex.hawkPutList
import com.angcyo.library.ex.splitList
import com.angcyo.library.utils.getMember
import com.angcyo.widget.DslViewHolder
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
    var autoCompleteFocusDelay = 0L

    /**显示下拉框删除的按钮*/
    var autoCompleteShowItemDelete = false

    /**长按下拉item事件*/
    var onItemDeleteClick: (adapter: RArrayAdapter<CharSequence>, position: Int) -> Unit =
        { adapter, position ->
            adapter.remove(position)
            getTag(R.id.lib_tag_hawk)?.run {
                if (this is String) {
                    if (this.isNotBlank()) {
                        hawkPutList("${adapter.dataList.contains(",")}")
                    }
                }
            }
            setDataList(adapter.dataList, autoCompleteShowOnFocus, autoCompleteFocusDelay)
            //dismissDropDown()
        }

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
            autoCompleteFocusDelay.toInt()
        ).toLong()

        autoCompleteShowItemDelete = typedArray.getBoolean(
            R.styleable.AutoCompleteEditText_r_auto_complete_show_item_delete,
            autoCompleteShowItemDelete
        )

        typedArray.recycle()

        try {
            val mPopup: ListPopupWindow =
                getMember(AutoCompleteTextView::class.java, "mPopup") as ListPopupWindow
            mPopup.listView?.setOnItemLongClickListener { parent, view, position, id ->
                if (adapter is RArrayAdapter<*>) {
                    onItemDeleteClick(adapter as RArrayAdapter<CharSequence>, position)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            L.w(e)
        }

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
                autoCompleteText.splitList(autoCompleteTextSeparator, false),
                autoCompleteShowOnFocus, autoCompleteFocusDelay.toLong()
            )
        }
    }

    /**设置下拉数据源*/
    fun setDataList(
        list: List<CharSequence>,
        showOnFocus: Boolean = true,
        focusDelay: Long = 0L,
        notifyFirst: Boolean = true
    ) {
        autoCompleteShowOnFocus = showOnFocus
        autoCompleteFocusDelay = focusDelay

        val adapter = object : RArrayAdapter<CharSequence>(context, list) {
            override fun onBindDropDownItemView(
                itemViewHolder: DslViewHolder,
                position: Int,
                itemBean: CharSequence?
            ) {
                super.onBindDropDownItemView(itemViewHolder, position, itemBean)
                itemViewHolder.visible(R.id.lib_delete_view, autoCompleteShowItemDelete)
                itemViewHolder.click(R.id.lib_delete_view) {
                    onItemDeleteClick(adapter as RArrayAdapter<CharSequence>, position)
                }
            }
        }
        setAdapter(adapter)

        if (list.isEmpty()) {
            dismissDropDown()
        } else {
            if (showOnFocus) {
                onFocusChange(notifyFirst) {
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
    }

    override fun showDropDown() {
        if (adapter == null || adapter.count <= 0) {
            dismissDropDown()
            return
        }
        try {
            super.showDropDown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
