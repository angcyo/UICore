package com.angcyo.item.form

import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.DslItemDecoration
import com.angcyo.item.R
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import kotlin.math.min

/**
 * 用于绘制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class DslFormItemDecoration : DslItemDecoration() {
    /**必填文本绘制*/
    var th: String = "*"

    /**必填文本颜色*/
    var thColor = "#FF3622".toColorInt()

    /**必填文本相对于label左边的偏移*/
    var thOffset = 2 * dpi

    val formPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**错误时Label的提示颜色*/
    var errorLabelColor = "#FF3622".toColorInt()

    /**错误提示*/
    var itemErrorTipTask: ItemErrorTipTask? = null

    /**label控件的id*/
    var labelViewId = R.id.lib_label_view

    //具有分组的表单, 不进行错误提示, 因为会被悬停挡住
    var haveGroup = false

    val _itemPath = Path()
    val _drawPath = Path()

    override fun onEachItemDoIt(
        canvas: Canvas?,
        parent: RecyclerView,
        state: RecyclerView.State,
        outRect: Rect?,
        beforeViewHolder: DslViewHolder?,
        viewHolder: DslViewHolder,
        afterViewHolder: DslViewHolder?,
        isOverDraw: Boolean
    ) {
        val adapterPosition = viewHolder.adapterPosition

        if (parent.adapter is DslAdapter && adapterPosition != RecyclerView.NO_POSITION) {
            (parent.adapter as? DslAdapter)?.getItemData(adapterPosition)?.let { item ->
                if (state.isPreLayout ||
                    state.willRunSimpleAnimations() ||
                    !isOverDraw
                ) {
                    //no op
                } else {
                    if (item is IFormItem) {
                        drawTh(canvas, viewHolder, item)
                    }
                    drawError(canvas, parent, viewHolder, item)
                }
            }
        }
    }

    /**绘制星星*/
    fun drawTh(canvas: Canvas?, viewHolder: DslViewHolder, item: IFormItem) {
        if (item.itemFormConfig.formRequired) {
            formPaint.color = thColor
            formPaint.style = Paint.Style.FILL

            //绘制`*`
            viewHolder.view(labelViewId)?.let {
                formPaint.textSize = 14 * dp

                val left = viewHolder.itemView.left +
                        it.left + it.paddingLeft -
                        formPaint.textWidth(th) - thOffset

                val viewContentHeight = it.measuredHeight - it.paddingTop - it.paddingBottom
                val viewContentCenterY = it.paddingTop + viewContentHeight / 2

                val top = viewHolder.itemView.top +
                        it.top + viewContentCenterY +
                        formPaint.textHeight() / 2

                canvas?.drawText(th, left, top, formPaint)
            }
        }
    }

    fun drawError(
        canvas: Canvas?,
        parent: RecyclerView,
        viewHolder: DslViewHolder,
        item: DslAdapterItem
    ) {
        haveGroup = haveGroup || item.itemIsGroupHead

        val adapterPosition = viewHolder.adapterPosition

        //错误动画提示
        if (itemErrorTipTask != null &&
            itemErrorTipTask?.errorPosition == adapterPosition &&
            canvas != null && !haveGroup
        ) {

            //错误文本的颜色
            viewHolder.tv(labelViewId)?.setTextColor(errorLabelColor)

            val task = itemErrorTipTask!!

            if (task.errorPathProgress > 1.2f) {
                //抛一帧, 让进度能够绘制完成的状态
                itemErrorTipTask = null
                return
            }

            val progress = min(1f, task.errorPathProgress)

            //动画提示
            if (progress == 0f) {
                viewHolder.itemView.error()
            }

            val width = 2 * dp

            //Path提示
            _itemPath.apply {
                //绘制的路径
                reset()
                addRect(
                    viewHolder.itemView.left + width / 2,
                    viewHolder.itemView.top + width / 2,
                    viewHolder.itemView.right - width / 2,
                    viewHolder.itemView.bottom - width / 2,
                    Path.Direction.CCW
                )

                //一定进度的绘制路径
                val pathMeasure = PathMeasure(this, false)
                _drawPath.reset()
                pathMeasure.getSegment(
                    0f,
                    progress * pathMeasure.length,
                    _drawPath,
                    true
                )

                //开始绘制
                formPaint.color = errorLabelColor
                formPaint.style = Paint.Style.STROKE
                formPaint.strokeWidth = width
                canvas.drawPath(_drawPath, formPaint)

                //动画
                task.errorPathProgress += 1f / 24
                parent.postInvalidate()
            }
        }
    }
}

/**错误提示*/
data class ItemErrorTipTask(
    //需要提示的位置
    var errorPosition: Int = RecyclerView.NO_POSITION,

    //绘制的进度
    var errorPathProgress: Float = 0f,

    //界面使用了behavior, 标题栏会被隐藏. 所以滚动目标position应该进行偏移
    var errorPositionOffset: Int = -1
)
