package com.angcyo.library.component

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.collection.ArrayMap
import com.angcyo.library.app
import com.angcyo.library.ex.undefined_res

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

class DslRemoteView {

    /**必须使用[@RemoteView]标注的控件, 否则就会崩溃*/
    @LayoutRes
    var layoutId: Int = undefined_res

    var clickMap = ArrayMap<Int, PendingIntent>()

    var onInitRemoteView: (RemoteViews) -> Unit = {}

    fun clickPending(viewId: Int, pendingIntent: PendingIntent) {
        clickMap[viewId] = pendingIntent
    }

    fun doIt(context: Context): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, layoutId)

        clickMap.forEach { entry ->
            remoteViews.setOnClickPendingIntent(entry.key, entry.value)
        }

        onInitRemoteView(remoteViews)

        return remoteViews
    }
}

fun dslRemoteView(context: Context = app(), action: DslRemoteView.() -> Unit): RemoteViews {
    val dslRemoteView = DslRemoteView()
    dslRemoteView.action()
    return dslRemoteView.doIt(context)
}