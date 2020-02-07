package com.angcyo.viewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/31
 */


class VMAProperty<VM : ViewModel>(val vmCls: Class<VM>) : ReadOnlyProperty<Fragment, VM> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): VM {
        return thisRef.ofa().get(vmCls)
    }
}

class VMProperty<VM : ViewModel>(val vmCls: Class<VM>) : ReadOnlyProperty<ViewModelStoreOwner, VM> {
    override fun getValue(thisRef: ViewModelStoreOwner, property: KProperty<*>): VM {
        return thisRef.of().get(vmCls)
    }
}