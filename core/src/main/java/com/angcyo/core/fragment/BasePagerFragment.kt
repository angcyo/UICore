package com.angcyo.core.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.angcyo.base.instantiate
import com.angcyo.behavior.refresh.IRefreshBehavior
import com.angcyo.core.R
import com.angcyo.core.viewpager.ViewPager1Delegate
import com.angcyo.getData
import com.angcyo.library.ex.*
import com.angcyo.putData
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.widget.base.resetChild
import com.angcyo.widget.recycler.scrollHelper
import com.angcyo.widget.tab
import com.angcyo.widget.vp
import kotlin.math.min

/**
 * Tab+ViewPager 页面结构
 *
 * 顶部 [DslTabLayout]
 * 底部 [DslViewPager]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BasePagerFragment : BaseTitleFragment() {

    companion object {
        /**默认选中的索引key*/
        const val TAB_SELECT_INDEX = "tab_select_index"
    }

    /**默认选中第几个tab item*/
    var defaultTabSelectIndex = 0

    /**页面数据*/
    val pageList = mutableListOf<PagerTabItem>()

    lateinit var fragmentAdapter: FragmentStatePagerAdapter

    /**tab item 的布局*/
    var tabItemLayoutId: Int = R.layout.lib_tab_text_item_layout

    /**是否将tab layout放在title栏中*/
    var enableTitleTabLayout: Boolean = false
        set(value) {
            field = value
            if (value) {
                titleLayoutId = R.layout.lib_tab_title_layout
                contentLayoutId = R.layout.lib_pager_fragment
            } else {
                titleLayoutId = -1
                contentLayoutId = R.layout.lib_tab_pager_fragment
            }
        }

    init {
        //默认tab在顶部
        contentLayoutId = R.layout.lib_tab_pager_fragment
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
        initPagerView()
    }

    /**界面核心初始化
     * [ViewPager]*/
    open fun initPagerView() {
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
                if (childCount <= 0) {
                    inflateTabItems(this)
                }
            })

            //离屏缓存数量
            offscreenPageLimit = getPageOffscreenLimit()

            //默认index
            initTabLayout()
        }
    }

    /**[DslTabLayout]*/
    open fun initTabLayout() {
        _vh.tab(R.id.lib_tab_layout)?.apply {
            configTabLayoutConfig {
                onGetTextStyleView = { itemView, _ ->
                    itemView.find(R.id.lib_tab_text_view)
                }
                onGetIcoStyleView = { itemView, _ ->
                    itemView.find(R.id.lib_tab_image_view)
                }
                onSelectIndexChange = { fromIndex, selectIndexList, reselect, fromUser ->
                    selectIndexList.firstOrNull()?.let {
                        //关键代码, 切换ViewPager
                        _viewPagerDelegate?.onSetCurrentItem(fromIndex, it, reselect, fromUser)
                        onTabLayoutIndexChange(fromIndex, it, reselect, fromUser)
                    }
                }
            }
            setCurrentItem(defaultTabSelectIndex)
        }
    }

    /**当切换了[DslTabLayout]时回调*/
    open fun onTabLayoutIndexChange(
        fromIndex: Int,
        toIndex: Int,
        reselect: Boolean,
        fromUser: Boolean
    ) {
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

        itemView.find<ImageView>(R.id.lib_tab_image_view)
            ?.setImageResource(pageList.getOrNull(itemIndex)?.imageResId ?: 0)

        itemView.onDoubleTap {
            //双击tab item 滚动至顶部
            getPageItem(itemIndex).view?.find<RecyclerView>(R.id.lib_recycler_view)
                ?.scrollHelper {
                    lockScrollToFirst {
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
    open fun getPageItem(position: Int): Fragment {
        val pagerTabItem = pageList[position]
        if (pagerTabItem._fragment == null) {
            pagerTabItem._fragment =
                pagerTabItem.fragment.instantiate()!!.apply(pagerTabItem.fragmentInitAction)
        }
        return pagerTabItem._fragment!!
    }

    /**[getPageItem]*/
    open fun <T : Fragment> getPageFragment(position: Int, cls: Class<T>): T =
        getPageItem(position) as T

    /**页面数量*/
    open fun getPageCount(): Int = pageList.size

    open fun getPageTitle(position: Int): CharSequence? {
        return pageList.getOrNull(position)?.text ?: getPageItem(position).run {
            if (this is BaseTitleFragment) {
                this.fragmentTitle ?: simpleClassName()
            } else {
                simpleClassName()
            }
        }
    }

    //</editor-fold desc="ViewPager相关">

    /**添加一个页面
     * 在init方法中调用
     * */
    fun addPage(
        title: CharSequence,
        fragment: Class<out Fragment>,
        init: Fragment.() -> Unit = {}
    ) {
        pageList.add(PagerTabItem(fragment, title, fragmentInitAction = init))
    }

    /** 比[addPage]更丰富
     *
     * [fragment] 需要切换到的界面
     * [title] tab item上的文本
     * [imageResId] tab item上的图标资源
     * */
    fun addTabPage(
        fragment: Class<out Fragment>,
        title: CharSequence,
        @DrawableRes imageResId: Int = 0,
        init: Fragment.() -> Unit = {}
    ) {
        pageList.add(PagerTabItem(fragment, title, imageResId, fragmentInitAction = init))
    }
}

data class PagerTabItem(
    /**对应的界面*/
    val fragment: Class<out Fragment>,
    /**Tab的文本*/
    val text: CharSequence,
    /**Tab的图标资源*/
    @DrawableRes
    val imageResId: Int = 0,
    /**缓存*/
    var _fragment: Fragment? = null,
    /**创建[fragment]时的初始化*/
    val fragmentInitAction: Fragment.() -> Unit = {}
)

/**设置默认tab的索引*/
fun BasePagerFragment.tabIndex(index: Int): BasePagerFragment {
    putData(index, BasePagerFragment.TAB_SELECT_INDEX)
    return this
}