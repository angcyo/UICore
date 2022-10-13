package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import com.angcyo.base.enableLayoutFullScreen
import com.angcyo.base.translucentStatusBar
import com.angcyo.library._screenWidth
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
fun DslDialogConfig.configBottomDialog(context: Context? = null): DslDialogConfig {
    return this.apply {
        canceledOnTouchOutside = false
        dialogWidth = -1
        dialogHeight = -2
        dialogGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        animStyleResId = R.style.LibDialogBottomTranslateAnimation
        setDialogBgColor(Color.TRANSPARENT)
        dialogContext = context ?: dialogContext
    }
}

/**快速配置一个显示在中心全屏的[DslDialogConfig]*/
fun DslDialogConfig.configCenterDialog(
    width: Int = _screenWidth * 3 / 5,
    height: Int = -2,
    context: Context? = null
): DslDialogConfig {
    return this.apply {
        canceledOnTouchOutside = false
        dialogWidth = width
        dialogHeight = height
        dialogGravity = Gravity.CENTER
        animStyleResId = R.style.LibDialogAlphaAnimation
        setDialogBgColor(Color.TRANSPARENT)
        dialogContext = context ?: dialogContext
    }
}

//</editor-fold desc="对话框基础配置">

//<editor-fold desc="常用对话框">

/**自定义对话框*/
fun Context.dslDialog(config: DslDialogConfig.() -> Unit): Dialog {
    return DslDialogConfig(this).run {
        dialogWidth = -1
        config()
        show()
    }
}

/**自定义全屏对话框*/
fun Context.customFullScreenDialog(config: DslDialogConfig.() -> Unit): Dialog {
    return DslDialogConfig(this).run {
        //R.style.LibDialogAnimation//R.style.LibFullPopupAnimation
        animStyleResId = R.style.LibFullPopupAnimation
        //全屏的三金刚属性
        dialogWidth = -1
        //很关键的一点, 高度一定要撑满全屏. 撑满之后, 如果导航栏显示了, 内部View布局会有点BUG, 顶部偏移有问题.
        dialogHeight = -1 //getRootHeight()
        setDialogBgColor(Color.TRANSPARENT)
        onConfigWindow = {
            it.enableLayoutFullScreen(true)
            it.translucentStatusBar(true)
//        it.translucentNavigationBar(true)
        }
        config()
        show()
    }
}

fun Context.normalDialog(config: NormalDialogConfig.() -> Unit): Dialog {
    return NormalDialogConfig(this).run {
        dialogWidth = -1
        config()
        show()
    }
}

fun Context.normalIosDialog(config: IosDialogConfig.() -> Unit): Dialog {
    return IosDialogConfig(this).run {
        dialogWidth = -1
        config()
        show()
    }
}

fun Context.textDialog(config: TextDialogConfig.() -> Unit): Dialog {
    return TextDialogConfig(this).run {
        configBottomDialog()
        canceledOnTouchOutside = true
        config()
        show()
    }
}

fun Context.messageDialog(config: MessageDialogConfig.() -> Unit): Dialog {
    return MessageDialogConfig(this).run {
        dialogWidth = -1
        canceledOnTouchOutside = false
        config()
        show()
    }
}

/**打开方式*/
fun Context.openWidthDialog(config: OpenWithDialogConfig.() -> Unit): Dialog {
    return OpenWithDialogConfig(this).run {
        configBottomDialog()
        canceledOnTouchOutside = true
        config()
        show()
    }
}

//</editor-fold desc="常用对话框">

//<editor-fold desc="高级对话框">

/**
 * 多选项, 选择对话框, 底部带 取消按钮, 标题栏不带取消
 * [com.angcyo.dialog.ItemDialogConfig.addDialogItem]
 * */
fun Context.itemsDialog(config: ItemDialogConfig.() -> Unit): Dialog {
    return ItemDialogConfig(this).run {
        configBottomDialog()
        config()
        show()
    }
}

/**
 * 文本输入对话框, 默认是单行, 无限制
 * */
fun Context.inputDialog(config: InputDialogConfig.() -> Unit): Dialog {
    return InputDialogConfig(this).run {
        configBottomDialog()
        config()
        show()
    }
}

/**多输入框*/
fun Context.inputMultiDialog(config: InputMultiDialogConfig.() -> Unit): Dialog {
    return InputMultiDialogConfig(this).run {
        configBottomDialog()
        config()
        show()
    }
}

/**
 * 多行文本输入框
 * */
fun Context.multiInputDialog(config: InputDialogConfig.() -> Unit): Dialog {
    return InputDialogConfig(this).run {
        configBottomDialog()
        canceledOnTouchOutside = false
        maxInputLength = 2000
        inputViewHeight = 100 * dpi
        /**多行输入时, 需要 [InputType.TYPE_TEXT_FLAG_MULTI_LINE] 否则输入框, 不能输入 回车 */
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        config()
        show()
    }
}

/**
 * 底部网格对话框
 * */
fun Context.gridDialog(config: GridDialogConfig.() -> Unit): Dialog {
    return GridDialogConfig(this).run {
        configBottomDialog()
        config()
        show()
    }
}

/**
 * 底部网格菜单对话框
 * */
fun Context.gridMenuDialog(config: GridMenuDialogConfig.() -> Unit): Dialog {
    return GridMenuDialogConfig(this).run {
        configBottomDialog()
        config()
        show()
    }
}

/**
 * 万级联动选项对话框
 * */
fun Context.optionDialog(config: OptionDialogConfig.() -> Unit): Dialog {
    return OptionDialogConfig().run {
        configBottomDialog(this@optionDialog)
        config()
        show()
    }
}

/**模拟IOS的全屏对话框*/
fun Context.iosFullScreenDialog(config: IosFullScreenDialogConfig.() -> Unit): Dialog {
    return IosFullScreenDialogConfig().run {
        dialogContext = this@iosFullScreenDialog
        //dialogWidth = getRootWidth()
        //dialogHeight = getRootHeight()
        config()
        show()
    }
}

/**
 * 验证码/密码输入对话框
 * */
fun Context.verifyCodeInputDialog(config: VerifyCodeInputDialogConfig.() -> Unit): Dialog {
    return VerifyCodeInputDialogConfig(this).run {
        configCenterDialog(_screenWidth - 2 * 36 * dpi, -2)
        config()
        show()
    }
}

//</editor-fold desc="高级对话框">

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

/**展示列表*/
fun Context.recyclerPopupWindow(anchor: View? = null, config: RecyclerPopupConfig.() -> Unit): Any {
    val popupConfig = RecyclerPopupConfig()
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
}

//</editor-fold desc="popupWindow">