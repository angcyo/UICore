package com.angcyo.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultCaller
import androidx.annotation.AnyThread
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.angcyo.dialog.LoadingDialog.dialogPool
import com.angcyo.dialog.LoadingDialog.removeDialog
import com.angcyo.drawable.base.BaseProgressDrawable
import com.angcyo.library.IActivityProvider
import com.angcyo.library.ex.elseNull
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.getLongNum
import com.angcyo.transition.dslTransition
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.dslViewHolder
import com.angcyo.widget.progress.DslProgressBar
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

    /**超时多久之后, 显示loading对话框, 默认1秒*/
    const val LOADING_TIMEOUT = 1_000L

    /**对话框的池子*/
    val dialogPool = Stack<WeakReference<Dialog>>()

    /**移除对话框*/
    fun removeDialog(dialog: Dialog?) {
        dialog?.let {
            try {
                for (element in dialogPool) {
                    if (element.get() == it) {
                        try {
                            dialogPool.remove(element)
                        } catch (e: Exception) {
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}

//<editor-fold desc="隐藏对话框">

/**最后个对话框[Dialog]*/
fun lastDialog(): Dialog? = dialogPool.lastOrNull()?.get()

/**隐藏最后一个dialog*/
@AnyThread
fun hideLoading(
    transition: Boolean = false,
    delay: Long = 888,
    onEnd: () -> Unit = {},
    action: DslViewHolder.() -> Unit = {}
) {
    if (dialogPool.isNotEmpty()) {
        var dialog: Dialog? = null
        while (true) {
            val get = dialogPool.pop().get()
            if (get?.isShowing == true) {
                dialog = get
                break
            }
            if (dialogPool.isEmpty()) {
                break
            }
        }
        hideLoading(dialog, transition, delay, onEnd, action)
    }
}

/**将对话框的文本改变, 然后延迟关闭*/
@AnyThread
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

/**隐藏对话框*/
fun hideLoading(
    dialog: Dialog?,
    transition: Boolean = false,
    delay: Long = 888,
    onEnd: () -> Unit = {},
    action: DslViewHolder.() -> Unit = {}
) {
    dialog?.apply {
        try {
            removeDialog(this)
            if (transition) {
                //执行转换
                window?.decorView?.apply {
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

/**更新加载进度*/
fun updateLoadingProgress(progress: String?) {
    lastDialog()?.window?.decorView?.dslViewHolder()?.apply {
        post {
            val view = view(R.id.lib_progress_view)
            if (view == null) {
                updateLoadingProgress(view(R.id.lib_loading_view), progress)
            } else {
                updateLoadingProgress(view, progress)
            }
        }
    }
}

/**[updateLoadingProgress]
 * [view] 要操作的视图
 * [progress] 进度文本*/
fun updateLoadingProgress(view: View?, progress: String?) {
    view?.isVisible = progress != null
    if (view is TextView) {
        view.text = progress
    } else if (view is DslProgressBar) {
        view.setProgress(progress.getLongNum()?.toFloat() ?: 0f)
    } else {
        val background = view?.background
        if (background is BaseProgressDrawable) {
            val p = progress?.toIntOrNull()
            if (p != null) {
                background.isIndeterminate = p <= 0
                background.progress = p
            }
        }
    }
}


//</editor-fold desc="隐藏对话框">

//<editor-fold desc="中间转菊花的对话框">

/**显示在中间转菊花*/
fun ActivityResultCaller.loadingCaller(
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
            is IActivityProvider -> getActivityContext()
            else -> null
        } ?: return null
        activity.loading(text, layoutId, showCloseView, config, onCancel)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun Context.loading(
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
            dimAmount = 0f
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
fun ActivityResultCaller.loadingBottomCaller(
    text: CharSequence? = "请稍等...",
    showCloseView: Boolean = true,
    onCancel: (dialog: Dialog) -> Unit = {}
): Dialog? {
    return loadingCaller(
        text,
        R.layout.lib_dialog_bottom_loading_layout,
        showCloseView,
        config = {
            dialogGravity = Gravity.BOTTOM
            animStyleResId = R.style.LibDialogBottomTranslateAnimation
            dimAmount = 0.2f
            dialogWidth = -1
        },
        onCancel = onCancel
    )
}

/**快速在[Fragment]显示底部loading, 通常用于包裹一个网络请求*/
fun ActivityResultCaller.loadLoadingBottomCaller(
    tip: CharSequence? = "请稍等...",
    successTip: CharSequence? = "请求完成!",
    showErrorToast: Boolean = false,
    showCloseView: Boolean = true,
    action: (cancel: AtomicBoolean, loadEnd: (data: Any?, error: Throwable?) -> Unit) -> Unit
): Dialog? {
    val isCancel = AtomicBoolean(false)
    val dialog = loadingBottomCaller(tip, showCloseView) {
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
fun ActivityResultCaller.loadLoadingCaller(
    tip: CharSequence? = null,
    action: (cancel: AtomicBoolean, loadEnd: (data: Any?, error: Throwable?) -> Unit) -> Unit
) {
    val isCancel = AtomicBoolean(false)
    loadingCaller(tip) {
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

/**快速显示[loadingCaller]对话框*/
data class LoadingConfig(
    var loadingText: CharSequence? = null,
    @LayoutRes
    var loadingLayoutId: Int = R.layout.lib_dialog_flow_loading_layout,
    var loadingShowCloseView: Boolean = true,
    var loadingConfig: DslDialogConfig.() -> Unit = {},
    var onLoadingCancel: (dialog: Dialog) -> Unit = {}
)

/**快速显示[loadingCaller]对话框*/
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
    }?.loadingCaller(
        config.loadingText,
        config.loadingLayoutId,
        config.loadingShowCloseView,
        {
            if (bottom) {
                dialogGravity = Gravity.BOTTOM
                animStyleResId = R.style.LibDialogBottomTranslateAnimation
                dimAmount = 0.2f
                dialogWidth = -1
            }
            config.loadingConfig(this)
        },
        {
            config.onLoadingCancel(it)
        }
    )
}