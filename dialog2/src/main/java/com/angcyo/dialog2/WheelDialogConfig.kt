package com.angcyo.dialog2

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.dialog2.widget.ArrayWheelAdapter
import com.angcyo.library.L
import com.angcyo.library.extend.IToText
import com.angcyo.widget.DslViewHolder
import com.contrarywind.view.WheelView

/**
 * 3D滚轮选择对话框配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class WheelDialogConfig : BaseDialogConfig() {

    /**数据集合*/
    var wheelItems: List<Any>? = null

    /**是否无限循环*/
    var wheelCyclic = false

    /**设置选中项, -1不设置*/
    var wheelSelectedIndex = -1

    /**单位设置*/
    var wheelUnit: CharSequence? = null

    /**选中回调, 返回true拦截默认操作*/
    var wheelItemSelectorAction: (dialog: Dialog, index: Int, item: Any) -> Boolean =
        { dialog, index, item ->
            L.i("选中->$index:${wheelItemToStringAction(item)}")
            false
        }

    /**上屏显示转换回调*/
    var wheelItemToStringAction: (item: Any) -> CharSequence? = {
        when (it) {
            is CharSequence -> it
            is IToText -> it.toText()
            else -> it.toString()
        }
    }

    //内部变量
    var _selectedIndex = -1

    var _wheelView: WheelView? = null

    init {
        dialogTitle = "请选择"
        dialogLayoutId = R.layout.lib_dialog_wheel_layout

        positiveButtonListener = { dialog, _ ->
            _wheelView?.apply {
                if (isScrollSetting) {
                    _selectedIndex = currentItem
                }
            }

            if (_selectedIndex in 0 until (wheelItems?.size ?: 0) &&
                wheelItemSelectorAction.invoke(dialog, _selectedIndex, wheelItems!![_selectedIndex])
            ) {
                //被拦截
            } else {
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.enable(R.id.positive_button, !wheelItems.isNullOrEmpty())

        dialogViewHolder.v<WheelView>(R.id.lib_wheel_view)?.apply {
            _wheelView = this

            val stringList = mutableListOf<CharSequence>()

            for (item in wheelItems ?: emptyList()) {
                stringList.add("${wheelItemToStringAction(item)}")
            }

            setOnItemSelectedListener {
                _selectedIndex = it
                L.v("wheel selected $it")
            }

            adapter = ArrayWheelAdapter(stringList)

            setCyclic(wheelCyclic)

            //wheel 在没有滑动的时候, 是不会触发[SelectedListener]的
            currentItem = if (wheelSelectedIndex in 0 until (wheelItems?.size ?: 0)) {
                wheelSelectedIndex
            } else {
                0
            }
            _selectedIndex = currentItem
        }

        //空数据提示
        dialogViewHolder.visible(R.id.lib_empty_view, wheelItems.isNullOrEmpty())

        //单位
        dialogViewHolder.tv(R.id.lib_unit_view)?.text = wheelUnit
    }

    /**添加Item*/
    fun addDialogItem(any: Any) {
        if (wheelItems is MutableList) {
        } else {
            wheelItems = mutableListOf()
        }
        val list = wheelItems
        if (list is MutableList) {
            list.add(any)
        }
    }
}

/**
 * 3D滚轮选择对话框
 * */
fun Context.wheelDialog(config: WheelDialogConfig.() -> Unit): Dialog {
    return WheelDialogConfig().run {
        configBottomDialog(this@wheelDialog)
        //wheelItems
        //wheelItemSelectorAction
        config()
        show()
    }
}

