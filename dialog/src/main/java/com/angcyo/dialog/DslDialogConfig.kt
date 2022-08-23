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
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.angcyo.base.dslAHelper
import com.angcyo.dialog.activity.DialogActivity
import com.angcyo.drawable.isGravityCenterVertical
import com.angcyo.library.L
import com.angcyo.library.UndefinedDrawable
import com.angcyo.library.ex.*
import com.angcyo.lifecycle.onDestroy
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.dslViewHolder
import java.io.Serializable

/**
 * 标准对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */

/**
 * [dialogContext] android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
 * android.view.ViewRootImpl#setView(android.view.View, android.view.WindowManager.LayoutParams, android.view.View, int)
 * */
open class DslDialogConfig(@Transient var dialogContext: Context? = null) :
    LifecycleOwner, Serializable {

    companion object {
        /** Dialog -> AppCompatDialog -> AlertDialog */

        /**最普通的对话框, [Dialog]]*/
        const val DIALOG_TYPE_DIALOG = 0

        /**[AppCompatDialog], 如果不是[AppCompatActivity], 会自动降级成 [Dialog]*/
        const val DIALOG_TYPE_APPCOMPAT = 1

        /**[AlertDialog], 如果不是[AppCompatActivity], 会自动降级成 [Dialog]*/
        const val DIALOG_TYPE_ALERT_DIALOG = 2

        /**[BottomSheetDialog], 需要[material]库支持*/
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

    /**对话框标题*/
    var dialogTitle: CharSequence? = null
        set(value) {
            field = value
            _dialogViewHolder?.tv(R.id.title_view)?.text = value
        }

    /**对话框内容*/
    var dialogMessage: CharSequence? = null

    /**对话框初始化监听*/
    var onDialogInitListener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit =
        { _, _ -> }

    /**作用在Windows上*/
    @Transient
    var dialogBgDrawable: Drawable? = UndefinedDrawable()
    var dialogWidth: Int = undefined_res
    var dialogHeight: Int = undefined_res
    var dialogGravity: Int = undefined_res

    /**作用在view上*/
    var contentBgDrawable: Drawable? = UndefinedDrawable()

    //测试好像没效果.
    var statusBarColor: Int = undefined_res
    var navigationBarColor: Int = undefined_res
    var navigationBarDividerColor: Int = undefined_res

    var onConfigWindow: (Window) -> Unit = {}

    /** 自动计算宽高 */
    var autoWidthHeight = false

    /** 0.2f
     * 对话框变暗指数, [0,1]
     * 0表示, 不变暗
     * 1表示, 全暗
     * undefined_res, 默认
     */
    var dimAmount: Float = undefined_float

    /** 64
     * 高斯模糊背景
     * [Build.VERSION_CODES.S]
     * */
    var blurBehindRadius: Int = undefined_size

    /** window动画资源
     * 0 取消动画*/
    @StyleRes
    var animStyleResId: Int = R.style.LibDialogAnimation

    /**创建对话框时的主题, 默认主题请使用0*/
    @StyleRes
    var dialogThemeResId: Int = R.style.LibDialogStyle

    /**
     * 显示dialog的类型
     * [AppCompatDialog] [AlertDialog] [BottomSheetDialog]
     *
     * [AlertDialog]通过系统自带的[Builder]构建
     * */
    var dialogType = DIALOG_TYPE_APPCOMPAT

    /** 系统默认3个按钮设置
     * [BaseDialogConfig]*/
    var positiveButtonText: CharSequence? = null //确定
    var negativeButtonText: CharSequence? = null //取消
    var neutralButtonText: CharSequence? = null //中立

    //确定
    var positiveButtonListener: ((dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit)? =
        { dialog, _ ->
            dialog.hideSoftInput()
            dialog.dismiss()
        }

    //取消
    var negativeButtonListener: ((dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit)? =
        { dialog, _ ->
            dialog.hideSoftInput()
            dialog.cancel()
        }

    //中立
    var neutralButtonListener: ((dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit)? = null

    /**Dismiss会触发*/
    var onDismissListener: ((dialog: Dialog) -> Unit)? = null

    /**cancel不一定会触发*/
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

    //保存[DslViewHolder]对象
    var _dialogViewHolder: DslViewHolder? = null

    @Throws
    open fun showAndConfigDialog(dialog: Dialog): Dialog {
        if (dialogContext == null) {
            throw NullPointerException("context is null.")
        }

        //lifecycle
        onDialogCreate(dialog)

        dialog.setCancelable(cancelable)
        if (!TextUtils.isEmpty(dialogTitle)) {
            dialog.setTitle(dialogTitle)
        }
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        val window = dialog.window
        val decorView: View

        //放在[setContentView]前面配置对话框
        configDialog(dialog)

        if (dialog is AlertDialog) {
            //从showAlertDialog传过来的dialog
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
            val viewHolder = decorView.dslViewHolder()
            _dialogViewHolder = viewHolder

            //lifecycle
            dialog.setOnShowListener {
                onDialogShow(dialog, viewHolder)
            }

            //lifecycle
            dialog.setOnDismissListener {
                onDialogDestroy(dialog, viewHolder)
                onDismissListener?.invoke(it as Dialog)
            }

            dialog.setOnCancelListener {
                onDialogCancel(dialog, viewHolder)
                onCancelListener?.invoke(it as Dialog)
            }

            initDialogView(dialog, viewHolder)
            onDialogInitListener(dialog, viewHolder)
        }

        return dialog
    }

    /** 配置window特性, 需要在setContentView之前调用 */
    open fun configDialog(dialog: Dialog) {
        _dialog = dialog
        val window = dialog.window

        //Window feature must be requested before adding content
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
        if (dialogBgDrawable !is UndefinedDrawable) {
            window.setBackgroundDrawable(dialogBgDrawable)
        }
        //变暗
        if (dimAmount != undefined_float) {
            window.addFlags(FLAG_DIM_BEHIND)
            window.setDimAmount(dimAmount)
        }
        //模糊
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurBehindRadius != undefined_size) {
            window.addFlags(FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = blurBehindRadius
            window.addFlags(FLAG_DIM_BEHIND)
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
            if (navigationBarDividerColor != undefined_res &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            ) {
                window.clearFlags(FLAG_TRANSLUCENT_NAVIGATION)
                window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.navigationBarDividerColor = navigationBarDividerColor
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
        //背景替换
        if (contentBgDrawable is UndefinedDrawable) {
            if (dialogGravity.isGravityCenterVertical()) {
                dialogViewHolder.itemView.setBackgroundResource(R.drawable.dialog_white_round_bg_shape)
            }
        }
        //width/height 优化
        val lp = dialogViewHolder.itemView.layoutParams
        if (dialogWidth != undefined_res && dialogWidth != -2) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        if (dialogHeight != undefined_res && dialogHeight != -2) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
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

        val builder = AlertDialog.Builder(dialogContext!!, dialogThemeResId)
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
            show(DIALOG_TYPE_APPCOMPAT)
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

    /**监听声明周期*/
    var _lifecycleObserver: LifecycleEventObserver? = null

    /**根据类型, 自动显示对应[Dialog]*/
    open fun show(type: Int = dialogType): Dialog {
        onDialogInit()

        val dialog = when (type) {
            DIALOG_TYPE_DIALOG -> showDialog()
            DIALOG_TYPE_APPCOMPAT -> {
                if (dialogContext is AppCompatActivity) {
                    showCompatDialog()
                } else {
                    showDialog()
                }
            }
            DIALOG_TYPE_ALERT_DIALOG -> {
                if (dialogContext is AppCompatActivity) {
                    showAlertDialog()
                } else {
                    showDialog()
                }
            }
            DIALOG_TYPE_BOTTOM_SHEET_DIALOG -> showSheetDialog()
            DIALOG_TYPE_ACTIVITY -> {
                showDialogActivity()
                Dialog(dialogContext!!).apply {
                    L.w("[DIALOG_TYPE_ACTIVITY] 类型, 无法返回[Dialog]对象")
                }
            }
            else -> showDialog()
        }

        //防止activity销毁时, dialog泄漏
        if (dialogContext is LifecycleOwner) {
            val observer: LifecycleEventObserver = (dialogContext as LifecycleOwner).onDestroy {
                dialog.cancel()
                true
            }
            _lifecycleObserver = observer
        }

        return dialog
    }

    //<editor-fold desc="Lifecycle支持">

    val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    @CallSuper
    open fun onDialogInit() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @CallSuper
    open fun onDialogCreate(dialog: Dialog) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @CallSuper
    open fun onDialogShow(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**[android.content.DialogInterface.OnCancelListener]*/
    @CallSuper
    open fun onDialogCancel(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        lifecycleRegistry.currentState
    }

    /**[android.content.DialogInterface.OnDismissListener]*/
    @CallSuper
    open fun onDialogDestroy(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _lifecycleObserver?.let { observer ->
            if (dialogContext is LifecycleOwner) {
                (dialogContext as LifecycleOwner).lifecycle.removeObserver(observer)
            }
        }
        dialogViewHolder.clear()
    }

    //</editor-fold desc="Lifecycle支持">
}

fun Dialog.dialogViewHolder(): DslViewHolder = DslViewHolder(window!!.decorView)

/**快速显示[AppCompatDialog]配置*/
fun dslDialog(context: Context, action: DslDialogConfig.() -> Unit): Dialog {
    val dslDialog = DslDialogConfig(context)
    dslDialog.action()
    return dslDialog.show()
}

fun dslDialog(context: Context, dialog: Dialog, action: DslDialogConfig.() -> Unit): Dialog {
    val dslDialog = DslDialogConfig(context)
    dslDialog.action()
    dslDialog.showAndConfigDialog(dialog)
    return dialog
}

/**隐藏软键盘*/
fun Dialog.hideSoftInput() {
    window?.decorView?.hideSoftInput()
}