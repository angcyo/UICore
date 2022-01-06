package com.angcyo.core.fragment

import android.animation.Animator
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.angcyo.core.R
import com.angcyo.core.appendTextItem
import com.angcyo.core.lifecycle.CompositeDisposableLifecycle
import com.angcyo.core.lifecycle.CoroutineScopeLifecycle
import com.angcyo.fragment.AbsFragment
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.library.ex.colorFilter
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.loadDrawable
import com.angcyo.widget.span.span
import com.angcyo.widget.text.DslTextView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope

/**
 * Created by angcyo on 2018/12/03 23:17
 *
 *
 * 生命周期的封装, 只需要关注 [.onFragmentShow] 和 [.onFragmentHide]
 */
abstract class BaseFragment : AbsLifecycleFragment() {

    //<editor-fold desc="Fragment样式配置">

    var fragmentUI: FragmentUI? = null
        get() = field ?: BaseUI.fragmentUI

    var fragmentConfig: FragmentConfig = FragmentConfig()

    //</editor-fold desc="Fragment样式配置">

    //<editor-fold desc="生命周期">

    /**
     * onActivityCreated -> onCreateAnimation -> onCreateAnimator
     */
    override fun onCreateAnimation(
        transit: Int,
        enter: Boolean,
        nextAnim: Int
    ): Animation? {
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun onCreateAnimator(
        transit: Int,
        enter: Boolean,
        nextAnim: Int
    ): Animator? {
        return super.onCreateAnimator(transit, enter, nextAnim)
    }

    /**
     * 此方法会在onCreateView之后回调
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onFragmentShow(bundle: Bundle?) {
        super.onFragmentShow(bundle)
        BaseUI.onFragmentShow?.invoke(this)
        changeCoordinatorBehavior()
    }

    /**当前[BaseFragment]是否有[CoordinatorLayout]布局*/
    open fun haveCoordinatorLayout() = view is CoordinatorLayout

    open fun closeCoordinatorLayout(close: Boolean = true) {
        val view = view
        if (view is CoordinatorLayout) {
            view.isEnabled = close
        }
    }

    /**协调布局内容行为控制*/
    open fun changeCoordinatorBehavior() {
        val parent = parentFragment
        val isParentHaveCoordinator =
            if (parent is BaseFragment) parent.haveCoordinatorLayout() else false

        if (isParentHaveCoordinator) {
            //关闭, parent/自身协调布局的特性
            var closeParentCoordinator = true
            if (this is BaseTitleFragment) {
                closeParentCoordinator = enableRefresh
            }

            if (parent is BaseFragment) {
                parent.closeCoordinatorLayout(!closeParentCoordinator)
            }

            closeCoordinatorLayout(closeParentCoordinator)
        }
    }

    override fun onFragmentHide() {
        super.onFragmentHide()
        BaseUI.onFragmentHide?.invoke(this)
    }

    //</editor-fold desc="生命周期">

    //<editor-fold desc="操作方法">

    /**[onCreateView]*/
    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        if (interceptRootTouchEvent()) {
            baseViewHolder?.itemView?.isClickable = true
        }
    }

    /**
     * 拦截RootView的事件, 防止事件穿透到底下的Fragment
     */
    open fun interceptRootTouchEvent(): Boolean {
        return true
    }

    /**顶层的[Fragment]*/
    fun topFragment(): Fragment {
        val parentFragment = parentFragment
        return if (parentFragment == null) {
            this
        } else {
            if (parentFragment is BaseFragment) {
                parentFragment.topFragment()
            } else {
                parentFragment
            }
        }
    }

    /**顶层的[FragmentManager]*/
    fun topFragmentManager(): FragmentManager? {
        return topFragment().parentFragmentManager
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="Rx 协程">

    //Rx调度管理
    val compositeDisposableLifecycle: CompositeDisposableLifecycle by lazy {
        CompositeDisposableLifecycle(this)
    }

    //协程域管理
    val coroutineScopeLifecycle: CoroutineScopeLifecycle by lazy {
        CoroutineScopeLifecycle(this)
    }

    /**取消所有异步调度*/
    fun cancelSchedule(reason: Int = -1) {
        cancelAllDisposable(reason)
        cancelCoroutineScope(reason)
    }

    /**管理[Disposable]*/
    fun Disposable.attach(): Disposable {
        compositeDisposableLifecycle.add(this)
        return this
    }

    /**取消所有[Disposable]*/
    fun cancelAllDisposable(reason: Int = -1) {
        compositeDisposableLifecycle.onCancelCallback(reason)
    }

    /**取消协程域*/
    fun cancelCoroutineScope(reason: Int = -1) {
        coroutineScopeLifecycle.onCancelCallback(reason)
    }

    /**启动一个具有生命周期的协程域*/
    fun launchLifecycle(block: suspend CoroutineScope.() -> Unit) =
        coroutineScopeLifecycle.launch(block)

    //</editor-fold desc="Rx 协程">

    //<editor-fold desc="Append操作">

    fun DslGroupHelper.appendItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        appendTextItem {
            gravity = Gravity.CENTER
            setTextColor(fragmentConfig.titleItemTextColor)
            this.text = span {

                if (ico != undefined_res) {
                    drawable {
                        backgroundDrawable =
                            loadDrawable(ico).colorFilter(fragmentConfig.titleItemIconColor)
                        textGravity = Gravity.CENTER
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

    //</editor-fold desc="Append操作">
}

/**关闭父[Fragment]中的协调布局功能*/
fun Fragment.closeParentCoordinator(close: Boolean = true) {
    val isInTitleFragment = parentFragment is BaseTitleFragment

    if (isInTitleFragment) {
        val parentTitleFragment = parentFragment as BaseTitleFragment
        parentTitleFragment._vh.v<CoordinatorLayout>(R.id.lib_coordinator_wrap_layout)?.isEnabled =
            !close
    }
}

/**关闭自身[Fragment]的协调布局功能*/
fun Fragment.closeCoordinator(close: Boolean = true) {
    if (this is AbsFragment) {
        _vh.v<CoordinatorLayout>(R.id.lib_coordinator_wrap_layout)?.isEnabled =
            !close
    }
}