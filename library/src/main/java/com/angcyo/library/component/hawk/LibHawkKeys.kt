package com.angcyo.library.component.hawk

import android.graphics.Color
import androidx.annotation.Keep
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.hawk.LibHawkKeys._pathAcceptableError
import com.angcyo.library.component.hawk.LibHawkKeys.alphaThreshold
import com.angcyo.library.component.hawk.LibHawkKeys.canvasEdgeThreshold
import com.angcyo.library.component.hawk.LibHawkKeys.canvasEdgeTranslateStep
import com.angcyo.library.component.hawk.LibHawkKeys.canvasRenderMaxCount
import com.angcyo.library.component.hawk.LibHawkKeys.enablePathBoundsExact
import com.angcyo.library.component.hawk.LibHawkKeys.pathAcceptableError
import com.angcyo.library.component.hawk.LibHawkKeys.pathAcceptableErrorMM
import com.angcyo.library.component.hawk.LibHawkKeys.svgTolerance
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.isDebug
import com.angcyo.library.getAppString
import com.angcyo.library.unit.toPixel

/**
 * 内部库中的一些持久化数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/18
 */

@Keep
object LibHawkKeys {

    /**合规key, 用来保存当前版本的合规状态*/
    const val KEY_COMPLIANCE_STATE = "KEY_COMPLIANCE_STATE"

    /**Hawk Key
     * [com.angcyo.library.getAppVersionCode]*/
    var KEY_COMPLIANCE = "${KEY_COMPLIANCE_STATE}_${getAppString("versionCode")}"

    /**是否合规了, 持久化*/
    val isCompliance: Boolean
        get() = KEY_COMPLIANCE.hawkGet() == "true"

    /**[com.angcyo.component.luban.DslLuban]
     * 压缩时, 最小的压缩像素大小 [kb]
     * */
    var minKeepSize: Int by HawkPropertyValue<Any, Int>(400)

    /**最小界面更新post延迟操作*/
    var minInvalidateDelay: Long by HawkPropertyValue<Any, Long>(16)

    /**最小post延迟操作*/
    var minPostDelay: Long by HawkPropertyValue<Any, Long>(160)

    /**当颜色的透明值小于此值时, 视为透明色
     * 影响图片的锯齿, 值越大锯齿越多
     * 图片旋转之后, 4条边上就会产生透明颜色*/
    var alphaThreshold: Int by HawkPropertyValue<Any, Int>(250)

    /**等同于[alphaThreshold], 只不过这个值在移除透明背景时使用
     * 或者在图片旋转后, 移除边上的透明颜色时使用*/
    var bgAlphaThreshold: Int by HawkPropertyValue<Any, Int>(250)

    /**部分算法处理时, 如果背景是透明色, 则使用此颜色替换*/
    var bgAlphaColor: Int by HawkPropertyValue<Any, Int>(Color.WHITE)

    /**灰度阈值, 大于这个值视为白色1不出光, 小于这个值视为黑色0出光
     * 白色传1, 1不出光.
     * 黑色传0, 0出光. */
    var grayThreshold: Int by HawkPropertyValue<Any, Int>(240)

    /**自动裁边时, 需要剔除的灰度阈值*/
    var cropGrayThreshold: Int by HawkPropertyValue<Any, Int>(240)

    /**日志单文件最大数据量的大小
     * 允许写入单个文件的最大大小10mb, 之后会重写*/
    var logFileMaxSize: Long by HawkPropertyValue<Any, Long>(2 * 1024 * 1024)

    /**是否要激活Canvas的渲染数量限制
     * 激活后[canvasRenderMaxCount]才有效*/
    var enableCanvasRenderLimit: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**[CanvasDelegate]item的bounds范围
     * l,t,r,b mm单位*/
    @MM
    var canvasItemBoundsLimit: String by HawkPropertyValue<Any, String>("-9999,-9999,9999,9999")

    /**[CanvasDelegate]item的宽高
     * minw,minh,maxw,maxh mm单位*/
    @Pixel
    var canvasItemSizeLimit: String by HawkPropertyValue<Any, String>("1,1,9999,99999")

    /**删除线/下划线的高度是文本高度的几分之一*/
    var canvasLineHeight: Int by HawkPropertyValue<Any, Int>(10)

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
    var enablePathBoundsExact: Boolean by HawkPropertyValue<Any, Boolean>(false)

    /**
     * [com.angcyo.library.ex.PathExKt.approximate2]
     * [android.graphics.Path.approximate]*/
    @Pixel
    var pathBoundsAcceptableError: Float by HawkPropertyValue<Any, Float>(0.25f)

    @MM
    var pathAcceptableErrorMM: Float by HawkPropertyValue<Any, Float>(0.1f)

    /**GCode矢量预览时, 单独的路径采样步长
     * 默认就是[pathAcceptableErrorMM]*/
    @MM
    var pathPreviewAcceptableErrorMM: Float? by HawkPropertyValue<Any, Float?>(null)

    /**计算[Bounds]时, 容错率, 应该也是each path的采样率
     * [enablePathBoundsExact]
     * [svgTolerance]
     * */
    @Pixel
    var pathAcceptableError: Float? by HawkPropertyValue<Any, Float?>(null)

    /**[pathAcceptableError]*/
    @Pixel
    val _pathAcceptableError: Float
        get() = pathAcceptableError ?: pathAcceptableErrorMM.toPixel()

    /**图片转GCode时, [gapValue]采样间隙, mm单位
     * [com.angcyo.engrave2.transition.SimpleTransition.covertBitmapPixel2GCode]*/
    @MM
    var pathPixelGapValue: Float by HawkPropertyValue<Any, Float>(0.3f)

    /**[TransitionParam.pixelGCodeGapValue]*/
    @Pixel
    var pixelGCodeGapValue: Float by HawkPropertyValue<Any, Float>(1f)

    /**[Path.eachPath]时的路径采样步长, 值越少影响枚举的性能, 但是不会影响精度
     * 配合[pathAcceptableDegrees]一起使用, 不会影响输出的数据精度.
     * 与之前的[_pathAcceptableError]等距离采样不同.
     * [LibLpHawkKeys.enableVectorRadiansSample]
     * */
    @Pixel
    var pathSampleStepRadians: Float by HawkPropertyValue<Any, Float>(1f)

    /**凸起公差, 公差范围内的点视为一条直线*/
    @MM
    var pathTolerance: Float? by HawkPropertyValue<Any, Float?>(null)

    /**默认的公差*/
    @MM
    var defPathTolerance: Float by HawkPropertyValue<Any, Float>(0.02f)

    /**LX2 默认的公差*/
    @MM
    var defLx2PathTolerance: Float by HawkPropertyValue<Any, Float>(0.001f)

    /**是否强制使用高刷*/
    var enableHighRefresh: Boolean by HawkPropertyValue<Any, Boolean>(isDebug())

    /**2个浮点比较, 误差小于此值视为相等
     * 0.0000000 //浮点小数点后面有7位*/
    var floatAcceptableError: Float by HawkPropertyValue<Any, Float>(0.00001f) //5位

    /**SVG采样近似误差
     * [pathAcceptableError]
     * [androidx.graphics.path.PathIterator]*/
    var svgTolerance: Float by HawkPropertyValue<Any, Float>(0.1f)

    /**2个双精度浮点比较, 误差小于此值视为相等
     * 0.0000000000000000 //双精度浮点小数点后面有16位*/
    var doubleAcceptableError: Double by HawkPropertyValue<Any, Double>(0.000000000000001) //15位

    /**是否要激活ai绘图
     * https://scribblediffusion.com/
     * */
    var enableAIDraw: Boolean by HawkPropertyValue<Any, Boolean>(false)

    /**矢量存储的精度*/
    var vectorDecimal: Int by HawkPropertyValue<Any, Int>(3)

    /**最小吸附时间*/
    var minAdsorbTime: Long by HawkPropertyValue<Any, Long>(300)

    /**只有一个元素时, 是否保持原样*/
    var keepSingleRenderProperty: Boolean by HawkPropertyValue<Any, Boolean>(false)

    /**最小检测运行脚本时间*/
    var minCheckScriptTime: Long by HawkPropertyValue<Any, Long>(5_000)

    /**是否激活脚本手势触发监听*/
    var enableScriptTouchListen: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**是否激活默认脚本执行*/
    var enableDefaultScript: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**是否激活App启动脚本执行*/
    var enableAppScript: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**空格文本的宽度*/
    var spaceTextWidth: Int? by HawkPropertyValue<Any, Int?>(null)
}