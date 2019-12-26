package com.angcyo.core.activity

import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.activity.havePermission
import com.angcyo.base.dslFHelper
import com.angcyo.core.fragment.PermissionFragment
import com.angcyo.http.rx.BaseObserver
import com.angcyo.library.L
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions

/**
 * 基础权限配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

abstract class BasePermissionsActivity : BaseAppCompatActivity() {

    lateinit var rxPermissions: RxPermissions

    //需要的权限列表
    val permissions = mutableListOf<PermissionBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (permissions.isEmpty()) {
            onPermissionGranted()
        } else {
            if (havePermission(permissions.mapTo(mutableListOf()) {
                    it.permission
                })) {
                onPermissionGranted()
            } else {
                showPermissionFragment()
            }
        }
    }

    /**显示权限请求界面*/
    open fun showPermissionFragment() {
        dslFHelper {
            restore(PermissionFragment()) {
                permissions.addAll(this@BasePermissionsActivity.permissions)

                //请求权限
                onPermissionRequest = {
                    requestPermissions(permissions.mapTo(mutableListOf()) {
                        it.permission
                    }) {
                        if (it) {
                            this@BasePermissionsActivity.dslFHelper {
                                removeAll()
                            }
                            onPermissionGranted()
                        } else {
                            onPermissionDenied()
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
    open fun onPermissionGranted() {

    }

    /**权限拒绝*/
    open fun onPermissionDenied() {

    }
}

data class PermissionBean(
    //需要申请的权限
    val permission: String, //Manifest.permission.WRITE_EXTERNAL_STORAGE
    //权限提示的图标
    val icon: Int = -1,
    //权限描述文本
    val des: String? = null
)
