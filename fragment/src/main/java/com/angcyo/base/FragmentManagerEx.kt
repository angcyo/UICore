package com.angcyo.base

import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.angcyo.DslFHelper
import com.angcyo.base.FragmentManagerHelper.factory
import com.angcyo.fragment.R
import com.angcyo.library.L
import com.angcyo.library.utils.setFieldValue
import kotlin.reflect.KClass

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */

object FragmentManagerHelper {

    var factory: FragmentFactory = FragmentFactory()

    /**判断当前的[fragment]是否在[containerViewId]相同的容器内*/
    fun isInContainer(fragment: Fragment, containerViewId: Int?): Boolean =
        if (containerViewId == null) {
            true
        } else {
            fragment.getFragmentContainerId() == containerViewId
        }
}

/**实例化*/
fun Class<out Fragment>.instantiate(): Fragment? {
    return instantiateFragment(classLoader!!, name)
}

/**实例化一个[Fragment]对象*/
fun instantiateFragment(classLoader: ClassLoader, className: String): Fragment? {
    return try {
        factory.instantiate(classLoader, className)
    } catch (e: Exception) {
        L.e("创建异常: $className")
        e.printStackTrace()
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

/**根据[tag], 恢复或者创建[Fragment], 并且设置[tag]*/
fun FragmentManager.restore(fClass: Class<out Fragment>, tag: String?): Fragment {
    val targetTag = tag ?: fClass.name
    val fragment = findFragmentByTag(targetTag)
    return (fragment ?: instantiateFragment(fClass.classLoader!!, fClass.name)!!).apply {
        setFieldValue(Fragment::class.java, "mTag", targetTag)
    }
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

/**是否有指定类型的[Fragment]*/
fun FragmentManager.have(cls: Class<*>): Boolean {
    return fragments.find { it.javaClass == cls } != null || have(cls.name)
}

/**查找是否有指定Tag的[Fragment]*/
fun FragmentManager.have(tag: String?): Boolean {
    var result = false
    findFragmentByTag(tag)?.let {
        result = true
    }
    return result
}

/**获取相同容器内的所有[Fragment]*/
fun FragmentManager.getAllContainerValidityFragment(fragment: Fragment): List<Fragment> =
    getAllContainerValidityFragment(fragment.getFragmentContainerId())

/**获取相同容器内的所有[Fragment]*/
fun FragmentManager.getAllContainerValidityFragment(containerViewId: Int?): List<Fragment> =
    fragments.filter { FragmentManagerHelper.isInContainer(it, containerViewId) }

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
    supportFragmentManager.dslFHelper(this, config)
}

/**[FragmentManager]*/
fun FragmentManager.dslFHelper(context: Context? = null, config: DslFHelper.() -> Unit) {
    DslFHelper(this, context).apply {
        this.config()
        doIt()
    }
}

/**打印[fragments]*/
fun FragmentManager.log(debug: Boolean = true): String {
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

    return builder.toString().apply {
        if (debug) {
            L.w(this)
        }
    }
}

fun Int.toVisibilityString(): String {
    return when (this) {
        View.INVISIBLE -> "INVISIBLE"
        View.GONE -> "GONE"
        else -> "VISIBLE"
    }
}

//region ---find---

/**查找[RootView]对应的[Fragment]*/
fun FragmentManager.findFragmentByView(view: View): Fragment? {
    return fragments.find { it.view == view }
}

fun <T : Fragment> FragmentManager.find(cls: Class<T>): T? {
    return fragments.find { it.javaClass == cls } as? T
}

fun <T : Fragment> FragmentManager.find(cls: KClass<T>): T? {
    return fragments.find { it.javaClass == cls.java } as? T
}

fun <T : Fragment> DslFHelper.find(cls: Class<T>): T? {
    return fm.find(cls)
}

fun <T : Fragment> DslFHelper.find(cls: KClass<T>): T? {
    return fm.find(cls)
}

/**
 * 查找锚点处, 前一个有效的 Fragment
 * 如果锚点为null, 那么查找最后一个有效的Fragment
 */
fun FragmentManager.findBeforeFragment(anchor: Fragment? = null): Fragment? {
    var isFindAnchor = anchor == null
    var fragment: Fragment? = null
    for (i in fragments.indices.reversed()) {
        val f = fragments[i]
        if (isFindAnchor) {
            if (f.isAdded && f.view != null) {
                fragment = f
                break
            }
        } else {
            isFindAnchor = anchor === f
        }
    }
    return fragment
}

//endregion ---find---

/**判断当前的[Fragment]是否在详情容器中*/
fun Fragment.isInDetailContainer() = getFragmentContainerId() == R.id.fragment_detail_container