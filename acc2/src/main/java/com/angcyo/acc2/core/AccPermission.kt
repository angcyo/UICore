package com.angcyo.acc2.core

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.angcyo.library.R
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
            tip("打开失败\n${e.message}", R.drawable.lib_ic_error)
        }
    }

    /**打开悬浮窗设置页面*/
    fun openOverlaysActivity(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + context.packageName)
            context.startActivity(intent)
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
}