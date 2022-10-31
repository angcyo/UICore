package com.angcyo.core.activity

import android.os.Bundle
import com.angcyo.base.dslFHelper
import com.angcyo.core.fragment.PermissionFragment
import com.angcyo.http.rx.BaseObserver
import com.angcyo.library.L
import com.angcyo.library.ex.havePermission
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions

/**
 * 加入了权限配置检查, 通常用来当做启动页
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

abstract class BasePermissionsActivity : BaseCoreAppCompatActivity() {

    lateinit var rxPermissions: RxPermissions

    /**需要的权限列表*/
    val permissions = mutableListOf<PermissionBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    override fun onComplianceCheckAfter(savedInstanceState: Bundle?) {
        super.onComplianceCheckAfter(savedInstanceState)
        //合规后初始化
        onCheckPermission(savedInstanceState)
    }

    open fun onCheckPermission(savedInstanceState: Bundle?) {
        if (permissions.isEmpty()) {
            onPermissionGranted(savedInstanceState)
        } else {
            if (havePermission(permissions.mapTo(mutableListOf()) {
                    it.permission
                })) {
                onPermissionGranted(savedInstanceState)
            } else {
                showPermissionFragment(savedInstanceState)
            }
        }
    }

    /**显示权限请求界面*/
    open fun showPermissionFragment(savedInstanceState: Bundle?) {
        dslFHelper {
            restore(PermissionFragment()) {
                permissions.addAll(this@BasePermissionsActivity.permissions)

                //请求权限
                onPermissionRequest = { _, _ ->
                    requestPermissions(permissions.mapTo(mutableListOf()) {
                        it.permission
                    }) {
                        if (it) {
                            onPermissionGranted(savedInstanceState)
                        } else {
                            onPermissionDenied(savedInstanceState)
                        }
                    }
                }
            }
        }
    }


    /**请求界面*/
    open fun requestPermissions(permissions: List<String>, action: (allGranted: Boolean) -> Unit) {
        rxPermissions.requestEach(*permissions.toTypedArray())
            .subscribe(BaseObserver<Permission>().apply {
                onObserverEnd = { _, _ ->

                    var allGranted = true
                    val builder = StringBuilder()

                    builder.appendln()
                        .append(this@BasePermissionsActivity.javaClass.simpleName)
                        .appendln(" 权限状态-->")

                    observerDataList.forEachIndexed { index, permission ->
                        builder.append(index).append("->").append(permission.name)
                            .appendln(if (permission.granted) " √" else " ×")

                        if (!permission.granted) {
                            allGranted = false
                        }
                    }

                    L.w(builder.toString())

                    action(allGranted)
                }
            })
    }

    /**权限通过*/
    open fun onPermissionGranted(savedInstanceState: Bundle?) {
        dslFHelper {
            finishActivityOnLastFragmentRemove = true
            removeAll()
        }
    }

    /**权限拒绝*/
    open fun onPermissionDenied(savedInstanceState: Bundle?) {

    }
}