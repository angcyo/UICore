package com.angcyo.viewmodel

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */

//<editor-fold desc="返回ViewModelProvider">

fun ViewModelStoreOwner.of(factory: ViewModelProvider.Factory? = null): ViewModelProvider =
    ViewModelProvider(this, factory ?: ViewModelProvider.NewInstanceFactory())

//自定义[ViewModelStore]

/**[activity] [factory] 参数二选一*/
fun ViewModelStore.of(
    activity: Activity? = null,
    factory: ViewModelProvider.Factory? = null
): ViewModelProvider {
    return ViewModelProvider(
        this,
        factory ?: ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)
    )
}

/**单独获取[Fragment]对应的[ViewModelProvider]*/
fun Fragment.of(factory: ViewModelProvider.Factory? = null): ViewModelProvider =
    ViewModelProvider(this, factory ?: ViewModelProvider.NewInstanceFactory())

/**获取[Fragment]所在[FragmentActivity]中包含的[ViewModelProvider]*/
fun Fragment.ofa(factory: ViewModelProvider.Factory? = null): ViewModelProvider =
    ViewModelProvider(this.requireActivity(), factory ?: ViewModelProvider.NewInstanceFactory())

/**单独获取[FragmentActivity]对应的[ViewModelProvider]*/
fun FragmentActivity.of(factory: ViewModelProvider.Factory? = null): ViewModelProvider =
    ViewModelProvider(this, factory ?: ViewModelProvider.NewInstanceFactory())

//</editor-fold desc="返回ViewModelProvider">

//<editor-fold desc="返回ViewModel">

/**获取[ViewModelStore]中的[ViewModel], 默认的[key]是[DEFAULT_KEY + ":" + modelClass.getCanonicalName()]*/
inline fun <reified VM : ViewModel> Fragment.vm(factory: ViewModelProvider.Factory? = null) =
    of(factory).get(VM::class.java)

inline fun <reified VM : ViewModel> Fragment.vma(factory: ViewModelProvider.Factory? = null) =
    ofa(factory).get(VM::class.java)

inline fun <reified VM : ViewModel> FragmentActivity.vm(factory: ViewModelProvider.Factory? = null) =
    of(factory).get(VM::class.java)

/**自定义[ViewModelStore]获取[ViewModel]*/
inline fun <reified VM : ViewModel> ViewModelStore.vm(
    activity: Activity? = null,
    factory: ViewModelProvider.Factory? = null
) = of(activity, factory).get(VM::class.java)

inline fun <reified VM : ViewModel> ViewModelProvider.vm() = get(VM::class.java)

//</editor-fold desc="返回ViewModel">