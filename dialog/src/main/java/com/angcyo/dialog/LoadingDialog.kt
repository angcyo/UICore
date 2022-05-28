package com.angcyo.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCaller
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.angcyo.dialog.LoadingDialog.dialogPool
import com.angcyo.library.ex.elseNull
import com.angcyo.library.toastQQ
import com.angcyo.transition.dslTransition
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.dslViewHolder
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 快速配置加载对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/07
 */

object LoadingDialog {
    val dialogPool = Stack<WeakReference<Dialog>>()
}

//<editor-fold desc="隐藏对话框">

/**隐藏最后一个dialog*/
fun hideLoading(
    transition: Boolean = false,
    delay: Long = 888,
    onEnd: () -> Unit = {},
    action: DslViewHolder.() -> Unit = {}
) {
    if (dialogPool.isNotEmpty()) {
        dialogPool.pop().get()?.run {
            try {
                if (transition) {
                    //执行转换
                    window?.decorView?.run {
                        val dialogViewHolder = dslViewHolder()

                        dslTransition(this as ViewGroup) {
                            onCaptureEndValues = {
                                dialogViewHolder.action()
                            }
                        }

                        dialogViewHolder.postDelay(delay) {
                            try {
                                //如果此时的Activity提前结束, 将会崩溃.
                                dismiss()
                                onEnd()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }.elseNull {
                        dismiss()
                        onEnd()
                    }
                } else {
                    dismiss()
                    onEnd()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/**将对话框的文本改变, 然后延迟关闭*/
fun hideLoading(text: CharSequence?) {
    if (text.isNullOrEmpty()) {
        hideLoading()
    } else {
        hideLoading(true) {
            tv(R.id.lib_text_view)?.run {
                this.text = text
                translationX = -(view(R.id.lib_loading_view)?.measuredWidth?.toFloat() ?: 0f)
            }
            invisible(R.id.lib_loading_view)
            gone(R.id.lib_close_view)
        }
    }
}

//</editor-fold desc="隐藏对话框">

//<editor-fold desc="中间转菊花的对话框">

/**显示在中间转菊花*/
fun ActivityResultCaller.loading(
    text: CharSequence? = null,
    @LayoutRes layoutId: Int = R.layout.lib_dialog_flow_loading_layout,
    showCloseView: Boolean = true,
    config: DslDialogConfig.() -> Unit = {},
    onCancel: (dialog: Dialog) -> Unit = {}
): Dialog? {
    return try {
        val activity = when (this) {
            is Fragment -> activity
            is Activity -> this
            is Context -> this
            else -> null
        } ?: return null
        activity.loading2(text, layoutId, showCloseView, config, onCancel)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun Context.loading2(
    text: CharSequence? = null,
    @LayoutRes layoutId: Int = R.layout.lib_dialog_flow_loading_layout,
    showCloseView: Boolean = true,
    config: DslDialogConfig.() -> Unit = {},
    onCancel: (dialog: Dialog) -> Unit = {}
): Dialog? {
    return try {
        val activity = this
        DslDialogConfig(activity).run {
            this.onDismissListener = {
                val dialog = it
                dialogPool.removeAll { ref ->
                    ref.get() == dialog
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
            onDialogInitListener = { dialog, dialogViewHolder ->
                dialogViewHolder.tv(R.id.lib_text_view)?.text = text
                dialogViewHolder.visible(R.id.lib_close_view, showCloseView)
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
        e.printStackTrace()
        return null
    }
}

//</editor-fold desc="中间转菊花的对话框">

//<editor-fold desc="底部弹出显示的loading对话框">

/**在底部显示的加载对话框*/
fun ActivityResultCaller.loadingBottom(
    text: CharSequence? = "请稍等...",
    showCloseView: Boolean = true,
    onCancel: (dialog: Dialog) -> Unit = {}
): Dialog? {
    return loading(
        text,
        R.layout.lib_dialog_bottom_loading_layout,
        showCloseView,
        config = {
            dialogGravity = Gravity.BOTTOM
            animStyleResId = R.style.LibDialogBottomTranslateAnimation
            amount = 0.2f
            dialogWidth = -1
        },
        onCancel = onCancel
    )
}

/**快速在[Fragment]显示底部loading, 通常用于包裹一个网络请求*/
fun ActivityResultCaller.loadLoadingBottom(
    tip: CharSequence? = "请稍等...",
    successTip: CharSequence? = "请求完成!",
    showErrorToast: Boolean = false,
    showCloseView: Boolean = true,
    action: (cancel: AtomicBoolean, loadEnd: (data: Any?, error: Throwable?) -> Unit) -> Unit
): Dialog? {
    val isCancel = AtomicBoolean(false)
    val dialog = loadingBottom(tip, showCloseView) {
        isCancel.set(true)
        action(isCancel) { _, _ ->
            //no op
        }
    }

    isCancel.set(false)
    action(isCancel) { data, error ->
        if (error != null) {
            //失败
            if (showErrorToast) {
                toastQQ(error.message)
            }
            hideLoading(error.message)
        } else {
            hideLoading(successTip)
        }
    }

    return dialog
}


/**快速在[Fragment]显示loading, 通常用于包裹一个网络请求*/
fun ActivityResultCaller.loadLoading(
    tip: CharSequence? = null,
    action: (cancel: AtomicBoolean, loadEnd: (data: Any?, error: Throwable?) -> Unit) -> Unit
) {
    val isCancel = AtomicBoolean(false)
    loading(tip) {
        isCancel.set(true)
        action(isCancel) { _, _ ->
            //no op
        }
    }

    isCancel.set(false)
    action(isCancel) { _, error ->
        hideLoading()
        error?.apply {
            toastQQ(message)
        }
    }
}

//</editor-fold desc="底部弹出显示的loading对话框">

/**快速显示[loading]对话框*/
data class LoadingConfig(
    var loadingText: CharSequence? = null,
    @LayoutRes
    var loadingLayoutId: Int = R.layout.lib_dialog_flow_loading_layout,
    var loadingShowCloseView: Boolean = true,
    var loadingConfig: DslDialogConfig.() -> Unit = {},
    var onLoadingCancel: (dialog: Dialog) -> Unit = {}
)

/**快速显示[loading]对话框*/
fun ActivityResultCaller.dslLoading(
    bottom: Boolean = false,
    action: LoadingConfig.() -> Unit = {}
): Dialog? {
    val config = LoadingConfig()
    if (bottom) {
        config.loadingLayoutId = R.layout.lib_dialog_bottom_loading_layout
    }
    config.action()
    return when (this) {
        is Fragment -> activity
        is Activity -> this
        else -> null
    }?.loading(
        config.loadingText,
        config.loadingLayoutId,
        config.loadingShowCloseView,
        {
            if (bottom) {
                dialogGravity = Gravity.BOTTOM
                animStyleResId = R.style.LibDialogBottomTranslateAnimation
                amount = 0.2f
                dialogWidth = -1
            }
            config.loadingConfig(this)
        },
        {
            config.onLoadingCancel(it)
        }
    )
}