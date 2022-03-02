package com.angcyo.library.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2017/10/11 10:53
 */

/**获取声明在[application]标签下的[meta-data]数据*/
fun Context.getAppMetaData(key: String?): String? {
    var value: String? = null
    try {
        val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        value = info.metaData.getString(key)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return value
}

/**获取声明在[activity]标签下的[meta-data]数据*/
fun Context.getActivityMetaData(activity: Class<*>, key: String?): String? {
    return getActivityMetaData(activity.name, key)
}

fun Context.getActivityMetaData(activityClass: String, key: String?): String? {
    var value: String? = null
    try {
        val componentName = ComponentName(packageName, activityClass)
        val info = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)
        value = info.metaData.getString(key)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return value
}

/**获取声明在[service]标签下的[meta-data]数据*/
fun Context.getServiceMetaData(service: Class<*>, key: String?): String? {
    return getServiceMetaData(service.name, key)
}

fun Context.getServiceMetaData(serviceClass: String, key: String?): String? {
    var value: String? = null
    try {
        val componentName = ComponentName(packageName, serviceClass)
        val info = packageManager.getServiceInfo(componentName, PackageManager.GET_META_DATA)
        value = info.metaData.getString(key)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return value
}

/**获取声明在[receiver]标签下的[meta-data]数据*/
fun Context.getReceiverMetaData(receiver: Class<*>, key: String?): String? {
    return getReceiverMetaData(receiver.name, key)
}

fun Context.getReceiverMetaData(receiverClass: String, key: String?): String? {
    var value: String? = null
    try {
        val componentName = ComponentName(packageName, receiverClass)
        val info = packageManager.getReceiverInfo(componentName, PackageManager.GET_META_DATA)
        value = info.metaData.getString(key)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return value
}
