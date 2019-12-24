package com.angcyo.core.fragment

import android.animation.Animator
import android.os.Bundle
import android.view.animation.Animation
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.angcyo.fragment.AbsLifecycleFragment

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

    fun parentFragmentManager(): FragmentManager? {
        return topFragment().fragmentManager
    }

    //</editor-fold desc="操作方法">
}