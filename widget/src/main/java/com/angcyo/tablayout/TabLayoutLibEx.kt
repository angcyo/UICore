package com.angcyo.tablayout

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.math.MathUtils
import com.angcyo.library.ex.dpi
import kotlin.math.absoluteValue

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/11/23
 */
val View.screenWidth: Int
    get() = context.resources.displayMetrics.widthPixels

val View.screenHeight: Int
    get() = context.resources.displayMetrics.heightPixels

val View.viewDrawWidth: Int
    get() = measuredWidth - paddingLeft - paddingRight

val View.viewDrawHeight: Int
    get() = measuredHeight - paddingTop - paddingBottom

/**Match_Parent*/
fun exactlyMeasure(size: Int): Int =
    View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)

fun exactlyMeasure(size: Float): Int = exactlyMeasure(size.toInt())

/**Wrap_Content*/
fun atmostMeasure(size: Int): Int =
    View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST)

fun clamp(value: Float, min: Float, max: Float): Float {
    if (value < min) {
        return min
    } else if (value > max) {
        return max
    }
    return value
}

fun clamp(value: Int, min: Int, max: Int): Int {
    if (value < min) {
        return min
    } else if (value > max) {
        return max
    }
    return value
}

fun Any.logi() {
    Log.i("DslTabLayout", "$this")
}

fun Any.logw() {
    Log.w("DslTabLayout", "$this")
}

fun Any.loge() {
    Log.e("DslTabLayout", "$this")
}

/**
 * 支持格式0.3pw 0.5ph, p表示[parent]的多少倍数, s表示[screen]的多少倍数
 * */
fun View.calcLayoutWidthHeight(
    rLayoutWidth: String?, rLayoutHeight: String?,
    parentWidth: Int, parentHeight: Int,
    rLayoutWidthExclude: Int = 0, rLayoutHeightExclude: Int = 0
): IntArray {
    val size = intArrayOf(-1, -1)
    if (TextUtils.isEmpty(rLayoutWidth) && TextUtils.isEmpty(rLayoutHeight)) {
        return size
    }
    size[0] = calcSize(rLayoutWidth, parentWidth, parentHeight, rLayoutWidthExclude)
    size[1] = calcSize(rLayoutHeight, parentWidth, parentHeight, rLayoutHeightExclude)
    return size
}

fun View.calcLayoutMaxHeight(
    rMaxHeight: String?,
    parentWidth: Int,
    parentHeight: Int,
    exclude: Int = 0
): Int {
    return calcSize(rMaxHeight, parentWidth, parentHeight, exclude)
}

fun View.calcSize(exp: String?, pWidth: Int, pHeight: Int, exclude: Int): Int {
    var result = -1
    if (!exp.isNullOrBlank()) {
        fun _get(ut: String, height: Int): Boolean {
            if (exp.contains(ut, true)) {
                val ratio = exp.replace(ut, "", true).toFloatOrNull()
                ratio?.let {
                    result = if (it >= 0) {
                        (it * (height - exclude)).toInt()
                    } else {
                        (height - it.absoluteValue * height - exclude).toInt()
                    }
                }
                return true
            }
            return false
        }

        fun _getDp(ut: String, density: Int): Boolean {
            if (exp.contains(ut, true)) {
                val ratio = exp.replace(ut, "", true).toFloatOrNull()
                ratio?.let {
                    result = if (it >= 0) {
                        ((it * density) - exclude).toInt()
                    } else {
                        (pHeight - it.absoluteValue * density - exclude).toInt()
                    }
                }
                return true
            }
            return false
        }

        when {
            _get("sh", screenHeight) -> {
            }
            _get("ph", pHeight) -> {
            }
            _get("sw", screenWidth) -> {
            }
            _get("pw", pWidth) -> {
            }
            _getDp("dip", dpi) -> {
            }
            _getDp("px", 1) -> {

            }
        }
    }
    return result
}

fun evaluateColor(fraction: Float /*0-1*/, startColor: Int, endColor: Int): Int {
    val fraction = MathUtils.clamp(fraction, 0f, 1f)
    val startA = startColor shr 24 and 0xff
    val startR = startColor shr 16 and 0xff
    val startG = startColor shr 8 and 0xff
    val startB = startColor and 0xff
    val endA = endColor shr 24 and 0xff
    val endR = endColor shr 16 and 0xff
    val endG = endColor shr 8 and 0xff
    val endB = endColor and 0xff
    return startA + (fraction * (endA - startA)).toInt() shl 24 or
            (startR + (fraction * (endR - startR)).toInt() shl 16) or
            (startG + (fraction * (endG - startG)).toInt() shl 8) or
            startB + (fraction * (endB - startB)).toInt()
}

fun Drawable?.tintDrawableColor(color: Int): Drawable? {

    if (this == null) {
        return this
    }

    val wrappedDrawable =
        DrawableCompat.wrap(this).mutate()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        DrawableCompat.setTint(wrappedDrawable, color)
    } else {
        wrappedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    return wrappedDrawable
}

fun View?.tintDrawableColor(color: Int) {
    when (this) {
        is TextView -> {
            val drawables = arrayOfNulls<Drawable?>(4)
            compoundDrawables.forEachIndexed { index, drawable ->
                drawables[index] = drawable?.tintDrawableColor(color)
            }
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
        }
        is ImageView -> {
            setImageDrawable(drawable?.tintDrawableColor(color))
        }
    }
}

fun Paint?.textWidth(text: String?): Float {
    if (TextUtils.isEmpty(text)) {
        return 0f
    }
    return this?.run {
        measureText(text)
    } ?: 0f
}

fun Paint?.textHeight(): Float = this?.run { descent() - ascent() } ?: 0f