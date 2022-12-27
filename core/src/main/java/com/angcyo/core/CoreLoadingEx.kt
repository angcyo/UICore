package com.angcyo.core

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.angcyo.coroutine.launchLifecycle
import com.angcyo.coroutine.withBlock
import com.angcyo.dialog.LoadingDialog.LOADING_TIMEOUT
import com.angcyo.dialog.hideLoading
import com.angcyo.dialog.loading
import com.angcyo.drawable.loading.TGStrokeLoadingDrawable
import com.angcyo.library.IActivityProvider
import com.angcyo.library.L
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.setBgDrawable
import com.angcyo.library.ex.toColorInt
import com.angcyo.library.toastQQ
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/09
 */

/**异步加载, 带loading dialog*/
fun <T> LifecycleOwner.loadingAsyncTg(block: () -> T?, action: (T?) -> Unit) {
    when (val context = this) {
        is ActivityResultCaller -> context.tgStrokeLoadingCaller { cancel, loadEnd ->
            context.launchLifecycle {
                val result = withBlock { block() }
                action(result)
                loadEnd(result, null)
            }
        }
        else -> {
            var activity: Context? = null
            if (context is Context) {
                activity = context
            } else if (context is IActivityProvider) {
                activity = context.getActivityContext()
            }
            if (activity == null) {
                L.w("context is not ActivityResultCaller!")
            } else {
                activity.tgStrokeLoading { cancel, loadEnd ->
                    context.launchLifecycle {
                        val result = withBlock { block() }
                        action(result)
                        loadEnd(result, null)
                    }
                }
            }
        }
    }
}

/**
 * 当异步执行多久之后, 仍未返回结果时, 则自动显示loading
 * [timeout] 异步执行超时时长, 毫秒
 * */
fun <T> LifecycleOwner.loadingAsyncTgTimeout(
    block: () -> T?,
    timeout: Long = LOADING_TIMEOUT,
    action: (T?) -> Unit = {}
) {
    loadingAsyncTimeout(block, { context ->
        if (context is ActivityResultCaller) {
            context.tgStrokeLoadingCaller { cancel, loadEnd ->
                //no op
            }
        } else {
            L.w("context is not ActivityResultCaller!")
            null
        }
    }, timeout, action)
}

/**
 * 当异步执行多久之后, 仍未返回结果时, 则自动显示loading
 * [timeoutAction] 超时时, 需要执行的代码块, 如果返回的是[Dialog]则会自动管理
 * [timeout] 异步执行超时时长, 毫秒
 * */
fun <T> LifecycleOwner.loadingAsyncTimeout(
    block: () -> T?,
    timeoutAction: (owner: LifecycleOwner) -> Any?,
    timeout: Long = LOADING_TIMEOUT,
    action: (T?) -> Unit = {}
) {
    val context = this
    var dialog: Any? = null
    val runnable = Runnable {
        dialog = timeoutAction(context) /*超时执行*/
    }
    MainExecutor.handler.postDelayed(runnable, timeout)//延迟显示loading
    context.launchLifecycle {
        val result = withBlock { block() /*后台执行*/ }
        MainExecutor.handler.removeCallbacks(runnable)
        if (dialog is Dialog) {
            hideLoading(dialog as Dialog)
        }
        action(result) /*前台执行*/
    }
}

//---

/**
 * TGStrokeLoadingDrawable 加载样式的loading
 * [cancel] 是否允许被取消*/
fun ActivityResultCaller.tgStrokeLoadingCaller(
    cancel: Boolean = false,
    showErrorToast: Boolean = false,
    action: (isCancel: AtomicBoolean, loadEnd: (data: Any?, error: Throwable?) -> Unit) -> Unit
): Dialog? {
    return try {
        val activity = when (this) {
            is Fragment -> activity
            is Activity -> this
            is Context -> this
            is IActivityProvider -> getActivityContext()
            else -> null
        } ?: return null
        activity.tgStrokeLoading(cancel, showErrorToast, action)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**扩展的对象不一样
 * [Context]*/
fun Context.tgStrokeLoading(
    cancel: Boolean = false,
    showErrorToast: Boolean = false,
    action: (isCancel: AtomicBoolean, loadEnd: (data: Any?, error: Throwable?) -> Unit) -> Unit
): Dialog? {
    val isCancel = AtomicBoolean(false)
    val dialog = loading(layoutId = R.layout.lib_tg_stroke_loading_layout, config = {
        cancelable = cancel
        onDialogInitListener = { dialog, dialogViewHolder ->
            val loadingDrawable = TGStrokeLoadingDrawable().apply {
                loadingOffset = 6 * dp
                loadingWidth = 6 * dp
                indeterminateSweepAngle = 1f
                loadingBgColor = "#ffffff".toColorInt()
                loadingColor = loadingBgColor
            }
            dialogViewHolder.view(R.id.lib_loading_view)?.setBgDrawable(loadingDrawable)
        }
    }) { dialog ->
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
            hideLoading()
        }
    }

    return dialog
}