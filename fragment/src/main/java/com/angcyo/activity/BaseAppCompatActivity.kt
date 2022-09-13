package com.angcyo.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Bundle
import android.os.Process
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.angcyo.DslAHelper
import com.angcyo.base.*
import com.angcyo.dslTargetIntentHandle
import com.angcyo.fragment.AbsFragment
import com.angcyo.fragment.R
import com.angcyo.library.L
import com.angcyo.library.Screen
import com.angcyo.library.component._delay
import com.angcyo.library.ex.Anim
import com.angcyo.library.ex.isShowDebug
import com.angcyo.library.ex.simpleHash
import com.angcyo.library.utils.resultString
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

    /**布局*/
    var activityLayoutId = R.layout.lib_activity_main_layout

    //<editor-fold desc="基础方法处理">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logActivityInfo()

        baseDslViewHolder = DslViewHolder(window.decorView)
        onCreateAfter(savedInstanceState)
        intent?.let { onHandleIntent(it) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { onHandleIntent(it, true) }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        onShowDebugInfoView(hasFocus)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        /*val config = resources.configuration
        val appConfig = app().resources.configuration
        L.w("config↓\nact:$config\napp:$appConfig")*/
        Screen.init(this)
    }

    override fun onPostResume() {
        super.onPostResume()
        Screen.init(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val oldConfig = resources.configuration
        super.onConfigurationChanged(newConfig)
        L.w("onConfigurationChanged↓\nold:$oldConfig\nnew:$newConfig")
        if (isShowDebug()) {
            onShowDebugInfoView()
        }
        Screen.init(this)
    }

    open fun onShowDebugInfoView(show: Boolean = true) {
        showDebugInfoView(show)
    }

    /**布局设置之后触发*/
    open fun onCreateAfter(savedInstanceState: Bundle?) {
        enableLayoutFullScreen()
        with(activityLayoutId) {
            if (this > 0) {
                setContentView(this)
            }
        }
    }

    /**
     * @param fromNewIntent [onNewIntent]
     * */
    open fun onHandleIntent(intent: Intent, fromNewIntent: Boolean = false) {
        handleTargetIntent(intent, fromNewIntent)

        //Intent转发
        supportFragmentManager.getAllValidityFragment().lastOrNull()?.let {
            if (it is AbsFragment) {
                it.onHandleActivityIntent(intent, fromNewIntent)
            }
        }

        if (L.debug) {
//            val am: ActivityManager =
//                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//
//            val appTasks = am.appTasks
//            val runningTasks = am.getRunningTasks(Int.MAX_VALUE)

            L.i(
                "${this.simpleHash()} new:$fromNewIntent $intent pid:${Process.myPid()} uid:${Process.myUid()} call:${
                    packageManager.getNameForUid(
                        Binder.getCallingUid()
                    )
                }"
            )
        }
    }

    /**检查是否有目标[Intent]需要启动
     * [com.angcyo.activity.BaseAppCompatActivity.onHandleIntent]
     * */
    open fun handleTargetIntent(intent: Intent, fromNewIntent: Boolean = false) {
        if (fromNewIntent) {
            dslTargetIntentHandle(intent)
        } else {
            _delay(Anim.ANIM_DURATION) {
                dslTargetIntentHandle(intent)
            }
        }
    }

    //</editor-fold desc="基础方法处理">

    //<editor-fold desc="事件拦截">

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
        //L.d("${ev.actionIndex} ${ev.action.actionToString()} ${ev.rawX},${ev.rawY} ${ev.eventTime}")
        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        L.d("onKeyDown:$keyCode ${KeyEvent.keyCodeToString(keyCode)}")
        return super.onKeyDown(keyCode, event)
    }

    //</editor-fold desc="事件拦截">

    //<editor-fold desc="默认回调">

    /**回退检查*/
    override fun onBackPressed() {
        if (checkBackPressedDispatcher()) {
            dslFHelper {
                if (back()) {
                    onBackPressedInner()
                }
            }
        }
    }

    /**整整的关闭界面*/
    open fun onBackPressedInner() {
        if (DslAHelper.isMainActivity(this)) {
            super.onBackPressed()
        } else {
            dslAHelper {
                //finishToActivity
                finish()
            }
        }
    }

    /**系统默认的[onActivityResult]触发, 需要在[Fragment]里面调用特用方法启动[Activity]才会触发*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        L.d(
            this.simpleHash(),
            " requestCode:$requestCode",
            " resultCode:${resultCode.resultString()}",
            " data$data"
        )
        supportFragmentManager.getAllValidityFragment().lastOrNull()?.run {
            onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * [android.content.pm.PackageManager.PERMISSION_GRANTED]
     * [android.content.pm.PackageManager.PERMISSION_DENIED]
     *
     * [androidx.activity.ComponentActivity.getActivityResultRegistry]
     * */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //</editor-fold desc="默认回调">

    //<editor-fold desc="高级扩展">

    /**快速观察[LiveData]*/
    fun <T> LiveData<T>.observe(
        autoClear: Boolean = false,
        action: (data: T?) -> Unit
    ): Observer<T> {
        val result: Observer<T>
        observe(this@BaseAppCompatActivity, Observer<T> {
            action(it)
            if (it != null && autoClear && this is MutableLiveData) {
                postValue(null)
            }
        }.apply {
            result = this
        })
        return result
    }

    /**快速观察[LiveData]一次, 确保不收到null数据*/
    fun <T> LiveData<T>.observeOnce(action: (data: T?) -> Unit): Observer<T> {
        var result: Observer<T>? = null
        observe(this@BaseAppCompatActivity, Observer<T> {
            if (it is List<*>) {
                if (it.isNotEmpty()) {
                    removeObserver(result!!)
                }
            } else if (it != null) {
                removeObserver(result!!)
            }
            action(it)
        }.apply {
            result = this
        })
        return result!!
    }

    //</editor-fold>
}