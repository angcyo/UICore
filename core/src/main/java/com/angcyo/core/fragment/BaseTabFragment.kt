package com.angcyo.core.fragment

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.angcyo.base.dslChildFHelper
import com.angcyo.core.R
import com.angcyo.widget.base.append
import com.angcyo.widget.base.find
import com.angcyo.widget.tab

/**
 * 简单的底部tab界面,不支持title, 多用于首页
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseTabFragment : BaseFragment() {

    /**Tab Item 的布局*/
    var tabItemLayoutId: Int = R.layout.lib_tab_item_layout

    /**线*/
    var showTabLine: Boolean = true

    init {
        //Fragment根布局, tab 在下面
        fragmentLayoutId = R.layout.lib_tab_fragment

        //tab 在上面的布局
        //fragmentLayoutId = R.layout.lib_top_tab_fragment
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        initTabLayout()
    }

    open fun initTabLayout() {
        _vh.visible(R.id.lib_tab_line_view, showTabLine)
        _vh.tab(R.id.lib_tab_layout)?.configTabLayoutConfig {
            onSelectIndexChange = { fromIndex, selectIndexList, reselect, fromUser ->
                val to = selectIndexList.first()
                if (!reselect) {
                    dslChildFHelper {
                        noAnim()
                        _fragmentList.getOrNull(to)?.apply {
                            restore(this)
                        }
                    }
                }
            }
        }
    }

    var _fragmentList: MutableList<Class<out Fragment>> = mutableListOf()

    /**添加tab item*/
    open fun addTabItem(
        fragment: Class<out Fragment>,
        text: CharSequence,
        @DrawableRes imageResId: Int
    ) {
        _fragmentList.add(fragment)
        _vh.tab(R.id.lib_tab_layout)?.appendTabItem(text, imageResId, tabItemLayoutId)
    }
}

fun ViewGroup.appendTabItem(
    text: CharSequence,
    @DrawableRes imageResId: Int = 0,
    tabItemLayoutId: Int = R.layout.lib_tab_item_layout
) {
    append(tabItemLayoutId) {
        find<TextView>(R.id.lib_tab_text_view)?.text = text
        find<ImageView>(R.id.lib_tab_image_view)?.setImageResource(imageResId)
    }
}