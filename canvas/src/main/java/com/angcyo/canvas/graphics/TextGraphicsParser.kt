package com.angcyo.canvas.graphics

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.widget.LinearLayout
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.data.toPixel
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataTextItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.textBounds
import com.angcyo.library.gesture.RectScaleGestureHandler

/**
 * 文本数据解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/22
 */
class TextGraphicsParser : IGraphicsParser {

    override fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_TEXT && !bean.text.isNullOrEmpty()) {
            val item = DataTextItem(bean)
            updateTextDrawable(item)
            return item
        }
        return super.parse(bean, canvasView)
    }

    /**更新文本内容, 重新绘制信息*/
    fun updateTextDrawable(item: DataTextItem) {
        val bean = item.dataBean
        item.updatePaint()

        val drawText = bean.text ?: ""
        //createStaticLayout(drawText, paint)
        val textWidth = item.calcTextWidth(drawText)
        val textHeight = item.calcTextHeight(drawText)

        @Pixel
        val width = textWidth.toInt()

        @Pixel
        val height = textHeight.toInt()

        item.renderDrawable = wrapScalePictureDrawable(width, height) {
            drawNormalText(this, item)
            //drawPathText(this, item, textWidth, textHeight)
        }

        @MM
        val newWidth = textWidth.toMm()
        val newHeight = textHeight.toMm()

        if (bean.width == null) {
            bean.width = newWidth
        } else {
            if (bean.scaleX == null) {
                val sx = (bean._width / newWidth).ensure(1f)
                bean.scaleX = sx
            }
        }
        if (bean.height == null) {
            bean.height = newHeight
        } else {
            if (bean.scaleY == null) {
                val sy = (bean._height / newHeight).ensure(1f)
                bean.scaleY = sy
            }
        }

        initDataModeWithPaintStyle(bean, item.itemPaint)
    }

    /**更新旋转偏移, 通常在改变宽高/文本之后需要调用,
     * 确保编辑后, 左上角锚点不变*/
    fun updateRotateOffset(item: DataTextItem) {
        val bean = item.dataBean
        val drawText = bean.text ?: ""
        //createStaticLayout(drawText, paint)
        val textWidth = item.calcTextWidth(drawText)
        val textHeight = item.calcTextHeight(drawText)

        //当有旋转角度的情况下, 修改了文本的宽高, 则保持可视化时左上角坐标不变
        @Pixel
        val rect = acquireTempRectF()
        rect.left = bean.left
        rect.top = bean.top
        rect.right = bean.left + bean._width
        rect.bottom = bean.top + bean._height
        bean.updateToRenderBounds(rect)

        //计算偏移
        val offsetPoint = RectScaleGestureHandler.calcRectUpdateOffset(
            rect,
            textWidth * bean._scaleX,
            textHeight * bean._scaleX,
            bean.angle
        )
        //核心
        bean.left = bean.left + offsetPoint.x.toMm()
        bean.top = bean.top + offsetPoint.y.toMm()

        rect.release()
    }

    //

    val _deleteLineRect = RectF()
    val _underLineRect = RectF()

    /**绘制普通文本*/
    fun drawNormalText(canvas: Canvas, textItem: DataTextItem) {
        val dataBean = textItem.dataBean
        val paint = textItem.itemPaint

        val oldUnderLine = paint.isUnderlineText
        val oldDeleteLine = paint.isStrikeThruText

        //因为是自己一个一个绘制的, 所以删除线和下划线也需要手绘
        paint.isUnderlineText = false
        paint.isStrikeThruText = false

        val lineTextList = dataBean.text.lineTextList()

        var x = 0f
        var y = 0f

        //删除线的宽度
        val lineWidth = paint.strokeWidth

        if (dataBean.orientation == LinearLayout.HORIZONTAL) {
            lineTextList.forEach { lineText ->
                val lineTextWidth = textItem.calcTextWidth(lineText)
                val lineTextHeight = textItem.calcTextHeight(lineText)
                val descent = textItem.measureTextDescent(lineText)

                val lineHeight = lineTextHeight / 10
                _deleteLineRect.set(
                    x + lineWidth / 2,
                    y + lineTextHeight / 2 - lineHeight / 2,
                    lineTextWidth - lineWidth / 2,
                    y + lineTextHeight / 2 + lineHeight / 2
                )
                _underLineRect.set(
                    x + lineWidth / 2,
                    y + lineTextHeight - lineHeight,
                    lineTextWidth - lineWidth / 2,
                    y + lineTextHeight
                )

                y += lineTextHeight

                //逐字绘制
                lineText.forEach { char ->
                    val text = "$char"
                    val charWidth = textItem.measureTextWidth(text)

                    val offsetX = when (paint.textAlign) {
                        Paint.Align.RIGHT -> charWidth - textItem._skewWidth
                        Paint.Align.CENTER -> charWidth / 2 - textItem._textMeasureBounds.left / 2 - textItem._skewWidth / 2
                        else -> -textItem._textMeasureBounds.left.toFloat()
                    }

                    canvas.drawText(text, x + offsetX, y - descent, paint)
                    x += charWidth + dataBean.charSpacing.toPixel()
                }

                //删除线
                if (oldDeleteLine) {
                    canvas.drawRect(_deleteLineRect, paint)
                }
                //下划线
                if (oldUnderLine) {
                    canvas.drawRect(_underLineRect, paint)
                }

                y += dataBean.lineSpacing.toPixel()
                x = 0f
            }
        } else {
            lineTextList.forEach { lineText ->
                val lineTextWidth = textItem.calcTextWidth(lineText)
                val lineTextHeight = textItem.calcTextHeight(lineText)

                //逐字绘制
                lineText.forEach { char ->
                    val text = "$char"
                    val charWidth = textItem.measureTextWidth(text)
                    val charHeight = textItem.measureTextHeight(text)
                    val descent = textItem.measureTextDescent(text)
                    val textBounds = paint.textBounds(text)

                    val offsetX = if (dataBean.isCompactText) {
                        when (paint.textAlign) {
                            Paint.Align.RIGHT -> lineTextWidth - textItem._skewWidth /*+ textBounds.left.toFloat()*/
                            Paint.Align.CENTER -> lineTextWidth / 2 - textItem._skewWidth / 2
                            else -> -textBounds.left.toFloat()
                        }
                    } else {
                        when (paint.textAlign) {
                            Paint.Align.RIGHT -> lineTextWidth - textItem._skewWidth.toInt()
                            Paint.Align.CENTER -> lineTextWidth / 2 - textItem._skewWidth.toInt() / 2
                            else -> 0f
                        }
                    }

                    val lineHeight = charHeight / 10
                    //删除线
                    if (oldDeleteLine) {
                        _deleteLineRect.set(
                            x + lineWidth / 2,
                            y + charHeight / 2 - lineHeight / 2,
                            x + charWidth - lineWidth / 2,
                            y + charHeight / 2 + lineHeight / 2
                        )

                        canvas.drawRect(_deleteLineRect, paint)
                    }
                    //下划线
                    if (oldUnderLine) {
                        _underLineRect.set(
                            x + lineWidth / 2,
                            y + charHeight - lineHeight,
                            x + charWidth - lineWidth / 2,
                            y + charHeight
                        )
                        canvas.drawRect(_underLineRect, paint)
                    }

                    y += charHeight
                    canvas.drawText(text, x + offsetX, y - descent, paint)
                    y += dataBean.charSpacing.toPixel()
                }

                x += lineTextWidth + dataBean.lineSpacing.toPixel()
                y = 0f
            }
        }

        paint.isUnderlineText = oldUnderLine
        paint.isStrikeThruText = oldDeleteLine
    }

    /**绘制路径文本*/
    fun drawPathText(canvas: Canvas, textItem: DataTextItem, textWidth: Float, textHeight: Float) {
        val dataBean = textItem.dataBean
        val paint = textItem.itemPaint

        //createStaticLayout(drawText, paint)
        var width = 0f
        var height = 0f

        val path = Path()
        //path.addOval(0f, 0f, 2000f, 1000f, Path.Direction.CCW)
        val pathWidth = textWidth
        val pathHeight = textHeight
        path.moveTo(0f, pathHeight)
        path.bezier(pathWidth / 2, 0f, pathWidth, pathHeight)
        val bounds = path.computePathBounds()

        width += bounds.width() + textHeight
        height += bounds.height() + textHeight

        canvas.translate(width / 2 - bounds.width() / 2, 0f)

        val lineTextList = dataBean.text.lineTextList()

        //CW, 绘制在圈外
        //CCW, 绘制在圈内
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL_AND_STROKE

        var hOffset = 0f
        var vOffset = 0f
        lineTextList.reversed().forEach { lineText ->
            val lineTextHeight = textItem.calcTextHeight(lineText)
            val descent = textItem.measureTextDescent(lineText)
            vOffset += descent
            lineText.forEach { char ->
                val text = "$char"
                val charWidth = textItem.measureTextWidth(text)

                val offsetX = when (paint.textAlign) {
                    Paint.Align.RIGHT -> charWidth - textItem._skewWidth
                    Paint.Align.CENTER -> charWidth / 2 - textItem._textMeasureBounds.left / 2 - textItem._skewWidth / 2
                    else -> -textItem._textMeasureBounds.left.toFloat()
                }

                canvas.drawTextOnPath(text, path, hOffset + offsetX, -vOffset, paint)
                hOffset += charWidth + dataBean.charSpacing.toPixel()
            }

            hOffset = 0f
            vOffset += lineTextHeight + dataBean.lineSpacing.toPixel()
        }
    }
}

/**获取每一行的文本*/
fun String?.lineTextList(): List<String> = this?.lines() ?: emptyList()