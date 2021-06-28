package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.angcyo.dialog.dslitem.DslOpenWidthItem
import com.angcyo.dsladapter.updateNow
import com.angcyo.library.L
import com.angcyo.library.component.appBean
import com.angcyo.library.ex.loadUrl
import com.angcyo.library.ex.mimeType
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget._rv
import com.angcyo.widget.recycler.initDslAdapter

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class OpenWithDialogConfig(context: Context? = null) : BaseTouchBackDialogConfig(context) {

    var openUri: Uri? = null

    init {
        dialogLayoutId = R.layout.lib_dialog_open_width
        dialogTitle = "打开方式"
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            val mimeType = openUri.loadUrl()?.mimeType()
            setDataAndType(openUri, mimeType)
            L.i("type:$mimeType uri:$openUri")
        }

        val context = dialogViewHolder.itemView.context
        val pm = context.packageManager
        val queryList = pm.queryIntentActivities(intent, 0)

        dialogViewHolder.visible(R.id.empty_view, queryList.isEmpty())

        dialogViewHolder._rv(R.id.lib_recycler_view)?.apply {

            //初始化DslAdapter
            initDslAdapter() {
                queryList.forEach { resolveInfo ->
                    resolveInfo.activityInfo.apply {
                        packageName.appBean(context)?.let { appBean ->
                            DslOpenWidthItem()() {
                                itemDrawable = appBean.appIcon
                                itemText = resolveInfo.loadLabel(pm) ?: appBean.appName

                                itemClick = {
                                    dialog.dismiss()
                                    intent.apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        //setClassName(packageName, targetActivity)
                                        setPackage(packageName)
                                        context.startActivity(this)
                                    }
                                }
                            }
                        }
                    }
                }
                updateNow()
            }
        }
    }

}