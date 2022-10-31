package com.angcyo.core.fragment

import android.graphics.Color
import android.graphics.Typeface
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

    /**是否使用白色标题栏
     * [null] 不接管操作
     * */
    var isLightStyle: Boolean? = null,

    /**拦截RootView的事件, 防止事件穿透到底下的Fragment
     * [com.angcyo.core.fragment.BaseTitleFragment.onInitFragment]*/
    var interceptRootTouchEvent: Boolean = true,

    /**是否显示标题栏下的阴影*/
    var showTitleLineView: Boolean = false,

    /**标题栏文本大小
     * [com.angcyo.core.fragment.BaseTitleFragment.onInitFragment]*/
    var titleTextSize: Float = getDimen(R.dimen.text_main_size).toFloat(),

    /**[Typeface.NORMAL]
     * [Typeface.BOLD]
     * [Typeface.ITALIC]
     * [Typeface.BOLD_ITALIC]
     * [com.angcyo.core.fragment.BaseTitleFragment.onInitFragment]
     * */
    var titleTextType: Int = Typeface.NORMAL,

    /**标题栏文本颜色*/
    var titleTextColor: Int = Color.WHITE,

    /**标题栏左右item的图标颜色
     * [com.angcyo.core.fragment.BaseTitleFragment.onInitFragment]*/
    var titleItemIconColor: Int = Color.WHITE,

    /**标题栏左右item的文本颜色
     * [com.angcyo.core.fragment.BaseTitleFragment.onInitFragment]*/
    var titleItemTextColor: Int = Color.WHITE,

    /**Fragment背景
     * [com.angcyo.core.fragment.BaseTitleFragment.onInitFragment]*/
    var fragmentBackgroundDrawable: Drawable? = ColorDrawable(getColor(R.color.bg_primary_color)),

    /**标题栏背景
     * [com.angcyo.core.fragment.BaseTitleFragment.onInitFragment]*/
    var titleBarBackgroundDrawable: Drawable? = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(getColor(R.color.colorPrimary), getColor(R.color.colorPrimaryDark))
    )
)