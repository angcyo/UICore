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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
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
    var iViewAddTransition: (rootView: View, end: () -> Unit) -> Unit = { _, end -> end() }

    /**移除[IView]时的过渡动画*/
    var iViewRemoveTransition: (rootView: View, end: () -> Unit) -> Unit = { _, end -> end() }

    /**[IView]显示的次数*/
    var showCount = 0

    //IView所在的容器
    var _parentView: ViewGroup? = null

    var iViewHolder: DslViewHolder? = null

    //----

    val _iVh: DslViewHolder get() = iViewHolder!!

    val viewHolder: DslViewHolder? get() = iViewHolder

    val iContext: Context get() = _parentView?.context!!

    val iActivity: Activity? get() = if (iContext is Activity) iContext as Activity else null

    //<editor-fold desc="操作方法区">

    /**
     * 将[IView]显示到[parent]中, 如果[parent]为空, 会将[IView]从原来的[parent]中移除
     * */
    @CallPoint
    fun show(parent: ViewGroup?) {
        if (_parentView != parent) {
            removeInner(_parentView) {
                addInner(parent)
            }
        } else {
            onIViewShow()
        }
    }

    /**
     * 将[IView]从到[_parentView]中移除
     * */
    @CallPoint
    fun hide(end: (() -> Unit)? = null) {
        removeInner(_parentView) {
            isEnabled = false//OnBackPressedCallback
            end?.invoke()
        }
    }

    /**是否附加到界面*/
    fun isAttach() = iViewHolder != null && iViewHolder?.itemView?.parent != null

    //</editor-fold desc="操作方法区">

    //<editor-fold desc="生命周期方法">

    open fun onIViewCreate() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @CallSuper
    open fun onIViewShow() {
        showCount++
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    @Deprecated("此方法触发时机待定")
    open fun onIViewHide() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @CallSuper
    open fun onIViewRemove() {
        showCount = 0
        iViewHolder?.clear()
        iViewHolder = null
        _parentView = null
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    //</editor-fold desc="生命周期方法">

    //<editor-fold desc="内部方法">

    fun addInner(parent: ViewGroup?) {
        if (parent != null) {
            if (iViewLayoutId == -1) {
                L.w("未指定[iViewLayoutId].")
            } else {
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

    fun removeInner(parent: ViewGroup?, end: () -> Unit) {
        if (parent == null) {
            end()
        } else {
            val rootView = iViewHolder?.itemView

            if (rootView != null) {
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
        val activity = iActivity
        if (activity is OnBackPressedDispatcherOwner) {
            isEnabled = true//OnBackPressedCallback
            activity.onBackPressedDispatcher.addCallback(this)
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

    val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    //</editor-fold desc="Lifecycle支持">

}