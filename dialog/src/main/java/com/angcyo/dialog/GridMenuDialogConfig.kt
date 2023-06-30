package com.angcyo.dialog

import android.content.Context

/**
 * 网格菜单dialog, 带标题
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/02/25
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class GridMenuDialogConfig(context: Context? = null) : GridDialogConfig(context) {

    init {
        dialogLayoutId = R.layout.lib_dialog_recycler_grid_menu_layout
    }

}