package com.angcyo.library.component

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.collection.ArrayMap
import androidx.core.math.MathUtils.clamp
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

    var _clickMap = ArrayMap<Int, PendingIntent?>()
    var _textMap = ArrayMap<Int, CharSequence?>()
    var _imageResMap = ArrayMap<Int, Int>()
    var _imageBitmapMap = ArrayMap<Int, Bitmap?>()
    var _progressMap = ArrayMap<Int, Int>()
    var _progressIndeterminateMap = ArrayMap<Int, Boolean>()
    var _visibilityMap = ArrayMap<Int, Int>()

    var onInitRemoteView: (RemoteViews) -> Unit = {}

    /**设置按钮点击事件*/
    fun setClickPending(viewId: Int, pendingIntent: PendingIntent) {
        _clickMap[viewId] = pendingIntent
    }

    /**设置文本*/
    fun setTextViewText(viewId: Int, text: CharSequence?) {
        _textMap[viewId] = text
    }

    /**设置图片*/
    fun setImageViewResource(viewId: Int, srcId: Int) {
        _imageResMap[viewId] = srcId
    }

    fun setImageViewBitmap(viewId: Int, bitmap: Bitmap) {
        _imageBitmapMap[viewId] = bitmap
    }

    /**进度条*/
    fun setProgressBar(viewId: Int, progress: Int) {
        _progressMap[viewId] = progress
        setProgressBar(viewId, progress < 0)
    }

    fun setProgressBar(viewId: Int, indeterminate: Boolean) {
        _progressIndeterminateMap[viewId] = indeterminate
    }

    /**可见性*/
    fun setViewVisibility(viewId: Int, visibility: Int) {
        _visibilityMap[viewId] = visibility
    }

    fun doIt(context: Context): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, layoutId)

        //事件
        _clickMap.forEach { entry ->
            remoteViews.setOnClickPendingIntent(entry.key, entry.value)
        }
        _clickMap.clear()

        //文本
        _textMap.forEach { entry ->
            remoteViews.setTextViewText(entry.key, entry.value)
        }

        //图片
        _imageResMap.forEach { entry ->
            remoteViews.setImageViewResource(entry.key, entry.value)
        }
        _imageBitmapMap.forEach { entry ->
            remoteViews.setImageViewBitmap(entry.key, entry.value)
        }
        _imageBitmapMap.clear()

        //进度
        _progressMap.forEach { entry ->
            remoteViews.setProgressBar(
                entry.key,
                100,
                clamp(entry.value, 0, 100),
                _progressIndeterminateMap.getOrDefault(entry.key, false)
            )
        }

        //可见性
        _visibilityMap.forEach { entry ->
            remoteViews.setViewVisibility(entry.key, entry.value)
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