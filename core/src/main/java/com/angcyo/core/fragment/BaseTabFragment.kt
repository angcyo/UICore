package com.angcyo.core.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.angcyo.base.dslChildFHelper
import com.angcyo.core.R
import com.angcyo.library.ex.find
import com.angcyo.library.ex.setWidthHeight
import com.angcyo.widget.base.append
import com.angcyo.widget.base.getChildOrNull
import com.angcyo.widget.tab
import com.angcyo.widget.text.BadgeTextView

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

    /**配置 tab item 中图标的大小*/
    var tabIconSize: Int? = null

    /**线*/
    var showTabLine: Boolean = true

    companion object {

        /**用来切换[DslTabLayout]*/
        const val KEY_TAB_INDEX = "KEY_TAB_INDEX"
    }

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

            onGetTextStyleView = { itemView, _ ->
                itemView.find(R.id.lib_tab_text_view)
            }
            onGetIcoStyleView = { itemView, _ ->
                itemView.find(R.id.lib_tab_image_view)
            }

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
        @DrawableRes imageResId: Int,
        action: View.() -> Unit = {}
    ) {
        _fragmentList.add(fragment)
        _vh.tab(R.id.lib_tab_layout)?.appendTabItem(text, imageResId, tabItemLayoutId) {
            find<ImageView>(R.id.lib_tab_image_view)?.apply {
                tabIconSize?.let {
                    setWidthHeight(it)
                }
            }
            action()
        }
    }

    /**更新角标*/
    open fun updateBadge(index: Int, badge: String?) {
        _vh.group(R.id.lib_tab_layout)?.getChildOrNull(index)
            ?.find<BadgeTextView>(R.id.lib_tab_badge_view)?.updateBadge(badge)
    }

    /**改变tab*/
    open fun changeTabIndex(index: Int, notify: Boolean = true, fromUser: Boolean = false) {
        _vh.tab(R.id.lib_tab_layout)?.apply {
            if (index in 0 until dslSelector.visibleViewList.size) {
                setCurrentItem(index, notify, fromUser)
            }
        }
    }

    override fun onFragmentShow(bundle: Bundle?) {
        super.onFragmentShow(bundle)
        bundle?.getInt(KEY_TAB_INDEX)?.let {
            changeTabIndex(it)
        }
    }
}

fun ViewGroup.appendTabItem(
    text: CharSequence,
    @DrawableRes imageResId: Int = 0,
    tabItemLayoutId: Int = R.layout.lib_tab_item_layout,
    action: View.() -> Unit = {}
) {
    append(tabItemLayoutId) {
        find<TextView>(R.id.lib_tab_text_view)?.text = text
        find<ImageView>(R.id.lib_tab_image_view)?.setImageResource(imageResId)
        action()
    }
}