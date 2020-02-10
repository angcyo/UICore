package com.angcyo.core.fragment

import android.animation.Animator
import android.os.Bundle
import android.view.animation.Animation
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.angcyo.core.lifecycle.CompositeDisposableLifecycle
import com.angcyo.core.lifecycle.CoroutineScopeLifecycle
import com.angcyo.fragment.AbsLifecycleFragment
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope

/**
 * Created by angcyo on 2018/12/03 23:17
 *
 *
 * 生命周期的封装, 只需要关注 [.onFragmentShow] 和 [.onFragmentHide]
 */
abstract class BaseFragment : AbsLifecycleFragment() {

    /**
     * onCreateAnimation -> onCreateAnimator
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

    //<editor-fold desc="操作方法">

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        if (interceptRootTouchEvent()) {
            baseViewHolder.itemView.isClickable = true
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

    val compositeDisposableLifecycle: CompositeDisposableLifecycle by lazy {
        CompositeDisposableLifecycle(this)
    }

    val coroutineScopeLifecycle: CoroutineScopeLifecycle by lazy {
        CoroutineScopeLifecycle(this)
    }

    /**管理[Disposable]*/
    fun Disposable.attach(): Disposable {
        compositeDisposableLifecycle.add(this)
        return this
    }

    /**取消所有[Disposable]*/
    fun cancelAllDisposable() {
        compositeDisposableLifecycle.dispose()
    }

    /**启动一个具有生命周期的协程域*/
    fun launchLifecycle(block: suspend CoroutineScope.() -> Unit) {
        coroutineScopeLifecycle.launch(block)
    }

    //</editor-fold desc="Rx 协程">
}