package com.angcyo.drawable.loading

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.core.graphics.withSkew
import androidx.core.graphics.withTranslation
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.toRadians
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.tan

/**
 * 模仿支付宝加载动画, 斜的四边形, 只有颜色变化
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/15
 */
class AlLoadingDrawable : PostLoadingDrawable() {

    /**倾斜角度*/
    var itemAngle: Float = 30f

    //倾斜的宽度
    val tiltWidth: Float
        get() = (intrinsicHeight / tan(itemAngle.toRadians()) / 2).absoluteValue

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        itemWidth = 10 * dpi
        itemGap = 6 * dpi
        itemMinHeightProgress = 100
        super.initAttribute(context, attributeSet)
    }

    override fun getIntrinsicWidth(): Int {
        return (super.getIntrinsicWidth() + tiltWidth).roundToInt()
    }

    override fun onDrawItem(canvas: Canvas, drawItem: DrawItem, index: Int) {
        canvas.withTranslation(tiltWidth, 0f) {
            withSkew(-tan(itemAngle.toRadians()), 0f) {
                super.onDrawItem(canvas, drawItem, index)
            }
        }
    }

}