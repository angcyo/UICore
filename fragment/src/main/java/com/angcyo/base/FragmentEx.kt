package com.angcyo.base

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.DslAHelper
import com.angcyo.DslFHelper
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.fragment.AbsFragment
import com.angcyo.fragment.IFragment
import com.angcyo.library.L
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor
import com.angcyo.widget.base.hideSoftInput

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**当需要操作[Activity]时*/
fun Fragment.withActivity(config: Activity.() -> Unit) {
    activity?.run { config() } ?: (context as? Activity)?.run { config() }
}

/**[Fragment]中*/
fun Fragment.dslFHelper(config: DslFHelper.() -> Unit) {
    withActivity {
        if (this is FragmentActivity) {
            this.dslFHelper {
                this.config()
            }
        }
    }
}

/**开始[Window]转场动画, 请调用[transition]*/
fun Fragment.dslAHelper(action: DslAHelper.() -> Unit) {
    context?.run {
        DslAHelper(this).apply {
            this.action()
            doIt()
        }
    }
}

/**[Fragment]中的[childFragmentManager]*/
fun Fragment.dslChildFHelper(config: DslFHelper.() -> Unit) {
    childFragmentManager.dslFHelper(context) {
        removeOverlayFragmentOnShow = false
        config()
    }
}

fun Fragment.getFragmentTag(): String {
    return if (this is IFragment) this.getFragmentTag() else this.javaClass.name
}

fun Fragment.log(builder: StringBuilder = StringBuilder()): StringBuilder {
    builder.append(Integer.toHexString(getFragmentContainerId()).toUpperCase())
    builder.append(" isAdd:")
    builder.append(if (isAdded) "√" else "×")
    builder.append(" isDetach:")
    builder.append(if (isDetached) "√" else "×")
    builder.append(" isHidden:")
    builder.append(if (isHidden) "√" else "×")
    builder.append(" isVisible:")
    builder.append(if (isVisible) "√" else "×")
    builder.append(" isResumed:")
    builder.append(if (isResumed) "√" else "×")
    builder.append(" userVisibleHint:")
    builder.append(if (userVisibleHint) "√" else "×")

    val view = view
    if (view != null) {
        builder.append(" visible:")

        builder.append(view.visibility.toVisibilityString())

        if (view.parent == null) {
            builder.append(" parent:×")
        } else {
            builder.append(" parent:√")
        }
    } else {
        builder.append(" view:×")
    }
    if (this is IFragment) {
        //builder.append(" 可视:")
        //builder.append(if (!(fragment as IFragment).isFragmentHide()) "√" else "×")
        builder.append(" TAG:").append(getFragmentTag())
    }
    if (view != null) {
        builder.append(" view:")
        builder.append(view)
    }
    return builder
}

/**
 * 通过反射, 获取Fragment所在视图的Id
 */
fun Fragment.getFragmentContainerId(): Int {
    var viewId = -1
    val fragmentView = view
    if (fragmentView == null) {
    } else if (fragmentView.parent is View) {
        viewId = (fragmentView.parent as View).id
    }
    if (viewId == View.NO_ID) {
        try {
            val field =
                Fragment::class.java.getDeclaredField("mContainerId")
            field.isAccessible = true
            viewId = field[this] as Int
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
    return viewId
}

/**返回当前的[Fragment]*/
fun Fragment.back() {
    activity?.onBackPressed() ?: L.w("activity is null.")
}

fun Fragment.delayMillis(or: Long): Long {
    return if (parentFragmentManager.getAllValidityFragment().isNotEmpty())
        app().resources.getInteger(R.integer.lib_animation_delay).toLong() else or
}

/**智能延迟处理, 如果在单独的Activity中, 则不延迟.*/
fun Fragment.delay(delayMillis: Long = delayMillis(-1), action: () -> Unit) {
    if (delayMillis >= 0) {
        val runnable = Runnable(action)
        MainExecutor.handler.postDelayed(runnable, delayMillis)
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    MainExecutor.handler.removeCallbacks(runnable)
                }
            }
        })
    } else {
        action()
    }
}

/**隐藏软键盘*/
fun Fragment.hideSoftInput() {
    view?.hideSoftInput()
}

fun Fragment.removeThis(init: DslFHelper.() -> Unit = {}) {
    dslFHelper {
        remove(this@removeThis)
        init()
    }
}

/**初始化*/
fun <T : DslAdapterItem> T.init(
    fragment: AbsFragment,
    payloads: List<Any> = emptyList(),
    dsl: T.() -> Unit
) {
    dsl()
    itemBind.invoke(fragment._vh, RecyclerView.NO_POSITION, this, payloads)
}