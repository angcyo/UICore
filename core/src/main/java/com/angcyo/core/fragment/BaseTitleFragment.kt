package com.angcyo.core.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.angcyo.base.getAllValidityFragment
import com.angcyo.behavior.placeholder.TitleBarPlaceholderBehavior
import com.angcyo.behavior.refresh.RefreshBehavior
import com.angcyo.behavior.refresh.RefreshHeaderBehavior
import com.angcyo.core.R
import com.angcyo.core.appendTextItem
import com.angcyo.core.behavior.ArcLoadingHeaderBehavior
import com.angcyo.library.ex.colorFilter
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.base.*
import com.angcyo.widget.layout.DslSoftInputLayout
import com.angcyo.widget.span.span
import com.angcyo.widget.text.DslTextView

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
        fragmentLayoutId = R.layout.lib_title_fragment
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
            if (isAdded) {
                _vh.tv(R.id.lib_title_text_view)?.text = value
            }
        }

    var fragmentUI: FragmentUI? = null
        get() = field ?: BaseUI.fragmentUI

    //</editor-fold desc="操作属性">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentUI?.onFragmentCreateAfter(this, fragmentConfig)
    }

    override fun onCreateRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateRootView(inflater, container, savedInstanceState)
        if (enableSoftInput) {
            val softInputLayout = DslSoftInputLayout(fContext()).apply {
                id = R.id.lib_soft_input_layout
                handlerMode = DslSoftInputLayout.MODE_CONTENT_HEIGHT
            }
            softInputLayout.addView(view)
            return softInputLayout
        }
        return view
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        onCreateViewAfter(savedInstanceState)
        fragmentUI?.onFragmentCreateViewAfter(this)
        return view
    }

    /**[onCreateView]*/
    open fun onCreateViewAfter(savedInstanceState: Bundle?) {
        if (enableBackItem()) {
            leftControl()?.append(onCreateBackItem())
        }
    }

    open fun onCreateBackItem(): View? {
        return fragmentUI?.onCreateFragmentBackItem(this)
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
            _vh.visible(wrapId)
            _vh.group(wrapId)?.replace(layoutId)
        } else {
            _vh.gone(wrapId, _vh.group(wrapId)?.childCount ?: 0 <= 0)
        }
    }

    /**初始化样式*/
    open fun onInitFragment() {
        _vh.itemView.isClickable = fragmentConfig.interceptRootTouchEvent

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

        fragmentTitle = fragmentTitle ?: this.javaClass.simpleName
    }

    /**初始化[Behavior]*/
    open fun onInitBehavior() {
        rootControl().group(R.id.lib_coordinator_wrap_layout)?.eachChild { _, child ->
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
            //HideTitleBarBehavior(fContext())
            R.id.lib_title_wrap_layout -> TitleBarPlaceholderBehavior(fContext())
            R.id.lib_content_wrap_layout -> RefreshBehavior(fContext())
            R.id.lib_refresh_wrap_layout -> if (enableRefresh) {
                ArcLoadingHeaderBehavior(fContext())
            } else {
                _vh.gone(R.id.lib_refresh_wrap_layout)
                RefreshHeaderBehavior(fContext())
            }
            else -> null
        }
    }

    /**常用控制助手*/
    open fun titleControl(): DslGroupHelper? =
        _vh.view(R.id.lib_title_wrap_layout)?.run { DslGroupHelper(this) }

    open fun leftControl(): DslGroupHelper? =
        _vh.view(R.id.lib_left_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rightControl(): DslGroupHelper? =
        _vh.view(R.id.lib_right_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rootControl(): DslGroupHelper = DslGroupHelper(_vh.itemView)

    open fun contentControl(): DslGroupHelper? =
        _vh.view(R.id.lib_content_wrap_layout)?.run { DslGroupHelper(this) }

    /**在确保布局已经测量过后, 才执行*/
    fun _laidOut(action: (View) -> Unit) {
        if (ViewCompat.isLaidOut(_vh.itemView)) {
            action(_vh.itemView)
        } else {
            _vh.itemView.doOnPreDraw(action)
        }
    }

    /**开始刷新*/
    open fun startRefresh() {
        _laidOut {
            refreshBehavior?.startRefresh()
        }
    }

    /**结束刷新*/
    open fun finishRefresh() {
        _laidOut {
            refreshBehavior?.finishRefresh()
        }
    }

    /**刷新回调*/
    open fun onRefresh(refreshBehavior: RefreshBehavior?) {

    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="扩展方法">

    fun DslGroupHelper.appendItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        appendTextItem {
            setTextColor(fragmentConfig.titleItemTextColor)
            this.text = span {

                if (ico != undefined_res) {
                    drawable {
                        backgroundDrawable =
                            getDrawable(ico).colorFilter(fragmentConfig.titleItemIconColor)
                    }
                }

                if (text != null) {
                    drawable(text) {
                        textGravity = Gravity.CENTER
                    }
                }
            }
            clickIt(onClick)
            this.action()
        }
    }

    fun appendLeftItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        leftControl()?.appendItem(text, ico, action, onClick)
    }

    fun appendRightItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        rightControl()?.appendItem(text, ico, action, onClick)
    }

    //</editor-fold desc="扩展方法">


}