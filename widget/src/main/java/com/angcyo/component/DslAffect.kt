package com.angcyo.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.collection.ArrayMap
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.angcyo.component.DslAffect.Companion.AFFECT_EMPTY
import com.angcyo.component.DslAffect.Companion.AFFECT_ERROR
import com.angcyo.component.DslAffect.Companion.AFFECT_LOADING
import com.angcyo.component.DslAffect.Companion.AFFECT_OTHER
import com.angcyo.component.DslAffect.Companion.CONTENT_AFFECT_INVISIBLE
import com.angcyo.widget.R
import java.lang.ref.WeakReference

/**
 * 情感图切换
 *
 * DslAffect.addAffect()
 *
 * DslAffect.install()
 *
 * DslAffect.showAffect()
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/10
 */

class DslAffect {

    companion object {
        /** 情感图 内容, 此变量用来切换显示内容视图. 请勿用来注册 */
        const val AFFECT_CONTENT = 0

        /** 情感图 加载中 */
        const val AFFECT_LOADING = 1

        /** 情感图 异常 */
        const val AFFECT_ERROR = 2

        /** 情感图 其他 */
        const val AFFECT_OTHER = 3

        /** 空数据 */
        const val AFFECT_EMPTY = 4

        /** 当切换到非内容情感时, 内容布局的处理方式, 默认[CONTENT_AFFECT_INVISIBLE] */
        const val CONTENT_AFFECT_NONE = 0
        const val CONTENT_AFFECT_INVISIBLE = 1
        const val CONTENT_AFFECT_GONE = 2
        const val CONTENT_AFFECT_REMOVE = 3
    }

    var _parent: ViewGroup? = null

    /**内容视图*/
    var contentViewList = mutableListOf<View>()

    /**状态和布局的对应关系*/
    var affectMap = ArrayMap<Int, Int>()

    /**状态视图缓存*/
    var _affectCache = ArrayMap<Int, WeakReference<View>>()

    /**是否使用缓存*/
    var enableAffectCache: Boolean = true

    /**当前状态*/
    var affectStatus = AFFECT_CONTENT

    /**内容视图处理方式*/
    var contentAffect = CONTENT_AFFECT_INVISIBLE

    /**状态改变之前回调, 返回true表示拦截处理*/
    var onAffectChangeBefore: (dslAffect: DslAffect, from: Int, to: Int, data: Any?) -> Boolean =
        { dslAffect, from, to, data ->
            false
        }

    /**情感图状态改变, 如果是切换到[AFFECT_CONTENT], 那么[fromView] [toView] 会是[parent]*/
    var onAffectChange: (dslAffect: DslAffect, from: Int, to: Int, fromView: View?, toView: View, data: Any?) -> Unit =
        { dslAffect, from, to, fromView, toView, data ->

        }

    /**配置状态布局*/
    fun addAffect(affectState: Int, @LayoutRes id: Int) {
        affectMap[affectState] = id
    }

    /**安装到[ViewGroup]中*/
    fun install(viewGroup: View?) {
        if (viewGroup is ViewGroup) {
            _parent = viewGroup
            if (contentViewList.isEmpty()) {
                _delay {
                    initContentView()
                }
            }
        } else {
            throw IllegalArgumentException("需要使用[ViewGroup]安装")
        }
    }

    fun _delay(action: () -> Unit) {
        _parent?.run {
            if (ViewCompat.isLaidOut(this)) {
                action()
            } else {
                this.doOnPreDraw {
                    action()
                }
            }
        }
    }

    /**初始化那些child是内容布局*/
    fun initContentView() {
        _parent?.run {
            contentViewList.clear()
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.getTag(R.id.tag_affect) == null) {
                    contentViewList.add(child)
                }
            }
        }
    }

    /**显示指定情感状态*/
    fun showAffect(affectState: Int, data: Any? = null) {

        if (_parent == null) {
            throw IllegalArgumentException("请先调用[install]方法")
        }

        if (affectState != AFFECT_CONTENT && !affectMap.containsKey(affectState)) {
            throw IllegalArgumentException("请先调用[addAffect]方法")
        }

        _delay {
            if (affectStatus != affectState) {
                val old = affectStatus

                if (!onAffectChangeBefore(this, old, affectState, data)) {
                    //未拦截
                    affectStatus = affectState

                    //处理内容视图
                    _handleContentView(affectState == AFFECT_CONTENT)

                    //处理其他状态
                    _handleOtherStatus(affectState)

                    val fromView: View? =
                        if (affectStatus == AFFECT_CONTENT) _parent else _affectCache[affectState]?.get()

                    val toView: View
                    if (affectState == AFFECT_CONTENT) {
                        //显示到内容
                        toView = _parent!!
                    } else {
                        var rootView: View? = null
                        if (enableAffectCache) {
                            rootView = _affectCache[affectState]?.get()
                        }

                        if (rootView == null) {
                            rootView = LayoutInflater.from(_parent!!.context)
                                .inflate(affectMap[affectState]!!, _parent, false)
                        }

                        if (rootView == null) {
                            throw NullPointerException()
                        }

                        rootView.setTag(R.id.tag_affect, affectMap[affectState])

                        _affectCache[affectState] = WeakReference(rootView)

                        _parent?.addView(rootView)

                        toView = rootView

                    }

                    onAffectChange(this, affectStatus, affectState, fromView, toView, data)
                }
            } else {
                val fromView: View? =
                    if (affectStatus == AFFECT_CONTENT) _parent else _affectCache[affectState]?.get()

                onAffectChange(this, affectState, affectState, fromView, fromView!!, data)
            }
        }
    }

    //单独处理内容布局
    fun _handleContentView(show: Boolean) {
        contentViewList.forEachIndexed { index, view ->
            when (contentAffect) {
                CONTENT_AFFECT_NONE -> {
                    //no op
                }
                CONTENT_AFFECT_GONE -> view.visibility = if (show) View.VISIBLE else View.GONE
                CONTENT_AFFECT_REMOVE -> {
                    if (show) _parent?.addView(view, index) else _parent?.removeView(view)
                }
                CONTENT_AFFECT_INVISIBLE -> view.visibility =
                    if (show) View.VISIBLE else View.INVISIBLE
                else -> {
                    //no op
                }
            }
        }
    }

    //其他状态视图
    fun _handleOtherStatus(affectState: Int) {
        val removeLayoutId = mutableListOf<Int>()
        for (entry in affectMap) {
            if (entry.key != affectState) {
                removeLayoutId.add(entry.value)
            }
        }

        _parent?.apply {
            for (i in childCount - 1 downTo 0) {
                val child = getChildAt(i)
                val layoutId = child.getTag(R.id.tag_affect)
                if (layoutId != null && removeLayoutId.contains(layoutId)) {
                    removeView(child)
                }
            }
        }
    }
}

/**快速获取情感图切换组件*/
fun dslAffect(viewGroup: View? = null, action: DslAffect.() -> Unit): DslAffect {
    return DslAffect().apply {
        //注册布局
        addAffect(AFFECT_LOADING, R.layout.lib_loading_layout)
        addAffect(AFFECT_EMPTY, R.layout.lib_empty_layout)
        addAffect(AFFECT_ERROR, R.layout.lib_error_layout)
        addAffect(AFFECT_OTHER, R.layout.lib_error_layout)
        //安装
        viewGroup?.run { install(this) }
        //内容处理方式
        contentAffect = CONTENT_AFFECT_INVISIBLE
        action()
    }
}