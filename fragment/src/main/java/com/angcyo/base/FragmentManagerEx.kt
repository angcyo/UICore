package com.angcyo.base

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.angcyo.DslFHelper
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */

/**实例化一个[Fragment]对象*/
fun instantiateFragment(classLoader: ClassLoader, className: String): Fragment? {
    return try {
        val cls = FragmentFactory.loadFragmentClass(classLoader, className)
        cls.getConstructor().newInstance()
    } catch (e: Exception) {
        null
    }
}

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

fun FragmentManager.restore(vararg fClass: Class<out Fragment>): List<Fragment> {
    val list = mutableListOf<Fragment>()

    for (cls in fClass) {
        val fragment = findFragmentByTag(cls.name)

        if (fragment == null) {
            instantiateFragment(cls.classLoader!!, cls.name)?.run { list.add(this) }
        } else {
            list.add(fragment)
        }
    }

    return list
}

fun FragmentManager.restore(vararg tag: String?): List<Fragment> {
    val list = mutableListOf<Fragment>()

    for (t in tag) {
        findFragmentByTag(t)?.let {
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

/**获取所有view!=null并且生命周期是[Lifecycle.State.RESUMED]的[Fragment]*/
fun FragmentManager.getAllResumedFragment(): List<Fragment> {
    val result = mutableListOf<Fragment>()

    fragments.forEach {
        if (it.view != null && it.isResumed) {
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

/**[FragmentActivity]中*/
fun FragmentActivity.dslFHelper(config: DslFHelper.() -> Unit) {
    supportFragmentManager.dslFHelper(config)
}

/**[FragmentManager]*/
fun FragmentManager.dslFHelper(config: DslFHelper.() -> Unit) {
    DslFHelper(this).apply {
        this.config()
        doIt()
    }
}

/**打印[fragments]*/
fun FragmentManager.log() {
    val builder = StringBuilder()

    fragments.forEachIndexed { index, fragment ->
        builder.appendln()
        builder.append(index)
        builder.append("->")
        fragment.log(builder)
        fragment.parentFragment?.run {
            builder.appendln().append("  └──(parent) ")
            this.log(builder)
        }
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
