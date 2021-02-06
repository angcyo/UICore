package com.angcyo.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.LruCache
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

/**
 * [RecyclerView] 截图辅助类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/06
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DslRecyclerViewShot {

    /**背景颜色*/
    var bgColor: Int = Color.WHITE

    /**从什么位置开始截图*/
    var fromIndex = 0

    /**截取多少个Item*/
    var itemCount = Int.MAX_VALUE

    /**当前[index]的item上面需要偏移的距离*/
    var offsetHeightAction: ((index: Int) -> Int)? = null

    /**绘制回调*/
    var drawAction: ((index: Int, canvas: Canvas) -> Unit)? = null

    fun doIt(recyclerView: RecyclerView): Bitmap? {
        val adapter = recyclerView.adapter
        if (adapter == null || adapter.itemCount <= 0) {
            return null
        }

        val endIndex = min(fromIndex + itemCount, adapter.itemCount)
        var height = 0
        val paint = Paint()
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        val bitmapCache = LruCache<String, Bitmap>(cacheSize)

        for (i in fromIndex until endIndex) {
            //offset
            height += offsetHeightAction?.invoke(i) ?: 0
            //获取item的图片
            val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i, listOf<Any>())
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            holder.itemView.layout(
                0, 0,
                holder.itemView.measuredWidth,
                holder.itemView.measuredHeight
            )
            holder.itemView.isDrawingCacheEnabled = true
            holder.itemView.buildDrawingCache()
            val drawingCache = holder.itemView.drawingCache
            if (drawingCache != null) {
                bitmapCache.put(i.toString(), drawingCache)
            }
            //累加高度
            height += holder.itemView.measuredHeight
        }

        //开始返回
        val resultBitmap =
            Bitmap.createBitmap(recyclerView.measuredWidth, height, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(resultBitmap)

        //绘制背景
        resultCanvas.drawColor(bgColor)
        val lBackground = recyclerView.background
        if (lBackground is ColorDrawable) {
            val lColor = lBackground.color
            resultCanvas.drawColor(lColor)
        }
        var drawHeight = 0
        for (i in fromIndex until endIndex) {
            drawHeight += offsetHeightAction?.invoke(i) ?: 0
            val bitmap = bitmapCache[i.toString()]
            resultCanvas.drawBitmap(bitmap, 0f, drawHeight.toFloat(), paint)
            drawHeight += bitmap.height
            bitmap.recycle()
            drawAction?.invoke(i, resultCanvas)
        }
        return resultBitmap
    }
}

/**[RecyclerView]截图*/
fun RecyclerView.shot(action: DslRecyclerViewShot.() -> Unit): Bitmap? {
    return DslRecyclerViewShot().run {
        action()
        doIt(this@shot)
    }
}