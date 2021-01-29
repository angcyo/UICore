package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.logFileName
import com.angcyo.core.component.file.wrapData
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.component.appBean
import com.angcyo.library.model.AppBean
import com.angcyo.library.tip
import com.angcyo.library.utils.FileUtils

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AccessibilityHelper {

    //临时变量
    val tempRect = Rect()

    var logFolderName = "accessibility"

    /**指定log文件的文件名, 不指定则按当天日期存储*/
    var logFileName: String? = null
        get() = field ?: logFileName()

    /**log文件全路径*/
    val logFilePath: String
        get() = FileUtils.appRootExternalFolderFile(
            app(),
            logFolderName,
            logFileName!!
        )?.absolutePath!!

    /**写入日志*/
    fun log(data: String) {
        DslFileHelper.write(logFolderName, logFileName!!, data.wrapData())
    }

    /**打开辅助工具界面*/
    fun openAccessibilityActivity(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            tip("打开失败\n${e.message}", R.drawable.lib_ic_error)
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
        return accessibilityServices.any { it.id.startsWith(context.packageName) }
    }

    /**获取无障碍应用列表信息*/
    fun accessibilityServicesAppList(context: Context, enableList: Boolean = false): List<AppBean> {
        val result = mutableListOf<AppBean>()

        val accessibilityManager: AccessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val accessibilityServices =
            if (enableList) accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            ) else accessibilityManager.installedAccessibilityServiceList

        accessibilityServices.forEach {
            it.id.split("/").getOrNull(0)?.appBean(context)?.apply {
                des = it.loadDescription(context.packageManager)
                result.add(this)
            }
        }
        return result
    }
}