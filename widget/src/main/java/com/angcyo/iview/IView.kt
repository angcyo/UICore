package com.angcyo.iview

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.Anim
import com.angcyo.library.ex.cancelAnimator
import com.angcyo.library.ex.mH
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R

/**
 * 提供一个轻量级的[Fragment]界面操作类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class IView : OnBackPressedCallback(true), LifecycleOwner {

    /**需要填充的布局id*/
    @LayoutRes
    var iViewLayoutId: Int = -1

    /**添加[IView]时的过渡动画*/
    var iViewAddTransition: (rootView: View, end: () -> Unit) -> Unit = { rootView, end ->
        rootView.cancelAnimator()
        rootView.doOnPreDraw {
            it.translationY = it.mH().toFloat()
            it.animate().translationY(0f).setDuration(Anim.ANIM_DURATION).start()
        }
        rootView.requestLayout()
        end()
    }

    /**移除[IView]时的过渡动画*/
    var iViewRemoveTransition: (rootView: View, end: () -> Unit) -> Unit = { rootView, end ->
        rootView.cancelAnimator()
        rootView.animate().translationY(rootView.mH().toFloat()).withEndAction {
            end()
        }.setDuration(Anim.ANIM_DURATION).start()
    }

    /**[IView]显示的次数*/
    var showCount = 0

    /**是否可以被取消*/
    var cancelable: Boolean = true

    //IView所在的容器
    var _parentView: ViewGroup? = null

    var iViewHolder: DslViewHolder? = null

    /**回退栈调度者*/
    var backPressedDispatcherOwner: OnBackPressedDispatcherOwner? = null

    /**生命周期事件通知*/
    var onIViewEvent: ((iView: IView, event: Lifecycle.Event) -> Unit)? = null

    //----

    val _iVh: DslViewHolder get() = iViewHolder!!

    val viewHolder: DslViewHolder? get() = iViewHolder

    val iContext: Context get() = _parentView?.context!!

    val iActivity: Activity? get() = if (iContext is Activity) iContext as Activity else null

    //<editor-fold desc="操作方法区">

    /**
     * 将[IView]显示到[parent]中, 如果[parent]为空, 会将[IView]从原来的[parent]中移除
     *
     * [addInner]
     * */
    @CallPoint
    open fun show(parent: ViewGroup?) {
        if (_parentView != parent) {
            removeInner(_parentView) {
                addInner(parent)
            }
        } else {
            onIViewReShow()
        }
    }

    /**
     * 将[IView]从到[_parentView]中移除
     *
     * [removeInner]
     * */
    @CallPoint
    open fun hide(end: (() -> Unit)? = null) {
        if (cancelable) {
            if (iViewHolder == null) {
                return
            }
            removeInner(_parentView) {
                end?.invoke()
            }
        }
    }

    /**是否附加到界面*/
    fun isAttach() = iViewHolder != null && iViewHolder?.itemView?.parent != null

    //</editor-fold desc="操作方法区">

    //<editor-fold desc="生命周期方法">

    @CallSuper
    open fun onIViewCreate() {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) {
            lifecycleRegistry = LifecycleRegistry(this)
        }
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        onLifecycleEvent(Lifecycle.Event.ON_CREATE)
        onLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @CallSuper
    open fun onIViewShow() {
        showCount++
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        onLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**再次显示*/
    @CallSuper
    open fun onIViewReShow() {
        showCount++
    }

    @CallSuper
    open fun onIViewHide() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        onLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @CallSuper
    open fun onIViewRemove() {
        showCount = 0
        iViewHolder?.clear()
        iViewHolder = null
        _parentView = null
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        onLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    /**声明周期事件回调*/
    open fun onLifecycleEvent(event: Lifecycle.Event) {
        L.v("onLifecycleEvent:$event")
        onIViewEvent?.invoke(this, event)
    }

    //</editor-fold desc="生命周期方法">

    //<editor-fold desc="内部方法">

    /**添加到界面*/
    fun addInner(parent: ViewGroup?) {
        if (parent != null) {
            if (iViewLayoutId == -1) {
                L.w("未指定[iViewLayoutId].")
            } else {
                isEnabled = true//OnBackPressedCallback
                _parentView = parent
                val rootView = LayoutInflater.from(iContext).inflate(iViewLayoutId, parent, false)
                iViewHolder = DslViewHolder(rootView)

                rootView.setTag(R.id.lib_tag_iview, this)
                parent.addView(rootView)

                onIViewCreate()
                initBackPressedDispatcher()

                iViewAddTransition(rootView) {
                    onIViewShow()
                }
            }
        }
    }

    /**直接移除界面*/
    fun removeInner(parent: ViewGroup? = _parentView, end: () -> Unit = {}) {
        if (parent == null) {
            end()
        } else {
            isEnabled = false//OnBackPressedCallback
            val rootView = iViewHolder?.itemView

            if (rootView != null) {
                iViewHolder?.clear()
                iViewHolder = null
                iViewRemoveTransition(rootView) {
                    //remove
                    rootView.setTag(R.id.lib_tag_iview, null)
                    parent.removeView(rootView)
                    onIViewHide()
                    onIViewRemove()
                    end()
                }
            } else {
                end()
            }
        }
    }

    /**[OnBackPressedDispatcherOwner]*/
    fun initBackPressedDispatcher() {
        isEnabled = true//OnBackPressedCallback

        val owner = backPressedDispatcherOwner
        if (owner != null) {
            owner.onBackPressedDispatcher.addCallback(this)
        } else {
            val activity = iActivity
            if (activity is OnBackPressedDispatcherOwner) {
                activity.onBackPressedDispatcher.addCallback(this)
            }
        }
    }

    /**处理返回按键*/
    override fun handleOnBackPressed() {
        hide {
            //no op
        }
    }

    //</editor-fold desc="内部方法">

    //<editor-fold desc="Lifecycle支持">

    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    //</editor-fold desc="Lifecycle支持">

}