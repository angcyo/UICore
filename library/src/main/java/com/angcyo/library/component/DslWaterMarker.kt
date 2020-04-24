package com.angcyo.library.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.angcyo.library.R
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toBitmap
import com.angcyo.library.ex.undefined_res

/**
 * 图片加水印配置类. 图片控件id需要使用[R.id.lib_image_view],必须.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslWaterMarker {

    //<editor-fold desc="需要加水印的目标">

    /**直接使用Bitmap对象, 加水印. 不会调用[recycle]*/
    var targetBitmap: Bitmap? = null

    /**从图片路径中获取Bitmap对象*/
    var targetBitmapPath: String? = null

    //</editor-fold desc="需要加水印的目标">

    //<editor-fold desc="布局的方式添加水印">

    /**水印布局*/
    var waterLayoutId: Int = undefined_res

    /**布局初始化*/
    var waterLayoutInit: (rootView: View) -> Unit = {}

    //</editor-fold desc="布局的方式添加水印">

    //<editor-fold desc="输出参数配置">

    /**自动适配水印的大小*/
    var outputAdapter: Boolean = true

    /**指定输出图片的宽高, 支持wrap_content*/
    var outputWidth = -2
    var outputHeight = -2

    var outputBitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565

    //</editor-fold desc="输出参数配置">

    /**开始添加水印*/
    open fun doWaterMarker(context: Context): Bitmap {

        if (targetBitmap == null && targetBitmapPath == null) {
            throw NullPointerException("[targetBitmap] and [targetBitmapPath] is null. nothing todo.")
        }

        val bitmap = targetBitmap ?: targetBitmapPath?.toBitmap()

        bitmap?.run {

            val dslDensityAdapter = if (outputAdapter) {
                DslDensityAdapter().apply {
                    originWidth = width
                    originDensity = 1f
                    adapterWidth = _screenWidth
                    adapterDensity = dp
                }
            } else {
                null
            }

            if (waterLayoutId != undefined_res) {
                dslDensityAdapter?.adapter(context)

                //布局的方式加水印
                val rootView = LayoutInflater.from(context).inflate(waterLayoutId, null)
                rootView.findViewById<ImageView>(R.id.lib_image_view)?.setImageBitmap(this)
                waterLayoutInit(rootView)

                val width = if (outputWidth > 0) {
                    outputWidth
                } else {
                    width
                }

                val height = if (outputHeight > 0) {
                    outputHeight
                } else {
                    height
                }

                //三部曲
                rootView.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                )
                rootView.layout(0, 0, width, height)

                //创建图片
                val result = Bitmap.createBitmap(width, height, outputBitmapConfig)
                val canvas = Canvas(result)
                rootView.draw(canvas)

                //回收
                if (this != targetBitmap && !this.isRecycled) {
                    this.recycle()
                }

                dslDensityAdapter?.restore()

                //返回
                return result
            }

            dslDensityAdapter?.restore()
        }

        return bitmap!!
    }
}