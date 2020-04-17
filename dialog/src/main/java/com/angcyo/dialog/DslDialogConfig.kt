package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.view.*
import android.view.WindowManager.LayoutParams.*
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.angcyo.base.dslAHelper
import com.angcyo.dialog.activity.DialogActivity
import com.angcyo.library.L
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.undefined_float
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.replace
import java.io.Serializable

/**
 * 标准对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */
open class DslDialogConfig(@Transient var dialogContext: Context? = null) : Serializable {

    companion object {

        /**最普通的对话框*/
        const val DIALOG_TYPE_DIALOG = 0
        const val DIALOG_TYPE_APPCOMPAT = 1
        const val DIALOG_TYPE_ALERT_DIALOG = 2

        /**需要[material]库支持*/
        const val DIALOG_TYPE_BOTTOM_SHEET_DIALOG = 3

        /**使用Dialog样式的Activity显示, 只能传递显示简单的对话框*/
        const val DIALOG_TYPE_ACTIVITY = 4

        /**
         * 测量 layout 或者 view 的大小
         *
         * @param layoutId 二选一
         * @param view     二选一
         */
        fun measureSize(context: Context, layoutId: Int, view: View?): IntArray {
            val measureView: View =
                view ?: LayoutInflater.from(context).inflate(layoutId, FrameLayout(context), false)
            var originWidth = -1
            var originHeight = -1
            val width: Int
            val height: Int
            val layoutParams = measureView.layoutParams
            if (layoutParams != null) {
                if (layoutParams.width > 0) {
                    originWidth = layoutParams.width
                }
                if (layoutParams.height > 0) {
                    originHeight = layoutParams.height
                }
            }
            measureView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            measureView.measure(
                View.MeasureSpec.makeMeasureSpec(
                    0,
                    View.MeasureSpec.UNSPECIFIED
                ),
                View.MeasureSpec.makeMeasureSpec(
                    0,
                    View.MeasureSpec.UNSPECIFIED
                )
            )
            width = if (originWidth != -1) {
                originWidth
            } else {
                measureView.measuredWidth
            }
            height = if (originHeight != -1) {
                originHeight
            } else {
                measureView.measuredHeight
            }
            return intArrayOf(width, height)
        }

        /**
         * 重置dialog的大小
         */
        fun resetDialogSize(dialog: Dialog?, width: Int, height: Int) {
            if (dialog != null) {
                val window = dialog.window
                window?.setLayout(width, height)
            }
        }
    }

    @LayoutRes
    var dialogLayoutId = -1

    /**自定义的对话框标题*/
    @LayoutRes
    var dialogTitleLayoutId = -1

    /** 优先使用 contentView, 其次再使用 layoutId */
    var dialogContentView: View? = null

    /** 是否可以cancel */
    var cancelable = true
        set(value) {
            field = value
            if (!value) {
                canceledOnTouchOutside = false
            }
        }

    var canceledOnTouchOutside = true
        set(value) {
            field = value
            if (value) {
                cancelable = true
            }
        }

    var dialogTitle: CharSequence? = null
    var dialogMessage: CharSequence? = null
    var onDialogInitListener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit =
        { _, _ -> }

    @Transient
    var dialogBgDrawable: Drawable? = null
    var dialogWidth: Int = undefined_res
    var dialogHeight: Int = undefined_res
    var dialogGravity: Int = undefined_res

    //测试好像没效果.
    var statusBarColor: Int = undefined_res
    var navigationBarColor: Int = undefined_res

    var onConfigWindow: (Window) -> Unit = {}

    /** 自动计算宽高 */
    var autoWidthHeight = false

    /**
     * 对话框变暗指数, [0,1]
     * 0表示, 不变暗
     * 1表示, 全暗
     * undefined_res, 默认
     */
    var amount: Float = undefined_float

    /** window动画资源 */
    @StyleRes
    var animStyleResId: Int = R.style.LibDialogAnimation

    /**创建对话框时的主题*/
    @StyleRes
    var dialogThemeResId: Int = R.style.LibDialogStyle

    /**
     * 显示dialog的类型
     * [AppCompatDialog] [AlertDialog] [BottomSheetDialog]
     * */
    var dialogType = DIALOG_TYPE_APPCOMPAT

    /** 系统默认3个按钮设置 */
    var positiveButtonText: CharSequence? = null
    var negativeButtonText: CharSequence? = null
    var neutralButtonText: CharSequence? = null

    var positiveButtonListener: ((dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit)? =
        { dialog, _ ->
            dialog.dismiss()
        }

    var negativeButtonListener: ((dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit)? =
        { dialog, _ ->
            dialog.cancel()
        }

    var neutralButtonListener: ((dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit)? = null

    var onDismissListener: ((dialog: Dialog) -> Unit)? = null
    var onCancelListener: ((dialog: Dialog) -> Unit)? = null

    open fun neutralButton(
        text: CharSequence? = neutralButtonText,
        listener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit
    ) {
        neutralButtonText = text
        neutralButtonListener = listener
    }

    open fun negativeButton(
        text: CharSequence? = negativeButtonText,
        listener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit
    ) {
        negativeButtonText = text
        negativeButtonListener = listener
    }

    open fun positiveButton(
        text: CharSequence? = positiveButtonText,
        listener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit
    ) {
        positiveButtonText = text
        positiveButtonListener = listener
    }

    /**
     * https://developer.android.google.cn/reference/android/view/Window.html
     *
     * @see Window.requestFeature
     */
    var windowFeature = Window.FEATURE_NO_TITLE

    /**
     * 正数表示addFlags, 负数表示clearFlags
     *
     * @see WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
     *
     * @see WindowManager.LayoutParams.FLAG_DIM_BEHIND
     *
     * @see WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
     *
     * @see WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN
     */
    var windowFlags: IntArray? = null

    /** 宽度全屏 */
    fun setWidthFullScreen() {
        setDialogBgColor(Color.TRANSPARENT)
        dialogWidth = -1
    }

    fun setDialogBgColor(@ColorInt color: Int) {
        dialogBgDrawable = ColorDrawable(color)
    }

    fun setDialogBgResource(@DrawableRes drawable: Int) {
        dialogBgDrawable = _drawable(drawable)
    }

    //保存[Dialog]对象
    var _dialog: Dialog? = null

    @Throws
    open fun showAndConfigDialog(dialog: Dialog): Dialog {
        if (dialogContext == null) {
            throw NullPointerException("context is null.")
        }

        dialog.setCancelable(cancelable)
        if (!TextUtils.isEmpty(dialogTitle)) {
            dialog.setTitle(dialogTitle)
        }
        if (onDismissListener != null) {
            dialog.setOnDismissListener {
                onDismissListener?.invoke(it as Dialog)
            }
        }
        if (onCancelListener != null) {
            dialog.setOnCancelListener {
                onCancelListener?.invoke(it as Dialog)
            }
        }
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        val window = dialog.window
        val decorView: View
        configDialog(dialog)
        if (dialog is AlertDialog) {
        } else {
            if (dialogContentView != null) {
                dialog.setContentView(dialogContentView!!)
            } else if (dialogLayoutId != -1) {
                dialog.setContentView(dialogLayoutId)
            }
        }

        //显示对话框
        dialog.show()

        if (window != null) {
            configWindowAfter(window)

            decorView = window.decorView
            val viewHolder = DslViewHolder(decorView)
            initDialogView(dialog, viewHolder)
            onDialogInitListener(dialog, viewHolder)
        }

        return dialog
    }

    /** 配置window特性, 需要在setContentView之前调用 */
    open fun configDialog(dialog: Dialog) {
        _dialog = dialog
        val window = dialog.window
        if (dialog is AppCompatDialog) {
            dialog.supportRequestWindowFeature(windowFeature)
        } else {
            dialog.requestWindowFeature(windowFeature)
        }

        window?.run { configWindow(this) }
    }

    open fun configWindow(window: Window) {
        if (windowFlags != null) {
            for (flag in windowFlags!!) {
                if (flag >= 0) {
                    window.addFlags(flag)
                } else {
                    window.clearFlags(-flag)
                }
            }
        }
        window.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
        if (dialogBgDrawable != null) {
            window.setBackgroundDrawable(dialogBgDrawable)
        }
        if (amount != undefined_float) {
            window.setDimAmount(amount)
        }
        if (animStyleResId != undefined_res) {
            window.setWindowAnimations(animStyleResId)
        }

        //设置了导航栏颜色, 键盘弹出,不会挤上布局.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (statusBarColor != undefined_res) {
                window.clearFlags(FLAG_TRANSLUCENT_STATUS)
                window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = statusBarColor
            }
            if (navigationBarColor != undefined_res) {
                window.clearFlags(FLAG_TRANSLUCENT_NAVIGATION)
                window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.navigationBarColor = navigationBarColor
            }
        }
        onConfigWindow(window)
    }

    open fun configWindowAfter(window: Window) {
        val attributes = window.attributes
        if (autoWidthHeight && dialogLayoutId != -1) {
            //自动测量 布局的宽高
            val size = measureSize(dialogContext!!, dialogLayoutId, null)
            window.setLayout(size[0], size[1])
        } else { // window的宽高设置
            if (dialogWidth != undefined_res && dialogHeight != undefined_res) {
                window.setLayout(dialogWidth, dialogHeight)
            } else {
                if (dialogHeight != undefined_res) {
                    window.setLayout(attributes.width, dialogHeight)
                }
                if (dialogWidth != undefined_res) {
                    window.setLayout(dialogWidth, attributes.height)
                }
            }
        }
        if (dialogGravity != undefined_res) {
            attributes.gravity = dialogGravity
            window.attributes = attributes
        }
    }

    open fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        //替换标题栏
        if (dialogTitleLayoutId > 0) {
            dialogViewHolder.group(R.id.title_layout)?.replace(dialogTitleLayoutId)
        }
    }

    /** Dialog -> AppCompatDialog -> AlertDialog */
    @Throws
    fun showAlertDialog(): AlertDialog {
        if (dialogContext == null) {
            throw NullPointerException("context is null.")
        }

        val builder =
            AlertDialog.Builder(dialogContext!!, dialogThemeResId)
        if (!TextUtils.isEmpty(dialogMessage)) {
            builder.setMessage(dialogMessage)
        }
        //积极的按钮 DialogInterface.BUTTON_POSITIVE
        if (!TextUtils.isEmpty(positiveButtonText)) {
            builder.setPositiveButton(positiveButtonText) { dialog, _ ->
                positiveButtonListener?.invoke(dialog as Dialog, dialog.dialogViewHolder())
            }
        }
        //消极的按钮 DialogInterface.BUTTON_NEGATIVE
        if (!TextUtils.isEmpty(negativeButtonText)) {
            builder.setNegativeButton(negativeButtonText) { dialog, _ ->
                negativeButtonListener?.invoke(dialog as Dialog, dialog.dialogViewHolder())
            }
        }
        //中立的按钮 DialogInterface.BUTTON_NEUTRAL
        if (!TextUtils.isEmpty(neutralButtonText)) {
            builder.setNeutralButton(neutralButtonText) { dialog, _ ->
                neutralButtonListener?.invoke(dialog as Dialog, dialog.dialogViewHolder())
            }
        }
        if (dialogContentView != null) {
            builder.setView(dialogContentView)
        } else if (dialogLayoutId != -1) {
            builder.setView(dialogLayoutId)
        }
        val alertDialog = builder.create()
        showAndConfigDialog(alertDialog)
        //alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        //alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        return alertDialog
    }

    /** Dialog -> AppCompatDialog -> BottomSheetDialog */
    fun showSheetDialog(): Dialog {
        if (dialogContext == null) {
            throw NullPointerException("context is null.")
        }
        return try {
            val cls = Class.forName("com.google.android.material.bottomsheet.BottomSheetDialog")
            val constructor = cls.getConstructor(Context::class.java, Int::class.java)
            val sheetDialog: Dialog =
                constructor.newInstance(dialogContext, dialogThemeResId) as Dialog
            showAndConfigDialog(sheetDialog)
        } catch (e: Exception) {
            L.w(e)
            showCompatDialog()
        }
    }

    /** Dialog -> AppCompatDialog */
    @Throws
    fun showCompatDialog(): AppCompatDialog {
        if (dialogContext == null) {
            throw NullPointerException("context is null.")
        }
        val dialog = AppCompatDialog(dialogContext, dialogThemeResId)
        showAndConfigDialog(dialog)
        return dialog
    }

    /** Dialog */
    fun showDialog(): Dialog {
        if (dialogContext == null) {
            throw NullPointerException("context is null.")
        }
        val dialog = Dialog(dialogContext!!, dialogThemeResId)
        showAndConfigDialog(dialog)
        return dialog
    }

    fun showDialogActivity() {
        if (dialogContext == null) {
            throw NullPointerException("context is null.")
        }
        dialogContext?.dslAHelper {
            start(DialogActivity.getDialogIntent(this@DslDialogConfig))
        }
    }

    /**根据类型, 自动显示对应[Dialog]*/
    fun show(): Dialog {
        return when (dialogType) {
            DIALOG_TYPE_DIALOG -> showDialog()
            DIALOG_TYPE_ALERT_DIALOG -> showAlertDialog()
            DIALOG_TYPE_BOTTOM_SHEET_DIALOG -> showSheetDialog()
            DIALOG_TYPE_ACTIVITY -> {
                showDialogActivity()
                Dialog(dialogContext!!).apply {
                    L.w("[DIALOG_TYPE_ACTIVITY] 类型, 无法返回[Dialog]对象")
                }
            }
            else -> showCompatDialog()
        }
    }
}

fun Dialog.dialogViewHolder(): DslViewHolder = DslViewHolder(window!!.decorView)

/**快速显示[AppCompatDialog]配置*/
fun dslDialog(context: Context, action: DslDialogConfig.() -> Unit): AppCompatDialog {
    val dslDialog = DslDialogConfig(context)
    dslDialog.action()
    return dslDialog.showCompatDialog()
}

fun dslDialog(context: Context, dialog: Dialog, action: DslDialogConfig.() -> Unit): Dialog {
    val dslDialog = DslDialogConfig(context)
    dslDialog.action()
    dslDialog.showAndConfigDialog(dialog)
    return dialog
}