package com.angcyo.core.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import com.angcyo.core.R
import com.angcyo.core.getColor
import com.angcyo.core.getDimen

/**
 *
 * Email:angcyo@126.com
 *
 * 存储一些res资源
 *
 * @author angcyo
 * @date 2019/09/27
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
data class ViewResConfig(

    /**标题栏文本大小*/
    var titleTextSize: Float = getDimen(R.dimen.text_main_size).toFloat(),

    /**标题栏文本颜色*/
    var titleTextColor: Int = Color.WHITE,

    /**标题栏左右item的图标颜色*/
    var titleItemIconColor: Int = Color.WHITE,

    /**标题栏左右item的文本颜色*/
    var titleItemTextColor: Int = Color.WHITE,

    /**Fragment背景*/
    var fragmentBackgroundDrawable: Drawable? = ColorDrawable(getColor(R.color.bg_primary_color)),

    /**标题栏背景*/
    var titleBarBackgroundDrawable: Drawable? = ColorDrawable(getColor(R.color.colorPrimary))
)