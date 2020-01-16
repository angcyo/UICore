package com.angcyo.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import com.angcyo.widget.layout.DslSoftInputLayout

/**
 * 统一标题管理的Fragment,
 *
 * 界面xml中, 已经有打底的RecycleView.
 *
 * 可以直接通过相关id, replace对应的布局结构
 *
 * Email:angcyo@126.com
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

    /**激活软键盘输入*/
    var enableSoftInput: Boolean = false

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        onCreateViewAfter(savedInstanceState)
        BaseUI.fragmentUI.onFragmentCreateViewAfter(this)
        return view
    }

    /**[onCreateView]*/
    open fun onCreateViewAfter(savedInstanceState: Bundle?) {

    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        onInitFragment()
        onInitBehavior()
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
    open fun onInitFragment() {
        baseViewHolder.itemView.isClickable = fragmentConfig.interceptRootTouchEvent

        //内容包裹
        if (enableSoftInput) {
            baseViewHolder.group(R.id.lib_content_wrap_layout)
                ?.replace(DslSoftInputLayout(fContext()).apply {
                    id = R.id.lib_soft_input_layout
                })
            _inflateTo(R.id.lib_soft_input_layout, contentLayoutId)
        } else {
            _inflateTo(R.id.lib_content_wrap_layout, contentLayoutId)
        }
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
    open fun onInitBehavior() {
        rootControl().eachChild { _, child ->
            onCreateBehavior(child)?.run {
                if (this is RefreshBehavior) {
                    refreshBehavior = this

                    //刷新监听
                    onRefresh = this@BaseTitleFragment::onRefresh
                } else if (this is RefreshHeaderBehavior) {
                    refreshBehavior?.let {
                        it.refreshBehaviorConfig = this
                    }
                }
                child.setBehavior(this)
            }
        }
    }

    /**根据[child]创建对应的[Behavior]*/
    open fun onCreateBehavior(child: View): CoordinatorLayout.Behavior<*>? {
        return when (child.id) {
            R.id.lib_title_wrap_layout -> HideTitleBarBehavior(fContext())
            R.id.lib_content_wrap_layout -> RefreshBehavior(fContext())
            R.id.lib_refresh_wrap_layout -> if (enableRefresh) {
                ArcLoadingHeaderBehavior(fContext())
            } else {
                baseViewHolder.gone(R.id.lib_refresh_wrap_layout)
                RefreshHeaderBehavior(fContext())
            }
            else -> null
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