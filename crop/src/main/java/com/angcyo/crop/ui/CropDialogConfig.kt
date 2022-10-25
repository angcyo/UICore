package com.angcyo.crop.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import com.angcyo.crop.R
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.widget.DslViewHolder

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/22
 */
class CropDialogConfig(context: Context? = null) : DslDialogConfig(context), ICropProvide {

    /**布局控制*/
    val cropLayoutHelper = CropLayoutHelper(this)

    /**需要裁剪的图片*/
    var cropBitmap: Bitmap? = null

    /**裁剪图片的回调*/
    var onCropResultAction: (cropBitmap: Bitmap?) -> Unit = {}

    init {
        dialogLayoutId = R.layout.lib_crop_dialog_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //
        cropLayoutHelper.initLayout(dialogViewHolder)

        //剪切图片源
        cropBitmap?.let {
            cropLayoutHelper.cropView?.cropDelegate?.updateBitmap(it)
        }

        //
        dialogViewHolder.click(R.id.lib_cancel_view) {
            dialog.cancel()
        }
        dialogViewHolder.click(R.id.lib_confirm_view) {
            cropLayoutHelper.cropDelegate?.crop()?.let {
                onCropResultAction(it)
            }
            dialog.dismiss()
        }
    }

    override fun onDialogCancel(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.onDialogCancel(dialog, dialogViewHolder)
        onCropResultAction(null)
    }

    override fun getOriginCropBitmap(): Bitmap? = cropBitmap

}

/** 底部弹出图片裁剪对话框 */
fun Context.cropDialog(config: CropDialogConfig.() -> Unit): Dialog {
    return CropDialogConfig().run {
        configBottomDialog(this@cropDialog)
        dialogWidth = -1
        dialogHeight = -1
        dialogThemeResId = R.style.LibDialogBaseFullTheme
        config()
        //cropBitmap
        //onCropResultAction
        show()
    }
}