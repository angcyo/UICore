package com.angcyo.activity

import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import com.angcyo.base.fullscreen
import com.angcyo.base.translucentNavigationBar
import com.angcyo.base.translucentStatusBar
import com.angcyo.library.getAppIcon
import com.angcyo.library.getAppName


/**
 * 锁屏通知基类, 一些锁屏相关配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

abstract class BaseLockNotifyActivity : BaseAppCompatActivity() {

    /**锁管理, 用于解锁*/
    lateinit var keyguardManager: KeyguardManager

    /**应用程序图标*/
    val appLogo: Drawable? get() = getAppIcon()

    /**应用程序名称*/
    val appName: CharSequence? get() = getAppName()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        initLockSetting()
    }

    override fun onCreateAfter(savedInstanceState: Bundle?) {
        super.onCreateAfter(savedInstanceState)
    }

    /**锁屏需要的的基础初始化*/
    open fun initLockSetting() {
        translucentStatusBar(true)
        translucentNavigationBar(true)

        fullscreen()

        val window = window
//        win.addFlags(
//            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
//                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
//                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕长亮
//                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON//打开屏幕
//        )

        //解锁
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }

        //亮屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        //使用手机的背景
        val wallPaper = WallpaperManager.getInstance(this).drawable
        window.setBackgroundDrawable(wallPaper)
    }

    /**解锁*/
    fun disableKeyguard() {
        try {
            keyguardManager.newKeyguardLock("").disableKeyguard()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**判断是否锁屏了
 * 4.1之后该类的API新增了一个isKeyguardLocked()的方法判断是否锁屏，
 * 但在4.1之前，我们只能用inKeyguardRestrictedInputMode()方法，如果为true，即为锁屏状态。
 * */
fun Context.isKeyguardLocked(): Boolean {

    if ((getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked) {
        return true
    }

    //如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
    val isScreenOn = (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive

    return !isScreenOn
}



