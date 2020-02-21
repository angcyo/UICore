package com.angcyo

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import com.angcyo.base.*
import com.angcyo.fragment.IFragment
import com.angcyo.fragment.R
import com.angcyo.library.L
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.undefined_res

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */
class DslFHelper(val fm: FragmentManager, val debug: Boolean = isDebug()) {

    companion object {
        var fragmentManagerLog: String = ""

        /**[androidx.fragment.app.FragmentTransaction.setCustomAnimations(int, int, int, int)]*/

        @AnimRes
        var DEFAULT_SHOW_ENTER_ANIM = R.anim.lib_translate_x_enter
        @AnimRes
        var DEFAULT_SHOW_EXIT_ANIM = 0

        @AnimRes
        var DEFAULT_REMOVE_ENTER_ANIM = 0
        @AnimRes
        var DEFAULT_REMOVE_EXIT_ANIM = R.anim.lib_translate_x_exit
    }

    //<editor-fold desc="属性">

    /**这个列表中的[Fragment]将会被执行[add]操作*/
    val showFragmentList = mutableListOf<Fragment>()

    /**这个列表中的[Fragment]将会被执行[remove]操作*/
    val removeFragmentList = mutableListOf<Fragment>()

    /**[Fragment]默认的容器id*/
    @IdRes
    var containerViewId: Int = fm.getLastFragmentContainerId(R.id.fragment_container)

    /**
     * 隐藏[hide], 操作之后最后一个[Fragment]前面第几个开始的所有[Fragment]
     * >=1 才有效.
     * */
    var hideBeforeIndex = 2

    /**最后一个[Fragment]在执行的[back]时, 是否需要[remove]*/
    var removeLastFragment: Boolean = false

    @AnimRes
    var enterAnimRes: Int = undefined_res

    @AnimRes
    var exitAnimRes: Int = undefined_res

    //</editor-fold desc="属性">

    init {
        //FragmentManager.enableDebugLogging(debug)
        if (debug) {
            //fm.registerFragmentLifecycleCallbacks(LogFragmentLifecycleCallbacks(), true)
        }
    }

    /**给Fragment配置参数*/
    fun configFragment(action: Fragment.() -> Unit) {
        showFragmentList.forEach {
            it.action()
        }
    }

    //<editor-fold desc="add 或者 show操作">

    fun show(vararg fClass: Class<out Fragment>) {
        val list = mutableListOf<Fragment>()
        for (cls in fClass) {
            instantiateFragment(cls.classLoader!!, cls.name)?.run { list.add(this) }

//            fm.fragmentFactory.instantiate(
//                cls.classLoader!!,
//                cls.name
//            )
        }
        show(list)
    }

    fun show(vararg fragment: Fragment) {
        show(fragment.toList())
    }

    /**
     * 如果显示的[Fragment]已经[add], 那么此[Fragment]上面的其他[Fragment]都将被[remove]
     * 这一点, 有点类似[Activity]的[SINGLE_TASK]启动模式
     * */
    fun show(fragmentList: List<Fragment>) {
        fragmentList.forEach {
            if (!showFragmentList.contains(it)) {
                showFragmentList.add(it)
                if (it.isAdded) {
                    remove(fm.getOverlayFragment(it))
                }
            }
        }
    }

    fun restore(vararg fClass: Class<out Fragment>) {
        show(fm.restore(*fClass))
    }

    fun restore(vararg tag: String?) {
        show(fm.restore(*tag))
    }

    fun restores(vararg fragment: Fragment) {
        show(fm.restore(*fragment))
    }

    fun <T : Fragment> restore(fragment: T, action: T.() -> Unit = {}) {
        show(fm.restore(fragment).apply {
            (this.first() as T).action()
        })
    }

    //</editor-fold desc="add 或者 show操作">

    //<editor-fold desc="remove操作">

    fun remove(vararg fClass: Class<out Fragment>) {
        for (cls in fClass) {
            remove(cls.name)
        }
    }

    fun remove(vararg tag: String?) {
        for (t in tag) {
            fm.findFragmentByTag(t)?.let { remove(it) }
        }
    }

    fun remove(fragment: Fragment?) {
        if (fragment == null) {
            return
        }
        if (!removeFragmentList.contains(fragment)) {
            removeFragmentList.add(fragment)
        }
    }

    fun removes(vararg fragment: Fragment) {
        remove(fragment.toList())
    }

    fun remove(fragmentList: List<Fragment>) {
        for (f in fragmentList) {
            remove(f)
        }
    }

    /**移除最后的指定个数的[Fragment]*/
    fun removeLast(count: Int = 1) {
        val allValidityFragment = fm.getAllValidityFragment()
        val size = allValidityFragment.size
        for (i in size - count until size) {
            remove(allValidityFragment[i])
        }
    }

    /**移除所有[getView]不为空的[Fragment]*/
    fun removeAll() {
        remove(fm.getAllValidityFragment())
    }

    /**保留前几个, 移除后面全部*/
    fun keep(before: Int) {
        val allValidityFragment = fm.getAllValidityFragment()
        for (i in before until allValidityFragment.size) {
            remove(allValidityFragment[i])
        }
    }

    fun keep(vararg tags: String?) {
        val allValidityFragment = fm.getAllValidityFragment()
        val keepList = mutableListOf<Fragment>()
        for (tag in tags) {
            fm.findFragmentByTag(tag)?.let {
                keepList.add(it)
            }
        }
        for (f in allValidityFragment) {
            if (!keepList.contains(f)) {
                remove(f)
            }
        }
    }

    fun keep(vararg fClass: Class<out Fragment>) {
        val tags = mutableListOf<String?>()
        for (cls in fClass) {
            tags.add(cls.name)
        }
        keep(*tags.toTypedArray())
    }

    //</editor-fold desc="remove操作">

    //<editor-fold desc="拦截操作">

    /**自定义的配置操作, 请勿在此执行[commit]操作*/
    var onConfigTransaction: (FragmentTransaction) -> Unit = {

    }

    /**提交操作*/
    var onCommit: (FragmentTransaction) -> Unit = {
        if (fm.isStateSaved) {
            it.commitNowAllowingStateLoss()
        } else {
            it.commitNow()
        }
    }

    //</editor-fold desc="拦截操作">

    //<editor-fold desc="其他操作">

    /**去掉默认的动画*/
    fun noAnim() {
        enterAnimRes = 0
        exitAnimRes = 0
    }

    /**
     * 回退操作, 请使用此方法.
     * 会进行回退检查
     *
     * @return 返回true, 表示当前Activity可以被关闭
     * */
    fun back(): Boolean {
        var result = true

        val allValidityFragment = fm.getAllValidityFragment()
        val lastFragment = allValidityFragment.lastOrNull()

        if (lastFragment != null) {
            if (lastFragment is IFragment) {
                if (!lastFragment.onBackPressed()) {
                    result = false
                }
            }

            if (result) {
                //可以remove
                if (showFragmentList.isEmpty() && allValidityFragment.size == 1) {
                    if (removeLastFragment) {
                        //只有一个Fragment
                        remove(lastFragment)
                        doIt()
                    }
                } else {
                    result = false
                    remove(lastFragment)
                    doIt()
                }
            }
        }

        return result
    }

    val _handle: Handler by lazy { Handler(Looper.getMainLooper()) }

    var _logRunnable: Runnable? = null

    /**执行操作*/
    fun doIt() {
        fm.beginTransaction().apply {
            if (fm.isDestroyed) {
                //no op
                L.w("fm is destroyed.")
                return
            }

            if (removeFragmentList.isEmpty() && showFragmentList.isEmpty()) {
                L.w("no op do it.")
                return
            }

            //如果需要显示的Fragment在移除列表中
            removeFragmentList.removeAll(showFragmentList)

            //一顿操作之后, 最终fm中, 应该有的Fragment列表
            val fmFragmentList = mutableListOf<Fragment>()

            val allValidityFragment = mutableListOf<Fragment>()
            val allNoViewFragment = mutableListOf<Fragment>()
            fm.fragments.forEach {
                if (it.view == null) {
                    allNoViewFragment.add(it)
                } else {
                    allValidityFragment.add(it)
                }
            }

            fmFragmentList.addAll(allValidityFragment)
            fmFragmentList.addAll(showFragmentList)
            fmFragmentList.removeAll(removeFragmentList)

            //...end

            //anim 动画需要在op之前设置, 否则不会有效果
            if (enterAnimRes == undefined_res && exitAnimRes == undefined_res) {
                if (showFragmentList.isNotEmpty() && allValidityFragment.isNotEmpty()) {
                    //显示F,并且非第一个Fragment
                    setCustomAnimations(
                        DEFAULT_SHOW_ENTER_ANIM,
                        DEFAULT_SHOW_EXIT_ANIM,
                        DEFAULT_SHOW_ENTER_ANIM,
                        DEFAULT_SHOW_EXIT_ANIM
                    )
                } else if (removeFragmentList.isNotEmpty()) {
                    //移除F
                    setCustomAnimations(
                        DEFAULT_REMOVE_ENTER_ANIM,
                        DEFAULT_REMOVE_EXIT_ANIM,
                        DEFAULT_REMOVE_ENTER_ANIM,
                        DEFAULT_REMOVE_EXIT_ANIM
                    )
                }
            } else {
                setCustomAnimations(enterAnimRes, exitAnimRes, enterAnimRes, exitAnimRes)
            }

            //op remove
            removeFragmentList.forEach {
                remove(it)
            }

            //op hide
            fmFragmentList.forEachIndexed { index, fragment ->
                if (index < fmFragmentList.size - 1) {
                    //除了顶上一个Fragment, 其他Fragment都要执行不可见生命周期回调
                    setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                }

                if (index < fmFragmentList.size - hideBeforeIndex) {
                    hide(fragment)
                } else {
                    show(fragment)
                }
            }

            //no view ,no op
//            allNoViewFragment.forEach {
//                if (it.retainInstance) {
//                    setMaxLifecycle(it, Lifecycle.State.STARTED)
//                }
//            }

            //op show
            val lastFragment = fmFragmentList.lastOrNull()
            showFragmentList.forEach { fragment ->
                when {
                    fragment.isDetached -> attach(fragment)
                    fragment.isAdded -> {
                    }
                    else -> add(
                        containerViewId,
                        fragment,
                        fragment.getFragmentTag()
                    )
                }
                if (fragment != lastFragment) {
                    setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                }
            }

            //op last
            lastFragment?.let {
                setPrimaryNavigationFragment(it)
                setMaxLifecycle(it, Lifecycle.State.RESUMED)
            }

            onConfigTransaction(this)

            onCommit(this)

            fragmentManagerLog = fm.log(false)

            if (debug) {
                _logRunnable?.run { _handle.removeCallbacks(this) }
                _logRunnable = object : Runnable {
                    override fun run() {
                        fm.log()
                        _handle.removeCallbacks(this)
                        _logRunnable = null
                    }
                }
                _handle.postDelayed(_logRunnable!!, 16)
            }
        }
    }

    //</editor-fold desc="其他操作">
}