package com.angcyo.dialog2

import android.app.Dialog
import android.content.Context
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.ViewGroup
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.dialog2.R
import com.angcyo.library.ex.size
import com.angcyo.widget.DslViewHolder
import com.contrarywind.adapter.WheelAdapter
import com.contrarywind.view.WheelView
import java.util.*

/**
 * [WheelView]组成的多个选项选择对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/03/05
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class WheelOptionsDialogConfig : BaseDialogConfig() {

    /**显示在[WheelView]上的label*/
    val labelList = mutableListOf<String?>()

    /**一组一组的选项*/
    val allOptionsList = mutableListOf<List<Any?>>()

    /**选中的项的索引*/
    val selectedOptionsList = SparseIntArray()

    /**将选项[item], 转成可以显示在界面的 文本类型*/
    var onOptionToString: ((item: Any?) -> CharSequence)? = null

    /**选中选项的回调
     * [wheelIndex] 第几个[WheelView]从0开始的索引
     * [index] 选中[WheelView]中的第几项*/
    var onOptionSelectedListener: ((wheelIndex: Int, index: Int) -> Unit)? = null

    /**点击确定后回调*/
    var onWheelOptionsResult: (dialog: Dialog, selectedList: SparseIntArray) -> Boolean = { _, _ ->
        false
    }

    init {
        dialogLayoutId = R.layout.lib_dialog_options_wheel_layout

        positiveButtonListener = { dialog, _ ->
            if (onWheelOptionsResult.invoke(dialog, selectedOptionsList)) {
                //被拦截
            } else {
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.group(R.id.wheel_wrap_layout)?.apply {
            for (i in 0 until allOptionsList.size()) {
                addView(_createWheelView(this, labelList.getOrNull(i), allOptionsList[i]).apply {
                    //默认选中
                    selectedOptionsList.get(i).let { selected ->
                        currentItem = selected
                    }

                    setOnItemSelectedListener {
                        selectedOptionsList.put(i, it)
                        onOptionSelectedListener?.invoke(i, it)
                    }
                })
            }
        }
    }

    fun _createWheelView(parent: ViewGroup, label: String?, list: List<Any?>): WheelView {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.lib_wheel_layout, parent, false)
        val wheelView = view.findViewById<WheelView>(R.id.lib_wheel_view)
        wheelView.label = label
        wheelView.isDrawLabelOnTextBehind = false
        wheelView.adapter = object : WheelAdapter<Any?> {
            override fun getItemsCount(): Int = list.size()

            override fun getItem(index: Int): Any? = onOptionToString?.run {
                this(list[index])
            } ?: list[index]

            override fun indexOf(o: Any?): Int = list.indexOf(o)
        }
        return wheelView
    }

    /**添加一组选项*/
    fun addOptions(itemList: List<Any?>, label: String? = null) {
        labelList.add(label)
        allOptionsList.add(itemList)
    }

}

/**
 * 多组滚轮选项对话框
 * */
fun Context.wheelOptionsDialog(config: WheelOptionsDialogConfig.() -> Unit): Dialog {
    return WheelOptionsDialogConfig().run {
        configBottomDialog(this@wheelOptionsDialog)
        config()
        show()
    }
}