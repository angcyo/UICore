package com.angcyo.core.component

import android.app.Dialog
import android.content.Context
import com.angcyo.core.R
import com.angcyo.core.component.model.DataShareModel
import com.angcyo.core.dslitem.DslFileSelectorItem
import com.angcyo.core.vmApp
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.annotation.DSL
import com.angcyo.library.component.pad.isInPadMode
import com.angcyo.library.ex._color
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.open
import com.angcyo.library.ex.tintDrawableColor
import com.angcyo.widget.DslViewHolder
import java.io.File
import kotlin.math.min

/**
 * 接收文件的对话框配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ReceiveFileDialogConfig(context: Context? = null) : DslDialogConfig(context) {

    /**接收到的文件*/
    private var receiveFile: File? = null

    init {
        //dialogTitle = "服务接口:xxx"
        dialogLayoutId = R.layout.dialog_receive_file
        canceledOnTouchOutside = false
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.img(R.id.lib_file_icon_view)?.setImageDrawable(
            _drawable(R.drawable.core_file_receive_icon).tintDrawableColor(
                _color(R.color.colorAccent)
            )
        )

        //取消服务
        dialogViewHolder.click(R.id.lib_cancel_button) {
            dialog.cancel()
        }

        dialogViewHolder.click(R.id.lib_file_icon_view) {
            receiveFile?.open(it.context) //打开文件
        }

        //监听文件
        vmApp<DataShareModel>().shareFileOnceData.observe(this) {
            it?.let {
                receiveFile = it
                val name = it.name
                dialogViewHolder.visible(R.id.lib_file_name_view)
                dialogViewHolder.tv(R.id.lib_file_name_view)?.text = name
                dialogViewHolder.img(R.id.lib_file_icon_view)
                    ?.setImageResource(DslFileSelectorItem.getFileIconRes(name))
            }
        }

        //监听文本
        vmApp<DataShareModel>().shareTextOnceData.observe(this) {
            it?.let {
                dialogViewHolder.visible(R.id.lib_body_wrap_view)
                dialogViewHolder.tv(R.id.lib_body_view)?.text = it
            }
        }
    }
}

@DSL
fun Context.receiveFileDialogConfig(config: ReceiveFileDialogConfig.() -> Unit): Dialog {
    return ReceiveFileDialogConfig(this).run {
        dialogWidth = -1
        if (isInPadMode()) {
            dialogWidth = min(_screenWidth, _screenHeight)
        }
        config()
        show()
    }
}