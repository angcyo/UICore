package com.angcyo.core.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.angcyo.base.getAllValidityFragment
import com.angcyo.behavior.HideTitleBarBehavior
import com.angcyo.behavior.refresh.RefreshBehavior
import com.angcyo.behavior.refresh.RefreshHeaderBehavior
import com.angcyo.core.R
import com.angcyo.core.behavior.ArcLoadingHeaderBehavior
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.base.replace
import com.angcyo.widget.base.setBehavior

/**
 * Email:angcyo@126.com
 * 统一标题管理的Fragment
 *
 * @author angcyo
 * @date 2018/12/07
 */
abstract class BaseTitleFragment : BaseFragment() {

    init {
        /**Fragment根布局*/
        fragmentLayoutId = R.layout.lib_title_fragment_layout
    }

    /**自定义内容布局*/
    var contentLayoutId: Int = -1

    /**自定义的刷新头部*/
    var refreshLayoutId: Int = -1

    /**自定义标题栏布局*/
    var titleLayoutId: Int = -1

    /**是否激活刷新回调*/
    var enableRefresh: Boolean = false

    var refreshBehavior: RefreshBehavior? = null

    //<editor-fold desc="操作属性">

    var fragmentConfig: FragmentConfig = FragmentConfig()

    /**标题*/
    var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            baseViewHolder.tv(R.id.lib_title_text_view)?.text = value
        }

    //</editor-fold desc="操作属性">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseUI.fragmentUI.onFragmentCreateAfter(this, fragmentConfig)
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        initTitleFragment()
        initBehavior()

        BaseUI.fragmentUI.onFragmentInitBaseViewAfter(this)
    }

    /**是否要显示返回按钮*/
    open fun enableBackItem(): Boolean {
        return topFragment() == this && fragmentManager?.getAllValidityFragment()?.size ?: 0 > 0
    }

    //<editor-fold desc="操作方法">

    fun _inflateTo(wrapId: Int, layoutId: Int) {
        if (layoutId > 0) {
            baseViewHolder.visible(wrapId)
            baseViewHolder.group(wrapId)?.replace(layoutId)
        } else {
            baseViewHolder.gone(wrapId, baseViewHolder.group(wrapId)?.childCount ?: 0 <= 0)
        }
    }

    /**初始化样式*/
    open fun initTitleFragment() {
        baseViewHolder.itemView.isClickable = fragmentConfig.interceptRootTouchEvent

        //内容包裹
        _inflateTo(R.id.lib_content_wrap_layout, contentLayoutId)
        //刷新头包裹
        _inflateTo(R.id.lib_refresh_wrap_layout, refreshLayoutId)
        //标题包裹
        _inflateTo(R.id.lib_title_wrap_layout, titleLayoutId)

        titleControl()?.apply {
            setBackground(fragmentConfig.titleBarBackgroundDrawable)
            selector(R.id.lib_title_text_view)
            setTextSize(fragmentConfig.titleTextSize)
            setTextColor(fragmentConfig.titleTextColor)
        }

        leftControl()?.apply {
            setDrawableColor(fragmentConfig.titleItemIconColor)
            setTextColor(fragmentConfig.titleItemTextColor)
        }

        rightControl()?.apply {
            setDrawableColor(fragmentConfig.titleItemIconColor)
            setTextColor(fragmentConfig.titleItemTextColor)
        }

        rootControl().setBackground(fragmentConfig.fragmentBackgroundDrawable)

        fragmentTitle = this.javaClass.simpleName
    }

    /**初始化[Behavior]*/
    open fun initBehavior() {
        val refreshHeaderBehavior = if (enableRefresh) {
            ArcLoadingHeaderBehavior(fContext())
        } else {
            baseViewHolder.gone(R.id.lib_refresh_wrap_layout)
            RefreshHeaderBehavior(fContext())
        }
        rootControl().eachChild { _, child ->
            when (child.id) {
                R.id.lib_title_wrap_layout -> child.setBehavior(HideTitleBarBehavior(fContext()))
                R.id.lib_content_wrap_layout -> child.setBehavior(RefreshBehavior(fContext()).apply {
                    refreshBehavior = this
                    onRefresh = this@BaseTitleFragment::onRefresh
                    refreshBehaviorConfig = refreshHeaderBehavior
                })
                R.id.lib_refresh_wrap_layout -> child.setBehavior(refreshHeaderBehavior)
            }
        }
    }

    /**常用控制助手*/
    open fun titleControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_title_wrap_layout)?.run { DslGroupHelper(this) }

    open fun leftControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_left_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rightControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_right_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rootControl(): DslGroupHelper = DslGroupHelper(baseViewHolder.itemView)

    open fun contentControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_content_wrap_layout)?.run { DslGroupHelper(this) }

    /**在确保布局已经测量过后, 才执行*/
    fun _laidOut(action: (View) -> Unit) {
        if (ViewCompat.isLaidOut(baseViewHolder.itemView)) {
            action(baseViewHolder.itemView)
        } else {
            baseViewHolder.itemView.doOnPreDraw(action)
        }
    }

    /**开始刷新*/
    open fun startRefresh() {
        _laidOut {
            refreshBehavior?.refreshStatus = RefreshBehavior.STATUS_REFRESH
        }
    }

    /**结束刷新*/
    open fun finishRefresh() {
        _laidOut {
            refreshBehavior?.refreshStatus = RefreshBehavior.STATUS_FINISH
        }
    }

    /**刷新回调*/
    open fun onRefresh(refreshBehavior: RefreshBehavior?) {

    }

    //</editor-fold desc="操作方法">
}