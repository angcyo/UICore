package com.angcyo.core.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.base.enableHighRefresh
import com.angcyo.core.R
import com.angcyo.core.checkShowCrashDialog
import com.angcyo.core.component.ComplianceCheck
import com.angcyo.core.component.StateModel
import com.angcyo.core.component.model.DataShareModel
import com.angcyo.core.component.model.LanguageModel
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.vmApp
import com.angcyo.library.annotation.CallComplianceAfter
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ

/**
 * back提示
 * 加入了合规检查, 崩溃检查的[Activity], 通常用来当做主页[Activity]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/02
 */

abstract class BaseCoreAppCompatActivity : BaseAppCompatActivity() {

    companion object {
        /**上一次是否发生过崩溃*/
        var haveLastCrash = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateAfter(savedInstanceState: Bundle?) {
        super.onCreateAfter(savedInstanceState)

        if (LibHawkKeys.enableHighRefresh) {
            window.enableHighRefresh()
        }

        //Compliance 合规后的初始化
        vmApp<StateModel>().waitState(
            ComplianceCheck.TYPE_COMPLIANCE_STATE,
            false //一直监听会内存泄漏
        ) { data, throwable ->
            if (throwable == null) {
                //合规后
                onComplianceCheckAfter(savedInstanceState)
            }
        }

        onComplianceCheck(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    /**开始合规检查*/
    open fun onComplianceCheck(savedInstanceState: Bundle?) {
        ComplianceCheck.check {
            //同意合规
            ComplianceCheck.agree()
        }
    }

    /**合规后的初始化*/
    @CallComplianceAfter
    open fun onComplianceCheckAfter(savedInstanceState: Bundle?) {
        checkCrash()
    }

    /**检查是否有崩溃, 合规之后
     * [com.angcyo.core.activity.BaseCoreAppCompatActivity.onComplianceCheckAfter]*/
    @CallComplianceAfter
    open fun checkCrash() {
        if (isDebug()) {
            showCrashDialog()
        }
    }

    /**显示崩溃日志对话框*/
    open fun showCrashDialog() {
        checkShowCrashDialog()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        vmApp<NightModel>().onConfigurationChanged(this, newConfig)
        vmApp<LanguageModel>().onConfigurationChanged(this, newConfig)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        vmApp<DataShareModel>().activityDispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    //<editor-fold desc="双击Back回调">

    /**双击返回时间间隔, 负数不开启*/
    var doubleBackTime = -1

    var _backPressedTime = 0L

    override fun onBackPressedInner() {
        if (doubleBackTime > 0) {
            val nowTime = nowTime()
            _backPressedTime = if (nowTime - _backPressedTime <= doubleBackTime) {
                super.onBackPressedInner()
                0
            } else {
                onDoubleBackPressed()
                nowTime
            }
        } else {
            super.onBackPressedInner()
        }
    }

    fun onDoubleBackPressed() {
        toastQQ("再按一次退出!", this, R.drawable.lib_ic_info)
    }

    //</editor-fold desc="双击Back回调">
}