package com.angcyo.dialog2

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.library.L
import com.angcyo.library.ex._string
import com.angcyo.library.ex.copy
import com.angcyo.widget.DslViewHolder
import com.jaredrummler.android.colorpicker.ColorPickerView
import com.skydoves.colorpickerview.AlphaTileView

/**
 * HSV: 色调、饱和度和值
 *
 * Hue 色调
 * Saturation 饱和度
 * Value 值
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class HsvColorPickerDialogConfig : BaseDialogConfig() {

    /**默认的颜色*/
    var initialColor: Int = Color.WHITE

    /**选中的颜色*/
    var selectedColor: Int = Color.TRANSPARENT

    /**是否显示透明滑块*/
    var showAlphaSlider: Boolean = true

    /**选中回调, 返回true拦截默认操作*/
    var colorPickerAction: (dialog: Dialog, color: Int) -> Boolean =
        { dialog, color ->
            L.i("选中颜色->$color")
            false
        }

    init {
        dialogTitle = _string(R.string.dialog_color_picker)
        dialogLayoutId = R.layout.lib_dialog_hsv_color_picker_layout

        positiveButtonListener = { dialog, _ ->
            if (colorPickerAction.invoke(dialog, selectedColor)) {
                //被拦截
            } else {
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
        val colorPickerView = dialogViewHolder.v<ColorPickerView>(R.id.lib_color_picker_view)
        val alphaTileView =
            dialogViewHolder.v<AlphaTileView>(R.id.lib_alpha_tile_view)
        val textView = dialogViewHolder.tv(R.id.lib_text_view)

        textView?.setOnClickListener {
            textView.text?.copy(it.context)
        }

        colorPickerView?.apply {
            setOnColorChangedListener {
                selectedColor = it
                alphaTileView?.setPaintColor(selectedColor)
                if (showAlphaSlider) {
                    textView?.text = String.format("#%08X", selectedColor)
                } else {
                    textView?.text = String.format("#%06X", 0xFFFFFF and selectedColor)
                }
            }
            setAlphaSliderVisible(showAlphaSlider)
            setColor(initialColor, true)
        }
    }
}

/**
 * HSV颜色选择对话框
 * */
fun Context.hsvColorPickerDialog(config: HsvColorPickerDialogConfig.() -> Unit): Dialog {
    return HsvColorPickerDialogConfig().run {
        configBottomDialog(this@hsvColorPickerDialog)
        //initialColor
        config()
        show()
    }
}
