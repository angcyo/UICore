package com.angcyo.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.angcyo.base.dslFHelper
import com.angcyo.base.enableLayoutFullScreen
import com.angcyo.fragment.R
import com.angcyo.library.ex.isDebug
import com.angcyo.widget.DslViewHolder

/**
 * [Activity] 基类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseAppCompatActivity : AppCompatActivity() {

    lateinit var baseDslViewHolder: DslViewHolder

    /**别名*/
    val _vh: DslViewHolder
        get() = baseDslViewHolder

    open fun getActivityLayoutId() = R.layout.lib_activity_main_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseDslViewHolder = DslViewHolder(window.decorView)
        onCreateAfter(savedInstanceState)
        intent?.let { onHandleIntent(it) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { onHandleIntent(it, true) }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        onShowDebugInfoView(hasFocus)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isDebug()) {
            baseDslViewHolder.postDelay(300) {
                onShowDebugInfoView()
            }
        }
    }

    open fun onShowDebugInfoView(show: Boolean = true) {
        showDebugInfoView(show)
    }

    /**布局设置之后触发*/
    open fun onCreateAfter(savedInstanceState: Bundle?) {
        enableLayoutFullScreen()
        with(getActivityLayoutId()) {
            if (this > 0) {
                setContentView(this)
            }
        }
    }

    /**
     * @param fromNew [onNewIntent]
     * */
    open fun onHandleIntent(intent: Intent, fromNew: Boolean = false) {

    }

    /**回退检查*/
    override fun onBackPressed() {
        if (onBackPressedDispatcher()) {
            dslFHelper {
                if (back()) {
                    super.onBackPressed()
                }
            }
        }
    }

    /**返回true, 表示可以关闭界面*/
    fun onBackPressedDispatcher(): Boolean {
        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            onBackPressedDispatcher.onBackPressed()
            return false
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    /**拦截界面所有Touch事件*/
    var interceptTouchEvent: Boolean = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            interceptTouchEvent = false
        }
        if (interceptTouchEvent) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}