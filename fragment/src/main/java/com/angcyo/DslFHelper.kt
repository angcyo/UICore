package com.angcyo

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.angcyo.base.getLastFragmentContainerId
import com.angcyo.base.log
import com.angcyo.base.restore
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.fragment.R
import com.angcyo.library.ex.isDebug

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */
class DslFHelper(val fm: FragmentManager) {

    /**这个列表中的[Fragment]将会被执行[add]操作*/
    val showFragmentList = mutableListOf<AbsLifecycleFragment>()

    /**这个列表中的[Fragment]将会被执行[remove]操作*/
    val removeFragmentList = mutableListOf<Fragment>()

    /**[Fragment]默认的容器id*/
    @IdRes
    var containerViewId: Int = fm.getLastFragmentContainerId(R.id.fragment_container)

    init {
        FragmentManager.enableDebugLogging(isDebug())
    }

    //<editor-fold desc="add 或者 show操作">

    fun show(vararg fClass: Class<out AbsLifecycleFragment>) {
        val list = mutableListOf<AbsLifecycleFragment>()
        for (cls in fClass) {
            list.add(
                fm.fragmentFactory.instantiate(
                    cls.classLoader!!,
                    cls.name
                ) as AbsLifecycleFragment
            )
        }
        show(list)
    }

    fun show(vararg fragment: AbsLifecycleFragment) {
        show(fragment.toList())
    }

    fun show(fragmentList: List<AbsLifecycleFragment>) {
        fragmentList.forEach {
            if (!showFragmentList.contains(it)) {
                showFragmentList.add(it)
            }
        }
    }

    fun restore(vararg tag: String?) {
        show(fm.restore(*tag))
    }

    fun restore(vararg fragment: AbsLifecycleFragment) {
        show(fm.restore(*fragment))
    }

    //</editor-fold desc="add 或者 show操作">

    //<editor-fold desc="remove操作">

    fun remove(vararg tag: String?) {
        for (t in tag) {
            fm.findFragmentByTag(t)?.let { remove(it) }
        }
    }

    fun remove(vararg fragment: Fragment) {
        remove(fragment.toList())
    }

    fun remove(fragmentList: List<Fragment>) {
        for (f in fragmentList) {
            if (!removeFragmentList.contains(f)) {
                removeFragmentList.add(f)
            }
        }
    }

    fun removeAll() {
        remove(fm.fragments)
    }

    //</editor-fold desc="remove操作">

    /**执行操作*/
    fun doIt() {
        fm.beginTransaction().apply {
            if (fm.isDestroyed) {
                //no op
                return
            }

            removeFragmentList.forEach {
                remove(it)
            }

            showFragmentList.forEachIndexed { index, absLifecycleFragment ->
                when {
                    absLifecycleFragment.isDetached -> attach(absLifecycleFragment)
                    absLifecycleFragment.isAdded -> {
                    }
                    else -> add(
                        containerViewId,
                        absLifecycleFragment,
                        absLifecycleFragment.getFragmentTag()
                    )
                }
                if (showFragmentList.lastIndex == index) {
                    setMaxLifecycle(absLifecycleFragment, Lifecycle.State.RESUMED)
                } else {
                    setMaxLifecycle(absLifecycleFragment, Lifecycle.State.STARTED)
                }
            }

            if (isDebug()) {
                runOnCommit {
                    fm.log()
                }
            }

            if (fm.isStateSaved) {
                commitNowAllowingStateLoss()
            } else {
                commitNow()
            }
        }

    }
}