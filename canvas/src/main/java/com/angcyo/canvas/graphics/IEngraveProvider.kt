package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.RectF
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.annotation.Pixel

/**
 * 雕刻数据提供者
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/17
 */
interface IEngraveProvider {

    /**数据旋转的角度*/
    val _rotate: Float
        get() = getEngraveDataItem()?.dataBean?.angle ?: 0f

    /**获取[DataItem]的渲染器, 如果有*/
    fun getEngraveRenderer(): IItemRenderer<*>? = null

    /**[com.angcyo.canvas.data.CanvasProjectItemBean]解析到内存中的数据*/
    fun getEngraveDataItem(): DataItem? = null

    /**获取用于直接雕刻的图片, 返回的图片应该已经进行了缩放和旋转 */
    fun getEngraveBitmap(): Bitmap?

    /**数据在创作上的绘制范围, 不包含旋转*/
    @Pixel
    fun getEngraveBounds(): RectF

    /**[getEngraveBounds]的旋转矩形*/
    @Pixel
    fun getEngraveRotateBounds(): RectF
}