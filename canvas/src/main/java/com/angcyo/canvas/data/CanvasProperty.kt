package com.angcyo.canvas.data

/**
 * Canvas绘制时的一些属性
 *
 * [CanvasProjectItemBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/02
 */
data class CanvasProperty(

    /**旋转的度数, 角度单位. 按照中心点旋转绘制*/
    var angle: Float = 0f,

    /**绘制时的缩放比例*/
    var scaleX: Float = 1f,

    var scaleY: Float = 1f,

    /**是否水平翻转, 如果是, 则就是[-scaleX]*/
    var flipX: Boolean = false,

    /**是否垂直翻转
     * [-scaleX]*/
    var flipY: Boolean = false,

    /**绘制时的倾斜度数, 角度单位.
     * 先缩放, 再倾斜. 然后旋转绘制
     *
     * ```
     * postSkew(tan(skewX.toRadians()).toFloat(), tan(skewY.toRadians()).toFloat(), rect.left, rect.top)
     * ```
     * */
    var skewX: Float = 0f,

    /**这是值始终为0*/
    var skewY: Float = 0f,
)
