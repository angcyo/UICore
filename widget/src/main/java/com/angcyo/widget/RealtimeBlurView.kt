package com.angcyo.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.graphics.*
import android.renderscript.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import com.angcyo.library.ex.activityContent
import com.angcyo.library.ex.dp
import kotlin.math.max

/**
 * 实时模糊, 来自:
 * https://github.com/kongzue/DialogV3
 *
 *
 * 实时模糊库:
 * https://github.com/mmin18/RealtimeBlurView
 *
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RealtimeBlurView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

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

    val rectSrc = Rect()
    val rectDst = Rect()

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

    /**模糊半径*/
    var blurRadius: Float = 10 * dp
        set(value) {
            if (field != value) {
                field = value
                _dirty = true
                invalidate()
            }
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

    /**标记:是否正在采样*/
    var _isRendering = false

    // mDecorView should be the root view of the activity (even if you are on a different window like a dialog)
    /**模糊的目标*/
    var _decorView: View? = null

    // If the view is on different root view (usually means we are on a PopupWindow),
    // we need to manually call invalidate() in onPreDraw(), otherwise we will not be able to see the changes
    /**标记:rootView发生了改变*/
    var _differentRoot = false

    val _preDrawListener = ViewTreeObserver.OnPreDrawListener {
        val locations = IntArray(2)
        var oldBmp = _blurredBitmap
        val decor = _decorView
        if (decor != null && isShown && prepare()) {
            val redrawBitmap = _blurredBitmap != oldBmp
            oldBmp = null
            decor.getLocationOnScreen(locations)
            var x = -locations[0]
            var y = -locations[1]
            getLocationOnScreen(locations)
            x += locations[0]
            y += locations[1]

            // just erase transparent, 擦除颜色
            _bitmapToBlur?.eraseColor(overlayColor and 0xffffff)
            val rc = _blurringCanvas!!.save()
            _isRendering = true
            RENDERING_COUNT++
            try {
                _blurringCanvas!!.scale(
                    1f * _bitmapToBlur!!.width / width,
                    1f * _bitmapToBlur!!.height / height
                )
                _blurringCanvas!!.translate(-x.toFloat(), -y.toFloat())
                if (decor.background != null) {
                    decor.background.draw(_blurringCanvas!!)
                }
                decor.draw(_blurringCanvas)
            } catch (e: StopException) {
            } finally {
                _isRendering = false
                RENDERING_COUNT--
                _blurringCanvas!!.restoreToCount(rc)
            }
            blur(_bitmapToBlur, _blurredBitmap)
            if (redrawBitmap || _differentRoot) {
                invalidate()
            }
        }
        true
    }

    val mPaint: Paint
    val mRectF: RectF
    var xRadius: Float = 15 * dp
    var yRadius: Float = 15 * dp
    var mRoundBitmap: Bitmap? = null
    var mTmpCanvas: Canvas? = null

    val _activityDecorView: View?
        get() {
            val ctx = context.activityContent()
            return if (ctx is Activity) {
                ctx.window.decorView
            } else {
                null
            }
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RealtimeBlurView)
        blurRadius = a.getDimension(R.styleable.RealtimeBlurView_realtimeBlurRadius, blurRadius)
        downSampleFactor =
            a.getFloat(R.styleable.RealtimeBlurView_realtimeDownSampleFactor, downSampleFactor)
        overlayColor = a.getColor(R.styleable.RealtimeBlurView_realtimeOverlayColor, overlayColor)

        //ready rounded corner
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mRectF = RectF()
        xRadius = a.getDimension(R.styleable.RealtimeBlurView_xRadius, xRadius)
        yRadius = a.getDimension(R.styleable.RealtimeBlurView_yRadius, yRadius)
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

    fun releaseBitmap() {
        if (_blurInput != null) {
            _blurInput!!.destroy()
            _blurInput = null
        }
        if (_blurOutput != null) {
            _blurOutput!!.destroy()
            _blurOutput = null
        }
        if (_bitmapToBlur != null) {
            _bitmapToBlur!!.recycle()
            _bitmapToBlur = null
        }
        if (_blurredBitmap != null) {
            _blurredBitmap!!.recycle()
            _blurredBitmap = null
        }
    }

    fun releaseScript() {
        if (_renderScript != null) {
            _renderScript!!.destroy()
            _renderScript = null
        }
        if (_blurScript != null) {
            _blurScript!!.destroy()
            _blurScript = null
        }
    }

    fun release() {
        releaseBitmap()
        releaseScript()
    }

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
            _blurScript!!.setRadius(radius)
        }
        val width = width
        val height = height
        val scaledWidth = max(1, (width / downSampleFactor).toInt())
        val scaledHeight = max(1, (height / downSampleFactor).toInt())
        if (_blurringCanvas == null ||
            _blurredBitmap == null ||
            _blurredBitmap!!.width != scaledWidth ||
            _blurredBitmap!!.height != scaledHeight
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

    fun blur(bitmapToBlur: Bitmap?, blurredBitmap: Bitmap?) {
        _blurInput?.apply {
            copyFrom(bitmapToBlur)
            _blurScript?.setInput(this)
            //复制数据到Allocation
            _blurScript?.forEach(_blurOutput)
            _blurOutput?.copyTo(blurredBitmap)
        }
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

    fun checkEnable() {
        if (ViewCompat.isAttachedToWindow(this) && visibility == VISIBLE) {
            _decorView = _activityDecorView
            if (_decorView != null) {
                _decorView?.apply {
                    viewTreeObserver.addOnPreDrawListener(_preDrawListener)
                    _differentRoot = rootView !== this@RealtimeBlurView.rootView
                    if (_differentRoot) {
                        postInvalidate()
                    }
                }
            } else {
                _differentRoot = false
            }
        } else {
            _decorView?.viewTreeObserver?.removeOnPreDrawListener(_preDrawListener)
            release()
        }
    }

    override fun draw(canvas: Canvas) {
        if (_isRendering) {
            // Quit here, don't draw views above me
            //throw STOP_EXCEPTION
        } else if (RENDERING_COUNT > 0) {
            // Doesn't support blurview overlap on another blurview
        } else {
            super.draw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBlurredBitmap(canvas, _blurredBitmap, overlayColor)
    }

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
            mRoundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                mTmpCanvas = Canvas(this)
            }
            val cutPaint = Paint()
            cutPaint.isAntiAlias = true
            cutPaint.color = Color.WHITE
            mTmpCanvas!!.drawRoundRect(mRectF, xRadius, yRadius, cutPaint)
        }
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        if (mRoundBitmap != null && !mRoundBitmap!!.isRecycled) {
            canvas.drawBitmap(mRoundBitmap!!, 0f, 0f, mPaint)
        }
    }

    class StopException : RuntimeException()
}