package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library.ex.getContentViewHeight
import com.angcyo.library.ex.undefined_res
import com.angcyo.library.getScreenHeight
import com.angcyo.widget.DslViewHolder
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**快速配置一个显示在底部全屏的[DslDialog]*/
fun Context.buildBottomDialog(): DslDialog {
    return DslDialog(this).apply {
        canceledOnTouchOutside = false
        dialogWidth = -1
        dialogHeight = -2
        dialogGravity = Gravity.BOTTOM
        setDialogBgColor(Color.TRANSPARENT)
    }
}

fun DslDialog.configDslDialog(dialogConfig: BaseDialogConfig) {
    cancelable = dialogConfig.dialogCancel
    canceledOnTouchOutside = dialogConfig.dialogCanceledOnTouchOutside
    onCancelListener = { dialog ->
        dialogConfig.onDialogCancel(dialog)
        dialogConfig.onDialogCancel.invoke(dialog)
    }
    onDismissListener = { dialog ->
        dialogConfig.onDialogDismiss(dialog)
        dialogConfig.onDialogDismiss.invoke(dialog)
    }
    dialogLayoutId = dialogConfig.dialogLayoutId
    onInitListener = { dialog, dialogViewHolder ->
        dialogConfig.onDialogInit(dialog, dialogViewHolder)
        dialogConfig.dialogInit.invoke(dialog, dialogViewHolder)
    }
    windowFeature = dialogConfig.windowFeature
    windowFlags = dialogConfig.windowFlags

    dialogConfig.dialogBgDrawable?.let {
        dialogBgDrawable = it
    }
    if (dialogConfig.dialogWidth != undefined_res) {
        dialogWidth = dialogConfig.dialogWidth
    }
    if (dialogConfig.dialogHeight != undefined_res) {
        dialogHeight = dialogConfig.dialogHeight
    }
}

/**根据类型, 自动显示对应[Dialog]*/
fun DslDialog.show(dialogConfig: BaseDialogConfig): Dialog {
    configDslDialog(dialogConfig)
    return when (dialogConfig.dialogType) {
        BaseDialogConfig.DIALOG_TYPE_ALERT_DIALOG -> showAlertDialog()
        BaseDialogConfig.DIALOG_TYPE_BOTTOM_SHEET_DIALOG -> showSheetDialog()
        else -> showCompatDialog()
    }
}

fun Context.normalDialog(config: NormalDialogConfig.() -> Unit): Dialog {
    val dialogConfig = NormalDialogConfig()
    dialogConfig.config()
    return DslDialog(this).run {
        dialogWidth = -1
        show(dialogConfig)
    }
}

fun Context.normalIosDialog(config: IosDialogConfig.() -> Unit): Dialog {
    val dialogConfig = IosDialogConfig()
    dialogConfig.config()

    return DslDialog(this).run {
        dialogWidth = -1
        show(dialogConfig)
    }
}
//
///**
// * 多选项, 选择对话框, 底部带 取消按钮, 标题栏不带取消
// * */
//fun Context.itemsDialog(config: ItemDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = ItemDialogConfig()
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
///**
// * 多选项, 菜单对话框, 底部不带取消按钮, 标题栏不带取消
// * */
//fun Context.menuDialog(config: MenuDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = MenuDialogConfig()
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
///**
// * 单选对话框, 底部不带取消按钮, 标题栏带取消和确定
// * */
//fun Context.singleChoiceDialog(config: MenuDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = MenuDialogConfig()
//    dialogConfig.choiceModel = ChoiceIView.CHOICE_MODE_SINGLE
//    dialogConfig.dialogCanceledOnTouchOutside = false
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
///**
// * 多选对话框, 底部不带取消按钮, 标题栏带取消和确定
// * */
//fun Context.multiChoiceDialog(config: MenuDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = MenuDialogConfig()
//    dialogConfig.choiceModel = ChoiceIView.CHOICE_MODE_MULTI
//    dialogConfig.dialogCanceledOnTouchOutside = false
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
//
///**
// * 3D滚轮选择对话框, 标题栏带取消和确定
// * */
//fun Context.wheelDialog(config: WheelDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = WheelDialogConfig()
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
//
///**
// * 文本输入对话框, 默认是单行, 无限制
// * */
//fun Context.inputDialog(config: InputDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = InputDialogConfig()
//    dialogConfig.dialogCanceledOnTouchOutside = false
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
///**多输入框*/
//fun Context.inputMultiDialog(config: InputMultiDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = InputMultiDialogConfig()
//    dialogConfig.dialogCanceledOnTouchOutside = false
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
//
///**
// * 多行文本输入框
// * */
//fun Context.multiInputDialog(config: InputDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = InputDialogConfig()
//    dialogConfig.dialogCanceledOnTouchOutside = false
//    dialogConfig.maxInputLength = 2000
//    dialogConfig.inputViewHeight = 100 * dpi
//    /**多行输入时, 需要 [InputType.TYPE_TEXT_FLAG_MULTI_LINE] 否则输入框, 不能输入 回车 */
//    dialogConfig.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
///**
// * 底部网格对话框
// * */
//fun Context.gridDialog(config: GridDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = GridDialogConfig()
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}

//<editor-fold desc="popupWindow">

/**
 * 展示一个popup window
 * */
fun Context.popupWindow(anchor: View? = null, config: PopupConfig.() -> Unit): PopupWindow {
    val popupConfig = PopupConfig()
    popupConfig.anchor = anchor
    popupConfig.config()

    val window = PopupWindow(this)

    window.apply {

        width = popupConfig.width
        height = popupConfig.height

        popupConfig.anchor?.let {
            val viewRect = it.getViewRect()
            if (popupConfig.exactlyHeight) {
                height = max(
                    it.context.getContentViewHeight(),
                    getScreenHeight()
                ) - viewRect.bottom
            }

            if (viewRect.bottom >= getScreenHeight()) {
                //接近屏幕底部
                if (popupConfig.gravity == Gravity.NO_GRAVITY) {
                    //手动控制无效
                    //popupConfig.gravity = Gravity.TOP

                    if (popupConfig.exactlyHeight) {
                        height = viewRect.top
                    }
                }
            }
        }

        isFocusable = popupConfig.focusable
        isTouchable = popupConfig.touchable
        isOutsideTouchable = popupConfig.outsideTouchable
        setBackgroundDrawable(popupConfig.background)

        animationStyle = popupConfig.animationStyle

        setOnDismissListener {
            popupConfig.onDismiss(window)
        }

        if (popupConfig.layoutId != -1) {
            popupConfig.contentView =
                LayoutInflater.from(this@popupWindow)
                    .inflate(popupConfig.layoutId, FrameLayout(this@popupWindow), false)
        }
        val view = popupConfig.contentView

        popupConfig.popupViewHolder = DslViewHolder(view!!)

        popupConfig.onPopupInit(window, popupConfig.popupViewHolder!!)
        popupConfig.popupInit(window, popupConfig.popupViewHolder!!)

        contentView = view
    }

    if (popupConfig.parent != null) {
        window.showAtLocation(
            popupConfig.parent,
            popupConfig.gravity,
            popupConfig.xoff,
            popupConfig.yoff
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.showAsDropDown(
            popupConfig.anchor,
            popupConfig.xoff,
            popupConfig.yoff,
            popupConfig.gravity
        )
    } else {
        window.showAsDropDown(popupConfig.anchor, popupConfig.xoff, popupConfig.yoff)
    }

    return window
}

//</editor-fold desc="popupWindow">

///**
// * 日期选择
// * */
//fun Context.dateDialog(config: DateDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = DateDialogConfig()
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
///**
// * 多级选项对话框
// * */
//fun Context.optionDialog(config: OptionDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = OptionDialogConfig()
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
///**
// * 日历选择对话框
// * */
//fun Context.calendarDialog(config: CalendarDialogConfig.() -> Unit): Dialog {
//    val dialogConfig = CalendarDialogConfig()
//    dialogConfig.config()
//
//    return buildBottomDialog().show(dialogConfig)
//}
//
