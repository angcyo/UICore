package com.angcyo.core.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.HideTitleBarBehavior
import com.angcyo.core.R
import com.angcyo.library._screenHeight
import com.angcyo.library.ex.dpi
import com.angcyo.widget.base.behavior
import com.angcyo.library.ex.replace
import com.angcyo.library.ex.setHeight

/**
 * 简单的头部放大行为界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021-6-24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseBehaviorDetailFragment : BaseDslFragment() {

    /**自定义背景布局*/
    var backgroundScaleLayoutId: Int = -1

    /**自定义头部布局*/
    var headerLayoutId: Int = -1

    override var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            if (isAdded) {
                _vh.tv(R.id.lib_title_text_view)?.text = value
                _vh.tv(R.id.lib_header_title_view)?.text = value
            }
        }

    init {
        contentOverlayLayoutId
        fragmentLayoutId = R.layout.lib_behavior_detail_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentConfig.titleItemIconColor = Color.WHITE
    }

    override fun onCreateBehavior(child: View): CoordinatorLayout.Behavior<*>? {
        return child.behavior() ?: when (child.id) {
            R.id.lib_title_wrap_layout -> HideTitleBarBehavior(fContext()).apply {
                ignoreStatusBar = true
            }
            else -> super.onCreateBehavior(child)
        }
    }

    override fun onInitFragment(savedInstanceState: Bundle?) {
        super.onInitFragment(savedInstanceState)
        initHeaderLayout()
    }

    open fun backgroundHeight(): Int {
        return _screenHeight / 3
    }

    /**初始化头部布局*/
    open fun initHeaderLayout() {
        //背景层
        _vh.group(R.id.lib_background_wrap_layout)?.replace(backgroundScaleLayoutId)
        //头部层
        _vh.group(R.id.lib_header_wrap_layout)?.replace(headerLayoutId)

        val backgroundHeight = backgroundHeight()
        _vh.view(R.id.lib_background_wrap_layout)?.setHeight(backgroundHeight)
        _vh.view(R.id.lib_header_wrap_layout)?.setHeight(backgroundHeight - 20 * dpi)
    }
}