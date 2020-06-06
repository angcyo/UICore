package com.angcyo.core.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.core.R
import com.angcyo.core.viewpager.ViewPager1Delegate
import com.angcyo.library.ex.simpleClassName
import com.angcyo.widget.base.eachChild
import com.angcyo.widget.base.find
import com.angcyo.widget.base.onDoubleTap
import com.angcyo.widget.base.resetChild
import com.angcyo.widget.recycler.scrollHelper
import com.angcyo.widget.tab
import com.angcyo.widget.vp
import kotlin.math.min

/**
 * Tab+ViewPager 页面结构
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BasePagerFragment : BaseTitleFragment() {

    lateinit var fragmentAdapter: FragmentStatePagerAdapter

    init {
        contentLayoutId = R.layout.lib_pager_fragment
    }

    override fun canFlingBack(): Boolean {
        return false
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        onInitPager()
    }

    /**界面核心初始化*/
    open fun onInitPager() {
        //adapter
        fragmentAdapter = object :
            FragmentStatePagerAdapter(
                childFragmentManager,
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
            ) {
            override fun getItem(position: Int): Fragment = getPageItem(position)

            override fun getCount(): Int = getPageCount()

            override fun getPageTitle(position: Int): CharSequence? = getPageTitle(position)
        }

        //tab
        _vh.vp(R.id.lib_view_pager)?.apply {
            adapter = fragmentAdapter
            ViewPager1Delegate.install(this, _vh.tab(R.id.lib_tab_layout)?.apply {
                if (childCount <= 0) {
                    inflateTabItems(this)
                }
            })

            //离屏缓存数量
            offscreenPageLimit = getPageOffscreenLimit()
        }
    }

    //<editor-fold desc="TabLayout相关">

    /**更新tab*/
    fun updateTabItems() {
        _vh.tab(R.id.lib_tab_layout)?.apply {
            inflateTabItems(this)
        }
    }

    /**填充Tab Item*/
    open fun inflateTabItems(viewGroup: ViewGroup) {
        viewGroup.resetChild(
            getPageCount(),
            R.layout.lib_tab_item_layout
        ) { itemView, itemIndex ->
            itemView.find<TextView>(R.id.lib_text_view)?.text =
                getPageTitle(itemIndex)

            itemView.onDoubleTap {
                //双击tab item 滚动至顶部
                getPageItem(itemIndex).view?.find<RecyclerView>(R.id.lib_recycler_view)
                    ?.scrollHelper {
                        scrollToFirst {
                            firstScrollAnim = false
                            scrollAnim = false
                        }
                    }
                false
            }
        }
    }

    /**遍历[TabLayout]*/
    fun eachTabLayout(action: (itemView: View, itemIndex: Int) -> Unit) {
        _vh.tab(R.id.lib_tab_layout)?.eachChild { index, child ->
            action(child, index)
        }
    }

    //</editor-fold desc="TabLayout相关">

    //<editor-fold desc="ViewPager相关">

    open fun getPageOffscreenLimit(): Int = min(5, getPageCount())

    /**获取对应页面*/
    abstract fun getPageItem(position: Int): Fragment

    /**页面数量*/
    abstract fun getPageCount(): Int

    open fun getPageTitle(position: Int): CharSequence? {
        return getPageItem(position).simpleClassName()
    }

    //</editor-fold desc="ViewPager相关">
}