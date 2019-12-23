package com.angcyo.base

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.angcyo.DslFHelper
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.fragment.IFragment
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */


/**
 * 优先根据[TAG]恢复已经存在[Fragment]
 * */
fun FragmentManager.restore(vararg fragment: Fragment): List<Fragment> {
    val list = mutableListOf<Fragment>()

    for (f in fragment) {
        list.add(findFragmentByTag(f.getFragmentTag()) ?: f)
    }

    return list
}

fun FragmentManager.restore(vararg tag: String?): List<AbsLifecycleFragment> {
    val list = mutableListOf<AbsLifecycleFragment>()

    for (t in tag) {
        (findFragmentByTag(t) as? AbsLifecycleFragment)?.let {
            list.add(it)
        }
    }

    return list
}

/**获取所有view!=null的[Fragment]*/
fun FragmentManager.getAllValidityFragment(): List<Fragment> {
    val result = mutableListOf<Fragment>()

    fragments.forEach {
        if (it.view != null) {
            result.add(it)
        }
    }

    return result
}

/**获取[Fragment]上层的所有[Fragment]*/
fun FragmentManager.getOverlayFragment(anchor: Fragment): List<Fragment> {
    val result = mutableListOf<Fragment>()

    var findAnchor = false
    fragments.forEach {
        if (findAnchor) {
            result.add(it)
        } else if (it == anchor) {
            findAnchor = true
        }
    }

    return result
}

@IdRes
fun FragmentManager.getLastFragmentContainerId(@IdRes defaultId: Int): Int {
    return getAllValidityFragment().lastOrNull()?.getFragmentContainerId() ?: defaultId
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

fun FragmentActivity.dslFHelper(config: DslFHelper.() -> Unit) {
    supportFragmentManager.dslFHelper(config)
}

fun FragmentManager.dslFHelper(config: DslFHelper.() -> Unit) {
    DslFHelper(this).apply {
        this.config()
        doIt()
    }
}

fun Fragment.getFragmentTag(): String? {
    return if (this is IFragment) this.getFragmentTag() else this.javaClass.name
}

fun FragmentManager.log() {
    val builder = StringBuilder()

    fragments.forEachIndexed { index, fragment ->
        builder.appendln()
        builder.append(index)
        builder.append("->")
        fragment.log(builder)
    }

    if (fragments.isEmpty()) {
        builder.append("no fragment to log.")
    }

    L.w(builder.toString())
}

fun Int.toVisibilityString(): String {
    return when (this) {
        View.INVISIBLE -> "INVISIBLE"
        View.GONE -> "GONE"
        else -> "VISIBLE"
    }
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