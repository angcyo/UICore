package com.angcyo.doodle.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import com.angcyo.bitmap.handle.BitmapHandle
import com.angcyo.core.loadingAsyncTg
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.doodle.R
import com.angcyo.doodle.core.IDoodleListener
import com.angcyo.doodle.element.BaseElement
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.widget.DslViewHolder

/**
 * 涂鸦界面弹窗
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
class DoodleDialogConfig(context: Context? = null) : DslDialogConfig(context) {

    val doodleLayoutHelper = DoodleLayoutHelper()

    /**返回的回调*/
    var onDoodleResultAction: (Bitmap) -> Unit = {}

    init {
        dialogLayoutId = R.layout.lib_doodle_dialog_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //
        doodleLayoutHelper.initLayout(dialogViewHolder)
        doodleLayoutHelper.doodleView?.doodleDelegate?.doodleListenerList?.add(object :
            IDoodleListener {

            override fun onLayerAdd(layer: BaseLayer) {
                updateConfirmButton(dialogViewHolder)
            }

            override fun onLayerRemove(layer: BaseLayer) {
                updateConfirmButton(dialogViewHolder)
            }

            override fun onElementAttach(elementList: List<BaseElement>, layer: BaseLayer) {
                updateConfirmButton(dialogViewHolder)
            }

            override fun onElementDetach(elementList: List<BaseElement>, layer: BaseLayer) {
                updateConfirmButton(dialogViewHolder)
            }
        })

        //确定
        dialogViewHolder.click(R.id.confirm_button) {
            doodleLayoutHelper.doodleView?.doodleDelegate?.apply {
                doodleLayerManager.backgroundLayer = null//不要背景

                //async
                loadingAsyncTg({
                    /*getPreviewBitmap().trimEdgeColor()*/
                    BitmapHandle.trimEdgeColor(getPreviewBitmap(), 200)
                }) { bitmap ->
                    onDoodleResultAction(bitmap!!)
                    //
                    dialog.dismiss()
                }
            }
        }

        //back
        dialogViewHolder.click(R.id.lib_title_back_view) {
            dialog.dismiss()
        }
    }

    fun updateConfirmButton(dialogViewHolder: DslViewHolder) {
        dialogViewHolder.enable(
            R.id.confirm_button,
            doodleLayoutHelper.doodleView?.doodleDelegate?.doodleLayerManager?.haveElement() == true
        )
    }

}

/** 底部弹出涂鸦对话框 */
fun Context.doodleDialog(config: DoodleDialogConfig.() -> Unit): Dialog {
    return DoodleDialogConfig().run {
        configBottomDialog(this@doodleDialog)
        dialogWidth = -1
        dialogHeight = -1
        dialogThemeResId = R.style.LibDialogBaseFullTheme
        config()
        show()
    }
}