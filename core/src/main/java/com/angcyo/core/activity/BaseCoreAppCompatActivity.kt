package com.angcyo.core.activity

import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.core.R
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.utils.RUtils
import com.angcyo.dialog.normalDialog
import com.angcyo.library.ex.*
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/02
 */

abstract class BaseCoreAppCompatActivity : BaseAppCompatActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        checkCrash()
    }

    open fun checkCrash() {
        if (!isRelease()) {
            DslCrashHandler.checkCrash(true) { filePath, message, crashTime ->
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
                    onInitListener = { _, dialogViewHolder ->
                        dialogViewHolder.click(R.id.message_view) {
                            filePath?.file()?.open(this@BaseCoreAppCompatActivity)
                        }
                    }
                }
            }
        }
    }
}