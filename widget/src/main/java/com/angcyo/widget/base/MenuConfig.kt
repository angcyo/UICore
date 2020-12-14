package com.angcyo.widget.base

import android.content.Context
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import com.angcyo.library.ex.undefined_res

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MenuConfig {

    /**菜单资源*/
    var menuRes: Int = undefined_res

    /**锚点[View]*/
    var anchorView: View? = null

    /**重力*/
    var gravity: Int = Gravity.NO_GRAVITY

    /**显示菜单图标*/
    var showIcon: Boolean = true

    /**相对于锚点的偏移位置*/
    var offsetX = 0
    var offsetY = 0

    /**创建菜单*/
    var createMenuAction: (context: Context) -> PopupMenu = {
        PopupMenu(it, anchorView!!, gravity)
    }

    /**菜单配置*/
    var configMenu: (PopupMenu, Menu) -> Unit = { _, _ -> }

    /**菜单点击事件*/
    var menuItemClickAction: ((item: MenuItem) -> Boolean)? = null

    /**菜单不关闭*/
    var menuDismissAction: ((PopupMenu) -> Unit)? = null
}

/**根据配置信息, 显示[PopupMenu]*/
fun MenuConfig.show(context: Context): PopupMenu {
    val config = this
    return config.createMenuAction(context).apply {
        inflate(config.menuRes)
        if (showIcon) {
            setOptionalIconsVisible()
        }
        config.configMenu(this, menu)

        setOnMenuItemClickListener {
            config.menuItemClickAction?.invoke(it) ?: false
        }

        setOnDismissListener {
            config.menuDismissAction?.invoke(this)
        }

        if (offsetX == 0 && offsetY == 0) {
            show()
        } else {
            //需要通过反射设置offset x/y
            try {
                val popupField = PopupMenu::class.java.getDeclaredField("mPopup")
                popupField.isAccessible = true
                //@RestrictTo(LIBRARY_GROUP_PREFIX)
                (popupField.get(this) as MenuPopupHelper).show(offsetX, offsetY)
            } catch (e: Exception) {
                e.printStackTrace();
            }
        }
    }
}
