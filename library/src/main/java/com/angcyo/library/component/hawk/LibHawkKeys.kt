package com.angcyo.library.component.hawk

import androidx.annotation.Keep
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.HawkPropertyValue
import com.angcyo.library.ex.isDebug

/**
 * 内部库中的一些持久化数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/18
 */

@Keep
object LibHawkKeys {

    /**[com.angcyo.component.luban.DslLuban]
     * 压缩时, 最小的压缩像素大小 [kb]
     * */
    var minKeepSize: Int by HawkPropertyValue<Any, Int>(400)

    /**当颜色的透明值小于此值时, 视为透明色
     * 影响图片的锯齿, 值越大锯齿越多
     * 图片旋转之后, 4条边上就会产生透明颜色*/
    var alphaThreshold: Int by HawkPropertyValue<Any, Int>(8)

    /**等同于[alphaThreshold], 只不过这个值在移除透明背景时使用
     * 或者在图片旋转后, 移除边上的透明颜色时使用*/
    var bgAlphaThreshold: Int by HawkPropertyValue<Any, Int>(250)

    /**灰度阈值, 大于这个值视为白色1不出光, 小于这个值视为黑色0出光
     * 白色传1, 1不出光.
     * 黑色传0, 0出光. */
    var grayThreshold: Int by HawkPropertyValue<Any, Int>(128)

    /**日志单文件最大数据量的大小
     * 允许写入单个文件的最大大小10mb, 之后会重写*/
    var logFileMaxSize: Long by HawkPropertyValue<Any, Long>(2 * 1024 * 1024)

    /**允许最大分配的图片大小,
     * 10mb 10 * 1024 * 1024 => 10,485,760
     * 64,000,000 61mb
     *
     * pixel 6 max:174 584 760 bytes
     * */
    var maxBitmapCanvasSize: Long by HawkPropertyValue<Any, Long>(60 * 1024 * 1024)

    /**是否要激活Canvas的渲染数量限制
     * 激活后[canvasRenderMaxCount]才有效*/
    var enableCanvasRenderLimit: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**[CanvasDelegate]item的bounds范围
     * l,t,r,b mm单位*/
    var canvasItemBoundsLimit: String by HawkPropertyValue<Any, String>("-1000,-1000,1000,8000")

    /**[com.angcyo.canvas.CanvasDelegate]允许添加的最大渲染元素数据*/
    var canvasRenderMaxCount: Int by HawkPropertyValue<Any, Int>(30)

    /**是否激活画布的边缘移动
     * [canvasEdgeThreshold] [canvasEdgeTranslateStep]*/
    var enableCanvasEdgeTranslate: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**当手指移动到距离画布多少距离时, 视为在边界, 毫米单位*/
    @MM
    var canvasEdgeThreshold: Float by HawkPropertyValue<Any, Float>(3f)

    /**在边界移动时的步长, 毫米*/
    @MM
    var canvasEdgeTranslateStep: Float by HawkPropertyValue<Any, Float>(1f)

    /**是否激活[Path]确切的bounds计算, 计算量会变大*/
    var enablePathBoundsExact: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**计算[Bounds]时, 容错率, 应该也是each path的采样率
     * [enablePathBoundsExact]
     * */
    @Pixel
    var pathAcceptableError: Float by HawkPropertyValue<Any, Float>(1f)

    /**是否强制使用高刷*/
    var enableHighRefresh: Boolean by HawkPropertyValue<Any, Boolean>(isDebug())

    /**2个浮点比较, 误差小于此值视为相等
     * 0.0000000 //浮点小数点后面有7位*/
    var floatAcceptableError: Float by HawkPropertyValue<Any, Float>(0.00001f) //5位

    /**2个双精度浮点比较, 误差小于此值视为相等
     * 0.0000000000000000 //双精度浮点小数点后面有16位*/
    var doubleAcceptableError: Double by HawkPropertyValue<Any, Double>(0.000000000000001) //15位
}