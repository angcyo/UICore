package com.angcyo.canvas.data

import android.graphics.RectF
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.graphics.PathGraphicsParser
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.items.data.DataTextItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.getAllDependRendererList
import com.angcyo.drawable.dslGravity
import com.angcyo.laserpacker.bean.LPElementBean
import com.angcyo.laserpacker.isBold
import com.angcyo.laserpacker.isItalic
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.add
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.have
import com.angcyo.library.ex.toBitmapOfBase64
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.toMm
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

//---

/**使用原始图片, 更新bean内的宽高*/
fun LPElementBean.updateWidthHeightByOriginImage() {
    val originBitmap = imageOriginal?.toBitmapOfBase64()
    originBitmap?.let {
        width = it.width.toMm()
        height = it.height.toMm()
    }
}

/**文本演示*/
fun LPElementBean.textStyle(): Int {
    var result = DataTextItem.TEXT_STYLE_NONE
    if (isBold()) {
        result = result.add(DataTextItem.TEXT_STYLE_BOLD)
    }
    if (isItalic()) {
        result = result.add(DataTextItem.TEXT_STYLE_ITALIC)
    }
    if (underline) {
        result = result.add(DataTextItem.TEXT_STYLE_UNDER_LINE)
    }
    if (linethrough) {
        result = result.add(DataTextItem.TEXT_STYLE_DELETE_LINE)
    }
    return result
}

/**设置文本样式*/
fun LPElementBean.setTextStyle(style: Int) {
    fontWeight = if (style.have(DataTextItem.TEXT_STYLE_BOLD)) "bold" else null
    fontStyle = if (style.have(DataTextItem.TEXT_STYLE_ITALIC)) "italic" else null
    underline = style.have(DataTextItem.TEXT_STYLE_UNDER_LINE)
    linethrough = style.have(DataTextItem.TEXT_STYLE_DELETE_LINE)
}

/**[generateName]*/
fun LPElementBean.generateNameByRenderer(list: List<BaseItemRenderer<*>>) {
    val beanList = mutableListOf<LPElementBean>()
    list.getAllDependRendererList().forEach {
        if (it is DataItemRenderer) {
            it.dataItem?.dataBean?.let { beanList.add(it) }
        }
    }
    generateName(beanList)
}

/**设置渲染的位置
 * [bounds] 返回值*/
fun LPElementBean.updateToRenderBounds(@Pixel bounds: RectF): RectF {
    val valueUnit = IValueUnit.MM_UNIT
    val l = valueUnit.convertValueToPixel(left)
    val t = valueUnit.convertValueToPixel(top)
    var w = valueUnit.convertValueToPixel(_width)
    var h = valueUnit.convertValueToPixel(_height)

    //限制大小
    w = max(PathGraphicsParser.MIN_PATH_SIZE, w)
    h = max(PathGraphicsParser.MIN_PATH_SIZE, h)

    val sx = _scaleX
    val sy = _scaleY
    bounds.set(l, t, l + w * sx, t + h * sy)
    return bounds
}

/**更新缩放比例
 * [w] 界面上显示的大小, 像素
 * [h] 界面上显示的大小, 像素*/
fun LPElementBean.updateScale(@Pixel w: Float, @Pixel h: Float) {
    val valueUnit = IValueUnit.MM_UNIT
    if (w != 0f && _width != 0f) {
        scaleX = (valueUnit.convertPixelToValue(w) / _width).ensure(1f)
    }
    if (h != 0f && _height != 0f) {
        scaleY = (valueUnit.convertPixelToValue(h) / _height).ensure(1f)
    }
}

/**更新坐标, 缩放比例数据*/
fun LPElementBean.updateByBounds(@Pixel bounds: RectF) {
    val valueUnit = IValueUnit.MM_UNIT
    left = valueUnit.convertPixelToValue(bounds.left)
    top = valueUnit.convertPixelToValue(bounds.top)

    val width = bounds.width()
    val height = bounds.height()
    updateScale(width, height)
}

/**复制元素
 * [offset] 是否开启偏移, 会在原数据的基础上+上偏移量*/
fun LPElementBean.copyBean(offset: Boolean = false): LPElementBean {
    val newBean = copy()
    if (offset) {
        newBean.left += GraphicsHelper.POSITION_STEP
        newBean.top += GraphicsHelper.POSITION_STEP

        newBean.index = null//清空索引
    }
    return newBean
}

/**使用[gravity]属性, 重新设置[left] [top]值
 * [bounds] 设备雕刻的最佳范围
 * [com.angcyo.engrave.model.AutoEngraveModel.initLocationWithGravity]*/
fun LPElementBean.resetLocationWithGravity(@Pixel bounds: RectF) {
    gravity?.let {
        val valueUnit = IValueUnit.MM_UNIT
        val offsetX = valueUnit.convertValueToPixel(left)
        val offsetY = valueUnit.convertValueToPixel(top)
        val width = valueUnit.convertValueToPixel(_width) * _scaleX
        val height = valueUnit.convertValueToPixel(_height) * _scaleY
        dslGravity(
            bounds,
            it,
            width,
            height,
            offsetX,
            offsetY,
            false
        ) { dslGravity, centerX, centerY ->
            //修改
            left = valueUnit.convertPixelToValue(dslGravity._gravityLeft)
            top = valueUnit.convertPixelToValue(dslGravity._gravityTop)
        }
    }
}
