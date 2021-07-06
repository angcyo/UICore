package com.angcyo.widget.blur

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.*
import android.renderscript.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import com.angcyo.library.ex.dp
import com.angcyo.widget.R
import kotlin.math.max

/**
 * 模糊控件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/06
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class BlurView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    companion object {

        //        init {
        //            try {
        //                RealtimeBlurView::class.java.classLoader?.loadClass("androidx.renderscript.RenderScript")
        //            } catch (e: ClassNotFoundException) {
        //                e.printStackTrace()
        //                throw RuntimeException(
        //                    """
        //错误！
        //RenderScript支持库未启用，要启用模糊效果，请在您的app的Gradle配置文件中添加以下语句：
        //android {
        //...
        //  defaultConfig {
        //    ...
        //    renderscriptTargetApi 17
        //    renderscriptSupportModeEnabled true
        //  }
        //}"""
        //                )
        //            }
        //        }

        // android:debuggable="true" in AndroidManifest.xml (auto set by build tool)
        var DEBUG: Boolean? = null

        var RENDERING_COUNT = 0

        //val STOP_EXCEPTION = StopException()

        fun isDebug(ctx: Context?): Boolean {
            if (DEBUG == null && ctx != null) {
                DEBUG = ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            }
            return DEBUG === java.lang.Boolean.TRUE
        }
    }

    /**采样率*/
    var downSampleFactor: Float = 4f
        /* default 4*/
        set(value) {
            require(value > 0) { "DownSample factor must be greater than 0." }
            if (field != value) {
                field = value
                _dirty = true // may also change blur radius
                releaseBitmap()
                invalidate()
            }
        }

    /**覆盖的颜色*/
    var overlayColor: Int = 0x00ffffff
        set(value) {
            field = value
            invalidate()
        }

    /**模糊半径, 为0则不模糊*/
    var blurRadius: Float = 10 * dp
        set(value) {
            if (field != value) {
                field = value
                _dirty = true
                invalidate()
            }
        }

    /**需要模糊的目标控件*/
    open var blurTargetView: View? = null
        set(value) {
            field = value
            if (value == null) {
                release()
            } else {
                checkEnable()
            }
        }

    val mPaint: Paint
    val mRectF: RectF

    /**圆角的半径*/
    var xRadius: Float = 0 * dp
    var yRadius: Float = 0 * dp

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BlurView)
        blurRadius = a.getDimension(R.styleable.BlurView_realtimeBlurRadius, blurRadius)
        downSampleFactor =
            a.getFloat(R.styleable.BlurView_realtimeDownSampleFactor, downSampleFactor)
        overlayColor = a.getColor(R.styleable.BlurView_realtimeOverlayColor, overlayColor)

        //ready rounded corner
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mRectF = RectF()
        xRadius = a.getDimension(R.styleable.BlurView_xRadius, xRadius)
        yRadius = a.getDimension(R.styleable.BlurView_yRadius, yRadius)
        a.recycle()
    }

    fun setRadius(x: Float, y: Float) {
        if (xRadius != x || yRadius != y) {
            xRadius = x
            yRadius = y
            _dirty = true
            invalidate()
        }
    }

    /**标记:是否正在采样*/
    var _isRendering = false

    override fun draw(canvas: Canvas) {
        when {
            _isRendering -> {
                //正在渲染....
                // Quit here, don't draw views above me
                //throw STOP_EXCEPTION
            }
            RENDERING_COUNT > 0 -> {
                // Doesn't support blurview overlap on another blurview
            }
            else -> {
                //开始绘制
                super.draw(canvas)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBlurredBitmap(canvas, _blurredBitmap, overlayColor)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        checkEnable()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        checkEnable()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        checkEnable()
    }

    // mDecorView should be the root view of the activity (even if you are on a different window like a dialog)
    /**模糊的目标*/
    var _targetView: View? = null

    // If the view is on different root view (usually means we are on a PopupWindow),
    // we need to manually call invalidate() in onPreDraw(), otherwise we will not be able to see the changes
    /**标记:rootView发生了改变*/
    var _differentRoot = false

    val _preDrawListener = ViewTreeObserver.OnPreDrawListener {
        val locations = IntArray(2)
        val oldBmp = _blurredBitmap
        val targetView = _targetView

        if (targetView != null && isShown && prepare()) {
            val blurCanvas = _blurringCanvas
            val bitmapToBlur = _bitmapToBlur

            if (blurCanvas != null && bitmapToBlur != null) {
                val redrawBitmap = _blurredBitmap != oldBmp

                //目标view的位置
                targetView.getLocationOnScreen(locations)
                var x = -locations[0]
                var y = -locations[1]

                //自己的位置
                getLocationOnScreen(locations)
                x += locations[0]
                y += locations[1]

                // just erase transparent, 擦除颜色
                bitmapToBlur.eraseColor(overlayColor and 0xffffff)
                val rc = blurCanvas.save()
                _isRendering = true
                RENDERING_COUNT++
                try {
                    blurCanvas.scale(
                        1f * bitmapToBlur.width / width,
                        1f * bitmapToBlur.height / height
                    )
                    blurCanvas.translate(-x.toFloat(), -y.toFloat())
                    if (targetView.background != null) {
                        targetView.background.draw(blurCanvas)
                    }
                    targetView.draw(blurCanvas)
                } catch (e: StopException) {
                    e.printStackTrace()
                } finally {
                    _isRendering = false
                    RENDERING_COUNT--
                    blurCanvas.restoreToCount(rc)
                }
                blur(bitmapToBlur, _blurredBitmap)
                if (redrawBitmap || _differentRoot) {
                    invalidate()
                }
            }
        }
        true
    }

    /**检查是否要激活模糊*/
    fun checkEnable() {
        release()
        if (ViewCompat.isAttachedToWindow(this) && visibility == VISIBLE) {
            _targetView = blurTargetView
            if (_targetView != null) {
                _targetView?.apply {
                    viewTreeObserver.addOnPreDrawListener(_preDrawListener)
                    _differentRoot = _targetView != blurTargetView
                    if (_differentRoot) {
                        postInvalidate()
                    }
                }
            } else {
                _differentRoot = false
            }
        }
    }

    /**释放图片*/
    fun releaseBitmap() {
        _blurInput?.destroy()
        _blurInput = null
        _blurOutput?.destroy()
        _blurOutput = null
        _bitmapToBlur?.recycle()
        _bitmapToBlur = null
        _blurredBitmap?.recycle()
        _blurredBitmap = null
        _roundBitmap?.recycle()
        _roundBitmap = null
    }

    /**释放脚本*/
    fun releaseScript() {
        _renderScript?.destroy()
        _renderScript = null
        _blurScript?.destroy()
        _blurScript = null
    }

    /**释放所有*/
    fun release() {
        _targetView?.viewTreeObserver?.removeOnPreDrawListener(_preDrawListener)
        releaseBitmap()
        releaseScript()
    }

    /**标记:是否需要重新采样*/
    var _dirty = false

    /**模糊前的图片*/
    var _bitmapToBlur: Bitmap? = null

    /**模糊后的图片*/
    var _blurredBitmap: Bitmap? = null
    var _blurringCanvas: Canvas? = null
    var _renderScript: RenderScript? = null
    var _blurScript: ScriptIntrinsicBlur? = null
    var _blurInput: Allocation? = null
    var _blurOutput: Allocation? = null

    var _roundBitmap: Bitmap? = null

    /**准备工作*/
    fun prepare(): Boolean {
        if (blurRadius == 0f) {
            release()
            return false
        }
        var downSampleFactor = downSampleFactor
        if (_dirty || _renderScript == null) {
            if (_renderScript == null) {
                try {
                    //创建一个RenderScript
                    _renderScript = RenderScript.create(context)
                    //创建模糊Render的脚本
                    _blurScript =
                        ScriptIntrinsicBlur.create(_renderScript, Element.U8_4(_renderScript))
                } catch (e: RSRuntimeException) {
                    return if (isDebug(context)) {
                        if (e.message != null && e.message!!.startsWith("Error loading RS jni library: java.lang.UnsatisfiedLinkError:")) {
                            throw RuntimeException("Error loading RS jni library, Upgrade buildToolsVersion=\"24.0.2\" or higher may solve this issue")
                        } else {
                            throw e
                        }
                    } else {
                        // In release mode, just ignore
                        releaseScript()
                        false
                    }
                }
            }
            _dirty = false
            var radius = blurRadius / downSampleFactor
            if (radius > 25) {
                downSampleFactor = downSampleFactor * radius / 25
                radius = 25f
            }
            _blurScript?.setRadius(radius)
        }
        val width = width
        val height = height
        val scaledWidth = max(1, (width / downSampleFactor).toInt())
        val scaledHeight = max(1, (height / downSampleFactor).toInt())
        if (_blurringCanvas == null ||
            _blurredBitmap == null ||
            _blurredBitmap?.width != scaledWidth ||
            _blurredBitmap?.height != scaledHeight
        ) {
            releaseBitmap()
            var r = false
            try {
                _bitmapToBlur =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                if (_bitmapToBlur == null) {
                    return false
                }
                _blurringCanvas = Canvas(_bitmapToBlur!!)
                //创建Allocation
                _blurInput = Allocation.createFromBitmap(
                    _renderScript,
                    _bitmapToBlur,
                    Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT
                )
                _blurOutput = Allocation.createTyped(_renderScript, _blurInput?.type)
                _blurredBitmap =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                if (_blurredBitmap == null) {
                    return false
                }
                r = true
            } catch (e: OutOfMemoryError) {
                // Bitmap.createBitmap() may cause OOM error
                // Simply ignore and fallback
            } finally {
                if (!r) {
                    releaseBitmap()
                    return false
                }
            }
        }
        return true
    }


    //原图矩形
    val rectSrc = Rect()

    //目标图矩形
    val rectDst = Rect()

    /**
     * Custom draw the blurred bitmap and color to define your own shape
     *
     * @param canvas
     * @param blurredBitmap
     * @param overlayColor
     */
    fun drawBlurredBitmap(canvas: Canvas, blurredBitmap: Bitmap?, overlayColor: Int) {
        //获取模糊的图片
        if (blurredBitmap != null) {
            rectSrc.right = blurredBitmap.width
            rectSrc.bottom = blurredBitmap.height
            rectDst.right = width
            rectDst.bottom = height
            canvas.drawBitmap(blurredBitmap, rectSrc, rectDst, null)
        }
        //绘制覆盖颜色
        canvas.drawColor(overlayColor)

        //绘制圆角图片
        if (width > 0 && height > 0) {
            //Rounded corner
            mRectF.right = width.toFloat()
            mRectF.bottom = height.toFloat()

            var tmpCanvas: Canvas?
            _roundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                tmpCanvas = Canvas(this)
            }
            val cutPaint = Paint()
            cutPaint.isAntiAlias = true
            cutPaint.color = Color.WHITE
            tmpCanvas?.drawRoundRect(mRectF, xRadius, yRadius, cutPaint)
        }
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        if (_roundBitmap != null && !_roundBitmap!!.isRecycled) {
            canvas.drawBitmap(_roundBitmap!!, 0f, 0f, mPaint)
        }
    }

    /**开始模糊图片
     * [bitmapToBlur] 模糊前的图片
     * [blurredBitmap] 模糊后的图片*/
    fun blur(bitmapToBlur: Bitmap?, blurredBitmap: Bitmap?) {
        _blurInput?.apply {
            copyFrom(bitmapToBlur)
            _blurScript?.setInput(this)
            //复制数据到Allocation
            _blurScript?.forEach(_blurOutput)
            _blurOutput?.copyTo(blurredBitmap)
        }
    }

    //异常
    class StopException : RuntimeException()
}