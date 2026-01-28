package com.angcyo.doodle.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
import com.angcyo.core.loadingAsyncTg
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.dialog.configFullScreenDialog
import com.angcyo.doodle.R
import com.angcyo.doodle.core.IDoodleListener
import com.angcyo.doodle.element.BaseElement
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.dsladapter.findItemByTag
import com.angcyo.library._screenWidth
import com.angcyo.widget.DslViewHolder

/**
 * 涂鸦界面弹窗
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
class DoodleDialogConfig(context: Context? = null) : DslDialogConfig(context) {

    val doodleLayoutHelper = DoodleLayoutHelper(this)

    /**返回的回调*/
    var onDoodleResultAction: (Bitmap) -> Unit = {}

    init {
        dialogLayoutId = R.layout.lib_doodle_dialog_layout
        softInputMode = SOFT_INPUT_ADJUST_NOTHING
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //
        doodleLayoutHelper.doodleItemWidth =/* if (isDebug()) _screenWidth / 4 else */
            _screenWidth / 3
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
                    getPreviewBitmap()
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

        //confirm
        dialogViewHolder.enable(R.id.confirm_button, false)
    }

    fun updateConfirmButton(dialogViewHolder: DslViewHolder) {
        val haveElement =
            doodleLayoutHelper.doodleView?.doodleDelegate?.doodleLayerManager?.haveElement() == true
        dialogViewHolder.enable(R.id.confirm_button, haveElement)

        doodleLayoutHelper._doodleItemAdapter?.findItemByTag(DoodleLayoutHelper.TAG_DOODLE_AI_DRAW)
            ?.apply {
                itemEnable = haveElement
                updateAdapterItem()
            }
    }

}

/** 底部弹出涂鸦对话框 */
fun Context.doodleDialog(config: DoodleDialogConfig.() -> Unit): Dialog {
    return DoodleDialogConfig().run {
        configFullScreenDialog(this@doodleDialog)
        config()
        show()
    }
}