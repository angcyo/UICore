package com.angcyo.item

import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ButtonStyleConfig
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.button

/**
 * 表单提交按钮布局item, 2个按钮
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/25
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslSubmitButtonItem : DslAdapterItem() {

    //<editor-fold desc="左边的button">

    /**样式*/
    var itemSaveButtonText: CharSequence? = null
        set(value) {
            field = value
            itemSaveButtonStyle.text = value
        }

    var itemSaveButtonStyle = ButtonStyleConfig().apply {
        textGravity = Gravity.CENTER
        fillStyle()
    }

    /**回调*/
    var saveAction: () -> Unit = {}

    //</editor-fold desc="左边的button">

    //<editor-fold desc="右边的button">

    /**样式*/
    var itemSubmitButtonText: CharSequence? = null
        set(value) {
            field = value
            itemSubmitButtonStyle.text = value
        }

    var itemSubmitButtonStyle = ButtonStyleConfig().apply {
        textGravity = Gravity.CENTER
        themeStyle()
    }

    /**回调*/
    var submitAction: () -> Unit = {}

    //<editor-fold desc="右边的button">

    init {
        itemLayoutId = R.layout.dsl_submit_button_item
        itemSaveButtonText = "保存"
        itemSubmitButtonText = "提交"
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.button(R.id.save_button)?.apply {
            itemSaveButtonStyle.updateStyle(this)
        }
        itemHolder.click(R.id.save_button) {
            saveAction()
        }

        itemHolder.button(R.id.submit_button)?.apply {
            itemSubmitButtonStyle.updateStyle(this)
        }
        itemHolder.click(R.id.submit_button) {
            submitAction()
        }
    }

    open fun configSaveButtonStyle(action: ButtonStyleConfig.() -> Unit) {
        itemSaveButtonStyle.action()
    }

    open fun configSubmitButtonStyle(action: ButtonStyleConfig.() -> Unit) {
        itemSubmitButtonStyle.action()
    }
}