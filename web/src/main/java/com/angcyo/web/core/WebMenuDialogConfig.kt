package com.angcyo.web.core

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.ITouchBackDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.gone
import com.angcyo.web.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.appendDslItem

/**
 * 网页菜单对话框配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class WebMenuDialogConfig : BaseDialogConfig(), ITouchBackDialogConfig {

    /**网页host*/
    var webHost: CharSequence? = null

    /**描述内容*/
    var webDes: CharSequence? = null

    /**第一行 菜单*/
    var line1Items = listOf<DslAdapterItem>()

    /**第二行 菜单*/
    var line2Items = listOf<DslAdapterItem>()

    init {
        dialogLayoutId = R.layout.dialog_web_menu
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
        initTouchBackLayout(dialog, dialogViewHolder)

        dialogViewHolder.tv(R.id.web_title_view)?.apply {
            text = "网页由 $webHost 提供"
            gone(webHost.isNullOrEmpty())
        }

        dialogViewHolder.tv(R.id.web_des_view)?.apply {
            text = webDes
            gone(webDes.isNullOrEmpty())
        }

        //第一行item
        if (line1Items.isEmpty()) {
            dialogViewHolder.gone(R.id.line1_wrap_layout)
        } else {
            dialogViewHolder.group(R.id.line1_layout)?.appendDslItem(line1Items)
        }

        //第二行item
        if (line2Items.isEmpty()) {
            dialogViewHolder.gone(R.id.line2_wrap_layout)
        } else {
            dialogViewHolder.group(R.id.line2_layout)?.appendDslItem(line1Items)
        }

        //取消
        dialogViewHolder.click(R.id.lib_cancel_view) {
            dialog.cancel()
        }
    }
}

fun Context.webMenuDialog(config: WebMenuDialogConfig.() -> Unit): Dialog {
    return WebMenuDialogConfig().run {
        configBottomDialog(this@webMenuDialog)
        canceledOnTouchOutside = true
        cancelable = true
        config()
        show()
    }
}