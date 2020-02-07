package com.angcyo.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.angcyo.dialog.LoadingDialog.dialogPool
import com.angcyo.library.L
import java.lang.ref.WeakReference
import java.util.*

/**
 * 快速配置加载对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/07
 */
object LoadingDialog {
    val dialogPool = Stack<WeakReference<Dialog>>()
}

/**隐藏最后一个dialog*/
fun hideLoading() {
    if (dialogPool.isNotEmpty()) {
        dialogPool.pop().get()?.dismiss()
    }
}

/**显示在中间转菊花*/
fun Activity.loading(
    text: CharSequence? = null,
    @LayoutRes layoutId: Int = R.layout.lib_dialog_flow_loading_layout,
    config: DslDialogConfig.() -> Unit = {},
    onCancel: (dialog: Dialog) -> Unit = {}
) {
    loading(this, text, layoutId, config, onCancel)
}

/**显示在中间转菊花*/
fun Fragment.loading(
    text: CharSequence? = null,
    @LayoutRes layoutId: Int = R.layout.lib_dialog_flow_loading_layout,
    config: DslDialogConfig.() -> Unit = {},
    onCancel: (dialog: Dialog) -> Unit = {}
) {
    activity?.run { loading(this, text, layoutId, config, onCancel) }
}

/**在底部显示的加载对话框*/
fun Fragment.loading(text: CharSequence = "加载中...", onCancel: (dialog: Dialog) -> Unit = {}) {
    activity?.loading(text, R.layout.lib_dialog_bottom_loading_layout, config = {
        dialogGravity = Gravity.BOTTOM
        animStyleResId = R.style.LibDialogBottomTranslateAnimation
        amount = 0.2f
        dialogWidth = -1
    }, onCancel = onCancel)
}

/**快速显示[loading]对话框*/
fun loading(
    activity: Activity?,
    text: CharSequence? = null,
    @LayoutRes layoutId: Int = R.layout.lib_dialog_flow_loading_layout,
    config: DslDialogConfig.() -> Unit = {},
    onCancel: (dialog: Dialog) -> Unit = {}
): Dialog? {
    return try {
        DslDialogConfig(activity).run {
            this.onDismissListener = {
                val dialog = it
                dialogPool.removeAll {
                    it.get() == dialog
                }
            }
            //取消监听, dismiss不触发cancel
            this.onCancelListener = onCancel
            //布局
            this.dialogLayoutId = layoutId
            //不允许外部点击关闭
            this.canceledOnTouchOutside = false
            //去掉默认的dialog背景
            dialogBgDrawable = ColorDrawable(Color.TRANSPARENT)
            //去掉变暗
            amount = 0f
            //动画样式
            animStyleResId = R.style.LibDialogAlphaAnimation
            //初始化布局
            onInitListener = { dialog, dialogViewHolder ->
                dialogViewHolder.tv(R.id.lib_text_view)?.text = text
                dialogViewHolder.click(R.id.lib_close_view) {
                    dialog.cancel()
                }
            }
            config()
            if (activity is AppCompatActivity) {
                showCompatDialog()
            } else {
                showDialog()
            }.apply {
                dialogPool.push(WeakReference(this))
            }
        }
    } catch (e: Exception) {
        L.w(e)
        return null
    }
}