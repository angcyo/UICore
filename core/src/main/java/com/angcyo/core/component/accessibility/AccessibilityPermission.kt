package com.angcyo.core.component.accessibility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.angcyo.library.ex.toApplicationDetailsSettings
import ezy.assist.compat.SettingsCompat

/**
 * Created by angcyo on 2018/10/20 19:40
 */
object AccessibilityPermission {

    //无障碍弹窗提示
    var isShowAccessibilityDialog = false

    //浮窗弹窗提示
    var isShowOverlaysDialog = false

    /**是否有无障碍权限和浮窗权限*/
    fun haveAllPermission(context: Context): Boolean {
        return haveAccessibilityService(context) && haveDrawOverlays(context)
    }

    /**是否有无障碍权限*/
    fun haveAccessibilityService(context: Context): Boolean {
        return try {
            AccessibilityHelper.isServiceEnabled(context)
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
        AccessibilityHelper.openAccessibilityActivity(context)
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

    /**权限通过 返回 true*/
    fun check(context: Context): Boolean {

        //优先检查浮窗权限
        if (!SettingsCompat.canDrawOverlays(context)) {
            if (!isShowOverlaysDialog) {
                AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("权限提示")
                    .setMessage("请打开\"悬浮窗\"权限.")
                    .setOnDismissListener {
                        isShowOverlaysDialog = false
                    }
                    .setPositiveButton("去打开") { dialog, which ->

                        try {
                            SettingsCompat.manageDrawOverlays(context)
                            AccessibilityTip.tip("请打开悬浮窗权限")
                        } catch (e: Exception) {
                            //Tip.tip("没有找到对应的程序.")
                            context.toApplicationDetailsSettings()
                        }
                    }
                    .show()
                isShowOverlaysDialog = true
            }
            return false
        }

        //检查无障碍权限
        if (!AccessibilityHelper.isServiceEnabled(context)) {
            if (!isShowAccessibilityDialog) {
                AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("权限提示")
                    .setMessage("请打开\"无障碍服务\".")
                    .setOnDismissListener {
                        isShowAccessibilityDialog = false
                    }
                    .setPositiveButton("去打开") { dialog, which ->
                        AccessibilityHelper.openAccessibilityActivity(context)
                        AccessibilityTip.show()
                    }
                    .show()
                isShowAccessibilityDialog = true
            }
            return false
        }

        return true
    }
}