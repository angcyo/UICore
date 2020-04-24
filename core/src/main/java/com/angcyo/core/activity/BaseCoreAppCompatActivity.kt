package com.angcyo.core.activity

import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.core.R
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.dialog.normalDialog
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.RUtils
import com.angcyo.widget.span.span

/**
 *
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
        checkCrash()
    }

    open fun checkCrash() {
        if (!isRelease()) {
            haveLastCrash = DslCrashHandler.checkCrash(true) { filePath, message, crashTime ->
                filePath?.file()?.readText()?.copy(this)

                normalDialog {
                    canceledOnTouchOutside = false
                    dialogTitle = "发生了什么啊^_^"
                    dialogMessage = span {
                        append(crashTime) {
                            foregroundColor = _color(R.color.colorAccent)
                        }
                        appendln()
                        append(message)
                    }
                    positiveButton("粘贴给作者?") { _, _ ->
                        RUtils.chatQQ(this@BaseCoreAppCompatActivity)
                    }
                    negativeButton("加入QQ群?") { _, _ ->
                        RUtils.joinQQGroup(this@BaseCoreAppCompatActivity)
                    }
                    neutralButton("分享文件?") { _, _ ->
                        filePath?.file()?.shareFile(this@BaseCoreAppCompatActivity)
                    }
                    onDialogInitListener = { _, dialogViewHolder ->
                        dialogViewHolder.click(R.id.message_view) {
                            filePath?.file()?.open(this@BaseCoreAppCompatActivity)
                        }
                    }
                }
            }
        }
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
        toastQQ("再按一次退出!", R.drawable.lib_ic_info)
    }

    //</editor-fold desc="双击Back回调">
}