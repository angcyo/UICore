package com.angcyo.item.style

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.angcyo.library.UndefinedDrawable
import com.angcyo.library.ex.undefined_size
import com.angcyo.widget.base.setBgDrawable
import com.angcyo.widget.base.setWidthHeight

/**
 * View基础样式配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class ViewStyleConfig {

    /**宽高*/
    var viewWidth: Int = undefined_size
    var viewMinWidth: Int = undefined_size

    var viewHeight: Int = undefined_size
    var viewMinHeight: Int = undefined_size

    /**padding值*/
    var paddingLeft: Int = undefined_size
    var paddingRight: Int = undefined_size
    var paddingTop: Int = undefined_size
    var paddingBottom: Int = undefined_size

    /**背景*/
    var backgroundDrawable: Drawable? = UndefinedDrawable()

    /**更新样式*/
    open fun updateStyle(view: View) {
        with(view) {
            if (backgroundDrawable is UndefinedDrawable) {
                backgroundDrawable = background
            }
            setBgDrawable(backgroundDrawable)

            //初始化默认值
            if (viewMinWidth == undefined_size && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewMinWidth = minimumWidth
            }
            if (viewMinHeight == undefined_size && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewMinHeight = minimumHeight
            }
            //设置
            if (viewMinWidth != undefined_size) {
                minimumWidth = viewMinWidth
                when (view) {
                    is ConstraintLayout -> view.minWidth = viewMinWidth
                }
            }
            if (viewMinHeight != undefined_size) {
                minimumHeight = viewMinHeight
                when (view) {
                    is ConstraintLayout -> view.minHeight = viewMinHeight
                }
            }

            //初始化默认值
            if (viewWidth == undefined_size) {
                viewWidth = layoutParams.width
            }
            if (viewHeight == undefined_size) {
                viewHeight = layoutParams.height
            }
            //设置
            setWidthHeight(viewWidth, viewHeight)
        }
    }
}