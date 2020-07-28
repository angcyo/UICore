package com.angcyo.widget.base

import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

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