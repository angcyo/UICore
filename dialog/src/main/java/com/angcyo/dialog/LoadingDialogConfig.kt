package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder
import java.lang.ref.WeakReference

/**
 * 静态存储对话框对象, 使用[hideLoading]隐藏对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class LoadingDialogConfig : BaseDialogConfig() {

    /**加载提示的文件*/
    var loadingText: CharSequence? by UpdateDialogConfigProperty(null)

    /**加载提示的资源id*/
    var loadingIconResId: Int by UpdateDialogConfigProperty(undefined_res)

    /**是否要显示关闭按钮*/
    var loadingShowCloseView: Boolean by UpdateDialogConfigProperty(true)

    init {
        canceledOnTouchOutside = false
        cancelable = false

        dialogGravity = Gravity.CENTER

        //去掉默认的dialog背景
        dialogBgDrawable = ColorDrawable(Color.TRANSPARENT)

        //去掉变暗
        dimAmount = 0f

        //动画样式
        animStyleResId = R.style.LibDialogAlphaAnimation

        //对话框布局
        dialogLayoutId = R.layout.lib_dialog_flow_loading_layout

        //对话框取消回调监听
        onCancelListener

        dialogType

        onDismissListener = { dialog ->
            LoadingDialog.dialogPool.removeAll {
                it.get() == dialog
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.tv(R.id.lib_text_view)?.text = loadingText
        dialogViewHolder.visible(R.id.lib_text_view, loadingText != null)

        dialogViewHolder.visible(R.id.lib_close_view, loadingShowCloseView)

        if (loadingIconResId != undefined_res) {
            dialogViewHolder.img(R.id.lib_image_view)?.setImageResource(loadingIconResId)
        }

        dialogViewHolder.click(R.id.lib_close_view) {
            dialog.cancel()
        }
    }

    override fun show(type: Int): Dialog {
        return super.show(type).apply {
            LoadingDialog.dialogPool.push(WeakReference(this))
        }
    }
}

/**加载对话框[]*/
fun Context.loadingDialog(
    layout: Int = R.layout.lib_dialog_flow_loading_layout,
    config: LoadingDialogConfig.() -> Unit = {}
): LoadingDialogConfig {
    val dialogConfig = LoadingDialogConfig()
    dialogConfig.dialogContext = this
    dialogConfig.dialogLayoutId = layout
    dialogConfig.config()
    dialogConfig.show()
    return dialogConfig
}