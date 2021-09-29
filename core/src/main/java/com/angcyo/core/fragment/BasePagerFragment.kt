package com.angcyo.core.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.base.instantiate
import com.angcyo.behavior.refresh.IRefreshBehavior
import com.angcyo.core.R
import com.angcyo.core.viewpager.ViewPager1Delegate
import com.angcyo.getData
import com.angcyo.library.ex.simpleClassName
import com.angcyo.putData
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

    companion object {
        const val TAB_SELECT_INDEX = "tab_select_index"
    }

    /**默认选中第几个tab item*/
    var defaultTabSelectIndex = 0

    /**保存所有页面*/
    val pages = mutableListOf<Fragment>()

    /**页面标题*/
    val titles = mutableListOf<CharSequence>()

    lateinit var fragmentAdapter: FragmentStatePagerAdapter

    /**tab item 的布局*/
    var tabItemLayoutId: Int = R.layout.lib_tab_text_item_layout

    init {
        contentLayoutId = R.layout.lib_pager_fragment
    }

    override fun canFlingBack(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defaultTabSelectIndex = getData(TAB_SELECT_INDEX) ?: defaultTabSelectIndex
    }

    override fun onCreateBehavior(child: View): CoordinatorLayout.Behavior<*>? {
        return super.onCreateBehavior(child)?.apply {
            if (this is IRefreshBehavior) {
                contentScrollView = _vh.view(R.id.lib_view_pager)
            }
        }
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        onInitPager()
    }

    /**界面核心初始化*/
    open fun onInitPager() {
        //adapter
        fragmentAdapter = object :
            FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): Fragment = getPageItem(position)

            override fun getCount(): Int = getPageCount()

            override fun getPageTitle(position: Int): CharSequence? =
                this@BasePagerFragment.getPageTitle(position)
        }

        //tab
        _vh.vp(R.id.lib_view_pager)?.apply {
            adapter = fragmentAdapter
            ViewPager1Delegate.install(this, _vh.tab(R.id.lib_tab_layout)?.apply {
                configTabLayoutConfig {
                    onGetTextStyleView = { itemView, _ ->
                        itemView.find(R.id.lib_tab_text_view)
                    }
                    onGetIcoStyleView = { itemView, _ ->
                        itemView.find(R.id.lib_tab_image_view)
                    }
                }
                if (childCount <= 0) {
                    inflateTabItems(this)
                }
            })

            //离屏缓存数量
            offscreenPageLimit = getPageOffscreenLimit()

            //默认index
            _vh.tab(R.id.lib_tab_layout)?.setCurrentItem(defaultTabSelectIndex)
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
        viewGroup.resetChild(getPageCount(), tabItemLayoutId, this::initTabItem)
    }

    open fun initTabItem(itemView: View, itemIndex: Int) {
        itemView.find<TextView>(R.id.lib_tab_text_view)?.text =
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
    open fun getPageItem(position: Int): Fragment = pages[position]

    /**页面数量*/
    open fun getPageCount(): Int = pages.size

    open fun getPageTitle(position: Int): CharSequence? {
        return titles.getOrNull(position) ?: getPageItem(position).run {
            if (this is BaseTitleFragment) {
                this.fragmentTitle ?: simpleClassName()
            } else {
                simpleClassName()
            }
        }
    }

    //</editor-fold desc="ViewPager相关">

    /**添加一个页面*/
    fun addPage(title: CharSequence, fragment: Class<out Fragment>) {
        titles.add(title)
        pages.add(fragment.instantiate()!!)
    }
}

/**设置默认tab的索引*/
fun BasePagerFragment.tabIndex(index: Int): BasePagerFragment {
    putData(index, BasePagerFragment.TAB_SELECT_INDEX)
    return this
}