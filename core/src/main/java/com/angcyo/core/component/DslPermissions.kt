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

    var onPermissionsResult: (allGranted: Boolean, foreverDenied: Boolean) -> Unit = { _, _ -> }

    fun addPermissions(vararg permissions: String) {
        this.permissions.addAll(permissions)
    }

    fun addPermissions(permissions: List<String>) {
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

                        //所有权限是否给予
                        var allGranted = true
                        //是否永久禁用了权限
                        var foreverDenied = false
                        val builder = StringBuilder()

                        builder.appendln().appendln("权限状态-->")

                        observerDataList.forEachIndexed { index, permission ->
                            builder.append(index).append("->").append(permission.name)
                                .append(" ${permission.shouldShowRequestPermissionRationale}") //是否需要重新请求, 如果是false则表示用户永远禁止了权限
                                .appendln(if (permission.granted) " √" else " ×")

                            if (!permission.granted) {
                                allGranted = false
                            }
                            if (!permission.shouldShowRequestPermissionRationale) {
                                foreverDenied = true
                            }
                        }

                        L.w(builder.toString())

                        onPermissionsResult(allGranted, foreverDenied)
                    }
                })
        }
    }
}

//region ---dslPermissions---

/**
 * [fragment]
 * [fragmentActivity]
 * */
fun dslPermissions(action: DslPermissions.() -> Unit = {}) {
    val dslPermissions = DslPermissions()
    dslPermissions.action()
    dslPermissions.doIt()
}

fun Fragment.dslPermissions(action: DslPermissions.() -> Unit = {}) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragment = this
    dslPermissions.action()
    dslPermissions.doIt()
}

//endregion

//region ---permission---

fun Fragment.dslPermission(
    permission: String,
    onResult: (allGranted: Boolean, foreverDenied: Boolean) -> Unit
) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragment = this
    if (permission.isNotBlank()) {
        dslPermissions.addPermissions(permission)
    }
    dslPermissions.onPermissionsResult = onResult
    dslPermissions.doIt()
}

/**
 * [permission] 权限,比如:[Manifest.permission.WRITE_EXTERNAL_STORAGE]*/
fun dslPermission(
    fragmentActivity: FragmentActivity?,
    permission: String,
    onResult: (allGranted: Boolean, foreverDenied: Boolean) -> Unit
) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragmentActivity = fragmentActivity
    if (!permission.isBlank()) {
        dslPermissions.addPermissions(permission)
    }
    dslPermissions.onPermissionsResult = onResult
    dslPermissions.doIt()
}

//endregion

//region ---permissionList---

fun Fragment.dslPermissions(
    permissionList: List<String>,
    onResult: (allGranted: Boolean, foreverDenied: Boolean) -> Unit
) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragment = this
    dslPermissions.addPermissions(permissionList)
    dslPermissions.onPermissionsResult = onResult
    dslPermissions.doIt()
}

fun FragmentActivity.dslPermissions(
    permissionList: List<String>,
    onResult: (allGranted: Boolean, foreverDenied: Boolean) -> Unit
) {
    val dslPermissions = DslPermissions()
    dslPermissions.fragmentActivity = this
    dslPermissions.addPermissions(permissionList)
    dslPermissions.onPermissionsResult = onResult
    dslPermissions.doIt()
}

//endregion
