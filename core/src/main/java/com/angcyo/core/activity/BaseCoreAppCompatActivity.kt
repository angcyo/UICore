package com.angcyo.core.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.core.R
import com.angcyo.core.component.ComplianceCheck
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.StateModel
import com.angcyo.core.component.model.LanguageModel
import com.angcyo.core.vmApp
import com.angcyo.dialog.normalDialog
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ
import com.angcyo.widget.span.span

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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

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

    /**开始合规检查*/
    open fun onComplianceCheck(savedInstanceState: Bundle?) {
        ComplianceCheck.agree()
    }

    /**合规后的初始化*/
    open fun onComplianceCheckAfter(savedInstanceState: Bundle?) {
        checkCrash()
    }

    /**检查是否有崩溃*/
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
        vmApp<LanguageModel>().onConfigurationChanged(this, newConfig)
    }

    //<editor-fold desc="双击Back回调">

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

/**崩溃对话框, 带复制/查看/分享功能*/
fun Context.checkShowCrashDialog() {
    BaseCoreAppCompatActivity.haveLastCrash =
        DslCrashHandler.checkCrash(true) { filePath, message, crashTime ->
            val file = filePath?.file()
            file?.readText()?.copy(this)

            normalDialog {
                canceledOnTouchOutside = false
                dialogTitle = "发生了什么^_^"
                dialogMessage = span {
                    append(crashTime) {
                        foregroundColor = _color(R.color.colorAccent)
                    }
                    appendln()
                    append(message)
                }
                /*positiveButton("粘贴给开发?") { _, _ ->
                    RUtils.chatQQ(this@checkShowCrashDialog)
                }
                negativeButton("加入QQ群?") { _, _ ->
                    RUtils.joinQQGroup(this@checkShowCrashDialog)
                }
                neutralButton("分享文件?") { _, _ ->
                    file?.shareFile(this@checkShowCrashDialog)
                }*/
                negativeButton("无视") { dialog, _ ->
                    dialog.dismiss()
                }
                positiveButton("分享") { _, _ ->
                    file?.shareFile(this@checkShowCrashDialog)
                }
                onDialogInitListener = { _, dialogViewHolder ->
                    dialogViewHolder.click(R.id.dialog_message_view) {
                        file?.open(this@checkShowCrashDialog)
                    }
                }
            }
        }
}