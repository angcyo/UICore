package com.angcyo.core.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import com.angcyo.core.R
import com.angcyo.library.ex.getColor
import com.angcyo.library.ex.getDimen

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
data class FragmentConfig(

    /**拦截RootView的事件, 防止事件穿透到底下的Fragment*/
    var interceptRootTouchEvent: Boolean = true,

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
    var titleBarBackgroundDrawable: Drawable? = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(getColor(R.color.colorPrimary), getColor(R.color.colorPrimaryDark))
    )
)