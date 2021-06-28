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

    companion object {
        /**点击缓存*/
        val CLICK_CACHE = mutableListOf<String>()
    }

    /**目标uri*/
    var openUri: Uri? = null

    /**mime type, 不指定会自动从uri中读取*/
    var mimeType: String? = null

    /**优先展示的包名*/
    var priorityList = mutableListOf<String>()

    init {
        dialogLayoutId = R.layout.lib_dialog_open_width
        dialogTitle = "打开方式"
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            val mimeType = mimeType ?: openUri.loadUrl()?.mimeType()
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
                queryList.apply {
                    //自然排序, 从小到大
                    sortWith { o1, o2 ->
                        val p1 = o1.activityInfo.packageName
                        val p2 = o2.activityInfo.packageName

                        //p1
                        var indexOf1 = priorityList.indexOf(p1)
                        if (indexOf1 == -1) {
                            indexOf1 = CLICK_CACHE.indexOf(p1)
                            if (indexOf1 == -1) {
                                indexOf1 = Int.MAX_VALUE
                            }
                        }

                        //p2
                        var indexOf2 = priorityList.indexOf(p2)
                        if (indexOf2 == -1) {
                            indexOf2 = CLICK_CACHE.indexOf(p2)
                            if (indexOf2 == -1) {
                                indexOf2 = Int.MAX_VALUE
                            }
                        }

                        when {
                            indexOf1 == indexOf2 -> 0
                            indexOf1 < indexOf2 -> -1
                            else -> 1
                        }
                    }
                }.forEach { resolveInfo ->
                    resolveInfo.activityInfo.apply {
                        packageName.appBean(context)?.let { appBean ->
                            DslOpenWidthItem()() {
                                itemDrawable = appBean.appIcon
                                itemText = resolveInfo.loadLabel(pm) ?: appBean.appName

                                if (L.debug) {
                                    if (priorityList.firstOrNull() == packageName) {
                                        itemText = "${itemText}[推荐]"
                                    } else if (CLICK_CACHE.firstOrNull() == packageName) {
                                        itemText = "${itemText}[最近]"
                                    }
                                }

                                itemClick = {
                                    dialog.dismiss()
                                    intent.apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        //setClassName(packageName, targetActivity)
                                        setPackage(packageName)
                                        context.startActivity(this)
                                        CLICK_CACHE.remove(packageName)
                                        CLICK_CACHE.add(0, packageName)
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