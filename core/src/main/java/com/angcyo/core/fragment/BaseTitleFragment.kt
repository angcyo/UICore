package com.angcyo.core.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import com.angcyo.DslAHelper
import com.angcyo.base.getAllValidityFragment
import com.angcyo.base.lightStatusBar
import com.angcyo.behavior.HideTitleBarBehavior
import com.angcyo.behavior.placeholder.TitleBarPlaceholderBehavior
import com.angcyo.behavior.refresh.IRefreshBehavior
import com.angcyo.behavior.refresh.IRefreshContentBehavior
import com.angcyo.behavior.refresh.RefreshContentBehavior
import com.angcyo.behavior.refresh.RefreshEffectBehavior
import com.angcyo.component.DslAffect
import com.angcyo.component.dslAffect
import com.angcyo.core.R
import com.angcyo.core.behavior.ArcLoadingHeaderBehavior
import com.angcyo.library.L
import com.angcyo.library.component.dslIntent
import com.angcyo.library.ex.*
import com.angcyo.library.model.Page
import com.angcyo.lifecycle.onStart
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.base.*
import com.angcyo.widget.layout.*
import com.angcyo.widget.recycler.DslRecyclerView
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
abstract class BaseTitleFragment : BaseFragment(), OnSoftInputListener {

    companion object {
        var DEFAULT_FIRST_REFRESH_DELAY = 240L
    }

    //<editor-fold desc="成员配置">

    /**自定义内容布局*/
    var contentLayoutId: Int = -1

    /**自定义内容覆盖布局*/
    var contentOverlayLayoutId: Int = -1

    /**自定义的刷新头部*/
    var refreshLayoutId: Int = -1

    /**自定义标题栏布局*/
    var titleLayoutId: Int = -1

    /**是否激活刷新回调*/
    var enableRefresh: Boolean = false
        set(value) {
            field = value
            if (value) {
                enableAdapterRefresh = true
            }
        }

    /**是否激活适配器的刷新回调*/
    var enableAdapterRefresh: Boolean = false

    /**首次加载刷新时, 延迟的时长. 以便动画流畅执行完
     * -1 自动设置*/
    var firstRefreshDelay: Long = -1

    //记录刷新的次数
    var _refreshCount: Int = 0

    /**激活软键盘输入*/
    var enableSoftInput: Boolean = false

    /**是否需要强制显示返回按钮, 否则智能判断*/
    var enableBackItem: Boolean = false

    /**是否隐藏标题布局, null表示智能默认行为*/
    var hideTitleLayout: Boolean? = null

    /**用于控制刷新状态, 开始刷新/结束刷新*/
    var refreshContentBehavior: IRefreshContentBehavior? = null

    /**情感图状态切换, 按需初始化.*/
    var affectUI: DslAffect? = null

    /**实时获取[DslRecyclerView]*/
    val _recycler: DslRecyclerView
        get() = (_vh.rv(R.id.lib_recycler_view) as? DslRecyclerView)
            ?: DslRecyclerView(fContext()).apply {
                L.e("注意:访问目标[_recycler]不存在!")
            }

    //</editor-fold desc="成员配置">

    //<editor-fold desc="操作属性">

    /**标题*/
    open var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            if (isAdded && baseViewHolder != null) {
                _vh.tv(R.id.lib_title_text_view)?.text = value
            }
        }

    init {
        //Fragment根布局
        fragmentLayoutId = R.layout.lib_title_fragment

        //默认标题
        fragmentTitle = this.javaClass.simpleName
    }

    //</editor-fold desc="操作属性">

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentUI?.fragmentCreateBefore?.invoke(this, fragmentConfig, savedInstanceState)
        val lightStyle = fragmentConfig.isLightStyle
        if (lightStyle != null) {
            if (lightStyle) {
                fragmentConfig.lightStyle()
            }
            fContext().let {
                if (it is Activity) {
                    it.lightStatusBar(lightStyle)
                }
            }
        }
        super.onCreate(savedInstanceState)
        fragmentUI?.fragmentCreateAfter?.invoke(this, fragmentConfig, savedInstanceState)
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
                addSoftInputListener(this@BaseTitleFragment)
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

        val isInTitleFragment = parentFragment is BaseTitleFragment

        //智能处理标题栏
        if (hideTitleLayout == null) {
            if (isInTitleFragment) {
                //在parent中, 智能去除标题栏
                _vh.gone(R.id.lib_title_wrap_layout)
            }
        } else {
            _vh.gone(R.id.lib_title_wrap_layout, hideTitleLayout == true)
        }

        //智能处理加载延迟
        if (firstRefreshDelay < 0) {
            if (parentFragment == null) {
                firstRefreshDelay = DEFAULT_FIRST_REFRESH_DELAY
            }
        }

        onCreateViewAfter(savedInstanceState)
        fragmentUI?.fragmentCreateViewAfter?.invoke(this)
        return view
    }

    /**[onCreateView]*/
    open fun onCreateViewAfter(savedInstanceState: Bundle?) {
        if (enableBackItem()) {
            leftControl()?.append(onCreateBackItem())
        }
    }

    open fun onCreateBackItem(): View? {
        return fragmentUI?.fragmentCreateBackItem?.invoke(this)
    }

    /**[onCreateView]*/
    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        onInitFragment(savedInstanceState)
        onInitBehavior()
    }

    /**是否要显示返回按钮*/
    open fun enableBackItem(): Boolean {
        var showBackItem = false
        val count = fragmentManager?.getAllValidityFragment()?.size ?: 0

        if (enableBackItem) {
            /*强制激活了返回按钮*/
            showBackItem = true
        } else if (topFragment() != this) {
            showBackItem = false
        } else if (count <= 0) {
            val activity = activity
            if (activity != null) {

                /*Activity中第一个Fragment*/
                if (!DslAHelper.isMainActivity(activity)) {
                    //当前Fragment所在Activity不是主界面
                    showBackItem = true
                }

                dslIntent {
                    queryAction = Intent.ACTION_MAIN
                    queryCategory = listOf(Intent.CATEGORY_LAUNCHER)
                    queryPackageName = activity.packageName

                    val launcherList = doQuery(activity)
                    if (launcherList.isEmpty() && DslAHelper.mainActivityClass == null) {
                        //如果没有启动页, 并且没有配置主页, 则不显示back按钮
                        showBackItem = false
                    } else if (launcherList.any { it.activityInfo.name == activity.className() }) {
                        //当前的[Activity]在xml中声明了主页标识
                        showBackItem = false
                    }
                }
            }
        } else {
            /*可见Fragment数量大于0*/
            showBackItem = topFragment() == this
        }

        return showBackItem
    }

    //<editor-fold desc="操作方法">

    fun _inflateTo(wrapId: Int, layoutId: Int) {
        if (layoutId > 0) {
            _vh.visible(wrapId)
            _vh.group(wrapId)?.replace(layoutId)
        } else {
            _vh.gone(wrapId, (_vh.group(wrapId)?.childCount ?: 0) <= 0)
        }
    }

    /**初始化样式, 在这个方法中操作[titleControl] [leftControl] [rightControl] 会应用颜色配置
     * [initBaseView]*/
    open fun onInitFragment(savedInstanceState: Bundle?) {
        _vh.itemView.isClickable = fragmentConfig.interceptRootTouchEvent
        //阴影
        _vh.visible(R.id.lib_title_line_view, fragmentConfig.showTitleLineView)

        //内容包裹
        _inflateTo(R.id.lib_content_wrap_layout, contentLayoutId)
        //内容覆盖层
        _inflateTo(R.id.lib_content_overlay_wrap_layout, contentOverlayLayoutId)
        //刷新头包裹
        _inflateTo(R.id.lib_refresh_wrap_layout, refreshLayoutId)
        //标题包裹
        _inflateTo(R.id.lib_title_wrap_layout, titleLayoutId)

        titleControl()?.apply {
            setBackground(fragmentConfig.titleBarBackgroundDrawable)
            selector(R.id.lib_title_text_view)
            setTextSize(fragmentConfig.titleTextSize)
            setTextStyle(fragmentConfig.titleTextType)
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

        fragmentTitle = fragmentTitle

        //双击标题栏
        titleControl()?.view(R.id.lib_title_wrap_layout)?.onDoubleTap {
            onDoubleTitleLayout()
        }
    }

    open fun onDoubleTitleLayout(): Boolean {
        if ((_recycler.adapter?.itemCount ?: 0) >= Page.PAGE_SIZE * 2) {
            _recycler.scrollToPosition(0)
        } else {
            _recycler.smoothScrollToPosition(0)
        }
        return true
    }

    /**初始化[Behavior]
     * [initBaseView]*/
    open fun onInitBehavior() {
        rootControl().group(R.id.lib_coordinator_wrap_layout)?.eachChild { _, child ->
            onCreateBehavior(child)?.run {
                if (this is RefreshContentBehavior) {
                    refreshContentBehavior = this

                    //刷新监听
                    refreshAction = {
                        _delayRefresh {
                            this@BaseTitleFragment.onRefresh(it)
                        }
                    }
                }
                child.setBehavior(this)
            }
        }
    }

    /**是否需要激活隐藏标题栏的行为*/
    var enableTitleBarHideBehavior = false

    /**是否开启内容边界回弹*/
    var enableContentBounds = true

    /**根据[child]创建对应的[Behavior]*/
    open fun onCreateBehavior(child: View): CoordinatorLayout.Behavior<*>? {
        return child.behavior() ?: when (child.id) {
            R.id.lib_title_wrap_layout -> if (enableTitleBarHideBehavior) {
                HideTitleBarBehavior(fContext()).apply {
                    ignoreStatusBar = true
                }
            } else {
                TitleBarPlaceholderBehavior(fContext())
            }
            R.id.lib_content_wrap_layout -> RefreshContentBehavior(fContext())
            R.id.lib_refresh_wrap_layout -> if (enableRefresh) {
                ArcLoadingHeaderBehavior(fContext()).apply {
                    enableTopOver = enableContentBounds
                    enableBottomOver = enableContentBounds
                }
            } else {
                _vh.gone(R.id.lib_refresh_wrap_layout)
                RefreshEffectBehavior(fContext()).apply {
                    enableTopOver = enableContentBounds
                    enableBottomOver = enableContentBounds
                }
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
            refreshContentBehavior?.setRefreshContentStatus(IRefreshBehavior.STATUS_REFRESH)
        }
    }

    /**结束刷新*/
    open fun finishRefresh() {
        _laidOut {
            refreshContentBehavior?.setRefreshContentStatus(IRefreshBehavior.STATUS_FINISH)
        }
    }

    /**刷新回调*/
    open fun onRefresh(refreshContentBehavior: IRefreshContentBehavior?) {
        _refreshCount++
    }

    fun _delayRefresh(action: Action) {
        if (_refreshCount <= 0) {
            _vh.postDelay(firstRefreshDelay, action)
        } else {
            action()
        }
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="扩展方法">

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

    //<editor-fold desc="软键盘监听">

    override fun onSoftInputChangeStart(action: Int, height: Int, oldHeight: Int) {
        super.onSoftInputChangeStart(action, height, oldHeight)
        if (action.isHideAction()) {
            //是隐藏动作
        }
    }

    override fun onSoftInputChangeEnd(action: Int, height: Int, oldHeight: Int) {
        super.onSoftInputChangeEnd(action, height, oldHeight)
        if (action.isShowAction()) {
            //是显示动作
        }
    }

    override fun onSoftInputChange(action: Int, height: Int, oldHeight: Int, fraction: Float) {
        super.onSoftInputChange(action, height, oldHeight, fraction)
    }

    /**返回键监听*/
    override fun onBackPressed(): Boolean {
        val softInputLayout = _vh.v<DslSoftInputLayout>(R.id.lib_soft_input_layout)
        if (softInputLayout != null) {
            return if (softInputLayout.onBackPress()) {
                super.onBackPressed()
            } else {
                false
            }
        }
        return super.onBackPressed()
    }

    //</editor-fold desc="软键盘监听">

    //<editor-fold desc="情感图切换">

    fun installAffect(viewGroup: ViewGroup?) {
        affectUI = dslAffect(viewGroup) {
            affectChangeBefore = this@BaseTitleFragment::onAffectChangeBefore
            affectChanged = this@BaseTitleFragment::onAffectChanged
        }
        viewGroup?.visible()
    }

    fun onAffectChangeBefore(dslAffect: DslAffect, from: Int, to: Int, data: Any?): Boolean {
        return false
    }

    fun onAffectChanged(
        dslAffect: DslAffect,
        from: Int,
        to: Int,
        fromView: View?,
        toView: View,
        data: Any?
    ) {
        if (to == DslAffect.AFFECT_LOADING) {
            //触发刷新
            onRefresh(null)
        }
    }

    /**显示加载中*/
    fun affectLoading() {
        affectUI ?: installAffect(_vh.v(R.id.lib_content_overlay_wrap_layout))
        affectUI?.showAffect(DslAffect.AFFECT_LOADING)
    }

    /**显示内容*/
    fun affectContent() {
        affectUI ?: installAffect(_vh.v(R.id.lib_content_overlay_wrap_layout))
        affectUI?.showAffect(DslAffect.AFFECT_CONTENT)
    }

    // </editor-fold desc="情感图切换">
}

/**设置为一个简单的内嵌列表界面*/
fun BaseTitleFragment.singleRecycler() {
    fragmentLayoutId = R.layout.lib_recycler_layout
}

/**设置为一个简单的内嵌列表界面, 请在[onCreateView]之后使用*/
fun BaseTitleFragment.hideTitle() {
    //titleLayoutId = R.layout.lib_empty_item
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
        _vh.gone(R.id.lib_title_wrap_layout)
    } else {
        lifecycle.onStart {
            _vh.gone(R.id.lib_title_wrap_layout)
            true
        }
    }
}