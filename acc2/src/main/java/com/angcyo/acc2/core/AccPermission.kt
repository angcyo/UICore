package com.angcyo.acc2.core

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.component.appBean
import com.angcyo.library.ex.have
import com.angcyo.library.model.AppBean
import com.angcyo.library.tip
import ezy.assist.compat.SettingsCompat

/**
 * Created by angcyo on 2018/10/20 19:40
 */
object AccPermission {

    /**是否有无障碍权限和浮窗权限*/
    fun haveAllPermission(context: Context): Boolean {
        return haveAccessibilityService(context) && haveDrawOverlays(context)
    }

    /**是否有无障碍权限*/
    fun haveAccessibilityService(context: Context): Boolean {
        return try {
            isServiceEnabled(context)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**是否有浮窗权限*/
    fun haveDrawOverlays(context: Context): Boolean {
        return try {
            SettingsCompat.canDrawOverlays(context)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**打开无障碍设置页面*/
    fun openAccessibilityActivity(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            tip("打开失败\n${e.message}", context, R.drawable.lib_ic_error)
        }
    }

    /**打开悬浮窗设置页面*/
    fun openOverlaysActivity(context: Context) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            if (context is Activity) {
                context.startActivityForResult(intent, 9)
            } else {
                context.startActivity(intent)
            }
        } else*/ if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + context.packageName)
            if (context is Activity) {
                context.startActivityForResult(intent, 9)
            } else {
                context.startActivity(intent)
            }
        } else {
            SettingsCompat.manageDrawOverlays(context)
        }
    }

    /**
     * 获取 Service 是否启用状态
     *
     * @return
     */
    fun isServiceEnabled(context: Context): Boolean {
        val accessibilityManager: AccessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        //val enabledServicesSetting = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return accessibilityServices.any { it.id.startsWith(context.packageName) }
    }

    /**返回其他激活了无障碍的服务列表 */
    fun getEnabledAccessibilityServiceList(context: Context = app()): List<AccessibilityServiceInfo> {
        val accessibilityManager: AccessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return accessibilityServices
    }

    /**返回其他激活了手势的无障碍的服务列表 */
    fun getEnabledAccessibilityGesturesServiceList(context: Context = app()): List<AccessibilityServiceInfo> {
        val accessibilityServices = getEnabledAccessibilityServiceList(context)
        return accessibilityServices.filter { it.capabilities.have(AccessibilityServiceInfo.CAPABILITY_CAN_PERFORM_GESTURES) }
    }

    fun getEnabledAccessibilityGesturesAppList(
        context: Context = app(),
        filterSelf: Boolean = true
    ): List<AppBean> {
        val result = mutableListOf<AppBean>()
        val accessibilityServices = getEnabledAccessibilityServiceList(context)
        accessibilityServices.filter { it.capabilities.have(AccessibilityServiceInfo.CAPABILITY_CAN_PERFORM_GESTURES) }
            .forEach {
                val pName = it.id.split("/").getOrNull(0)
                if (filterSelf && pName == context.packageName) {
                    //filter
                } else {
                    pName?.appBean(context)?.apply {
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appName = it.loadSummary(context.packageManager)
                        }*/
                        appName = it.resolveInfo.serviceInfo.loadLabel(context.packageManager)
                        des = it.loadDescription(context.packageManager)
                        result.add(this)
                    }
                }
            }
        return result
    }
}