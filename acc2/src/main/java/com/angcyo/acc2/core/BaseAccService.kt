package com.angcyo.acc2.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.annotation.TargetApi
import android.graphics.Path
import android.graphics.Point
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import com.angcyo.library.toastQQ


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

abstract class BaseAccService : AccessibilityService() {

    companion object {
        var isServiceConnected = false

        var lastService: BaseAccService? = null
    }

    /**手势处理*/
    var gesture: DslAccessibilityGesture = DslAccessibilityGesture().apply {
        service = this@BaseAccService
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        L.i("无障碍服务已连接.")
        //AccessibilityHelper.log("onServiceConnected")
        isServiceConnected = true
        /*serviceInfo.apply {
            //可以获取window内容, getWindows
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            //服务需要所有视图
            flags = flags or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            //服务需要视图id
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            //flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            serviceInfo = this
        }*/
        lastService = this
    }

    /**
     * 当收到应用发过来的[AccessibilityEvent]无障碍事件
     * */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        lastService = this
    }

    /**中断了反馈*/
    override fun onInterrupt() {
        //AccessibilityHelper.log("onInterrupt")
    }

    override fun onDestroy() {
        super.onDestroy()
        //AccessibilityHelper.log("onDestroy")
        L.i("无障碍服务已销毁.")
        isServiceConnected = false
        lastService = null
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        L.v("${this.simpleHash()} $event")
        return super.onKeyEvent(event)
    }

    @TargetApi(24)
    fun pressLocation(position: Point) {
        val builder = GestureDescription.Builder()
        val p = Path()
        p.moveTo(position.x.toFloat(), position.y.toFloat())
        //p.lineTo(position.x.toFloat() + 10, position.y.toFloat() + 10)
        builder.addStroke(StrokeDescription(p, 0, 100L))
        val gesture = builder.build()
        val isDispatched = dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                toastQQ("手势完成")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                toastQQ("手势被取消")
            }
        }, null)
    }

}