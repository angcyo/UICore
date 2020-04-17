package com.angcyo.widget.text

import android.text.*
import android.text.style.CharacterStyle
import android.widget.TextView
import com.angcyo.library.L
import com.angcyo.library.ex.clearSpans
import com.angcyo.library.ex.undefined_color
import com.angcyo.widget.span.DslDrawableSpan
import com.angcyo.widget.span.SpanClickMethod

/**
 * 系统的[TextView]设置maxLine>1后
 * 设置 [setEllipsize] [android.text.TextUtils.TruncateAt.END] 后, 并无效果.
 *
 * 这里手动在后面加一个[span], 达到相同效果.
 *
 * setText时, 需要使用[TextView.BufferType.EDITABLE] [TextView.BufferType.SPANNABLE]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MaxLineDelegate {

    /**
     * 最大显示多少行, 当超过时, 会显示[moreText]+[foldText]的组合形式
     * */
    var maxShowLine = -1

    var moreText = "..."
    var foldText = ""

    var moreTextColor = undefined_color
    var foldTextColor = undefined_color

    val moreSpan = MaxLinePlaceholderSpan()
    val foldSpan = MaxLinePlaceholderSpan().apply {
        spanClickAction = { view, span ->
            if (view is TextView) {
                //展开所有
                expandAllLine(view)
            }
        }
    }

    //保存原始的文本
    var _originText: CharSequence? = null

    var installSpanClickMethod: Boolean = false

    /**多行折叠检查*/
    fun checkMaxShowLine(textView: TextView) {
        if (maxShowLine > 0 && installSpanClickMethod) {
            SpanClickMethod.install(textView)
        }

        val layout: Layout? = textView.layout
        if (maxShowLine > 0 && layout != null) {
            val lines = layout.lineCount
            if (lines > 0) {
                if (lines >= maxShowLine) {
                    //需要折叠

                    val originText: CharSequence? = textView.text

                    //参与折叠的文本
                    val targetText = MaxLineSpannableString(
                        if (originText is MaxLineSpannableString) {
                            _originText
                        } else {
                            _originText = originText
                            SpannableString.valueOf(originText)
                        }
                    )

                    //targetText.removeSpan(moreSpan)
                    //targetText.removeSpan(foldSpan)

                    val textLength = targetText.length
                    if (textLength <= moreText.length + foldText.length) {
                        setMaxShowLine(textView, -1) //换行字符太多的情况
                        return
                    }
                    val lineStart = layout.getLineStart(maxShowLine) //返回第几行的第一个字符, 在字符串中的index

                    /*+ 20 * ImageTextSpan.getSpace(getContext())*/ //需要预留绘制文件的空间宽度
                    val needWidth: Float = textView.paint.measureText(moreText + foldText)

                    //找出需要剔除多少个字符,才够空间绘制
                    var startPosition = -1
                    var i = 0
                    while (i < textLength && lineStart > 0) {
                        val start = lineStart - i
                        if (start in 0 until lineStart) {
                            val charSequence = targetText.subSequence(start, lineStart)
                            val textWidth: Float = textView.paint.measureText("$charSequence")
                            if (textWidth > needWidth) {
                                startPosition = lineStart - i - 1 //多预留一个位置, 防止不够宽度无法绘制
                                break
                            }
                        }
                        i++
                    }

                    moreSpan.apply {
                        textSize = textView.textSize
                        textColor =
                            if (moreTextColor == undefined_color) textView.currentTextColor else moreTextColor
                        showText = moreText
                    }

                    foldSpan.apply {
                        textSize = textView.textSize
                        textColor =
                            if (foldTextColor == undefined_color) textView.currentTextColor else foldTextColor
                        showText = foldText
                    }

                    //int startPosition = lineStart - more.length() - foldString.length();
                    if (startPosition < 0) {
                        //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 1");
                        targetText.clearSpans(lineStart - 1)
                        targetText.setSpan(
                            moreSpan,
                            lineStart - 1,
                            lineStart,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        return
                    }
                    val start: Int = findStartPosition(targetText, startPosition)
                    targetText.clearSpans(start)

                    val offset: Int = moreText.length //(sequence.length() % 2 == 0) ? 4 : 3;
                    if (TextUtils.isEmpty(foldText)) {
                        if (!TextUtils.isEmpty(moreText)) {
                            //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 2:" + start);
                            targetText.setSpan(
                                moreSpan,
                                start,
                                textLength,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    } else {
                        if (!TextUtils.isEmpty(moreText)) {
                            //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 3:" + start);
                            targetText.setSpan(
                                moreSpan,
                                start,
                                start + offset,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 4:" + start);
                        targetText.setSpan(
                            foldSpan,
                            start + offset,
                            textLength,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    //setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredHeight() + density() * 140));

                    L.i("")

                    textView.text = targetText
                }
            }
        }
    }

    /**展开所有, 展开所有行*/
    fun expandAllLine(textView: TextView) {
        setMaxShowLine(textView, -1)
        textView.text = _originText
    }

    /**
     * 设置允许显示的最大行数
     */
    fun setMaxShowLine(textView: TextView, line: Int) {
        this.maxShowLine = line
        textView.ellipsize = null//去掉系统默认处理
        if (line < 0) {
            textView.maxLines = Int.MAX_VALUE
        } else {
            //textView.ellipsize = TextUtils.TruncateAt.END
            textView.maxLines = line
        }
    }

    /**
     * 检查当前位置是否命中在spannable上, 如果是, 返回spannable的start position
     */
    private fun findStartPosition(spannable: Spannable, startWidthPosition: Int): Int {
        val oldSpans =
            spannable.getSpans(startWidthPosition, spannable.length, CharacterStyle::class.java)
        var position = startWidthPosition

        for (oldSpan in oldSpans) {
            val spanStart = spannable.getSpanStart(oldSpan)
            val spanEnd = spannable.getSpanEnd(oldSpan)
            if (startWidthPosition in spanStart until spanEnd) {
                position = spanStart
            }
            if (spanStart >= startWidthPosition) {
                spannable.removeSpan(oldSpan)
            }
        }
        //L.e("call: findStartPosition([spannable, startWidthPosition]) " + startWidthPosition + " -> " + position);
        return position
    }

    /**占位标识用*/
    class MaxLinePlaceholderSpan : DslDrawableSpan()

    /**占位标识用*/
    class MaxLineSpannableString(source: CharSequence?) : SpannableString(source)
}