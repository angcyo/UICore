package com.angcyo.core.activity

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/31
 */
data class PermissionBean(
    //需要申请的权限
    val permission: String, //Manifest.permission.WRITE_EXTERNAL_STORAGE
    //权限提示的图标
    val icon: Int = -1,
    //权限描述文本
    val des: String? = null
)
