package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import com.angcyo.library.ex.dpi

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="对话框基础配置">

/**快速配置一个显示在底部全屏的[DslDialogConfig]*/
fun DslDialogConfig.configBottomDialog(): DslDialogConfig {
    return this.apply {
        canceledOnTouchOutside = false
        dialogWidth = -1
        dialogHeight = -2
        dialogGravity = Gravity.BOTTOM
        animStyleResId = R.style.LibDialogBottomTranslateAnimation
        setDialogBgColor(Color.TRANSPARENT)
    }
}

//</editor-fold desc="对话框基础配置">

//<editor-fold desc="常用对话框">

fun Context.dslDialog(config: DslDialogConfig.() -> Unit): Dialog {
    val dialogConfig = DslDialogConfig(this)
    dialogConfig.config()
    return dialogConfig.run {
        dialogWidth = -1
        show()
    }
}


fun Context.normalDialog(config: NormalDialogConfig.() -> Unit): Dialog {
    val dialogConfig = NormalDialogConfig(this)
    dialogConfig.config()
    return dialogConfig.run {
        dialogWidth = -1
        show()
    }
}

fun Context.normalIosDialog(config: IosDialogConfig.() -> Unit): Dialog {
    val dialogConfig = IosDialogConfig(this)
    dialogConfig.config()

    return dialogConfig.run {
        dialogWidth = -1
        show()
    }
}

//</editor-fold desc="常用对话框">


/**
 * 多选项, 选择对话框, 底部带 取消按钮, 标题栏不带取消
 * */
fun Context.itemsDialog(config: ItemDialogConfig.() -> Unit): Dialog {
    val dialogConfig = ItemDialogConfig(this)
    dialogConfig.configBottomDialog()
    dialogConfig.config()
    return dialogConfig.show()
}

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

/**
 * 文本输入对话框, 默认是单行, 无限制
 * */
fun Context.inputDialog(config: InputDialogConfig.() -> Unit): Dialog {
    val dialogConfig = InputDialogConfig(this)
    dialogConfig.configBottomDialog()
    dialogConfig.canceledOnTouchOutside = false
    dialogConfig.config()

    return dialogConfig.show()
}

/**多输入框*/
fun Context.inputMultiDialog(config: InputMultiDialogConfig.() -> Unit): Dialog {
    val dialogConfig = InputMultiDialogConfig(this)
    dialogConfig.configBottomDialog()
    dialogConfig.canceledOnTouchOutside = false
    dialogConfig.config()

    return dialogConfig.show()
}


/**
 * 多行文本输入框
 * */
fun Context.multiInputDialog(config: InputDialogConfig.() -> Unit): Dialog {
    val dialogConfig = InputDialogConfig(this)
    dialogConfig.configBottomDialog()
    dialogConfig.canceledOnTouchOutside = false
    dialogConfig.maxInputLength = 2000
    dialogConfig.inputViewHeight = 100 * dpi
    /**多行输入时, 需要 [InputType.TYPE_TEXT_FLAG_MULTI_LINE] 否则输入框, 不能输入 回车 */
    dialogConfig.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    dialogConfig.config()

    return dialogConfig.show()
}

/**
 * 底部网格对话框
 * */
fun Context.gridDialog(config: GridDialogConfig.() -> Unit): Dialog {
    val dialogConfig = GridDialogConfig(this)
    dialogConfig.configBottomDialog()
    dialogConfig.canceledOnTouchOutside = false
    dialogConfig.config()

    return dialogConfig.show()
}

//<editor-fold desc="popupWindow">

/** 展示一个popup window */
fun Context.popupWindow(anchor: View? = null, config: PopupConfig.() -> Unit): Any {
    val popupConfig = PopupConfig()
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
}

fun Context.fullPopupWindow(anchor: View? = null, config: PopupConfig.() -> Unit): Any {
    val popupConfig = FullPopupConfig()
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
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
