package com.angcyo.dialog.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */

open class DialogActivity : BaseAppCompatActivity() {
    companion object {
        const val KEY_DIALOG_CONFIG = "key_dialog_config"

        /**获取Intent*/
        fun getDialogIntent(dialogConfig: DslDialogConfig?): Intent {
            return Intent(app(), DialogActivity::class.java).apply {
                putExtra(KEY_DIALOG_CONFIG, dialogConfig)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onHandleIntent(intent: Intent, fromNew: Boolean) {
        super.onHandleIntent(intent, fromNew)
        val dslDialogConfig: DslDialogConfig? =
            intent.getSerializableExtra(KEY_DIALOG_CONFIG) as? DslDialogConfig

        L.i(dslDialogConfig)

        dslDialogConfig?.apply {
            configWindow(window)
            setContentView(dialogLayoutId)
            configWindowAfter(window)

            val viewHolder = DslViewHolder(window.decorView)
            val dialog = DelegateDialog()
            initDialogView(dialog, viewHolder)
            onDialogInitListener(dialog, viewHolder)
        }
    }

    override fun onShowDebugInfoView(show: Boolean) {
        //super.onShowDebugInfoView(show)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    inner class DelegateDialog : Dialog(this) {
        override fun cancel() {
            super.cancel()
            finish()
        }

        override fun dismiss() {
            super.dismiss()
            finish()
        }
    }
}