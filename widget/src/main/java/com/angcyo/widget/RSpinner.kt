package com.angcyo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatSpinner
import com.angcyo.library.ex.dpi
import com.angcyo.widget.base.exactly
import com.angcyo.widget.base.getMode

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/01/11
 */
open class RSpinner : AppCompatSpinner, AdapterView.OnItemSelectedListener {

    val itemSelectedListener = mutableListOf<OnItemSelectedListener>()

    constructor(context: Context) : super(context)
    constructor(context: Context, mode: Int) : super(context, mode)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onItemSelectedListener = this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isInEditMode && heightMeasureSpec.getMode() != MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, exactly(40 * dpi))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    //<editor-fold defaultState="collapsed" desc="事件转发">

    override fun onNothingSelected(parent: AdapterView<*>) {
        itemSelectedListener.forEach { it.onNothingSelected(parent) }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, rowId: Long) {
        itemSelectedListener.forEach { it.onItemSelected(parent, view, position, rowId) }
    }

    override fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        if (listener == this) {
            super.setOnItemSelectedListener(listener)
        } else {
            if (listener != null) {
                itemSelectedListener.add(listener)
            }
        }
    }

    fun addOnItemSelectedListener(listener: OnItemSelectedListener) {
        itemSelectedListener.add(listener)
    }

    fun removeOnItemSelectedListener(listener: OnItemSelectedListener) {
        itemSelectedListener.remove(listener)
    }

    //</editor-fold defaultState="collapsed" desc="事件转发">

    //<editor-fold defaultState="collapsed" desc="数据设置">

    /**获取选中的数据*/
    fun <Data> getSelectedData(): Data? = selectedItem as? Data

    /**设置数据源*/
    fun setStrings(
        list: List<CharSequence>,
        @LayoutRes viewLayout: Int = R.layout.lib_item_single_view_layout,
        @LayoutRes dropDownLayout: Int = R.layout.lib_item_single_drop_down_layout,
        listener: (position: Int) -> Unit = {}
    ) {
        setDataList(list, { it as? CharSequence }, viewLayout, dropDownLayout, listener)
    }

    fun setDataList(
        list: List<Any>,
        convert: (data: Any?) -> CharSequence?,
        @LayoutRes viewLayout: Int = R.layout.lib_item_single_view_layout,
        @LayoutRes dropDownLayout: Int = R.layout.lib_item_single_drop_down_layout,
        listener: (position: Int) -> Unit = {}
    ) {
        val adapter = RArrayAdapter(context, viewLayout, dropDownLayout, list)
        adapter.onCharSequenceConvert = convert
        setAdapter(adapter)

        onItemSelectedListener = object : OnSpinnerItemSelected() {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                rowId: Long
            ) {
                listener(position)
            }
        }
    }

    //</editor-fold defaultState="collapsed" desc="数据设置">

}

abstract class OnSpinnerItemSelected : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, rowId: Long) {

    }
}