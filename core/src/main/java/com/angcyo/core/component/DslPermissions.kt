package com.angcyo.core.component

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.angcyo.http.rx.BaseObserver
import com.angcyo.library.L
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class DslPermissions {

    var fragment: Fragment? = null
    var fragmentActivity: FragmentActivity? = null
    val permissions = mutableListOf<String>()

    var onPermissionsResult: (allGranted: Boolean) -> Unit = {}

    fun addPermissions(vararg permissions: String) {
        this.permissions.addAll(permissions)
    }

    fun doIt() {
        if (fragment == null && fragmentActivity == null) {
            L.w("fragment or fragmentActivity is null.")
            return
        }

        val rxPermissions =
            fragment?.run { RxPermissions(this) } ?: fragmentActivity?.run { RxPermissions(this) }

        rxPermissions?.apply {
            requestEach(*permissions.toTypedArray())
                .subscribe(BaseObserver<Permission>().apply {
                    onObserverEnd = { _, _ ->

                        var allGranted = true
                        val builder = StringBuilder()

                        builder.appendln().appendln("权限状态-->")

                        observerDataList.forEachIndexed { index, permission ->
                            builder.append(index).append("->").append(permission.name)
                                .appendln(if (permission.granted) " √" else " ×")

                            if (!permission.granted) {
                                allGranted = false
                            }
                        }

                        L.w(builder.toString())

                        onPermissionsResult(allGranted)
                    }
                })
        }
    }
}

/**[Manifest.permission.WRITE_EXTERNAL_STORAGE]*/
fun dslPermissions(
    fragmentActivity: FragmentActivity?,
    permission: String,
    onResult: (allGranted: Boolean) -> Unit
) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragmentActivity = fragmentActivity
    if (!permission.isBlank()) {
        dslPermissions.addPermissions(permission)
    }
    dslPermissions.onPermissionsResult = onResult
    dslPermissions.doIt()
}

fun dslPermissions(action: DslPermissions.() -> Unit = {}) {
    val dslPermissions = DslPermissions()
    dslPermissions.action()
    dslPermissions.doIt()
}

fun Fragment.dslPermissions(permission: String, onResult: (allGranted: Boolean) -> Unit) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragment = this
    if (!permission.isBlank()) {
        dslPermissions.addPermissions(permission)
    }
    dslPermissions.onPermissionsResult = onResult
    dslPermissions.doIt()
}

fun Fragment.dslPermissions(action: DslPermissions.() -> Unit = {}) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragment = this
    dslPermissions.action()
    dslPermissions.doIt()
}