package com.angcyo.widget.base

import android.content.Context
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**
 * 通过[MenuConfig] 快速显示一个 [PopupMenu]
 * */
fun Context.showPopupMenu(
    anchorView: View?,
    menuRes: Int,
    action: MenuConfig.() -> Unit
): PopupMenu? {
    if (anchorView == null) {
        return null
    }
    val config = MenuConfig()
    config.menuRes = menuRes
    config.anchorView = anchorView
    config.action()
    return config.show(this)
}

/**通过反射, 设置[PopupMenu]菜单图标的可见性*/
fun PopupMenu.setOptionalIconsVisible(visible: Boolean = true) {
    try {
        val declaredMethod =
            MenuBuilder::class.java.getDeclaredMethod(
                "setOptionalIconsVisible",
                Boolean::class.java
            )
        declaredMethod.isAccessible = true
        declaredMethod.invoke(menu, visible)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}