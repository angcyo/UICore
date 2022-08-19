package com.angcyo.doodle.ui

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.doodle.R
import com.angcyo.widget.DslViewHolder

/**
 * 涂鸦界面弹窗
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
class DoodleDialogConfig(context: Context? = null) : DslDialogConfig(context) {

    val doodleUI = DoodleUI()

    init {
        dialogLayoutId = R.layout.lib_doodle_dialog_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //
        doodleUI.initUI(dialogViewHolder)
    }

}

/** 底部弹出涂鸦对话框 */
fun Context.doodleDialog(config: DoodleDialogConfig.() -> Unit): Dialog {
    return DoodleDialogConfig().run {
        configBottomDialog(this@doodleDialog)
        dialogWidth = -1
        dialogHeight = -1
        config()
        show()
    }
}