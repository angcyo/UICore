package com.angcyo.dialog

import android.app.Dialog
import android.graphics.Color
import com.angcyo.library.L
import com.angcyo.library.ex._string
import com.angcyo.widget.DslViewHolder

/**
 * 颜色选择对话框基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/28
 */
abstract class BaseColorPickerDialog : BaseDialogConfig() {

    /**默认的颜色*/
    var initialColor: Int = Color.WHITE

    /**选中的颜色*/
    var selectedColor: Int = Color.TRANSPARENT

    /**选中回调, 返回true拦截默认操作*/
    var colorPickerResultAction: (dialog: Dialog, color: Int) -> Boolean =
        { dialog, color ->
            L.i("选中颜色->$color")
            false
        }

    init {
        dialogTitle = _string(R.string.dialog_color_picker)

        positiveButtonListener = { dialog, _ ->
            if (colorPickerResultAction.invoke(dialog, selectedColor)) {
                //被拦截
            } else {
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
    }

}