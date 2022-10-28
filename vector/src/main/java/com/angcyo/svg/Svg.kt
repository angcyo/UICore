package com.angcyo.svg

import android.graphics.Color
import android.graphics.Paint
import com.pixplicity.sharp.SharpDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/28
 */

/**
 * [com.pixplicity.sharp.SharpDrawable.pathList]
 * [SharpDrawable]
 * */
fun String.toSvgSharpDrawable(): SharpDrawable? = if (isEmpty()) null else Svg.loadSvgDrawable(this)

/**只读取Path数据*/
fun String.toSvgPathSharpDrawable(): SharpDrawable? =
    if (isEmpty()) null else Svg.loadSvgPathDrawable(
        this,
        -1,
        null,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.BLACK
            this.style = Paint.Style.STROKE
            strokeWidth = 1f
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        },
        0,
        0
    )