package com.angcyo.acc2.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import java.lang.ref.WeakReference

/**
 * 无障碍服务分发.
 *
 * https://developer.android.google.cn/guide/topics/ui/accessibility/service
 *
 * 1: 声明无障碍服务. (为了与 Android 4.1 及更高版本兼容，) (https://developer.android.google.cn/guide/topics/ui/accessibility/service#manifest)
 *
 * ```
 * <application>
 *   <service android:name=".MyAccessibilityService"
 *       android:enabled="true"
 *       android:exported="true"
 *       android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
 *       android:label="@string/accessibility_service_label">
 *     <intent-filter>
 *       <action android:name="android.accessibilityservice.AccessibilityService" />
 *     </intent-filter>
 *   </service>
 * </application>
 * ```
 *
 * 2: 配置无障碍服务 (https://developer.android.google.cn/guide/topics/ui/accessibility/service#service-config)
 *
 * ```
 * <service android:name=".MyAccessibilityService">
 *   ...
 *   <meta-data
 *     android:name="android.accessibilityservice"
 *     android:resource="@xml/lib_accessibility_service_config" />
 *   </service>
 * ```
 *
 * //api 24 android 7 后无障碍服务可以执行手势操作
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

abstract class BaseAccessibilityService : AccessibilityService() {

    companion object {
        var isServiceConnected = false

        var weakService: WeakReference<BaseAccessibilityService>? = null

        val lastService: BaseAccessibilityService? get() = weakService?.get()
    }

    /**手势处理*/
    var gesture: DslAccessibilityGesture = DslAccessibilityGesture().apply {
        service = this@BaseAccessibilityService
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        //AccessibilityHelper.log("onServiceConnected")
        isServiceConnected = true
        serviceInfo.apply {
            //可以获取window内容, getWindows
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            //服务需要所有视图
            flags = flags or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            //服务需要视图id
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            serviceInfo = this
        }
        weakService = WeakReference(this)
    }

    /**
     * 当收到应用发过来的[AccessibilityEvent]无障碍事件
     * */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //no op
    }

    /**中断了反馈*/
    override fun onInterrupt() {
        //AccessibilityHelper.log("onInterrupt")
    }

    override fun onDestroy() {
        super.onDestroy()
        //AccessibilityHelper.log("onDestroy")
        isServiceConnected = false
        weakService?.clear()
        weakService = null
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        L.v("${this.simpleHash()} $event")
        return super.onKeyEvent(event)
    }

}