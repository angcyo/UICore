package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import com.angcyo.library.ex.copy
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.slider.SingleColorSliderView
import com.skydoves.colorpickerview.AlphaTileView

/**
 * 简单的渐变颜色选择对话框
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/28
 */
class SingleColorPickerDialogConfig : BaseColorPickerDialog() {

    /**开始的颜色*/
    var pickerStartColor: Int = Color.WHITE

    /**结束的颜色*/
    var pickerEndColor: Int = Color.BLACK

    init {
        dialogLayoutId = R.layout.lib_dialog_single_color_picker_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
        val colorPickerView = dialogViewHolder.v<SingleColorSliderView>(R.id.lib_color_picker_view)
        val alphaTileView = dialogViewHolder.v<AlphaTileView>(R.id.lib_alpha_tile_view)
        val textView = dialogViewHolder.tv(R.id.lib_text_view)

        textView?.setOnClickListener {
            textView.text?.copy(it.context)
        }

        colorPickerView?.apply {
            onColorChangedListener = object : SingleColorSliderView.OnColorChangedListener {
                override fun onColorChanged(newColor: Int, fromUser: Boolean) {
                    selectedColor = newColor
                    alphaTileView?.setPaintColor(selectedColor)
                    textView?.text = String.format("#%08X", selectedColor)
                }
            }
            startColor = pickerStartColor
            endColor = pickerEndColor
            currentColor = initialColor
        }
    }
}

/**
 * 简单颜色选择对话框
 * */
fun Context.singleColorPickerDialog(config: SingleColorPickerDialogConfig.() -> Unit): Dialog {
    return SingleColorPickerDialogConfig().run {
        configBottomDialog(this@singleColorPickerDialog)
        //initialColor
        config()
        show()
    }
}
